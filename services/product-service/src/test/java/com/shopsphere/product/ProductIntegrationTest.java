
package com.shopsphere.product;

import com.shopsphere.product.domain.entity.OutboxEvent;
import com.shopsphere.product.domain.repository.OutboxEventRepository;
import com.shopsphere.product.dto.request.CreateProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "shopsphere.outbox.publisher.enabled=false"
)
@AutoConfigureTestRestTemplate
class ProductIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("product_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    private static final String PRODUCTS_URL = "/api/v1/products";

    @Test
    void shouldCreateAndRetrieveProduct() {

        CreateProductRequest request = new CreateProductRequest(
                "TEST-PHONE-001",
                "Integration Test Phone",
                "Phone created during integration testing",
                "Electronics",
                new java.math.BigDecimal("49999.00"),
                "INR"
        );

        ResponseEntity<ProductResponse> createResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        ProductResponse.class
                );

        assertEquals(201, createResponse.getStatusCode().value());
        assertNotNull(createResponse.getBody());

        ProductResponse createdProduct = createResponse.getBody();

        assertNotNull(createdProduct.id());
        assertEquals("TEST-PHONE-001", createdProduct.sku());
        assertEquals("Integration Test Phone", createdProduct.name());

        ResponseEntity<ProductResponse> getResponse =
                restTemplate.getForEntity(
                        PRODUCTS_URL + "/" + createdProduct.id(),
                        ProductResponse.class
                );

        assertEquals(200, getResponse.getStatusCode().value());
        assertNotNull(getResponse.getBody());
        assertEquals(createdProduct.id(), getResponse.getBody().id());
    }

    @Test
    void shouldRejectDuplicateSku() {

        CreateProductRequest request = new CreateProductRequest(
                "DUPLICATE-001",
                "First Product",
                "First product description",
                "Electronics",
                new java.math.BigDecimal("1000.00"),
                "INR"
        );

        ResponseEntity<ProductResponse> firstResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        ProductResponse.class
                );

        assertEquals(201, firstResponse.getStatusCode().value());

        ResponseEntity<String> duplicateResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        String.class
                );

        assertEquals(409, duplicateResponse.getStatusCode().value());
    }

    @Test
    void shouldRejectInvalidProductRequest() {

        CreateProductRequest request = new CreateProductRequest(
                "",
                "",
                "Invalid product",
                "",
                new java.math.BigDecimal("-100.00"),
                "INVALID"
        );

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        String.class
                );

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldSoftDeleteProduct() {

        CreateProductRequest request = new CreateProductRequest(
                "DELETE-TEST-001",
                "Product to Delete",
                "Testing soft deletion",
                "Electronics",
                new java.math.BigDecimal("2500.00"),
                "INR"
        );

        ResponseEntity<ProductResponse> createResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        ProductResponse.class
                );

        assertEquals(201, createResponse.getStatusCode().value());
        assertNotNull(createResponse.getBody());

        var productId = createResponse.getBody().id();

        restTemplate.delete(PRODUCTS_URL + "/" + productId);

        ResponseEntity<String> getResponse =
                restTemplate.getForEntity(
                        PRODUCTS_URL + "/" + productId,
                        String.class
                );

        assertEquals(404, getResponse.getStatusCode().value());
    }


    @Test
    void shouldFilterProductsByCategoryAndPaginateResults() {

        CreateProductRequest phone1 = new CreateProductRequest(
                "FILTER-PHONE-001",
                "Samsung Galaxy",
                "Samsung smartphone",
                "Integration-Test-Electronics",
                new java.math.BigDecimal("50000.00"),
                "INR"
        );

        CreateProductRequest phone2 = new CreateProductRequest(
                "FILTER-PHONE-002",
                "iPhone",
                "Apple smartphone",
                "Integration-Test-Electronics",
                new java.math.BigDecimal("80000.00"),
                "INR"
        );

        CreateProductRequest laptop = new CreateProductRequest(
                "FILTER-LAPTOP-001",
                "MacBook",
                "Apple laptop",
                "Integration-Test-Computers",
                new java.math.BigDecimal("120000.00"),
                "INR"
        );

        restTemplate.postForEntity(PRODUCTS_URL, phone1, String.class);
        restTemplate.postForEntity(PRODUCTS_URL, phone2, String.class);
        restTemplate.postForEntity(PRODUCTS_URL, laptop, String.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                PRODUCTS_URL + "?category=Integration-Test-Electronics&page=0&size=1",
                String.class
        );

        assertEquals(200, response.getStatusCode().value());

        String body = response.getBody();

        assertNotNull(body);
        assertTrue(body.contains("FILTER-PHONE-001")
                || body.contains("FILTER-PHONE-002"));

        assertFalse(body.contains("FILTER-LAPTOP-001"));
        assertTrue(body.contains("\"totalElements\":2"));
        assertTrue(body.contains("\"totalPages\":2"));
    }


    @Test
    void shouldRejectDuplicateSkuRegardlessOfCase() {

        CreateProductRequest first = new CreateProductRequest(
                "CASE-TEST-001",
                "First Product",
                "First product",
                "Electronics",
                new java.math.BigDecimal("1000.00"),
                "INR"
        );

        CreateProductRequest second = new CreateProductRequest(
                "case-test-001",
                "Second Product",
                "Second product",
                "Electronics",
                new java.math.BigDecimal("2000.00"),
                "INR"
        );

        ResponseEntity<String> firstResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL, first, String.class);

        ResponseEntity<String> secondResponse =
                restTemplate.postForEntity(
                        PRODUCTS_URL, second, String.class);

        assertEquals(201, firstResponse.getStatusCode().value());
        assertEquals(409, secondResponse.getStatusCode().value());
    }


    @Test
    void shouldPersistProductAndOutboxEventTogether() {

        CreateProductRequest request = new CreateProductRequest(
                "OUTBOX-TEST-001",
                "Outbox Test Product",
                "Testing transactional outbox",
                "Electronics",
                new java.math.BigDecimal("15000.00"),
                "INR"
        );

        ResponseEntity<ProductResponse> response =
                restTemplate.postForEntity(
                        PRODUCTS_URL,
                        request,
                        ProductResponse.class
                );

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        ProductResponse product = response.getBody();

        List<OutboxEvent> events =
                outboxEventRepository.findByAggregateIdAndStatus(
                        product.id().toString(),
                        "PENDING"
                );

        assertEquals(1, events.size());

        OutboxEvent event = events.get(0);

        assertEquals("PRODUCT", event.getAggregateType());
        assertEquals("PRODUCT_CREATED", event.getEventType());
        assertEquals("shopsphere.product.events", event.getTopic());
        assertNotNull(event.getPayload());
    }



}
