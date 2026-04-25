#!/bin/sh
set -e

KEYCLOAK_URL="http://keycloak:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin"
REALM="sprj"

echo "[init] Aguardando Keycloak..."
until curl -sf "${KEYCLOAK_URL}/realms/master" > /dev/null 2>&1; do
  sleep 3
done
echo "[init] Keycloak respondendo."

echo "[init] Obtendo token de admin..."
RESPONSE=$(curl -sf -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli&grant_type=password&username=${ADMIN_USER}&password=${ADMIN_PASS}")

TOKEN=$(echo "$RESPONSE" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//;s/"//')

if [ -z "$TOKEN" ]; then
  echo "[init] ERRO: nao foi possivel obter token de admin."
  exit 1
fi

echo "[init] Configurando SMTP (Mailpit)..."
curl -sf -X PUT "${KEYCLOAK_URL}/admin/realms/${REALM}" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "smtpServer": {
      "host": "mailpit",
      "port": "1025",
      "from": "no-reply@sprj.local",
      "fromDisplayName": "SPRJ Auth",
      "ssl": "false",
      "starttls": "false"
    }
  }'
echo "[init] SMTP configurado."

echo "[init] Criando usuario de teste..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "${KEYCLOAK_URL}/admin/realms/${REALM}/users" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teste",
    "email": "teste@sprj.com",
    "firstName": "Teste",
    "lastName": "Usuario",
    "enabled": true,
    "emailVerified": true,
    "credentials": [
      {
        "type": "password",
        "value": "teste123",
        "temporary": false
      }
    ]
  }')

if [ "$STATUS" = "201" ]; then
  echo "[init] Usuario 'teste' criado com sucesso."
elif [ "$STATUS" = "409" ]; then
  echo "[init] Usuario 'teste' ja existe, ignorando."
else
  echo "[init] Status inesperado ao criar usuario: $STATUS"
fi

echo "[init] Concluido."
