#!/bin/bash
set -e

databases=(
  auth_db
  product_db
  order_db
  inventory_db
  payment_db
  fulfillment_db
  shipping_db
  notification_db
)

for db in "${databases[@]}"; do
  echo "Creating database: $db"

  psql \
    -v ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    --command "CREATE DATABASE $db OWNER $POSTGRES_USER;"
done

echo "All ShopSphere databases created successfully."