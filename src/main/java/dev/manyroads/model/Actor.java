package dev.manyroads.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Actor {

    @Id
    private int actorId;
    private String firstName;
    private String lastName;
    private LocalDate lastUpdate;

}
