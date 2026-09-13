import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ThemeStore } from '../core/theme/theme.store';

const LABEL = {
  system: 'Tema del sistema',
  light: 'Tema claro',
  dark: 'Tema oscuro',
} as const;

@Component({
  selector: 'app-theme-toggle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="toggle" [title]="label()" [attr.aria-label]="label()"
            (click)="theme.next()">
      @switch (theme.preference()) {
        @case ('light') {
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="4.2" />
            <path d="M12 2.5v2.2M12 19.3v2.2M2.5 12h2.2M19.3 12h2.2M5.3 5.3l1.6 1.6M17.1 17.1l1.6 1.6M18.7 5.3l-1.6 1.6M6.9 17.1l-1.6 1.6" />
          </svg>
        }
        @case ('dark') {
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M20 14.5A8.5 8.5 0 0 1 9.5 4a8.5 8.5 0 1 0 10.5 10.5Z" />
          </svg>
        }
        @default {
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <rect x="2.8" y="4.5" width="18.4" height="12" rx="1.8" />
            <path d="M8.5 20h7" />
          </svg>
        }
      }
    </button>
  `,
  styles: `
    .toggle {
      display: grid;
      place-items: center;
      width: 36px;
      height: 36px;
      padding: 0;
      border: 1px solid var(--border);
      border-radius: var(--radius-full);
      background: var(--ground);
      color: var(--fg-secondary);
      transition: color 120ms ease, border-color 120ms ease;
    }

    .toggle:hover {
      color: var(--accent-text);
      border-color: var(--accent);
    }

    svg {
      width: 18px;
      height: 18px;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.6;
      stroke-linecap: round;
    }
  `,
})
export class ThemeToggle {

  protected readonly theme = inject(ThemeStore);
  protected readonly label = computed(() => LABEL[this.theme.preference()]);
}
