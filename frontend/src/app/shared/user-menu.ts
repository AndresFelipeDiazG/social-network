import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { Avatar } from './avatar';

/**
 * El avatar es el unico control visible; el resto se despliega al pulsarlo. La
 * barra queda limpia y la accion destructiva deja de estar a un clic de
 * distancia por accidente.
 */
@Component({
  selector: 'app-user-menu',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar],
  host: { '(keydown.escape)': 'close()' },
  template: `
    <div class="menu">
      <button
        type="button"
        class="menu__trigger"
        [class.menu__trigger--open]="open()"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="'Cuenta de ' + name()"
        aria-haspopup="menu"
        (click)="toggle()"
      >
        <app-avatar [name]="name()" [small]="true" />
        <svg class="menu__caret" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M6.5 9.5l5.5 5.5 5.5-5.5" />
        </svg>
      </button>

      @if (open()) {
        <!-- Cierra al pulsar fuera sin necesidad de escuchar el documento. -->
        <button type="button" class="menu__backdrop" tabindex="-1" aria-hidden="true"
                (click)="close()"></button>

        <div class="menu__panel" role="menu">
          <div class="menu__who">
            <span class="menu__name">{{ name() }}</span>
            <span class="menu__handle">&#64;{{ username() }}</span>
          </div>

          <button type="button" class="menu__item" role="menuitem" (click)="confirm()">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M14.5 8.2V6.4A1.9 1.9 0 0 0 12.6 4.5H6.4A1.9 1.9 0 0 0 4.5 6.4v11.2A1.9 1.9 0 0 0 6.4 19.5h6.2a1.9 1.9 0 0 0 1.9-1.9v-1.8" />
              <path d="M10 12h9.5" />
              <path d="M16.8 8.8 20 12l-3.2 3.2" />
            </svg>
            Cerrar sesión
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .menu {
      position: relative;
    }

    .menu__trigger {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 3px 6px 3px 3px;
      border: 1px solid transparent;
      border-radius: var(--radius-full);
      background: none;
    }

    .menu__trigger:hover,
    .menu__trigger--open {
      border-color: var(--border);
      background: var(--surface);
    }

    .menu__caret {
      width: 14px;
      height: 14px;
      fill: none;
      stroke: var(--fg-muted);
      stroke-width: 1.8;
      stroke-linecap: round;
      transition: transform 140ms ease;
    }

    .menu__trigger--open .menu__caret {
      transform: rotate(180deg);
    }

    .menu__backdrop {
      position: fixed;
      inset: 0;
      z-index: 15;
      padding: 0;
      border: 0;
      background: none;
    }

    .menu__panel {
      position: absolute;
      top: calc(100% + 8px);
      right: 0;
      z-index: 20;
      min-width: 196px;
      padding: 6px;
      border: 1px solid var(--border);
      border-radius: var(--radius-xl);
      background: var(--ground);
      box-shadow: 0 10px 28px -12px rgb(0 0 0 / 28%);
      transform-origin: top right;
      animation: unfold 130ms ease;
    }

    @keyframes unfold {
      from {
        opacity: 0;
        transform: translateY(-6px) scaleY(0.94);
      }
    }

    .menu__who {
      padding: 8px 10px 10px;
      border-bottom: 1px solid var(--border);
    }

    .menu__name {
      display: block;
      font-size: 0.8125rem;
      font-weight: 600;
    }

    .menu__handle {
      display: block;
      margin-top: 1px;
      font-family: var(--font-data);
      font-variant-numeric: tabular-nums;
      font-size: 0.6875rem;
      color: var(--fg-muted);
    }

    .menu__item {
      display: flex;
      align-items: center;
      gap: 9px;
      width: 100%;
      margin-top: 4px;
      padding: 9px 10px;
      border: 0;
      border-radius: var(--radius-lg);
      background: none;
      color: var(--fg-secondary);
      font-size: 0.8125rem;
      text-align: left;
    }

    .menu__item:hover {
      background: var(--danger-soft);
      color: var(--danger);
    }

    .menu__item svg {
      width: 16px;
      height: 16px;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.7;
      stroke-linecap: round;
      stroke-linejoin: round;
    }
  `,
})
export class UserMenu {

  readonly name = input.required<string>();
  readonly username = input.required<string>();

  readonly signOut = output<void>();

  protected readonly open = signal(false);

  protected toggle(): void {
    this.open.update((open) => !open);
  }

  protected close(): void {
    this.open.set(false);
  }

  protected confirm(): void {
    this.close();
    this.signOut.emit();
  }
}
