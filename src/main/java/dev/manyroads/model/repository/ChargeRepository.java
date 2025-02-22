package dev.manyroads.model.repository;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

    @Query(
            value = "SELECT c " +
                    "FROM Charge c " +
                    "WHERE (c.chargeStatus = :cs1 " +
                    "OR c.chargeStatus = :cs2 ) " +
                    "AND c.customer.customerNr = :customerNr "
    )
    Optional<List<Charge>> findByCustomerNrAndChargeStatus(@Param("cs1") ChargeStatusEnum chargeStatus1,
                                                           @Param("cs2") ChargeStatusEnum chargeStatus2,
                                                           @Param("customerNr") long customerNr);

    @Query(
            value = "SELECT c " +
                    "FROM Charge c " +
                    "WHERE c.chargeStatus = :cs1 " +
                    //       "OR c.chargeStatus = :cs2 ) " +
                    //       "WHERE c.customer.customerNr = :customerNr "
                    "AND c.customer.customerNr = :customerNr "
    )
    Optional<List<Charge>> findByCustomerNrAndChargeStatuss(Long customerNr, @Param("cs1") ChargeStatusEnum chargeStatusEnum);
}
