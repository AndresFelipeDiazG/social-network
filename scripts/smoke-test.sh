#!/usr/bin/env bash
# Prueba de humo de extremo a extremo contra el gateway: autenticacion, creacion
# y listado de publicaciones. Documenta el contrato de la API de forma
# ejecutable y sirve de guion para la demostracion.
#
#   ./scripts/smoke-test.sh
#   BASE_URL=http://localhost:8081 ./scripts/smoke-test.sh
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-acorrea}"
PASSWORD="${PASSWORD:-Password123!}"

fail() { printf '\n[FALLO] %s\n' "$1" >&2; exit 1; }
ok()   { printf '[  OK  ] %s\n' "$1"; }

# Separa el cuerpo del codigo de estado que curl escribe en la ultima linea.
status_of() { printf '%s' "$1" | tail -n1; }
body_of()   { printf '%s' "$1" | sed '$d'; }

printf 'Gateway: %s\n\n' "$BASE_URL"

# --- 1. Rechazo sin credenciales -------------------------------------------
response=$(curl -sS -o /dev/null -w '%{http_code}' "$BASE_URL/api/posts" || true)
[ "$response" = "401" ] || fail "GET /api/posts sin token devolvio $response, se esperaba 401"
ok "sin token se rechaza con 401"

# --- 2. Login (POST, el que usa el frontend) -------------------------------
response=$(curl -sS -w '\n%{http_code}' -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")
status=$(status_of "$response"); body=$(body_of "$response")
[ "$status" = "200" ] || fail "POST /api/auth/login devolvio $status: $body"

token=$(printf '%s' "$body" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -n "$token" ] || fail "no se pudo extraer accessToken de: $body"
ok "login POST como ${USERNAME}"

# --- 3. Login (GET con Basic, el requisito literal del enunciado) ----------
response=$(curl -sS -o /dev/null -w '%{http_code}' -u "${USERNAME}:${PASSWORD}" \
  "$BASE_URL/api/auth/login")
[ "$response" = "200" ] || fail "GET /api/auth/login devolvio $response"
ok "login GET con Authorization: Basic"

# --- 4. Credenciales incorrectas ------------------------------------------
response=$(curl -sS -o /dev/null -w '%{http_code}' -X POST "$BASE_URL/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"incorrecta\"}")
[ "$response" = "401" ] || fail "contrasena incorrecta devolvio $response, se esperaba 401"
ok "contrasena incorrecta se rechaza con 401"

# --- 5. Crear publicacion --------------------------------------------------
mensaje="Publicacion de prueba de humo $(date +%H:%M:%S)"
response=$(curl -sS -w '\n%{http_code}' -X POST "$BASE_URL/api/posts" \
  -H "Authorization: Bearer ${token}" \
  -H 'Content-Type: application/json' \
  -d "{\"message\":\"${mensaje}\"}")
status=$(status_of "$response"); body=$(body_of "$response")
[ "$status" = "201" ] || fail "POST /api/posts devolvio $status: $body"
ok "publicacion creada"

# --- 6. Validacion: mensaje vacio -----------------------------------------
response=$(curl -sS -o /dev/null -w '%{http_code}' -X POST "$BASE_URL/api/posts" \
  -H "Authorization: Bearer ${token}" \
  -H 'Content-Type: application/json' \
  -d '{"message":"   "}')
[ "$response" = "400" ] || fail "mensaje vacio devolvio $response, se esperaba 400"
ok "mensaje vacio se rechaza con 400"

# --- 7. Listados por scope -------------------------------------------------
for scope in ALL OTHERS MINE; do
  response=$(curl -sS -w '\n%{http_code}' -H "Authorization: Bearer ${token}" \
    "$BASE_URL/api/posts?scope=${scope}")
  status=$(status_of "$response"); body=$(body_of "$response")
  [ "$status" = "200" ] || fail "GET /api/posts?scope=${scope} devolvio $status: $body"
  total=$(printf '%s' "$body" | sed -n 's/.*"totalElements"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p')
  ok "listado scope=${scope} (${total:-?} publicaciones)"
done

# --- 8. Token manipulado ---------------------------------------------------
response=$(curl -sS -o /dev/null -w '%{http_code}' \
  -H "Authorization: Bearer ${token}manipulado" "$BASE_URL/api/posts")
[ "$response" = "401" ] || fail "token manipulado devolvio $response, se esperaba 401"
ok "token manipulado se rechaza con 401"

printf '\nTodas las comprobaciones pasaron.\n'
