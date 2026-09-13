// Plantilla. El entrypoint del contenedor la procesa con envsubst y escribe
// config.js antes de que nginx arranque. Angular compila a JavaScript estatico,
// asi que no puede leer variables de entorno por si mismo: esta es la forma de
// configurarlo sin recompilar la imagen.
window.__APP_CONFIG__ = {
  apiBaseUrl: '${API_BASE_URL}',
  appName: '${APP_NAME}',
  postsPageSize: ${POSTS_PAGE_SIZE}
};
