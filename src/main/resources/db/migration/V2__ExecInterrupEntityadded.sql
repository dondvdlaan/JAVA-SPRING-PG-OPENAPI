CREATE TABLE IF NOT EXISTS exec_interrups
(
exec_interrup_id        uuid NOT NULL,
customer_nr             bigint NOT NULL UNIQUE,
matter_id               character varying(255) COLLATE pg_catalog."default",
exec_interrup_status    character varying(255) COLLATE pg_catalog."default" NOT NULL,

PRIMARY KEY (exec_interrup_id )
);