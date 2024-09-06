package com.sonnet.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PreCacheJob {

    @Scheduled(cron = "25 28 14 1/1 * ? ")
    public void testSchedule() {
        System.out.println("this is a schedule job !!");
    }

}
