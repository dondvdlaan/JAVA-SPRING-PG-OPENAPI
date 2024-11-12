package dev.manyroads.repository;

import dev.manyroads.model.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Integer> {

    Optional<String> findByFirstName(String firstName);
}
