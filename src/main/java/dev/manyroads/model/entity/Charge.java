package dev.manyroads.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "charges")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Charge {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private long chargeId;
    private String chargeStatus;
    @ManyToOne()
    @JoinColumn(name = "customerID")
    private Customer customer;

}
