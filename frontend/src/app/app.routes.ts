import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';

// Las dos pantallas se cargan en diferido: el arranque solo baja el nucleo y la
// que toque, no las dos.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'muro' },
  {
    path: 'entrar',
    title: 'Entrar',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then((m) => m.LoginPage),
  },
  {
    path: 'muro',
    title: 'Muro',
    canActivate: [authGuard],
    loadComponent: () => import('./features/posts/feed/feed').then((m) => m.FeedPage),
  },
  { path: '**', redirectTo: 'muro' },
];
