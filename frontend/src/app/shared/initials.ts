/**
 * Dos iniciales como maximo: la primera y la ultima palabra. Con una sola
 * palabra devuelve su primera letra, y con un nombre vacio un interrogante,
 * para que el avatar nunca quede en blanco.
 */
export function initialsOf(name: string | null | undefined): string {
  const words = (name ?? '').trim().split(/\s+/).filter((word) => word.length > 0);

  if (words.length === 0) {
    return '?';
  }

  const first = words[0].charAt(0);
  const last = words.length > 1 ? words[words.length - 1].charAt(0) : '';

  return (first + last).toUpperCase();
}
