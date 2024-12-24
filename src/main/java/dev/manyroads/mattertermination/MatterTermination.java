package dev.manyroads.mattertermination;

import dev.manyroads.model.repository.MatterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatterTermination {

    MatterRepository matterRepository;


    @Scheduled(cron = ("* * 4 * * *"))
    void cleanUpMatterDatabase() {

        matterRepository.deleteAll();
    }
}
