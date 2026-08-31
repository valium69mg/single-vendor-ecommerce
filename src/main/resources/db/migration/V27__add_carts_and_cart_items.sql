CREATE TABLE IF NOT EXISTS carts (
    cart_id    BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id       BIGSERIAL PRIMARY KEY,
    cart_id            BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity           INTEGER NOT NULL CHECK (quantity > 0),
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants(product_variant_id),
    CONSTRAINT uq_cartitem_cart_variant UNIQUE (cart_id, product_variant_id)
);
