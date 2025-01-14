CREATE TABLE IF NOT EXISTS mis_communication
(
 mis_comm_id        uuid NOT NULL,
 request_uri        character varying(255) COLLATE pg_catalog."default",
 message_body       bytea,
 headers_as_json    character varying(255) COLLATE pg_catalog."default",
 http_method        character varying(255) COLLATE pg_catalog."default",
 retries            integer,

 PRIMARY KEY (mis_comm_id)
);
