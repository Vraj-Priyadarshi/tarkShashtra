#!/bin/bash
# ──────────────────────────────────────────────────────────────────────
# Truncate_DB.sh — Wipes ALL data from tarkshastra_db
# Usage:  bash Truncate_DB.sh
# After running, restart the backend so DataSeeder re-seeds institutes.
# ──────────────────────────────────────────────────────────────────────

DB_USER="root"
DB_PASS="Khushi79#"
DB_NAME="tarkshastra_db"

echo "⚠️  This will DELETE ALL DATA from '${DB_NAME}'!"
read -p "Are you sure? (yes/no): " confirm

if [[ "$confirm" != "yes" ]]; then
    echo "Aborted."
    exit 0
fi

# Get all table names as a comma-separated list
TABLES=$(mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -B -e \
    "SELECT GROUP_CONCAT(table_name) FROM information_schema.tables WHERE table_schema='${DB_NAME}';")

if [[ -z "$TABLES" || "$TABLES" == "NULL" ]]; then
    echo "No tables found in ${DB_NAME}."
    exit 0
fi

# Build a single SQL statement: disable FK checks → truncate each table → re-enable
SQL="SET FOREIGN_KEY_CHECKS=0;"
IFS=',' read -ra TABLE_ARRAY <<< "$TABLES"
for TABLE in "${TABLE_ARRAY[@]}"; do
    SQL+=" TRUNCATE TABLE \`${TABLE}\`;"
done
SQL+=" SET FOREIGN_KEY_CHECKS=1;"

mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "$SQL"

if [[ $? -eq 0 ]]; then
    echo "✅ All ${#TABLE_ARRAY[@]} tables truncated successfully."
    echo "   Restart the backend to re-seed institutes: java -jar Backend/hackathon-app/target/hackathon-app-0.0.1-SNAPSHOT.jar"
else
    echo "❌ Truncation failed. Check MySQL credentials and database name."
    exit 1
fi
