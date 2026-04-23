# auth-app

Serviço centralizado de autenticação e gerenciamento de usuários do ecossistema SPRJ.

## Componentes

| Componente | Tecnologia | Porta (DEV) |
|---|---|---|
| `auth-keycloak` | Keycloak | 8080 |
| `auth-backend` | Quarkus | 8081 |
| `auth-frontend` | Angular | 4200 |
| PostgreSQL | postgres:16 | 5432 |

## Ambiente de desenvolvimento

```bash
docker compose up -d
```

## Produção (Azure)

| Recurso | Nome | URL |
|---|---|---|
| Keycloak | `sprj-ca-auth-keycloak` | `id.seudominio.com` |
| Backend | `sprj-ca-auth-backend` | `api-auth.seudominio.com` |
| Frontend | `sprj-swa-auth-frontend` | `auth.seudominio.com` |
