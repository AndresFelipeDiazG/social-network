import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { firstValueFrom } from 'rxjs';
import { initialsOf } from '../../shared/initials';
import { messageFrom } from '../http/api-error';
import { AuthClient } from './auth.client';
import { AuthenticatedUser, Credentials } from './auth.models';
import { SessionStorage } from './session-storage';

interface AuthState {
  readonly user: AuthenticatedUser | null;
  readonly token: string | null;
  readonly expiresAt: number | null;
  readonly submitting: boolean;
  readonly error: string | null;
}

const SIGNED_OUT: AuthState = {
  user: null,
  token: null,
  expiresAt: null,
  submitting: false,
  error: null,
};

const WRONG_CREDENTIALS = 'Usuario o contraseña incorrectos';
const EXPIRED = 'Tu sesión ha caducado. Vuelve a iniciarla.';

/**
 * Unica fuente de verdad de la sesion. El resto de la aplicacion lee señales y
 * no vuelve a preguntar al servidor quien esta conectado.
 */
export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState(SIGNED_OUT),

  withComputed(({ user, token }) => ({
    authenticated: computed(() => token() !== null),
    displayName: computed(() => user()?.displayName ?? ''),
    username: computed(() => user()?.username ?? ''),
    initials: computed(() => initialsOf(user()?.displayName)),
  })),

  withMethods((store, client = inject(AuthClient), storage = inject(SessionStorage)) => ({

    /** Devuelve si se pudo entrar, para que el componente decida la navegacion. */
    async login(credentials: Credentials): Promise<boolean> {
      patchState(store, { submitting: true, error: null });

      try {
        const response = await firstValueFrom(client.login(credentials));

        // El backend devuelve segundos restantes, no una fecha: se convierte a
        // instante absoluto porque es lo que hay que comparar tras recargar.
        const session = {
          accessToken: response.accessToken,
          user: response.user,
          expiresAt: Date.now() + response.expiresIn * 1000,
        };

        storage.write(session);
        patchState(store, {
          token: session.accessToken,
          user: session.user,
          expiresAt: session.expiresAt,
          submitting: false,
          error: null,
        });

        return true;
      } catch (failure) {
        patchState(store, {
          submitting: false,
          error: messageFrom(failure, WRONG_CREDENTIALS),
        });

        return false;
      }
    },

    logout(): void {
      storage.clear();
      patchState(store, SIGNED_OUT);
    },

    /** El servidor rechazo el token: se cierra la sesion avisando del motivo. */
    expire(): void {
      storage.clear();
      patchState(store, { ...SIGNED_OUT, error: EXPIRED });
    },

    dismissError(): void {
      patchState(store, { error: null });
    },

    /**
     * Recupera la sesion guardada al arrancar. Un token caducado se descarta
     * aqui en lugar de esperar al primer 401: asi no se muestra el muro vacio
     * un instante antes de rebotar al login.
     */
    restore(): void {
      const session = storage.read();

      if (session === null) {
        return;
      }

      if (session.expiresAt <= Date.now()) {
        storage.clear();
        return;
      }

      patchState(store, {
        token: session.accessToken,
        user: session.user,
        expiresAt: session.expiresAt,
      });
    },
  })),

  withHooks({
    onInit(store) {
      store.restore();
    },
  }),
);
