import { initialsOf } from './initials';

describe('initialsOf', () => {
  it('toma la primera y la ultima palabra', () => {
    expect(initialsOf('Ana Correa')).toBe('AC');
  });

  it('ignora los nombres intermedios', () => {
    expect(initialsOf('Maria del Carmen Vargas')).toBe('MV');
  });

  it('con una sola palabra devuelve una inicial', () => {
    expect(initialsOf('Diego')).toBe('D');
  });

  it('no se rompe con espacios de sobra', () => {
    expect(initialsOf('   Lucia    Vargas  ')).toBe('LV');
  });

  it('devuelve un interrogante cuando no hay nombre', () => {
    expect(initialsOf('')).toBe('?');
    expect(initialsOf(null)).toBe('?');
    expect(initialsOf(undefined)).toBe('?');
  });
});
