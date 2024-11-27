package dev.manyroads.model.repository;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.enums.ChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

    @Query(
            "SELECT c " +
                    "FROM Charge c " +
                    "WHERE (c.chargeStatus = :cs1 " +
                    "OR c.chargeStatus = :cs2 ) " +
                    "AND c.customerNr = :customerNr "
    )
    Optional<Charge> findByCustomerNrAndChargeStatus(@Param("cs1") ChargeStatus chargeStatus1,
                                                     @Param("cs2") ChargeStatus chargeStatus2,
                                                     @Param("customerNr") long customerNr);

   List<Charge> findByCustomerNr(Long customerNr);
}
