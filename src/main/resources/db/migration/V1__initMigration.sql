CREATE TABLE IF NOT EXISTS charges
(
charge_id      bigint NOT NULL,
charge_status  character varying(255) COLLATE pg_catalog."default" NOT NULL,

CONSTRAINT  charges_pkey PRIMARY KEY (charge_id)
);