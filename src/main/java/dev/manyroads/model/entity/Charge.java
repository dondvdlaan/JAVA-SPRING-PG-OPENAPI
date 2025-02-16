package dev.manyroads.model.entity;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.messages.ChargeMessage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    @Column(name = "charge_id")
    private UUID chargeID = UUID.randomUUID();
    @Column(name = "charge_status")
    @Enumerated(EnumType.STRING)
    private ChargeStatusEnum chargeStatus;
    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private VehicleTypeEnum vehicleType;
    @Column(name = "start_date")
    private Instant startDate;
    @ManyToOne()
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToMany(
            //cascade = CascadeType.PERSIST,
           // fetch = FetchType.LAZY,
            mappedBy = "charge")
    private Set<Matter> matters = new HashSet<>();

    public ChargeMessage getChargeMessage() {
        return new ChargeMessage(chargeID, chargeStatus);
    }

}
