import { relativeTime } from './relative-time';

const NOW = new Date('2026-09-13T12:00:00Z');

function ago(seconds: number): string {
  return new Date(NOW.getTime() - seconds * 1000).toISOString();
}

describe('relativeTime', () => {
  it('menos de un minuto es ahora', () => {
    expect(relativeTime(ago(30), NOW)).toBe('ahora');
  });

  it('minutos', () => {
    expect(relativeTime(ago(5 * 60), NOW)).toBe('hace 5 min');
  });

  it('horas', () => {
    expect(relativeTime(ago(3 * 3600), NOW)).toBe('hace 3 h');
  });

  it('dias', () => {
    expect(relativeTime(ago(2 * 86400), NOW)).toBe('hace 2 d');
  });

  it('a partir de una semana pasa a fecha absoluta', () => {
    expect(relativeTime(ago(30 * 86400), NOW)).toContain('2026');
  });

  it('un reloj adelantado en el cliente no muestra tiempos futuros', () => {
    expect(relativeTime(ago(-120), NOW)).toBe('ahora');
  });

  it('una fecha ilegible no rompe la vista', () => {
    expect(relativeTime('no es una fecha', NOW)).toBe('');
  });
});
