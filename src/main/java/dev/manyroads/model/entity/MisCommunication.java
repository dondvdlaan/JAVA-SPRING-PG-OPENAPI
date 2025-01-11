package dev.manyroads.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "mis_communication")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MisCommunication {
    @Id
    @Column(name = "mis_comm_id")
    @Builder.Default
    private UUID misCommID = UUID.randomUUID();
    @Column(name = "request_uri")
    private String requestURI;
    @Column(name = "message_body")
    private String messageBody;
    @Column(name = "headers_as_json")
    private String headersAsJson;
    @Column(name = "http_method")
    private String httpMethod;
    @Column(name = "retries")
    private int retries;
}
