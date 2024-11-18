package dev.manyroads.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="customer")
@Getter
@Setter
@Builder
public class Customer {
    @Id
    @Builder.Default
    private UUID id= UUID.randomUUID();
    private Long customerID;
    @OneToMany(mappedBy="Charge")
    private Charge charge;

}
