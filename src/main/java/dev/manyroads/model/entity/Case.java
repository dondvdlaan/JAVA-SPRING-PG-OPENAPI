package dev.manyroads.model.entity;

import dev.manyroads.model.VehicleTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cases")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Case {

    @Id
    @Builder.Default
    @Column(name="case_id")
    private UUID caseID = UUID.randomUUID();
    private String chargeStatus;
    @Column(name = "customer_nr")
    private Long customerNr;
    @ManyToOne()
    @JoinColumn(name="case_id")
    private Charge charge;
}
