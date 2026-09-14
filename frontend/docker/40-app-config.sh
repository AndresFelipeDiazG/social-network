#!/bin/sh
# La imagen oficial de nginx ejecuta los scripts de /docker-entrypoint.d en
# orden alfabetico antes de arrancar el servidor. El 40 lo coloca despues de los
# que trae la propia imagen.
set -eu

: "${API_BASE_URL:=/api}"
: "${APP_NAME:=Red Social}"
: "${POSTS_PAGE_SIZE:=20}"

export API_BASE_URL APP_NAME POSTS_PAGE_SIZE

# La plantilla vive fuera de la raiz publica: no se sirve y sobrevive al arranque.
# Borrarla despues de generar config.js dejaba el contenedor sin poder reiniciarse.
#
# La lista explicita de variables es obligatoria. Sin ella, envsubst sustituye
# todo lo que parezca $algo y destroza cualquier JavaScript que use el simbolo.
envsubst '${API_BASE_URL} ${APP_NAME} ${POSTS_PAGE_SIZE}' \
  < /usr/share/nginx/config.template.js \
  > /usr/share/nginx/html/config.js


echo "[40-app-config] config.js generado con apiBaseUrl=${API_BASE_URL}"
