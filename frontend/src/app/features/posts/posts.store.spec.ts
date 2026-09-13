import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { APP_CONFIG, AppConfig } from '../../core/config/app-config';
import { FeedScope, Post, PostPage } from './post.models';
import { PostsClient } from './posts.client';
import { PostsStore } from './posts.store';

const CONFIG: AppConfig = { apiBaseUrl: '/api', appName: 'Red Social', postsPageSize: 2 };

function post(id: string, message: string, author = 'jmendoza', mine = false): Post {
  return {
    id,
    message,
    publishedAt: '2026-09-13T10:00:00Z',
    author: { id: `autor-${author}`, username: author, displayName: author.toUpperCase() },
    mine,
    likes: 0,
    likedByMe: false,
    imageUrl: null,
  };
}

function page(content: Post[], number: number, totalPages: number, totalElements: number): PostPage {
  return { content, page: number, size: CONFIG.postsPageSize, totalElements, totalPages };
}

describe('PostsStore', () => {
  let list: ReturnType<typeof vi.fn>;
  let create: ReturnType<typeof vi.fn>;
  let like: ReturnType<typeof vi.fn>;
  let unlike: ReturnType<typeof vi.fn>;
  let uploadImage: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    list = vi.fn().mockReturnValue(of(page([post('1', 'hola')], 0, 1, 1)));
    uploadImage = vi.fn().mockReturnValue(of({ id: 'img-1', url: '/api/posts/images/img-1' }));
    create = vi.fn().mockReturnValue(of(post('nueva', 'recien publicada', 'acorrea', true)));
    like = vi.fn().mockReturnValue(of(undefined));
    unlike = vi.fn().mockReturnValue(of(undefined));

    TestBed.configureTestingModule({
      providers: [
        PostsStore,
        { provide: PostsClient, useValue: { list, create, like, unlike, uploadImage } },
        { provide: APP_CONFIG, useValue: CONFIG },
      ],
    });
  });

  function store() {
    return TestBed.inject(PostsStore);
  }

  it('empieza en el filtro de otros usuarios, que es lo que pide el muro', () => {
    expect(store().scope()).toBe('OTHERS');
  });

  it('carga la primera pagina con el tamano configurado', async () => {
    const posts = store();

    await posts.load();

    expect(list).toHaveBeenCalledWith('OTHERS', 0, 2);
    expect(posts.visible()).toHaveLength(1);
    expect(posts.loading()).toBe(false);
  });

  it('cambiar de filtro reemplaza la lista', async () => {
    const posts = store();
    await posts.load();

    list.mockReturnValue(of(page([post('9', 'mia', 'acorrea', true)], 0, 1, 1)));
    await posts.load('MINE');

    expect(posts.scope()).toBe('MINE');
    expect(posts.visible().map((item) => item.id)).toEqual(['9']);
  });

  it('sabe si queda mas por cargar', async () => {
    list.mockReturnValue(of(page([post('1', 'a'), post('2', 'b')], 0, 3, 6)));
    const posts = store();

    await posts.load();

    expect(posts.hasMore()).toBe(true);
    expect(posts.loadedCount()).toBe(2);
    expect(posts.totalCount()).toBe(6);
  });

  it('la siguiente pagina se añade al final', async () => {
    list.mockReturnValue(of(page([post('1', 'a'), post('2', 'b')], 0, 2, 4)));
    const posts = store();
    await posts.load();

    list.mockReturnValue(of(page([post('3', 'c'), post('4', 'd')], 1, 2, 4)));
    await posts.loadMore();

    expect(posts.visible().map((item) => item.id)).toEqual(['1', '2', '3', '4']);
    expect(posts.hasMore()).toBe(false);
  });

  it('descarta las repetidas cuando alguien publica entre dos peticiones', async () => {
    list.mockReturnValue(of(page([post('1', 'a'), post('2', 'b')], 0, 2, 4)));
    const posts = store();
    await posts.load();

    // La ventana se desplaza y la '2' vuelve a aparecer en la pagina siguiente.
    list.mockReturnValue(of(page([post('2', 'b'), post('3', 'c')], 1, 2, 4)));
    await posts.loadMore();

    expect(posts.visible().map((item) => item.id)).toEqual(['1', '2', '3']);
  });

  it('no pide mas paginas si ya se cargaron todas', async () => {
    const posts = store();
    await posts.load();
    list.mockClear();

    await posts.loadMore();

    expect(list).not.toHaveBeenCalled();
  });

  it('publicar recarga el muro desde el servidor', async () => {
    const posts = store();
    await posts.load('ALL');
    list.mockClear();

    await expect(posts.publish('mensaje nuevo')).resolves.toBe(true);

    expect(create).toHaveBeenCalledWith({ message: 'mensaje nuevo' });
    expect(list).toHaveBeenCalledWith('ALL', 0, 2);
  });

  it('publicar desde el filtro de otros pasa a todas, donde si cabe la propia', async () => {
    const posts = store();
    await posts.load('OTHERS');

    await posts.publish('mensaje nuevo');

    expect(posts.scope()).toBe('ALL');
  });

  it('si el servidor rechaza la publicacion se muestra su motivo', async () => {
    create.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 400,
        error: { errors: { message: 'El mensaje no puede superar los 280 caracteres' } },
      })),
    );
    const posts = store();

    await expect(posts.publish('x'.repeat(300))).resolves.toBe(false);

    expect(posts.error()).toBe('El mensaje no puede superar los 280 caracteres');
    expect(posts.publishing()).toBe(false);
  });

  it('un fallo al cargar deja el mensaje y no la lista a medias', async () => {
    list.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 0 })));
    const posts = store();

    await posts.load();

    expect(posts.error()).toContain('conectar con el servidor');
    expect(posts.loading()).toBe(false);
    expect(posts.isEmpty()).toBe(true);
  });

  it('el buscador filtra por mensaje y por autor sobre lo ya cargado', async () => {
    list.mockReturnValue(of(page([
      post('1', 'Desplegando el proyecto', 'jmendoza'),
      post('2', 'Buenos dias', 'lvargas'),
    ], 0, 1, 2)));
    const posts = store();
    await posts.load();

    posts.search('desplegando');
    expect(posts.visible().map((item) => item.id)).toEqual(['1']);

    posts.search('LVARGAS');
    expect(posts.visible().map((item) => item.id)).toEqual(['2']);

    posts.search('   ');
    expect(posts.visible()).toHaveLength(2);
  });

  it('filtrar no altera el total cargado que se muestra al pie', async () => {
    list.mockReturnValue(of(page([post('1', 'a'), post('2', 'b')], 0, 1, 2)));
    const posts = store();
    await posts.load();

    posts.search('a');

    expect(posts.loadedCount()).toBe(2);
  });

  it('con imagen sube primero y crea despues con el identificador', async () => {
    const posts = store();
    const file = new File(['x'], 'foto.png', { type: 'image/png' });

    await expect(posts.publish('Con foto', file)).resolves.toBe(true);

    expect(uploadImage).toHaveBeenCalledWith(file);
    expect(create).toHaveBeenCalledWith({ message: 'Con foto', imageId: 'img-1' });
  });

  it('si falla la subida no se crea la publicacion', async () => {
    uploadImage.mockReturnValue(throwError(() => new HttpErrorResponse({
      status: 400,
      error: { detail: 'La imagen no puede superar los 2 MB' },
    })));
    const posts = store();

    await expect(posts.publish('Con foto', new File(['x'], 'f.png'))).resolves.toBe(false);

    expect(create).not.toHaveBeenCalled();
    expect(posts.error()).toBe('La imagen no puede superar los 2 MB');
  });

  it('sin imagen no se sube nada', async () => {
    const posts = store();

    await posts.publish('Solo texto');

    expect(uploadImage).not.toHaveBeenCalled();
    expect(create).toHaveBeenCalledWith({ message: 'Solo texto', imageId: undefined });
  });

  it('el me gusta se pinta antes de que responda el servidor', async () => {
    list.mockReturnValue(of(page([post('1', 'a')], 0, 1, 1)));
    const posts = store();
    await posts.load();

    await posts.toggleLike(posts.visible()[0]);

    expect(like).toHaveBeenCalledWith('1');
    expect(posts.visible()[0].likedByMe).toBe(true);
    expect(posts.visible()[0].likes).toBe(1);
  });

  it('volver a pulsar lo retira', async () => {
    list.mockReturnValue(of(page([{ ...post('1', 'a'), likes: 5, likedByMe: true }], 0, 1, 1)));
    const posts = store();
    await posts.load();

    await posts.toggleLike(posts.visible()[0]);

    expect(unlike).toHaveBeenCalledWith('1');
    expect(posts.visible()[0].likedByMe).toBe(false);
    expect(posts.visible()[0].likes).toBe(4);
  });

  it('si el servidor rechaza el me gusta, se deshace lo pintado', async () => {
    list.mockReturnValue(of(page([post('1', 'a')], 0, 1, 1)));
    like.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 0 })));
    const posts = store();
    await posts.load();

    await posts.toggleLike(posts.visible()[0]);

    expect(posts.visible()[0].likedByMe).toBe(false);
    expect(posts.visible()[0].likes).toBe(0);
    expect(posts.error()).toContain('conectar con el servidor');
  });

  const scopes: readonly FeedScope[] = ['OTHERS', 'ALL', 'MINE'];

  it('pide al servidor cada uno de los filtros del contrato', async () => {
    const posts = store();

    for (const scope of scopes) {
      await posts.load(scope);
      expect(list).toHaveBeenCalledWith(scope, 0, 2);
    }
  });
});
