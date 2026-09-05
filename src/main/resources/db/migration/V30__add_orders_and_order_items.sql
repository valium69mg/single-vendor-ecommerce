-- V30__add_orders_and_order_items.sql
-- Greenfield checkout/order-creation schema (order-checkout). Orders snapshot
-- the shipping address and line-item data at purchase time, independent of
-- later product/variant edits.
CREATE TABLE orders (
    order_id             BIGSERIAL PRIMARY KEY,
    user_id              UUID NOT NULL,
    -- Nullable at the DB level only for the instant between the two-step save
    -- (insert to obtain order_id, then UPDATE order_number = ORD-<date>-<id>
    -- in the same transaction); OrderService never returns a row with it null.
    order_number         VARCHAR(50) UNIQUE,
    status               VARCHAR(20) NOT NULL,
    shipping_recipient   VARCHAR(200) NOT NULL,
    shipping_line1       VARCHAR(255) NOT NULL,
    shipping_line2       VARCHAR(255),
    shipping_city        VARCHAR(100) NOT NULL,
    shipping_state       VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country     VARCHAR(100) NOT NULL,
    shipping_phone       VARCHAR(30) NOT NULL,
    subtotal             NUMERIC(10,2) NOT NULL,
    shipping_cost        NUMERIC(10,2) NOT NULL,
    total                NUMERIC(10,2) NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Supports "list my orders, most recent first".
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);

CREATE TABLE order_items (
    order_item_id      BIGSERIAL PRIMARY KEY,
    order_id           BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    product_name       VARCHAR(200) NOT NULL,
    variant_label      VARCHAR(255),
    sku                VARCHAR(100) NOT NULL,
    unit_price         NUMERIC(10,2) NOT NULL,
    quantity           INTEGER NOT NULL CHECK (quantity > 0),
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants(product_variant_id)
);
