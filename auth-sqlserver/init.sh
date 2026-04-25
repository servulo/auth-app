#!/bin/bash
set -e

# Inicia o SQL Server em background
/opt/mssql/bin/sqlservr &
MSSQL_PID=$!

echo "[sqlserver] Aguardando SQL Server iniciar..."
for i in $(seq 1 60); do
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" -No 2>/dev/null && break
    sleep 2
done

echo "[sqlserver] Criando banco de dados sprj_auth (se não existir)..."
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -No -Q \
    "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'sprj_auth') CREATE DATABASE sprj_auth;"

echo "[sqlserver] Banco de dados pronto."
wait $MSSQL_PID
