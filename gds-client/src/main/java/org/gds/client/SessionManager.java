package org.gds.client;

import org.slf4j.Logger;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SessionManager {
    private static Logger log = getLogger(SessionManager.class);

    public static final int MAX_SESSIONS = 10;

    private GdsApiClient client;
    private final List<GdsSession> activeSessions;



    public SessionManager(GdsApiClient client) {
        this.client = client;
        activeSessions = new ArrayList<>();
    }

    public boolean areSessionsAvailable(){
        log.info("Active sessions: {}", activeSessions.size());
        var busySessions = activeSessions.stream().filter(s -> s.getAssignedJobId() > 0).count();
        return busySessions < MAX_SESSIONS;
    }

    public GdsSession getAvailableSession(){
        var availableSession = activeSessions.stream().filter(s -> s.getAssignedJobId() == 0).findFirst();
        if(availableSession.isPresent()){
            return availableSession.get();
        } else if (activeSessions.size()<MAX_SESSIONS) {
            return createSession();
        }
        return null;
    }

    private GdsSession createSession(){
        log.info("Creating new session");
        String id = client.createSession();
        var session = new GdsSession(id, LocalDateTime.now());
        activeSessions.add(session);
        log.info("Session {} created.", id);
        return session;
    }

    public void releaseSession(int jobId){
        activeSessions.stream().filter(s->s.getAssignedJobId() == jobId)
                .findFirst().ifPresent(s->s.assignJob(0));
    }

}
