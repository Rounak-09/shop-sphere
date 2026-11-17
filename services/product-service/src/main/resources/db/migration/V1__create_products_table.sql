CREATE TABLE products
(
    id UUID PRIMARY KEY,

    sku VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(2000),

    category VARCHAR(100) NOT NULL,

    price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    active BOOLEAN NOT NULL DEFAULT TRUE,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_products_sku UNIQUE (sku),

    CONSTRAINT chk_products_price
        CHECK (price > 0),

    CONSTRAINT chk_products_currency
        CHECK (currency ~ '^[A-Z]{3}$')
    );

CREATE INDEX idx_products_category
    ON products(category);

CREATE INDEX idx_products_active
    ON products(active);

CREATE INDEX idx_products_created_at
    ON products(created_at DESC);