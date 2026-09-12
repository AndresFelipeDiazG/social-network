# Node solo hace falta para compilar. La imagen final es nginx sirviendo
# estaticos: sin Node, sin node_modules y sin codigo fuente dentro.

FROM node:24-alpine AS build
WORKDIR /build

# package-lock antes del codigo, por la cache de capas de Docker. 'npm ci'
# instala las versiones exactas del lock en lugar de resolver el rango, que es
# lo que hace reproducible la instalacion.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build


FROM nginx:1.29-alpine

COPY --from=build /build/dist/frontend/browser /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

# La imagen oficial de nginx ejecuta todo script de /docker-entrypoint.d antes
# de arrancar. Ahi se genera config.js a partir de las variables de entorno, de
# modo que la MISMA imagen sirve para cualquier entorno sin recompilar.
COPY docker/config.template.js /usr/share/nginx/html/config.template.js
COPY docker/40-app-config.sh /docker-entrypoint.d/40-app-config.sh
RUN chmod +x /docker-entrypoint.d/40-app-config.sh

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/ || exit 1
