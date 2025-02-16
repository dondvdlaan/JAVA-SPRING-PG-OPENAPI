package dev.manyroads.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Customer {
    @Id
    @Builder.Default
    @Column(name = "customer_id")
    private UUID customerID = UUID.randomUUID();
    @Column(name = "customer_nr")
    private Long customerNr;
    @OneToMany(
            //cascade = CascadeType.PERSIST,
            //fetch = FetchType.LAZY,
            mappedBy = "customer")
    @Builder.Default
    private List<Charge> charges = new ArrayList<>();
    @Column(name = "stand_by_flag")
    private boolean standByFlag;

}
