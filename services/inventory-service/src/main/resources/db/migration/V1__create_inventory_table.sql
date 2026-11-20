CREATE TABLE inventory
(
    id UUID PRIMARY KEY,

    product_id UUID NOT NULL UNIQUE,

    sku VARCHAR(100) NOT NULL UNIQUE,

    available_quantity INTEGER NOT NULL DEFAULT 0,

    reserved_quantity INTEGER NOT NULL DEFAULT 0,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_available_quantity
        CHECK (available_quantity >= 0),

    CONSTRAINT chk_reserved_quantity
        CHECK (reserved_quantity >= 0)
);

CREATE INDEX idx_inventory_product_id
    ON inventory(product_id);

CREATE INDEX idx_inventory_sku
    ON inventory(sku);