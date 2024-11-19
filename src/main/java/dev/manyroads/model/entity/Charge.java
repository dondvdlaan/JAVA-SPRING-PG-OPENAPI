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
    @Column(name="charge_id")
    private UUID chargeID = UUID.randomUUID();
    private String chargeStatus;
    @Column(name = "customer_nr")
    private Long customerNr;
    @ManyToOne()
    @JoinColumn(name="customer_id")
    private Customer customer;

}
