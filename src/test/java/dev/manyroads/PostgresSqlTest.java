package dev.manyroads;

import dev.manyroads.model.Actor;
import dev.manyroads.repository.ActorRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class PostgresSqlTest {

    @Autowired
    ActorRepository testRepository;

    @Test
    void actorRepoTest(){

        // Prepare
        Actor actor = new Actor();
        String firstName = "Aap";
        actor.setFirstName(firstName);
        Actor savedActor = testRepository.save(actor);

        // Act
        Optional<Actor> oResult = testRepository.findById(savedActor.getActorId());

        // Verify
        oResult.ifPresent(s -> assertEquals(firstName, s.getFirstName()));

    }
}
