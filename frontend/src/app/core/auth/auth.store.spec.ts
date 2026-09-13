import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthClient } from './auth.client';
import { LoginResponse } from './auth.models';
import { AuthStore } from './auth.store';
import { SessionStorage, StoredSession } from './session-storage';

const RESPONSE: LoginResponse = {
  accessToken: 'jwt-de-prueba',
  tokenType: 'Bearer',
  expiresIn: 3600,
  user: { id: '11111111-1111-1111-1111-111111111111', username: 'acorrea', displayName: 'Ana Correa' },
};

const CREDENTIALS = { username: 'acorrea', password: 'Password123!' };

class StorageDouble {
  session: StoredSession | null = null;

  read(): StoredSession | null {
    return this.session;
  }

  write(session: StoredSession): void {
    this.session = session;
  }

  clear(): void {
    this.session = null;
  }
}

function unauthorized(detail: string): HttpErrorResponse {
  return new HttpErrorResponse({ status: 401, error: { title: 'Credenciales invalidas', detail } });
}

describe('AuthStore', () => {
  let storage: StorageDouble;
  let login: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    storage = new StorageDouble();
    login = vi.fn().mockReturnValue(of(RESPONSE));

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthClient, useValue: { login } },
        { provide: SessionStorage, useValue: storage },
      ],
    });
  });

  // El store se inyecta dentro de cada prueba y no en el beforeEach porque su
  // hook de arranque lee el almacenamiento: hay que prepararlo antes.
  function store() {
    return TestBed.inject(AuthStore);
  }

  it('guarda el token y el usuario al entrar', async () => {
    const auth = store();

    await expect(auth.login(CREDENTIALS)).resolves.toBe(true);

    expect(auth.token()).toBe('jwt-de-prueba');
    expect(auth.authenticated()).toBe(true);
    expect(auth.displayName()).toBe('Ana Correa');
    expect(auth.initials()).toBe('AC');
    expect(auth.error()).toBeNull();
  });

  it('convierte los segundos restantes en un instante absoluto', async () => {
    const before = Date.now();
    const auth = store();

    await auth.login(CREDENTIALS);

    expect(auth.expiresAt()).toBeGreaterThanOrEqual(before + 3600 * 1000);
  });

  it('persiste la sesion para que recargar no la pierda', async () => {
    await store().login(CREDENTIALS);

    expect(storage.session?.accessToken).toBe('jwt-de-prueba');
  });

  it('con credenciales incorrectas deja el mensaje del servidor y ninguna sesion', async () => {
    login.mockReturnValue(throwError(() => unauthorized('Usuario o contraseña incorrectos')));
    const auth = store();

    await expect(auth.login(CREDENTIALS)).resolves.toBe(false);

    expect(auth.token()).toBeNull();
    expect(auth.authenticated()).toBe(false);
    expect(auth.error()).toBe('Usuario o contraseña incorrectos');
    expect(storage.session).toBeNull();
  });

  it('deja de enviar mientras responde y despues del fallo', async () => {
    login.mockReturnValue(throwError(() => unauthorized('no')));
    const auth = store();

    await auth.login(CREDENTIALS);

    expect(auth.submitting()).toBe(false);
  });

  it('al salir borra la sesion guardada', async () => {
    const auth = store();
    await auth.login(CREDENTIALS);

    auth.logout();

    expect(auth.authenticated()).toBe(false);
    expect(auth.error()).toBeNull();
    expect(storage.session).toBeNull();
  });

  it('cuando el servidor rechaza el token explica que la sesion caduco', async () => {
    const auth = store();
    await auth.login(CREDENTIALS);

    auth.expire();

    expect(auth.authenticated()).toBe(false);
    expect(auth.error()).toContain('caducado');
  });

  it('recupera la sesion guardada al arrancar', () => {
    storage.session = {
      accessToken: 'token-anterior',
      user: RESPONSE.user,
      expiresAt: Date.now() + 60_000,
    };

    const auth = store();

    expect(auth.authenticated()).toBe(true);
    expect(auth.username()).toBe('acorrea');
  });

  it('descarta la sesion guardada si ya expiro', () => {
    storage.session = {
      accessToken: 'token-caducado',
      user: RESPONSE.user,
      expiresAt: Date.now() - 1,
    };

    const auth = store();

    expect(auth.authenticated()).toBe(false);
    expect(storage.session).toBeNull();
  });
});
