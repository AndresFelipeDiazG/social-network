import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthClient } from '../../../core/auth/auth.client';
import { authInterceptor } from '../../../core/auth/auth.interceptor';
import { SessionStorage } from '../../../core/auth/session-storage';
import { APP_CONFIG, AppConfig } from '../../../core/config/app-config';
import { LoginPage } from './login';

const CONFIG: AppConfig = { apiBaseUrl: '/api', appName: 'Red Social', postsPageSize: 20 };

const RESPONSE = {
  accessToken: 'jwt',
  tokenType: 'Bearer',
  expiresIn: 3600,
  user: { id: '1', username: 'acorrea', displayName: 'Ana Correa' },
};

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let login: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    login = vi.fn().mockReturnValue(of(RESPONSE));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        // El interceptor se registra de verdad: asi la prueba tambien cubre que
        // su cadena de inyeccion resuelve, que es donde un ciclo se manifestaria.
        provideHttpClient(withInterceptors([authInterceptor])),
        { provide: APP_CONFIG, useValue: CONFIG },
        { provide: AuthClient, useValue: { login } },
        { provide: SessionStorage, useValue: { read: () => null, write: () => {}, clear: () => {} } },
      ],
    });

    navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);

    fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
  });

  function html(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function field(id: string): HTMLInputElement {
    return (fixture.nativeElement as HTMLElement).querySelector(`#${id}`) as HTMLInputElement;
  }

  function click(selector: string): void {
    ((fixture.nativeElement as HTMLElement).querySelector(selector) as HTMLElement).click();
  }

  function write(id: string, value: string): void {
    const input = field(id);
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  it('muestra el saludo y el nombre de la aplicacion', () => {
    expect(html()).toContain('Bienvenido/a de vuelta');
    expect(html()).toContain('Ingresa a tu cuenta para ver que hay de nuevo.');
    expect(html()).toContain('Red Social');
  });

  it('la contrasena empieza oculta y el ojo la revela', async () => {
    expect(field('password').type).toBe('password');

    click('.field__action');
    await fixture.whenStable();

    expect(field('password').type).toBe('text');
  });

  it('no llama al servidor con el formulario vacio', async () => {
    click('.submit');
    await fixture.whenStable();

    expect(login).not.toHaveBeenCalled();
    expect(html()).toContain('Escribe tu usuario');
  });

  it('entra y navega al muro', async () => {
    write('username', 'acorrea');
    write('password', 'Password123!');

    click('.submit');
    await fixture.whenStable();

    expect(login).toHaveBeenCalledWith({ username: 'acorrea', password: 'Password123!' });
    expect(navigate).toHaveBeenCalledWith('/muro');
  });

  it('muestra el motivo del rechazo y no navega', async () => {
    login.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { detail: 'Usuario o contraseña incorrectos' } })),
    );

    write('username', 'acorrea');
    write('password', 'mala');
    click('.submit');
    await fixture.whenStable();

    expect(html()).toContain('Usuario o contraseña incorrectos');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('la cuenta de demostracion esta a la vista', () => {
    expect(html()).toContain('Cuenta de demostración');
    expect(html()).toContain('acorrea');
    expect(html()).toContain('Password123!');
  });

  it('el boton de la cuenta de demostracion rellena las dos casillas', async () => {
    click('.demo__fill');
    await fixture.whenStable();

    expect(field('username').value).toBe('acorrea');
    expect(field('password').value).toBe('Password123!');
  });

  it('los otros usuarios tambien rellenan el formulario', async () => {
    click('.demo__other');
    await fixture.whenStable();

    expect(field('username').value).toBe('jmendoza');
    expect(field('password').value).toBe('Password123!');
  });
});
