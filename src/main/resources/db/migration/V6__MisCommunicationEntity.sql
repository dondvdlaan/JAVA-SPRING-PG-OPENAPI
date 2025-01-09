CREATE TABLE IF NOT EXISTS mis_communication
(
 mis_comm_id UUID NOT NULL PRIMARY KEY,
 request_uri VARCHAR(256),
 message_body VARCHAR(256),
 headers_as_json VARCHAR(256),
 http_method VARCHAR(256),
 retries integer
);
