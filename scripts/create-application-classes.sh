
#!/bin/bash
set -e

while read -r service package class; do

    directory="services/$service/src/main/java/$(echo "$package" | tr '.' '/')"

    mkdir -p "$directory"

    cat > "$directory/${class}Application.java" <<EOF
package $package;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${class}Application {

    public static void main(String[] args) {
        SpringApplication.run(
            ${class}Application.class,
            args
        );
    }
}
EOF

    echo "Created $service"

done <<EOF
api-gateway com.shopsphere.apigateway ApiGateway
auth-service com.shopsphere.auth Auth
product-service com.shopsphere.product Product
cart-service com.shopsphere.cart Cart
order-service com.shopsphere.order Order
inventory-service com.shopsphere.inventory Inventory
payment-service com.shopsphere.payment Payment
fulfillment-service com.shopsphere.fulfillment Fulfillment
shipping-service com.shopsphere.shipping Shipping
notification-service com.shopsphere.notification Notification
EOF
