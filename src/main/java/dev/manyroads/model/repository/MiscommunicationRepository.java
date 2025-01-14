package dev.manyroads.model.repository;

import dev.manyroads.model.entity.MisCommunication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MiscommunicationRepository extends JpaRepository<MisCommunication, UUID> {

}
