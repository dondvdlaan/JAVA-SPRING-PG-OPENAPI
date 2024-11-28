package dev.manyroads.model.entity;

import dev.manyroads.model.ExecInterrupEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="exec_interrups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecInterrup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "exec_interrup_id")
    private UUID execInterrupID;
    @Column(name = "customer_nr")
    private Long customerNr;
    @Column(name = "matter_id")
    private String matterID;
    @Column(name = "exec_interrup_status")
    @Enumerated(EnumType.STRING)
    private ExecInterrupEnum execInterrupStatus;



}
