package dev.manyroads.model.repository;

import dev.manyroads.model.entity.ExecInterrup;
import dev.manyroads.model.entity.Matter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecInterrupRepository extends JpaRepository<ExecInterrup, UUID> {

}
