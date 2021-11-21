package org.gds.client.scheduler;

import org.gds.client.GdsSession;
import org.gds.client.Job;
import org.gds.client.SessionManager;
import org.slf4j.Logger;
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
public class JobsExecutor {
    private static Logger log = getLogger(JobsExecutor.class);

    private final SessionManager sessionManager;

    private final Deque<Job> jobsReadyToBeExecuted;
    private final Deque<Job> waitingJobs;

    private final ExecutorService executor;
    private final Set<FutureTask<Integer>> tasks;

    public JobsExecutor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        jobsReadyToBeExecuted = new ArrayDeque<>();
        waitingJobs = new ArrayDeque<>();
        executor = Executors.newFixedThreadPool(MAX_SESSIONS);
        tasks = new HashSet<>();
    }

    public void handle(Job job){
        if (sessionManager.areSessionsAvailable()){
            attachSessionToAJob(job);
        } else {
            log.info("No sessions available");
            waitingJobs.add(job);
        }
        log.info("Tasks running: {}, waiting {} ", tasks.size(), waitingJobs.size());
    }

    private void attachSessionToAJob(Job job){
        GdsSession session = sessionManager.getAvailableSession();
        if(session != null) {
            session.assignJob(job.attachSession(session.getSessionId()));
            jobsReadyToBeExecuted.add(job);
            log.info("Attached session of {} to job {}.", session.getSessionId(), session.getAssignedJobId());
        }
    }

    @Scheduled(initialDelay=0, fixedRate = 500)
    public void checkIfAreSessionsAvailableForWaitingJobs() {
        if(!waitingJobs.isEmpty() && sessionManager.areSessionsAvailable() ){
            log.info("There are some free sessions for waiting jobs. Que size {}", waitingJobs.size());
            attachSessionToAJob(waitingJobs.poll());
        }
    }

    @Scheduled(initialDelay=0, fixedRate = 100)
    public void startReadyJobs() throws InterruptedException {
        if(!jobsReadyToBeExecuted.isEmpty()){
            Job job = jobsReadyToBeExecuted.poll();
            FutureTask<Integer> task = new FutureTask<>(job, job.getId());
            tasks.add(task);
            executor.submit(task);
            log.info("Task for Job added {}", job.getId());
        }
    }

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
    }
}
