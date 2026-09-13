import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Marca el hueco de una publicacion mientras llega la respuesta. */
@Component({
  selector: 'app-post-skeleton',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="row" aria-hidden="true">
      <span class="avatar"></span>
      <span class="lines">
        <span class="line line--short"></span>
        <span class="line"></span>
      </span>
    </div>
  `,
  styles: `
    :host {
      display: block;
    }

    .row {
      display: flex;
      gap: 12px;
      padding: 20px 22px;
      border: 1px solid var(--border);
      border-radius: var(--radius-2xl);
      background: var(--ground);
      box-shadow: var(--shadow-card);
    }

    .lines {
      display: flex;
      flex: 1;
      flex-direction: column;
      gap: 8px;
      padding-top: 6px;
    }

    .avatar {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-full);
    }

    .line {
      height: 10px;
      border-radius: var(--radius-lg);
    }

    .line--short {
      width: 35%;
    }

    .avatar,
    .line {
      background: var(--surface);
      animation: pulse 1.4s ease-in-out infinite;
    }

    @keyframes pulse {
      50% {
        opacity: 0.45;
      }
    }
  `,
})
export class PostSkeleton {
}
