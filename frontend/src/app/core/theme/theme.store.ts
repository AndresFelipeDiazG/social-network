import { Injectable, signal } from '@angular/core';

export type ThemePreference = 'system' | 'light' | 'dark';

const KEY = 'social-network.theme';
const ORDER: readonly ThemePreference[] = ['system', 'light', 'dark'];

/**
 * El tema vive en un atributo del elemento raiz y los estilos reaccionan a el.
 * 'system' no escribe el atributo: asi manda la consulta prefers-color-scheme y
 * el navegador sigue al sistema operativo sin que haya que escucharlo.
 */
@Injectable({ providedIn: 'root' })
export class ThemeStore {

  private readonly current = signal<ThemePreference>(readStored());

  readonly preference = this.current.asReadonly();

  constructor() {
    this.apply(this.current());
  }

  next(): void {
    const chosen = ORDER[(ORDER.indexOf(this.current()) + 1) % ORDER.length];

    this.current.set(chosen);
    this.apply(chosen);

    try {
      localStorage.setItem(KEY, chosen);
    } catch {
      // La preferencia se pierde al recargar, pero la sesion actual la respeta.
    }
  }

  private apply(preference: ThemePreference): void {
    const root = document.documentElement;

    if (preference === 'system') {
      root.removeAttribute('data-theme');
      return;
    }

    root.setAttribute('data-theme', preference);
  }
}

function readStored(): ThemePreference {
  try {
    const stored = localStorage.getItem(KEY);
    return ORDER.includes(stored as ThemePreference) ? (stored as ThemePreference) : 'system';
  } catch {
    return 'system';
  }
}
