CREATE TABLE IF NOT EXISTS customers
(
customer_id    uuid NOT NULL,
customer_nr    bigint NOT NULL UNIQUE,

PRIMARY KEY (customer_id )
);

CREATE TABLE IF NOT EXISTS charges
(
charge_id       uuid NOT NULL,
customer_id     uuid,
charge_status   character varying(255) COLLATE pg_catalog."default" NOT NULL,
customer_nr     bigint NOT NULL UNIQUE,
vehicle_type    character varying(255) COLLATE pg_catalog."default",
start_date      timestamp without time zone,

PRIMARY KEY (charge_id)
);

CREATE TABLE IF NOT EXISTS matters
(
-- matterId is to make each row unique, whereas matterNr is not unique
matter_id       uuid NOT NULL UNIQUE,
matter_nr       character varying(255) COLLATE pg_catalog."default" NOT NULL,
charge_id       uuid,
matter_status   character varying(255) COLLATE pg_catalog."default" NOT NULL,

PRIMARY KEY (matter_id )
);

ALTER TABLE charges ADD CONSTRAINT fk_customer_id FOREIGN KEY (customer_id) REFERENCES customers(customer_id);
--ALTER TABLE matters ADD CONSTRAINT fk_charge_id FOREIGN KEY (charge_id) REFERENCES charges(charge_id);
