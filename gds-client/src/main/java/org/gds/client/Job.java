package org.gds.client;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Job implements Runnable {
    private final int id;
    private final int requiredTime;
    private String attachedSession;

    public Job(int id, int requiredTime) {
        this.id = id;
        this.requiredTime = requiredTime;
    }

    public int getId(){
        return id;
    }

    public int attachSession(String sessionId){
        attachedSession = sessionId;
        return id;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000 * requiredTime);
        }
        catch (InterruptedException ex) {
            Logger.getLogger(Job.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
