package org.gds.client;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.gds.client.SessionManager.MAX_SESSIONS;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableAsync
public class JobsExecutor {
    private static Logger log = getLogger(JobsExecutor.class);

    private final SessionManager sessionManager;

    private final List<Job> jobsReadyToBeExecuted;
    private final Deque<Job> waitingJobs;

    private final ExecutorService executor;
    private final Set<FutureTask<Integer>> tasks;

    public JobsExecutor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        jobsReadyToBeExecuted = new ArrayList<>();
        waitingJobs = new ArrayDeque<>();
        executor = Executors.newFixedThreadPool(MAX_SESSIONS);
        tasks = new HashSet<>();
    }

    public void handle(Job job){
        if (sessionManager.areSessionsAvailable()){
            attachSessionToAJob(job);
        } else {
            waitingJobs.add(job);
        }
    }

    private void attachSessionToAJob(Job job){
        GdsSession session = sessionManager.getAvailableSession();
        if(session != null) {
            session.assignJob(job.attachSession(session.getSessionId()));
            jobsReadyToBeExecuted.add(job);
            log.info("Attached session of {} to job {}. Jobs ready: {}", session.getSessionId(), session.getAssignedJobId(), jobsReadyToBeExecuted.size());
        }
    }

    @Async
    @Scheduled(initialDelay=0, fixedRate = 500)
    public void checkIfAreSessionsAvailableForWaitingJobs() {
        if(!waitingJobs.isEmpty() && sessionManager.areSessionsAvailable() ){
            attachSessionToAJob(waitingJobs.poll());
        }
    }

    @Async
    @Scheduled(initialDelay=0, fixedRate = 100)
    public void startReadyJobs() throws InterruptedException {
        for(var job : jobsReadyToBeExecuted){
            FutureTask<Integer> task = new FutureTask<>(job, job.getId());
            tasks.add(task);
            executor.submit(task);
            log.info("Task for Job added {}", job.getId());
        }
    }

    @Async
    @Scheduled(initialDelay=0, fixedRate = 300)
    public void cleanFinishedJobs(){
        for(var t : tasks){
            if(t.isDone()){
                try {
                    detachSessionFromJob(t.get());
                    log.info("Job {} completed", t.get());
                } catch (ExecutionException | InterruptedException e) {
                    log.error("Exception occured: ", e);
                }
            }
        }
        tasks.removeIf(FutureTask::isDone);
    }

    public void detachSessionFromJob(int jobId){
        sessionManager.releaseSession(jobId);
        jobsReadyToBeExecuted.removeIf(job -> job.getId() == jobId);
    }
}
