package dev.manyroads.model.entity;

import dev.manyroads.model.enums.MatterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "matters")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Matter {

    @Id
    @Builder.Default
    @Column(name="matter_id")
    private UUID matterID = UUID.randomUUID();
    @Column(name = "customer_nr")
    private Long customerNr;
    @Column(name = "matter_status")
    @Enumerated(EnumType.STRING)
    private MatterStatus matterStatus;
    @ManyToOne()
    @JoinColumn(name="charge_id")
    private Charge charge;
}
