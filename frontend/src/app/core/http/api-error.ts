import { HttpErrorResponse } from '@angular/common/http';

/** Cuerpo de error RFC 7807, el formato que devuelven los dos servicios. */
interface ProblemDetail {
  readonly title?: string;
  readonly detail?: string;
  readonly errors?: Record<string, string>;
}

const NETWORK = 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.';
const UNEXPECTED = 'Ha ocurrido un error inesperado. Inténtalo de nuevo.';

/**
 * Traduce la respuesta a un mensaje presentable. Se prefiere el detalle del
 * servidor porque ya viene redactado para el usuario; el resto son respaldos
 * para cuando no hay cuerpo que leer, como en un fallo de red.
 */
export function messageFrom(error: unknown, fallback = UNEXPECTED): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  // status 0 significa que la peticion no llego a salir o no hubo respuesta:
  // servidor caido, DNS, CORS o el navegador sin red.
  if (error.status === 0) {
    return NETWORK;
  }

  const problem = error.error as ProblemDetail | string | null;

  if (typeof problem === 'string' || problem === null) {
    return fallback;
  }

  const firstFieldError = problem.errors ? Object.values(problem.errors)[0] : undefined;

  return firstFieldError ?? problem.detail ?? problem.title ?? fallback;
}

export function isUnauthorized(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status === 401;
}
