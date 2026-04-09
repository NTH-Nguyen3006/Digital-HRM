#!/usr/bin/env bash

set -euo pipefail

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
HOST="${MSSQL_HOST:-db}"
PORT="${MSSQL_PORT:-1433}"
PASSWORD="${MSSQL_SA_PASSWORD:?MSSQL_SA_PASSWORD is required}"

echo "Waiting for SQL Server at ${HOST}:${PORT}..."
for attempt in $(seq 1 60); do
  if "${SQLCMD}" -S "${HOST},${PORT}" -U sa -P "${PASSWORD}" -C -Q "SELECT 1" >/dev/null 2>&1; then
    echo "SQL Server is ready."
    break
  fi

  if [ "${attempt}" -eq 60 ]; then
    echo "SQL Server did not become ready in time." >&2
    exit 1
  fi

  sleep 2
done

echo "Ensuring database ${MSSQL_DATABASE:-DigitalHRM} exists..."
"${SQLCMD}" -S "${HOST},${PORT}" -U sa -P "${PASSWORD}" -C -i /scripts/create_database.sql

echo "Database bootstrap completed."
