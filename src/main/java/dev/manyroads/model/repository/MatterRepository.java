package dev.manyroads.model.repository;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatterRepository extends JpaRepository<Matter, UUID> {

    Optional<Matter> findByMatterNrAndCharge(String matterNr, Charge charge);
    Optional<List<Matter>> findByMatterNr(String matterNr);
}
