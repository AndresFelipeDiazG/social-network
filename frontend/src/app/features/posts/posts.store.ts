import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { firstValueFrom } from 'rxjs';
import { APP_CONFIG } from '../../core/config/app-config';
import { messageFrom } from '../../core/http/api-error';
import { FeedScope, Post } from './post.models';
import { PostsClient } from './posts.client';

interface PostsState {
  readonly items: readonly Post[];
  readonly scope: FeedScope;
  readonly page: number;
  readonly totalElements: number;
  readonly totalPages: number;
  readonly query: string;
  readonly loading: boolean;
  readonly loadingMore: boolean;
  readonly publishing: boolean;
  readonly error: string | null;
}

const EMPTY: PostsState = {
  items: [],
  scope: 'OTHERS',
  page: 0,
  totalElements: 0,
  totalPages: 0,
  query: '',
  loading: false,
  loadingMore: false,
  publishing: false,
  error: null,
};

export const PostsStore = signalStore(
  withState(EMPTY),

  withComputed(({ items, query, page, totalPages, totalElements, loading }) => ({
    /**
     * El backend no expone busqueda, asi que el filtro se aplica sobre lo ya
     * cargado. Se avisa en la interfaz para que nadie lo confunda con una
     * busqueda en todo el muro.
     */
    visible: computed(() => {
      const term = query().trim().toLowerCase();

      if (term === '') {
        return items();
      }

      return items().filter(
        (post) =>
          post.message.toLowerCase().includes(term) ||
          post.author.displayName.toLowerCase().includes(term) ||
          post.author.username.toLowerCase().includes(term),
      );
    }),
    hasMore: computed(() => page() + 1 < totalPages()),
    loadedCount: computed(() => items().length),
    totalCount: computed(() => totalElements()),
    isEmpty: computed(() => !loading() && items().length === 0),
  })),

  withMethods((store, client = inject(PostsClient), config = inject(APP_CONFIG)) => {

    async function fetchPage(scope: FeedScope, page: number) {
      return firstValueFrom(client.list(scope, page, config.postsPageSize));
    }

    /** Primera pagina del filtro indicado. Reemplaza lo que hubiera. */
    function apply(postId: string, change: Partial<Post>): void {
      patchState(store, {
        items: store.items().map((post) => (post.id === postId ? { ...post, ...change } : post)),
      });
    }

    async function loadFirstPage(scope: FeedScope = store.scope()): Promise<void> {
      patchState(store, { loading: true, error: null, scope });

      try {
        const result = await fetchPage(scope, 0);

        patchState(store, {
          items: result.content,
          page: result.page,
          totalElements: result.totalElements,
          totalPages: result.totalPages,
          loading: false,
        });
      } catch (failure) {
        patchState(store, {
          loading: false,
          error: messageFrom(failure, 'No se pudieron cargar las publicaciones.'),
        });
      }
    }

    return {
      load: loadFirstPage,

      /**
       * Siguiente pagina, añadiendo al final. Se descartan las repetidas: si
       * alguien publica entre dos peticiones, la ventana se desplaza y la
       * ultima de la pagina anterior reaparece en la siguiente.
       */
      async loadMore(): Promise<void> {
        if (store.loadingMore() || store.loading() || !store.hasMore()) {
          return;
        }

        patchState(store, { loadingMore: true, error: null });

        try {
          const result = await fetchPage(store.scope(), store.page() + 1);
          const known = new Set(store.items().map((post) => post.id));

          patchState(store, {
            items: [...store.items(), ...result.content.filter((post) => !known.has(post.id))],
            page: result.page,
            totalElements: result.totalElements,
            totalPages: result.totalPages,
            loadingMore: false,
          });
        } catch (failure) {
          patchState(store, {
            loadingMore: false,
            error: messageFrom(failure, 'No se pudieron cargar más publicaciones.'),
          });
        }
      },

      /**
       * Con imagen son dos peticiones: primero se sube y despues se crea la
       * publicacion con el identificador. Si la subida falla no se crea nada, que
       * es preferible a dejar una publicacion sin la foto que el usuario eligio.
       */
      async publish(message: string, image?: File | null): Promise<boolean> {
        patchState(store, { publishing: true, error: null });

        try {
          const imageId = image
            ? (await firstValueFrom(client.uploadImage(image))).id
            : undefined;

          // Sin fecha: el servidor usa el instante de guardado, que es el contrato.
          await firstValueFrom(client.create({ message, imageId }));
          patchState(store, { publishing: false });

          // Recargar en lugar de insertar la respuesta en la lista: el orden y
          // los totales los decide el servidor. Y si el filtro era OTHERS se
          // cambia a ALL, porque la publicacion propia no cabe en ese filtro y
          // el usuario esperaria verla.
          await loadFirstPage(store.scope() === 'OTHERS' ? 'ALL' : store.scope());

          return true;
        } catch (failure) {
          patchState(store, {
            publishing: false,
            error: messageFrom(failure, 'No se pudo publicar el mensaje.'),
          });

          return false;
        }
      },

      /**
       * Pinta el cambio antes de que responda el servidor y lo deshace si falla.
       * Un me gusta que tarda 300 ms en verse se siente roto, y el servidor es
       * idempotente: reintentar no descuadra el recuento.
       */
      async toggleLike(post: Post): Promise<void> {
        const liked = !post.likedByMe;

        apply(post.id, { likedByMe: liked, likes: post.likes + (liked ? 1 : -1) });

        try {
          await firstValueFrom(liked ? client.like(post.id) : client.unlike(post.id));
        } catch (failure) {
          apply(post.id, { likedByMe: post.likedByMe, likes: post.likes });
          patchState(store, { error: messageFrom(failure, 'No se pudo registrar el me gusta.') });
        }
      },

      search(query: string): void {
        patchState(store, { query });
      },

      dismissError(): void {
        patchState(store, { error: null });
      },

      reset(): void {
        patchState(store, EMPTY);
      },
    };
  }),
);
