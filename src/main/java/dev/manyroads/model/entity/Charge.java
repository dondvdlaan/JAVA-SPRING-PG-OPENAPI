package dev.manyroads.model.entity;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.messages.ChargeMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
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
    @Enumerated(EnumType.STRING)
    private ChargeStatusEnum chargeStatus;
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

    public ChargeMessage getChargeMessage(){
        return new ChargeMessage(chargeID,chargeStatus);
    }

}
