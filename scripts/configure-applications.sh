
#!/bin/bash
set -e

write_db_config() {
    service="$1"
    port="$2"
    database="$3"
    kafka="$4"

    file="services/$service/src/main/resources/application.yml"

    cat > "$file" <<EOF
spring:
  application:
    name: $service

  datasource:
    url: \${DB_URL:jdbc:postgresql://localhost:5432/$database}
    username: \${DB_USERNAME:shopsphere}
    password: \${DB_PASSWORD:shopsphere_local_password}

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
EOF

    if [ "$kafka" = "true" ]; then
        cat >> "$file" <<EOF

  kafka:
    bootstrap-servers: \${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: $service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
EOF
    fi

    cat >> "$file" <<EOF

server:
  port: \${SERVER_PORT:$port}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
EOF

    echo "Configured $service"
}

write_db_config auth-service 8081 auth_db false
write_db_config product-service 8082 product_db false

write_db_config order-service 8085 order_db true
write_db_config inventory-service 8086 inventory_db true
write_db_config payment-service 8087 payment_db true
write_db_config fulfillment-service 8088 fulfillment_db true
write_db_config shipping-service 8089 shipping_db true

echo "Database service configuration completed."
