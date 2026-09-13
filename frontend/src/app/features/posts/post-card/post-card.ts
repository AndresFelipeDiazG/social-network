import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Avatar } from '../../../shared/avatar';
import { absoluteTime, relativeTime } from '../../../shared/relative-time';
import { Post } from '../post.models';

@Component({
  selector: 'app-post-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar],
  template: `
    <article class="post" [class.post--mine]="post().mine">
      <header class="post__head">
        <app-avatar [name]="post().author.displayName" />

        <div class="post__who">
          <span class="post__name">{{ post().author.displayName }}</span>
          <span class="post__meta">
            &#64;{{ post().author.username }} &middot;
            <time [attr.datetime]="post().publishedAt" [title]="exactTime()">{{ shortTime() }}</time>
          </span>
        </div>

        @if (post().mine) {
          <span class="post__badge">Tú</span>
        }
      </header>

      @if (post().message) {
        <p class="post__message">{{ post().message }}</p>
      }

      @if (post().imageUrl) {
        <img class="post__image" [src]="post().imageUrl" [alt]="'Imagen de ' + post().author.displayName"
             loading="lazy" />
      }

      <footer class="post__foot">
        <button
          type="button"
          class="like"
          [class.like--on]="post().likedByMe"
          [attr.aria-pressed]="post().likedByMe"
          [attr.aria-label]="post().likedByMe ? 'Quitar me gusta' : 'Me gusta'"
          (click)="toggleLike.emit(post())"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 20.2s-7.5-4.4-7.5-9.4a4.3 4.3 0 0 1 7.5-2.9 4.3 4.3 0 0 1 7.5 2.9c0 5-7.5 9.4-7.5 9.4Z" />
          </svg>
          @if (post().likes > 0) {
            <span class="like__count">{{ post().likes }}</span>
          }
        </button>
      </footer>
    </article>
  `,
  styles: `
    :host {
      display: block;
    }

    .post {
      padding: 20px 22px;
      border: 1px solid var(--border);
      border-radius: var(--radius-2xl);
      background: var(--ground);
      box-shadow: var(--shadow-card);
      transition: box-shadow 140ms ease, border-color 140ms ease;
    }

    .post:hover {
      border-color: color-mix(in srgb, var(--fg-muted) 35%, var(--border));
      box-shadow: 0 2px 4px rgb(16 24 40 / 6%), 0 14px 30px -18px rgb(16 24 40 / 45%);
    }

    /* Las propias se distinguen por el filo, no por el fondo: un color de relleno
       distinto en la mitad de las tarjetas ensucia la lectura de la columna. */
    .post--mine {
      border-left: 3px solid var(--accent);
    }

    .post__head {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    /* El nombre arriba y el usuario con la fecha debajo: en una sola linea, los
       tres datos se leen como una frase y el nombre deja de destacar. */
    .post__who {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .post__name {
      font-size: 0.9375rem;
      font-weight: 600;
      letter-spacing: -0.01em;
    }

    .post__meta {
      margin-top: 1px;
      font-size: 0.8125rem;
      font-variant-numeric: tabular-nums;
      color: var(--fg-muted);
    }

    .post__badge {
      margin-left: auto;
      align-self: flex-start;
      padding: 2px 7px;
      border-radius: var(--radius-full);
      background: var(--accent-soft);
      color: var(--accent-text);
      font-size: 0.625rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    /* A ancho completo bajo la cabecera, no en la columna del nombre: sangrar el
       texto bajo el avatar estrecha la linea y encarece la lectura. */
    /* Altura reservada por proporcion para que la tarjeta no salte cuando la
       imagen termina de cargar. */
    .post__image {
      display: block;
      width: 100%;
      max-height: 420px;
      margin-top: 14px;
      border: 1px solid var(--border);
      border-radius: var(--radius-lg);
      object-fit: cover;
      background: var(--surface);
    }

    .post__foot {
      display: flex;
      margin-top: 12px;
    }

    .like {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 5px 10px 5px 8px;
      border: 0;
      border-radius: var(--radius-full);
      background: none;
      color: var(--fg-muted);
      font-size: 0.75rem;
      font-variant-numeric: tabular-nums;
      transition: color 120ms ease, background 120ms ease;
    }

    .like:hover {
      background: var(--danger-soft);
      color: var(--danger);
    }

    .like svg {
      width: 17px;
      height: 17px;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.7;
      stroke-linejoin: round;
    }

    /* Relleno solido cuando esta marcado: el color por si solo no basta para
       quien no distingue bien el rojo del gris. */
    .like--on {
      color: var(--danger);
    }

    .like--on svg {
      fill: currentColor;
    }

    .post__message {
      margin: 14px 0 0;
      font-size: 0.9375rem;
      line-height: 1.6;
      overflow-wrap: anywhere;
      white-space: pre-wrap;
    }
  `,
})
export class PostCard {

  readonly post = input.required<Post>();

  readonly toggleLike = output<Post>();

  protected readonly shortTime = computed(() => relativeTime(this.post().publishedAt));
  protected readonly exactTime = computed(() => absoluteTime(this.post().publishedAt));
}
