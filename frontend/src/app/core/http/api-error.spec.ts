import { HttpErrorResponse } from '@angular/common/http';
import { isUnauthorized, messageFrom } from './api-error';

function problem(status: number, body: unknown): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: body });
}

describe('messageFrom', () => {
  it('prefiere el detalle del ProblemDetail', () => {
    const error = problem(401, { title: 'Credenciales invalidas', detail: 'Usuario o contrasena incorrectos' });

    expect(messageFrom(error)).toBe('Usuario o contrasena incorrectos');
  });

  it('usa el titulo cuando no hay detalle', () => {
    expect(messageFrom(problem(404, { title: 'Usuario no encontrado' }))).toBe('Usuario no encontrado');
  });

  it('saca el primer error de validacion, que es mas concreto que el detalle', () => {
    const error = problem(400, {
      detail: 'Alguno de los campos enviados no es valido',
      errors: { message: 'El mensaje es obligatorio' },
    });

    expect(messageFrom(error)).toBe('El mensaje es obligatorio');
  });

  it('status 0 se explica como fallo de conexion', () => {
    expect(messageFrom(problem(0, null))).toContain('conectar con el servidor');
  });

  it('sin cuerpo aprovechable cae en el mensaje de respaldo', () => {
    expect(messageFrom(problem(500, 'Internal Server Error'), 'respaldo')).toBe('respaldo');
  });

  it('lo que no sea una respuesta HTTP tambien cae en el respaldo', () => {
    expect(messageFrom(new TypeError('x'), 'respaldo')).toBe('respaldo');
  });
});

describe('isUnauthorized', () => {
  it('distingue el 401 del resto', () => {
    expect(isUnauthorized(problem(401, null))).toBe(true);
    expect(isUnauthorized(problem(403, null))).toBe(false);
    expect(isUnauthorized(new Error('x'))).toBe(false);
  });
});
