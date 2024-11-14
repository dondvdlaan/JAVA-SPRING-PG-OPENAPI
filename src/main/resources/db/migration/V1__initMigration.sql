CREATE TABLE IF NOT EXISTS charges
(
id             uuid NOT NULL,
charge_id      bigint NOT NULL,
charge_status  character varying(255) COLLATE pg_catalog."default" NOT NULL,

CONSTRAINT  id_pkey PRIMARY KEY (id)
);