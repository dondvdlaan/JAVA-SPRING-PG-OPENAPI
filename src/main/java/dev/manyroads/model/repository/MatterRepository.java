package dev.manyroads.model.repository;

import dev.manyroads.model.entity.Matter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatterRepository extends JpaRepository<Matter, UUID> {

}
