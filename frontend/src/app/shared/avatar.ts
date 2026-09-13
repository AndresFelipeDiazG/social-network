import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { initialsOf } from './initials';

@Component({
  selector: 'app-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '<span class="avatar" [class.avatar--small]="small()" aria-hidden="true">{{ initials() }}</span>',
  styles: `
    .avatar {
      display: grid;
      place-items: center;
      flex: 0 0 auto;
      width: 44px;
      height: 44px;
      border-radius: var(--radius-full);
      background: var(--accent-soft);
      color: var(--accent-text);
      font-size: 0.875rem;
      font-weight: 600;
      letter-spacing: 0.02em;
      user-select: none;
    }

    .avatar--small {
      width: 32px;
      height: 32px;
      font-size: 0.6875rem;
    }
  `,
})
export class Avatar {

  readonly name = input.required<string>();
  readonly small = input(false);

  protected readonly initials = computed(() => initialsOf(this.name()));
}
