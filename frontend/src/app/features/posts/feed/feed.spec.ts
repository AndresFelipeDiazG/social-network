import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthClient } from '../../../core/auth/auth.client';
import { SessionStorage } from '../../../core/auth/session-storage';
import { APP_CONFIG, AppConfig } from '../../../core/config/app-config';
import { Post, PostPage } from '../post.models';
import { PostsClient } from '../posts.client';
import { FeedPage } from './feed';

const CONFIG: AppConfig = { apiBaseUrl: '/api', appName: 'Red Social', postsPageSize: 20 };

const VIEWER = { id: '1', username: 'acorrea', displayName: 'Ana Correa' };

function post(id: string, message: string, username: string, displayName: string): Post {
  return {
    id,
    message,
    publishedAt: new Date().toISOString(),
    author: { id: `autor-${username}`, username, displayName },
    mine: false,
    likes: 0,
    likedByMe: false,
    imageUrl: null,
  };
}

function page(content: Post[], totalPages = 1): PostPage {
  return { content, page: 0, size: 20, totalElements: content.length, totalPages };
}

const SEEDED = [
  post('1', 'Migraciones versionadas desde el primer dia.', 'dcastillo', 'Diego Castillo'),
  post('2', 'Buenos dias a todos.', 'lvargas', 'Lucia Vargas'),
];

describe('FeedPage', () => {
  let fixture: ComponentFixture<FeedPage>;
  let list: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;

  async function render(): Promise<void> {
    fixture = TestBed.createComponent(FeedPage);
    await fixture.whenStable();
  }

  beforeEach(() => {
    list = vi.fn().mockReturnValue(of(page(SEEDED)));
    create = vi.fn().mockReturnValue(of(post('3', 'nueva', 'acorrea', 'Ana Correa')));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: APP_CONFIG, useValue: CONFIG },
        { provide: PostsClient, useValue: { list, create } },
        { provide: AuthClient, useValue: { login: vi.fn() } },
        {
          provide: SessionStorage,
          useValue: {
            read: () => ({ accessToken: 'jwt', user: VIEWER, expiresAt: Date.now() + 60_000 }),
            write: () => {},
            clear: () => {},
          },
        },
      ],
    });
  });

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function all(selector: string): HTMLElement[] {
    return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll(selector));
  }

  it('pide el muro de los demas al entrar y lo pinta', async () => {
    await render();

    expect(list).toHaveBeenCalledWith('OTHERS', 0, 20);
    expect(all('app-post-card')).toHaveLength(2);
    expect(text()).toContain('Diego Castillo');
    expect(text()).toContain('Buenos dias a todos.');
  });

  it('la barra solo muestra el avatar, y el menu se despliega al pulsarlo', async () => {
    await render();

    // Cerrado, la opcion destructiva no esta a un clic de distancia.
    expect(text()).not.toContain('Cerrar sesión');
    expect(all('.menu__trigger')).toHaveLength(1);

    all('.menu__trigger')[0].click();
    await fixture.whenStable();

    expect(text()).toContain('Ana Correa');
    expect(text()).toContain('@acorrea');
    expect(text()).toContain('Cerrar sesión');
  });

  it('el menu se cierra al pulsar fuera', async () => {
    await render();

    all('.menu__trigger')[0].click();
    await fixture.whenStable();

    all('.menu__backdrop')[0].click();
    await fixture.whenStable();

    expect(text()).not.toContain('Cerrar sesión');
  });

  it('cerrar sesion lleva de vuelta a la entrada', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    await render();

    all('.menu__trigger')[0].click();
    await fixture.whenStable();

    all('.menu__item')[0].click();
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith('/entrar');
  });

  it('no envia fecha: la pone el servidor al guardar', async () => {
    await render();

    const area = (fixture.nativeElement as HTMLElement).querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Sin fecha en la peticion';
    area.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    all('.composer__send')[0].click();
    await fixture.whenStable();

    expect(create).toHaveBeenCalledWith({ message: 'Sin fecha en la peticion' });
  });

  it('el selector de emojis los inserta donde esta el cursor', async () => {
    await render();

    const area = (fixture.nativeElement as HTMLElement).querySelector('textarea') as HTMLTextAreaElement;
    area.value = 'Buenos dias ';
    area.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    all('.picker__trigger')[0].click();
    await fixture.whenStable();

    const emojis = all('.picker__emoji');
    expect(emojis.length).toBeGreaterThan(20);

    emojis[0].click();
    await fixture.whenStable();

    expect(area.value).toBe('Buenos dias \u{1F600}');
  });

  it('el contador dice cuantas se ven de cuantas hay', async () => {
    list.mockReturnValue(of({ ...page(SEEDED, 3), totalElements: 6 }));
    await render();

    expect(text()).toContain('Mostrando 2 de 6');
    expect(text()).toContain('Cargar más publicaciones');
  });

  it('sin mas paginas no ofrece cargar mas', async () => {
    await render();

    expect(text()).not.toContain('Cargar más publicaciones');
  });

  it('cambiar de filtro vuelve a pedir con ese scope', async () => {
    await render();

    all('.chip')[2].click();
    await fixture.whenStable();

    expect(list).toHaveBeenLastCalledWith('MINE', 0, 20);
  });

  it('el boton de actualizar repite la peticion', async () => {
    await render();
    list.mockClear();

    all('.refresh')[0].click();
    await fixture.whenStable();

    expect(list).toHaveBeenCalledWith('OTHERS', 0, 20);
  });

  it('un muro vacio se explica en lugar de quedarse en blanco', async () => {
    list.mockReturnValue(of(page([])));
    await render();

    expect(text()).toContain('Nadie más ha publicado todavía.');
  });

  it('publicar envia el mensaje y pasa al filtro de todas', async () => {
    await render();

    const textarea = (fixture.nativeElement as HTMLElement).querySelector('textarea') as HTMLTextAreaElement;
    textarea.value = '  Hola mundo  ';
    textarea.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    all('.composer__send')[0].click();
    await fixture.whenStable();

    // El mensaje viaja sin los espacios de los extremos.
    expect(create).toHaveBeenCalledWith({ message: 'Hola mundo' });
    expect(list).toHaveBeenLastCalledWith('ALL', 0, 20);
    expect(textarea.value).toBe('');
    expect(text()).toContain('Publicado');
  });

  it('el buscador filtra lo cargado y explica cuando no hay coincidencias', async () => {
    await render();

    const search = (fixture.nativeElement as HTMLElement).querySelector('.search__input') as HTMLInputElement;
    search.value = 'migraciones';
    search.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(all('app-post-card')).toHaveLength(1);

    search.value = 'no existe';
    search.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(text()).toContain('Ninguna de las 2 publicaciones cargadas coincide');
  });
});
