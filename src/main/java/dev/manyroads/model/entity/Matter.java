package dev.manyroads.model.entity;

import dev.manyroads.model.IntermediateReportExplanationEnum;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.messages.MatterMessage;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "matters")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Matter {

    @Id
    @Builder.Default
    @Column(name = "matter_id")
    private UUID matterID = UUID.randomUUID();
    @Column(name = "matter_nr")
    private String matterNr;
    @Column(name = "matter_status")
    @Enumerated(EnumType.STRING)
    private MatterStatus matterStatus;
    @ManyToOne()
    @JoinColumn(name = "charge_id")
    private Charge charge;
    @Column(name = "reason_termination")
    @Enumerated(EnumType.STRING)
    private IntermediateReportExplanationEnum reasonTermination;
    @Column(name = "termination_call_back_url")
    private String terminationCallBackUrl;

    public MatterMessage convertToMatterMessage() {
        return new MatterMessage(this.matterNr, this.matterStatus);
    }
}