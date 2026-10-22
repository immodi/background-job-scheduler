package com.projects.backgroundjobscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackgroundJobSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackgroundJobSchedulerApplication.class, args);
    }

}
