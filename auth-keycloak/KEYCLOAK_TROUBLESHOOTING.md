# Keycloak — Problemas e Soluções

Registro dos problemas encontrados durante a configuração do Keycloak 24+ no projeto Auth App
e os procedimentos adotados para resolvê-los.

---

## 1. `unsupported_grant_type` ao tentar login via ROPC

### Sintoma
```json
{ "error": "unsupported_grant_type", "error_description": "..." }
```

### Causa
O Keycloak 24 introduziu controle de grant types a nível de **realm** (além do cliente).
Mesmo com `directAccessGrantsEnabled: true` no cliente, o Resource Owner Password Credentials
(ROPC) precisa estar habilitado nas configurações avançadas do realm.

### Solução
Adicionar o atributo de realm no `realm-export.json`:

```json
"attributes": {
  "oauth2.grant.password.enabled": "true"
}
```

Isso garante que a configuração persiste após qualquer `docker compose down -v`.

---

## 2. Usuário não persiste após reset do volume Docker

### Sintoma
Após `docker compose down -v && docker compose up -d`, o usuário criado manualmente
no Keycloak Admin Console desaparece. Login retorna:
```json
{ "error": "invalid_grant", "error_description": "Invalid user credentials" }
```
O log do Keycloak confirma: `error="user_not_found"`.

### Causa
O Keycloak armazena dados (realm, usuários, sessões) no PostgreSQL. O flag `-v` no
`docker compose down` apaga o volume — toda a configuração manual é perdida.

O `realm-export.json` importado via `--import-realm` repõe o realm, mas **não importa
usuários com senha em texto plano** no Keycloak 24+ (as credenciais são silenciosamente
ignoradas durante o import).

### Solução
Adicionar um container de inicialização (`keycloak-init`) no `docker-compose.yml` que,
após o Keycloak estar disponível, cria o usuário de desenvolvimento via Admin REST API:

```yaml
keycloak-init:
  image: curlimages/curl:latest
  container_name: auth-keycloak-init
  volumes:
    - ./auth-keycloak/init.sh:/init.sh
  entrypoint: ["sh", "/init.sh"]
  depends_on:
    - keycloak
  restart: "no"
```

O script `init.sh`:
1. Aguarda o Keycloak responder (`/realms/master`)
2. Obtém um token de admin via `admin-cli`
3. Cria o usuário via `POST /admin/realms/sprj/users` com credencial já no payload
4. Ignora conflito 409 (usuário já existe — idempotente)

---

## 3. Healthcheck do Keycloak falha — `curl` não disponível na imagem

### Sintoma
```
Container auth-keycloak  Error  dependency keycloak failed to start
```
O container nunca atinge o estado `healthy`.

### Causa
A imagem `quay.io/keycloak/keycloak:latest` é baseada em **Red Hat UBI Minimal**, que
não inclui `curl` por padrão. O healthcheck configurado como
`curl -sf http://localhost:8080/...` falha com `exec: curl: not found`.

### Solução
Remover o healthcheck do serviço Keycloak no `docker-compose.yml` e delegar a espera
ao próprio `init.sh`, que já contém um loop de polling:

```sh
until curl -sf "${KEYCLOAK_URL}/realms/master" > /dev/null 2>&1; do
  sleep 3
done
```

O `curlimages/curl` (imagem do container de init) **tem** curl disponível e faz a
espera a partir de fora do container do Keycloak.

---

## 4. JWT access token sem a claim `sub`

### Sintoma
O access token retornado pelo login não contém o campo `sub` (Keycloak User ID),
mesmo após adicionar `scope=openid` na requisição de token.

```json
{
  "preferred_username": "teste",
  "email": "teste@sprj.com"
  // sub ausente
}
```

### Causa
No Keycloak 24+, a claim `sub` no **access token** não é incluída automaticamente.
Ela passou a depender de um Protocol Mapper explícito configurado no cliente.
O scope `openid` inclui `sub` no **ID token**, mas não necessariamente no access token
quando o cliente não tem o mapper configurado.

