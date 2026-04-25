# auth-app

Serviço centralizado de autenticação e gerenciamento de usuários do ecossistema SPRJ.

## Componentes

| Componente | Tecnologia | Porta (DEV) |
|---|---|---|
| `auth-keycloak` | Keycloak | 8080 |
| `auth-backend` | Quarkus | 8081 |
| `auth-frontend` | Angular | 4200 |
| SQL Server | mssql/server:2022 | 1433 |

## Ambiente de desenvolvimento

```bash
docker compose up -d
```

> O container `auth-sqlserver` cria automaticamente o banco `sprj_auth` na primeira inicialização.
> O `auth-backend` e o `auth-frontend` devem ser iniciados separadamente:
> ```bash
> # Backend
> cd auth-backend && ./mvnw quarkus:dev
>
> # Frontend
> cd auth-frontend && ng serve
> ```

## Produção (Azure)

| Recurso | Nome | URL |
|---|---|---|
| Keycloak | `sprj-ca-auth-keycloak` | `id.seudominio.com` |
| Backend | `sprj-ca-auth-backend` | `api-auth.seudominio.com` |
| Frontend | `sprj-swa-auth-frontend` | `auth.seudominio.com` |
| Banco de dados | Azure SQL Database | free tier permanente |
