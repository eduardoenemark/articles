
--DROP DATABASE IF EXISTS postgres;
--CREATE DATABASE postgres;
--
--DROP SCHEMA postgres CASCADE;
--CREATE SCHEMA postgres;
--GRANT ALL ON SCHEMA postgres TO postgres;

CREATE SEQUENCE sq_product INCREMENT BY 1 START WITH 1 MINVALUE 1 NO MAXVALUE CACHE 1;

CREATE TABLE tb_product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC(10, 2),
    producer VARCHAR(255),
    universal_product_code VARCHAR(255),
    country VARCHAR(255),
    entry_date DATE,
    amount INTEGER
);

