const MINUTE = 60;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;
const WEEK = 7 * DAY;

const FULL_DATE = new Intl.DateTimeFormat('es', { day: 'numeric', month: 'short', year: 'numeric' });

/**
 * Antigüedad legible: "ahora", "hace 5 min", "hace 3 h", "hace 2 d". A partir de
 * una semana pasa a fecha absoluta, porque "hace 43 d" ya no le dice nada a nadie.
 *
 * El instante de referencia se recibe por parametro en lugar de leer el reloj
 * aqui, para que las pruebas no dependan de la hora a la que se ejecutan.
 */
export function relativeTime(isoDate: string, now: Date = new Date()): string {
  const published = new Date(isoDate);

  if (Number.isNaN(published.getTime())) {
    return '';
  }

  const seconds = Math.floor((now.getTime() - published.getTime()) / 1000);

  // Un reloj adelantado en el cliente da diferencias negativas. Se muestra
  // "ahora" en lugar de un tiempo futuro.
  if (seconds < MINUTE) {
    return 'ahora';
  }
  if (seconds < HOUR) {
    return `hace ${Math.floor(seconds / MINUTE)} min`;
  }
  if (seconds < DAY) {
    return `hace ${Math.floor(seconds / HOUR)} h`;
  }
  if (seconds < WEEK) {
    return `hace ${Math.floor(seconds / DAY)} d`;
  }

  return FULL_DATE.format(published);
}

const FULL_DATE_TIME = new Intl.DateTimeFormat('es', {
  dateStyle: 'long',
  timeStyle: 'short',
});

/** Fecha completa para el title del elemento: el relativo pierde precision. */
export function absoluteTime(isoDate: string): string {
  const published = new Date(isoDate);
  return Number.isNaN(published.getTime()) ? '' : FULL_DATE_TIME.format(published);
}
