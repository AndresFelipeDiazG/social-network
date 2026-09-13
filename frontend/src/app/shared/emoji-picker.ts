import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

/**
 * Paleta fija y corta. Un catalogo completo pide busqueda, categorias y una
 * dependencia de datos de varios cientos de kilobytes; para escribir un mensaje
 * de 280 caracteres basta con los que de verdad se usan.
 */
const EMOJIS = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🙂', '😉', '😊', '😍',
  '😎', '🤔', '🤨', '😐', '😴', '😭', '😤', '🥳', '🤝', '👀',
  '👍', '👎', '👏', '🙌', '🙏', '💪', '✌️', '🤞', '❤️', '🔥',
  '⭐', '✨', '💯', '✅', '❌', '⚡', '🎉', '🚀', '🎯', '📌',
  '💻', '☕', '📝', '🐛', '🌱', '🌙', '☀️', '🍕', '🥑', '🎧',
] as const;

@Component({
  selector: 'app-emoji-picker',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(keydown.escape)': 'close()' },
  template: `
    <div class="picker">
      <button
        type="button"
        class="picker__trigger"
        [class.picker__trigger--open]="open()"
        [disabled]="disabled()"
        [attr.aria-expanded]="open()"
        aria-label="Añadir emoji"
        title="Añadir emoji"
        (click)="toggle()"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="8.4" />
          <path d="M9 14.2c.8.9 1.8 1.4 3 1.4s2.2-.5 3-1.4" />
          <path d="M9.3 9.6h.01M14.7 9.6h.01" />
        </svg>
      </button>

      @if (open()) {
        <button type="button" class="picker__backdrop" tabindex="-1" aria-hidden="true"
                (click)="close()"></button>

        <div class="picker__panel" role="group" aria-label="Emojis">
          @for (emoji of emojis; track emoji) {
            <button type="button" class="picker__emoji" [attr.aria-label]="emoji"
                    (click)="pick.emit(emoji)">{{ emoji }}</button>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .picker {
      position: relative;
      display: flex;
    }

    .picker__trigger {
      display: grid;
      place-items: center;
      width: 30px;
      height: 30px;
      padding: 0;
      border: 1px solid transparent;
      border-radius: var(--radius-full);
      background: none;
      color: var(--fg-muted);
    }

    .picker__trigger:hover:not(:disabled),
    .picker__trigger--open {
      border-color: var(--border);
      color: var(--accent-text);
    }

    .picker__trigger svg {
      width: 18px;
      height: 18px;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.6;
      stroke-linecap: round;
    }

    .picker__backdrop {
      position: fixed;
      inset: 0;
      z-index: 15;
      padding: 0;
      border: 0;
      background: none;
    }

    .picker__panel {
      position: absolute;
      top: calc(100% + 8px);
      left: 0;
      z-index: 20;
      display: grid;
      grid-template-columns: repeat(10, 1fr);
      gap: 2px;
      width: max-content;
      max-width: min(320px, 78vw);
      padding: 8px;
      border: 1px solid var(--border);
      border-radius: var(--radius-xl);
      background: var(--ground);
      box-shadow: 0 12px 30px -14px rgb(0 0 0 / 30%);
      animation: unfold 130ms ease;
    }

    @keyframes unfold {
      from {
        opacity: 0;
        transform: translateY(-6px);
      }
    }

    .picker__emoji {
      width: 28px;
      height: 28px;
      padding: 0;
      border: 0;
      border-radius: var(--radius-lg);
      background: none;
      font-size: 1rem;
      line-height: 1;
    }

    .picker__emoji:hover {
      background: var(--accent-soft);
    }

    @media (max-width: 420px) {
      .picker__panel {
        grid-template-columns: repeat(8, 1fr);
      }
    }
  `,
})
export class EmojiPicker {

  readonly disabled = input(false);

  /** No se cierra al elegir: casi siempre se ponen varios seguidos. */
  readonly pick = output<string>();

  protected readonly emojis = EMOJIS;
  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((open) => !open);
  }

  protected close(): void {
    this.open.set(false);
  }
}
