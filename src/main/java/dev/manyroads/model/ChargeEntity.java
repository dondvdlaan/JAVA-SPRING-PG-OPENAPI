package dev.manyroads.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name="charges")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChargeEntity {

    @Id
    private long chargeId;
    private String chargeStatus;

}
