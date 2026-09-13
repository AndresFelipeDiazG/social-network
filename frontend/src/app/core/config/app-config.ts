import { InjectionToken } from '@angular/core';

export interface AppConfig {
  readonly apiBaseUrl: string;
  readonly appName: string;
  readonly postsPageSize: number;
}

export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');

interface ConfigWindow extends Window {
  __APP_CONFIG__?: Partial<AppConfig>;
}

const FALLBACK: AppConfig = {
  apiBaseUrl: '/api',
  appName: 'Red Social',
  postsPageSize: 20,
};

/**
 * Angular compila a JavaScript estatico y no puede leer variables de entorno. El
 * contenedor escribe config.js antes de arrancar nginx y aqui se lee una sola vez,
 * envuelto en un token para que el resto del codigo no toque window.
 */
export function readRuntimeConfig(): AppConfig {
  const provided = (window as ConfigWindow).__APP_CONFIG__;

  if (!provided) {
    return FALLBACK;
  }

  return {
    apiBaseUrl: provided.apiBaseUrl?.trim() || FALLBACK.apiBaseUrl,
    appName: provided.appName?.trim() || FALLBACK.appName,
    postsPageSize: Number(provided.postsPageSize) || FALLBACK.postsPageSize,
  };
}
