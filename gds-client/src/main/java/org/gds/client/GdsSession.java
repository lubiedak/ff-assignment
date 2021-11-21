package org.gds.client;

import java.time.LocalDateTime;

public class GdsSession {
    private int assignedJobId;
    private final String sessionId;
    private final LocalDateTime timeOfCreation;

    public GdsSession(String sessionId, LocalDateTime timeOfCreation) {
        this.sessionId = sessionId;
        this.timeOfCreation = timeOfCreation;
        assignedJobId = 0;
    }

    public int getAssignedJobId() {
        return assignedJobId;
    }

    public void assignJob(int assignedJobId) {
        this.assignedJobId = assignedJobId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getTimeOfCreation() {
        return timeOfCreation;
    }
}
