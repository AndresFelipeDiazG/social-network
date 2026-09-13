import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Avatar } from '../../../shared/avatar';

/**
 * Columna lateral con la cuenta que ha iniciado sesion. Lo que muestra ya esta
 * en memoria: no pide nada al servidor.
 */
@Component({
  selector: 'app-side-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar],
  template: `
    <section class="panel">
      <div class="me">
        <app-avatar [name]="name()" />
        <span class="me__text">
          <span class="me__name">{{ name() }}</span>
          <span class="me__handle">&#64;{{ username() }}</span>
        </span>
      </div>

      <button type="button" class="panel__action" (click)="showMine.emit()">
        Ver mis publicaciones
      </button>
    </section>
  `,
  styles: `
    :host {
      display: block;
    }

    .panel {
      padding: 18px;
      border: 1px solid var(--border);
      border-radius: var(--radius-2xl);
      background: var(--ground);
      box-shadow: var(--shadow-card);
    }

    .me {
      display: flex;
      align-items: center;
      gap: 11px;
    }

    .me__text {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .me__name {
      font-size: 0.875rem;
      font-weight: 600;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .me__handle {
      font-size: 0.75rem;
      color: var(--fg-muted);
    }

    .panel__action {
      width: 100%;
      margin-top: 16px;
      padding: 10px;
      border: 1px solid var(--border);
      border-radius: var(--radius-lg);
      background: none;
      color: var(--fg-secondary);
      font-size: 0.8125rem;
    }

    .panel__action:hover {
      border-color: var(--accent);
      color: var(--accent-text);
    }
  `,
})
export class SidePanel {

  readonly name = input.required<string>();
  readonly username = input.required<string>();

  readonly showMine = output<void>();
}
