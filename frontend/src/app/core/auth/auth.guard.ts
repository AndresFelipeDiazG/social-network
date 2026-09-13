import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';

/** Protege las rutas privadas y recuerda a donde queria ir el usuario. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthStore);
  const router = inject(Router);

  if (auth.authenticated()) {
    return true;
  }

  return router.createUrlTree(['/entrar'], {
    queryParams: state.url === '/muro' ? {} : { volverA: state.url },
  });
};

/** Evita volver al formulario de entrada con la sesion ya iniciada. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthStore);
  const router = inject(Router);

  return auth.authenticated() ? router.createUrlTree(['/muro']) : true;
};