### Solução
Duas alterações no `realm-export.json`:

**1. Adicionar `openid` aos `defaultClientScopes` do cliente:**
```json
"defaultClientScopes": [
  "openid",
  "web-origins",
  "profile",
  "roles",
  "email"
]
```

**2. Adicionar um Protocol Mapper que mapeia o `id` do usuário para a claim `sub`
no access token:**
```json
"protocolMappers": [
  {
    "name": "sub",
    "protocol": "openid-connect",
    "protocolMapper": "oidc-usermodel-property-mapper",
    "consentRequired": false,
    "config": {
      "userinfo.token.claim": "true",
      "user.attribute": "id",
      "id.token.claim": "true",
      "access.token.claim": "true",
      "claim.name": "sub",
      "jsonType.label": "String"
    }
  }
]
```

O mapper usa `user.attribute: "id"` que mapeia para `UserModel.getId()` — o UUID
interno do Keycloak, imutável e único por usuário.

---

## 5. Token sem `scope=openid` → `sub` ausente no backend Quarkus

### Sintoma
Mesmo com o mapper configurado, o `sub` não aparecia porque o backend não estava
solicitando `scope=openid` na chamada ao token endpoint.

### Causa
O `AuthService.java` montava o form body sem incluir o parâmetro `scope`:

```java
Map.of(
    "grant_type", "password",
    "client_id", clientId,
    "username", request.username(),
    "password", request.password()
    // scope ausente
)
```

### Solução
Adicionar `"scope", "openid profile email"` nos métodos `login` e `refresh`:

```java
Map.of(
    "grant_type", "password",
    "client_id", clientId,
    "username", request.username(),
    "password", request.password(),
    "scope", "openid profile email"
)
```

---

## 6. Conexões JDBC inválidas após reset do volume

### Sintoma
Após `docker compose down -v && docker compose up -d` sem reiniciar o Quarkus:
```
JDBCConnectionException: An I/O error occurred while sending to the backend
```
Em seguida:
```
SQLGrammarException: relation "user_profiles" does not exist
```

### Causa
O Quarkus (`quarkus dev`) mantém um pool de conexões ativas com o PostgreSQL.
Quando o volume é recriado, o container do PostgreSQL reinicia com um banco zerado,
mas o pool do Quarkus ainda aponta para conexões da instância anterior (já destruída).

Além disso, o Flyway só executa as migrations na **inicialização do processo** Quarkus
(`migrate-at-start=true`). Se o banco for recriado sem reiniciar o Quarkus, as
migrations não são reaplicadas.

### Solução
Sempre reiniciar o processo `quarkus dev` após um `docker compose down -v`:

```bash
# Terminal 1 — infraestrutura
docker compose down -v && docker compose up -d

# Terminal 2 — backend (após o compose subir)
# Ctrl+C no processo atual, depois:
cd auth-backend
quarkus dev
```

---

## Resumo das alterações permanentes

| Arquivo | Alteração |
|---|---|
| `auth-keycloak/realm-export.json` | `attributes.oauth2.grant.password.enabled = true` |
| `auth-keycloak/realm-export.json` | `openid` adicionado em `defaultClientScopes` |
| `auth-keycloak/realm-export.json` | Protocol Mapper `sub` adicionado ao cliente |
| `auth-keycloak/init.sh` | Script de criação do usuário de dev via Admin API |
| `docker-compose.yml` | Serviço `keycloak-init` adicionado |
| `docker-compose.yml` | Healthcheck removido do Keycloak (curl ausente na imagem) |
| `auth-backend/AuthService.java` | `scope=openid profile email` nos requests de token |

---

## Procedimento padrão para reset de ambiente

```bash
# 1. Para o Quarkus (Ctrl+C no terminal do quarkus dev)

# 2. Recria a infraestrutura do zero
docker compose down -v && docker compose up -d

# 3. Aguarda o keycloak-init concluir
docker compose logs -f keycloak-init

# 4. Sobe o backend (Flyway roda automaticamente)
cd auth-backend && quarkus dev
```
