package com.sonnet.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@Slf4j
public class PreCacheJob {

    @Scheduled(cron = "*/5 * * * * ?")
    public void testSchedule() {
        System.out.println("this is a schedule job !!");
    }

}
