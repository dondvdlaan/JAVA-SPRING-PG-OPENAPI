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
public class Customer {
    @Id
    @Builder.Default
    @Column(name = "customer_id")
    private UUID customerID = UUID.randomUUID();
    @Column(name = "customer_nr")
    private Long customerNr;
    @OneToMany(mappedBy = "customer")
    private List<Charge> charge = new ArrayList<>();
    @Column(name = "stand_by_flag")
    private boolean standByFlag;

    @Override
    public String toString() {
        return "Customer{" +
                "customerID=" + customerID +
                ", customerNr=" + customerNr +
                ", charge=" + charge +
                ", standByFlag=" + standByFlag +
                '}';
    }
}
