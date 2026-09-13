import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FeedScope } from '../post.models';

export interface ScopeOption {
  readonly value: FeedScope;
  readonly label: string;
}

@Component({
  selector: 'app-feed-filters',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="filters">
      <div class="chips" role="group" aria-label="Filtrar publicaciones">
        @for (scope of scopes(); track scope.value) {
          <button
            type="button"
            class="chip"
            [class.chip--active]="active() === scope.value"
            [attr.aria-pressed]="active() === scope.value"
            [disabled]="busy()"
            (click)="select.emit(scope.value)"
          >
            {{ scope.label }}
          </button>
        }
      </div>

      <button
        type="button"
        class="refresh"
        [class.refresh--spinning]="busy()"
        [disabled]="busy()"
        (click)="refresh.emit()"
        title="Actualizar"
        aria-label="Actualizar publicaciones"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M20 12a8 8 0 1 1-2.6-5.9" />
          <path d="M20.5 4.5V10H15" />
        </svg>
      </button>
    </div>
  `,
  styles: `
    .filters {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      margin: 18px 0 14px;
    }

    .chips {
      display: flex;
      gap: 6px;
    }

    .chip {
      padding: 7px 14px;
      border: 1px solid var(--border);
      border-radius: var(--radius-full);
      background: var(--ground);
      color: var(--fg-secondary);
      font-size: 0.8125rem;
    }

    .chip:hover:not(:disabled) {
      color: var(--fg-primary);
    }

    .chip--active {
      border-color: var(--accent);
      background: var(--accent-soft);
      color: var(--accent-text);
      font-weight: 500;
    }

    .refresh {
      display: grid;
      place-items: center;
      width: 34px;
      height: 34px;
      padding: 0;
      border: 1px solid var(--border);
      border-radius: var(--radius-full);
      background: var(--ground);
      color: var(--fg-secondary);
    }

    .refresh:hover:not(:disabled) {
      color: var(--accent-text);
      border-color: var(--accent);
    }

    .refresh svg {
      width: 15px;
      height: 15px;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.8;
      stroke-linecap: round;
    }

    /* Gira mientras carga: sin eso, pulsar actualizar con el muro ya al dia no
       da ninguna senal de que haya pasado algo. */
    .refresh--spinning svg {
      animation: spin 800ms linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }
  `,
})
export class FeedFilters {

  readonly scopes = input.required<readonly ScopeOption[]>();
  readonly active = input.required<FeedScope>();
  readonly busy = input(false);

  readonly select = output<FeedScope>();
  readonly refresh = output<void>();
}
