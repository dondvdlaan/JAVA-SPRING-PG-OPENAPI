package dev.manyroads.model.entity;

import dev.manyroads.model.ChargeStatus;
import dev.manyroads.model.VehicleTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "charges")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Charge {

    @Id
    @Builder.Default
    @Column(name="charge_id")
    private UUID chargeID = UUID.randomUUID();
    @Column(name="charge_status")
    @Enumerated(EnumType.STRING)
    private ChargeStatus chargeStatus;
    @Column(name = "customer_nr")
    private Long customerNr;
    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private VehicleTypeEnum vehicleType;
    @Column(name = "start_date")
    private Instant startDate;
    @ManyToOne()
    @JoinColumn(name="customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "charge")
    private Set<Matter> matters = new HashSet<>();

}
