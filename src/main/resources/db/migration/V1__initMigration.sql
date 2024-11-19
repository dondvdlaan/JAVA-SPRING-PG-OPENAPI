DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS charges;

CREATE TABLE IF NOT EXISTS customers
(
customer_id    uuid NOT NULL,
customer_nr    integer NOT NULL UNIQUE,

PRIMARY KEY (customer_id )
);

CREATE TABLE IF NOT EXISTS charges
(
charge_id      uuid NOT NULL,
customer_id    uuid NOT NULL,
charge_status  character varying(255) COLLATE pg_catalog."default" NOT NULL,
customer_nr    integer NOT NULL UNIQUE,
vehicle_type   character varying(255) COLLATE pg_catalog."default"

PRIMARY KEY (charge_id)
);

ALTER TABLE charges ADD CONSTRAINT fk_customer_id FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
