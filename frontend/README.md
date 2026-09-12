# Frontend

Angular 21 sin `zone.js`, con NgRx SignalStore para el estado y Vitest para las
pruebas. La documentacion completa del proyecto esta en el
[README de la raiz](../README.md).

## Desarrollo

```bash
npm install
npm start     # http://localhost:4200
```

`proxy.conf.json` redirige `/api` al gateway en `localhost:8080`, de modo que en
desarrollo se comparte origen igual que en produccion y no hace falta CORS. El
gateway tiene que estar levantado:

```bash
docker compose up -d api-gateway
```

## Pruebas

```bash
npm test
```

## Configuracion

La aplicacion lee `window.__APP_CONFIG__`, definido en `config.js`. En desarrollo
ese archivo es `public/config.js`; en el contenedor lo genera el entrypoint de
nginx a partir de las variables de entorno, asi que la misma imagen sirve para
cualquier entorno sin recompilar.

Todo lo que hay ahi llega al navegador y es publico: nunca un secreto.
