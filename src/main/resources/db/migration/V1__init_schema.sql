-- Create products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    currency VARCHAR(3) NOT NULL,
    amount INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    in_stock BOOLEAN NOT NULL DEFAULT false,
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create product_tags table
CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_in_stock ON products(in_stock);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_product_tags_product_id ON product_tags(product_id);

-- Insert sample data
INSERT INTO products (sku, name, description, currency, amount, quantity, in_stock, category, tags)
VALUES
    ('VT-SHIRT-001', 'Classic White T-Shirt', 'Premium cotton t-shirt with comfortable fit', 'USD', 2999, 100, true, 'clothing', NULL),
    ('VT-JEANS-001', 'Slim Fit Denim Jeans', 'Modern slim fit jeans with stretch fabric', 'USD', 7999, 50, true, 'clothing', NULL),
    ('VT-SHOE-001', 'Running Sneakers', 'Lightweight running shoes with cushioned sole', 'USD', 8999, 30, true, 'footwear', NULL),
    ('VT-BAG-001', 'Leather Messenger Bag', 'Genuine leather messenger bag with multiple compartments', 'USD', 12999, 20, true, 'accessories', NULL),
    ('VT-WATCH-001', 'Minimalist Watch', 'Elegant minimalist watch with leather strap', 'USD', 15999, 15, true, 'accessories', NULL);

-- Insert tags for sample products
INSERT INTO product_tags (product_id, tag) VALUES
    (1, 'shirt'),
    (1, 'casual'),
    (1, 'cotton'),
    (2, 'jeans'),
    (2, 'denim'),
    (2, 'casual'),
    (3, 'shoes'),
    (3, 'running'),
    (3, 'sports'),
    (4, 'bag'),
    (4, 'leather'),
    (4, 'professional'),
    (5, 'watch'),
    (5, 'accessories'),
    (5, 'minimalist');

