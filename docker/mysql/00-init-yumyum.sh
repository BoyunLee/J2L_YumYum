#!/usr/bin/env bash
set -e

run_sql_file() {
    local file_path="$1"

    if command -v docker_process_sql >/dev/null 2>&1; then
        docker_process_sql < "$file_path"
        return
    fi

    mysql \
        --default-character-set=utf8mb4 \
        --protocol=socket \
        -uroot \
        "-p${MYSQL_ROOT_PASSWORD}" \
        --database="${MYSQL_DATABASE}" \
        < "$file_path"
}

run_sql() {
    if command -v docker_process_sql >/dev/null 2>&1; then
        docker_process_sql
        return
    fi

    mysql \
        --default-character-set=utf8mb4 \
        --protocol=socket \
        -uroot \
        "-p${MYSQL_ROOT_PASSWORD}" \
        --database="${MYSQL_DATABASE}"
}

echo "[yumyum-init] Running schema script: yumyum.sql"
run_sql_file /docker-entrypoint-sql/01-yumyum.sql

echo "[yumyum-init] Loading food nutrition dump: SSAFY_COACH_Dump.sql"
run_sql_file /docker-entrypoint-sql/02-food-nutrition-data.sql

echo "[yumyum-init] food_nutrition row count after seed"
run_sql <<'SQL'
SELECT COUNT(*) AS food_nutrition_count FROM food_nutrition;
SQL
