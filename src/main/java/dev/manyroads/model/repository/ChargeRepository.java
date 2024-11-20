package dev.manyroads.model.repository;

import dev.manyroads.model.ChargeStatus;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

    @Query(
            "SELECT c " +
                    "FROM Charge c " +
                    "WHERE (c.chargeStatus = :c1 " +
                    "OR c.chargeStatus = :c2 ) " +
                    "AND c.customerNr = :customerNr "
    )
    Optional<Charge> findByCustomerNrAndChargeStatus(@Param("c1") ChargeStatus chargeStatus1,
                                                     @Param("c2") ChargeStatus chargeStatus2,
                                                     @Param("customerNr") long customerNr);
}
