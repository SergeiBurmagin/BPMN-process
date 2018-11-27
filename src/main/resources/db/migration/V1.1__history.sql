CREATE TABLE IF NOT EXISTS product_order_history (LIKE product_order
);
CREATE TABLE IF NOT EXISTS product_order_item_history (LIKE product_order_item
);
CREATE TABLE IF NOT EXISTS shipping_history (LIKE shipping
);

CREATE OR REPLACE FUNCTION copy_to_product_order_history()
  RETURNS TRIGGER AS $product_order_audit$
BEGIN
  INSERT INTO product_order_history SELECT NEW.*;
  RETURN NEW;
END;
$product_order_audit$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS product_order_audit
ON product_order;
CREATE TRIGGER product_order_audit
  AFTER INSERT OR UPDATE
  ON product_order
  FOR EACH ROW
EXECUTE PROCEDURE copy_to_product_order_history();

CREATE OR REPLACE FUNCTION copy_to_product_order_item_history()
  RETURNS TRIGGER AS $product_order_item_audit$
BEGIN
  INSERT INTO product_order_item_history SELECT NEW.*;
  RETURN NEW;
END;
$product_order_item_audit$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS product_order_item_audit
ON product_order_item;
CREATE TRIGGER product_order_item_audit
  AFTER INSERT OR UPDATE
  ON product_order_item
  FOR EACH ROW
EXECUTE PROCEDURE copy_to_product_order_item_history();

CREATE OR REPLACE FUNCTION copy_to_shipping_history()
  RETURNS TRIGGER AS $shipping_audit$
BEGIN
  INSERT INTO shipping_history SELECT NEW.*;
  RETURN NEW;
END;
$shipping_audit$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS shipping_audit
ON shipping;
CREATE TRIGGER shipping_audit
  AFTER INSERT OR UPDATE
  ON shipping
  FOR EACH ROW
EXECUTE PROCEDURE copy_to_shipping_history();