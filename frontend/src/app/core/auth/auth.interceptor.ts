import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { isUnauthorized } from '../http/api-error';
import { AuthStore } from './auth.store';

const LOGIN_PATH = '/auth/login';

/**
 * Añade el token a las llamadas a la API y centraliza la reaccion al 401: sin
 * esto, cada componente tendria que acordarse de poner la cabecera y de cerrar
 * la sesion cuando el servidor la rechaza.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthStore);
  const router = inject(Router);
  const token = auth.token();

  // El login lleva su propia cabecera Basic; sobreescribirla con el Bearer de
  // una sesion anterior haria fallar el intento de entrar de nuevo.
  const authorized =
    token !== null && !request.url.includes(LOGIN_PATH)
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorized).pipe(
    catchError((failure: unknown) => {
      // Solo si habia sesion: un 401 del propio login son credenciales malas y
      // lo gestiona la pantalla de entrada.
      if (isUnauthorized(failure) && token !== null) {
        auth.expire();
        void router.navigateByUrl('/entrar');
      }

      return throwError(() => failure);
    }),
  );
};
