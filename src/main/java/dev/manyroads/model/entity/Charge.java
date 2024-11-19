package dev.manyroads.model.entity;

import dev.manyroads.model.VehicleTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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
    @Column(name="charge_status")
    private String chargeStatus;
    @Column(name = "customer_nr")
    private Long customerNr;
    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private VehicleTypeEnum vehicleType;
    @ManyToOne()
    @JoinColumn(name="customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "case")
    private Set<Case> cases = new HashSet<>();

}
