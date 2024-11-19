package dev.manyroads.model.repository;

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
            "WHERE (c.chargeStatus = :chargeStatus1 " +
            "OR c.chargeStatus = :chargeStatus2 ) " +
            "AND c.customerNr = :customerNr "
    )
    Optional<Charge> findByCustomerNrAndChargeStatus(
            @Param("chargeStatus1") String chargeStatus1,
            @Param("chargeStatus2") String chargeStatus2,
            @Param("customerNr") long customerNr);
}
