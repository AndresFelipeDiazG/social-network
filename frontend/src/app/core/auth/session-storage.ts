import { Injectable } from '@angular/core';
import { AuthenticatedUser } from './auth.models';

export interface StoredSession {
  readonly accessToken: string;
  readonly user: AuthenticatedUser;
  readonly expiresAt: number;
}

const KEY = 'social-network.session';

/**
 * El token se guarda en localStorage para que recargar la pagina no cierre la
 * sesion. Queda expuesto a XSS; la alternativa correcta es una cookie HttpOnly
 * con SameSite, pero eso exige que el backend la emita y aqui el contrato
 * devuelve el token en el cuerpo.
 *
 * Todo acceso va envuelto en try/catch: en modo privado o con las cookies
 * bloqueadas, localStorage existe pero lanza al escribir.
 */
@Injectable({ providedIn: 'root' })
export class SessionStorage {

  read(): StoredSession | null {
    try {
      const raw = localStorage.getItem(KEY);
      return raw === null ? null : this.parse(raw);
    } catch {
      return null;
    }
  }

  write(session: StoredSession): void {
    try {
      localStorage.setItem(KEY, JSON.stringify(session));
    } catch {
      // Sin persistencia la sesion vive en memoria hasta que se recargue.
    }
  }

  clear(): void {
    try {
      localStorage.removeItem(KEY);
    } catch {
      // Nada que hacer: la sesion en memoria ya se ha descartado.
    }
  }

  // Lo almacenado puede venir de una version anterior del formato o haber sido
  // editado a mano. Si no cumple la forma esperada se descarta.
  private parse(raw: string): StoredSession | null {
    try {
      const value = JSON.parse(raw) as Partial<StoredSession>;
      const user = value.user;

      const valid =
        typeof value.accessToken === 'string' &&
        typeof value.expiresAt === 'number' &&
        typeof user?.id === 'string' &&
        typeof user?.username === 'string';

      return valid ? (value as StoredSession) : null;
    } catch {
      return null;
    }
  }
}
