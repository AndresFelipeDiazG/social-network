// Valores de desarrollo, usados por 'ng serve'. Dentro del contenedor este
// archivo lo reescribe el entrypoint de nginx a partir de las variables de
// entorno, de modo que dev y produccion recorren el mismo camino.
window.__APP_CONFIG__ = {
  apiBaseUrl: '/api',
  appName: 'Red Social',
  postsPageSize: 20,
  sessionExpiryWarningSeconds: 60
};
