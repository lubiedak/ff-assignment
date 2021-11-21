package org.gds.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.gds.client.SessionManager.MAX_SESSIONS;
import static org.slf4j.LoggerFactory.*;

@Component
public class JobsPoolManager {
    private static Logger log = getLogger(JobsPoolManager.class);
    private final JobHandler jobHandler;

    public JobsPoolManager(JobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    @PostConstruct
    public void simulateJobsQue() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_SESSIONS);
        Set<FutureTask<Integer>> tasks = new HashSet<>();
        for (int i = 1; i < 100; i++) {

            int requiredTime = ThreadLocalRandom.current().nextInt(5, 16);
            Job job = new Job(i, requiredTime);
            log.info("New job created: {} need work {} seconds", i, requiredTime);
            jobHandler.handle(job);
            if(job.canStart()) {
                FutureTask<Integer> task = new FutureTask<>(job, job.getId());
                tasks.add(task);
                executor.submit(task);
            } else {

            }

            for(var t : tasks){
                if(t.isDone()){
                    try {
                        jobHandler.detachSessionFromJob(t.get());
                        log.info("Job {} completed", t.get());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            tasks.removeIf(t->t.isDone());

            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1000));
        }
    }
}
