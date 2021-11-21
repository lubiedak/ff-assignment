package org.gds.client;

import org.gds.client.scheduler.JobsExecutor;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

import static org.slf4j.LoggerFactory.*;

@Component
public class JobsProducer {
    private static int JOB_COUNTER = 0;

    private static Logger log = getLogger(JobsProducer.class);
    private final JobsExecutor jobsExecutor;

    public JobsProducer(JobsExecutor jobHandler) {
        this.jobsExecutor = jobHandler;
    }

    @Scheduled(fixedDelay = 500)
    public void simulateJobsQue() {
        if(JOB_COUNTER < 100) {
            int requiredTime = ThreadLocalRandom.current().nextInt(5, 16);
            Job job = new Job(JOB_COUNTER++, requiredTime);
            log.info("New job created: {} need work {} seconds", JOB_COUNTER, requiredTime);
            jobsExecutor.handle(job);
        }
    }
}
