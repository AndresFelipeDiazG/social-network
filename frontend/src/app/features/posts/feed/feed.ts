import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { APP_CONFIG } from '../../../core/config/app-config';
import { Brand } from '../../../shared/brand';
import { PostSkeleton } from '../../../shared/post-skeleton';
import { ThemeToggle } from '../../../shared/theme-toggle';
import { UserMenu } from '../../../shared/user-menu';
import { Composer } from '../composer/composer';
import { FeedFilters } from './feed-filters';
import { PostCard } from '../post-card/post-card';
import { FeedScope } from '../post.models';
import { PostsStore } from '../posts.store';
import { SidePanel } from '../side-panel/side-panel';

interface ScopeOption {
  readonly value: FeedScope;
  readonly label: string;
  readonly emptyMessage: string;
}

const SCOPES: readonly ScopeOption[] = [
  {
    value: 'OTHERS',
    label: 'De otros',
    emptyMessage: 'Nadie más ha publicado todavía.',
  },
  {
    value: 'ALL',
    label: 'Todas',
    emptyMessage: 'Aún no hay publicaciones. Escribe la primera.',
  },
  {
    value: 'MINE',
    label: 'Mías',
    emptyMessage: 'No has publicado nada todavía.',
  },
];

const SKELETONS = [1, 2, 3];

@Component({
  selector: 'app-feed',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Brand, Composer, FeedFilters, PostCard, PostSkeleton, SidePanel, ThemeToggle, UserMenu],
  // El store se provee aqui y no en la raiz: al salir del muro se destruye con
  // el componente, de modo que no queda el muro de la sesion anterior en memoria.
  providers: [PostsStore],
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class FeedPage {

  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly posts = inject(PostsStore);
  protected readonly appName = inject(APP_CONFIG).appName;

  protected readonly displayName = this.auth.displayName;
  protected readonly username = this.auth.username;

  protected readonly scopes = SCOPES;
  protected readonly skeletons = SKELETONS;

  protected readonly emptyMessage = computed(
    () => SCOPES.find((scope) => scope.value === this.posts.scope())?.emptyMessage ?? '',
  );

  /** Con el buscador activo no hay coincidencias, que no es lo mismo que un muro vacio. */
  protected readonly noMatches = computed(
    () => this.posts.query().trim() !== '' && this.posts.visible().length === 0 && !this.posts.isEmpty(),
  );

  constructor() {
    void this.posts.load();
  }

  protected async signOut(): Promise<void> {
    this.auth.logout();
    await this.router.navigateByUrl('/entrar');
  }
}
