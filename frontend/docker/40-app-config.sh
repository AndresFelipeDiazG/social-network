#!/bin/sh
# La imagen oficial de nginx ejecuta los scripts de /docker-entrypoint.d en
# orden alfabetico antes de arrancar el servidor. El 40 lo coloca despues de los
# que trae la propia imagen.
set -eu

: "${API_BASE_URL:=/api}"
: "${APP_NAME:=Red Social}"
: "${POSTS_PAGE_SIZE:=20}"
: "${SESSION_EXPIRY_WARNING_SECONDS:=60}"

export API_BASE_URL APP_NAME POSTS_PAGE_SIZE SESSION_EXPIRY_WARNING_SECONDS

# La lista explicita de variables es obligatoria. Sin ella, envsubst sustituye
# todo lo que parezca $algo y destroza cualquier JavaScript que use el simbolo.
envsubst '${API_BASE_URL} ${APP_NAME} ${POSTS_PAGE_SIZE} ${SESSION_EXPIRY_WARNING_SECONDS}' \
  < /usr/share/nginx/html/config.template.js \
  > /usr/share/nginx/html/config.js

rm -f /usr/share/nginx/html/config.template.js

echo "[40-app-config] config.js generado con apiBaseUrl=${API_BASE_URL}"
