package dev.manyroads.model.repository;

import dev.manyroads.model.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

}
