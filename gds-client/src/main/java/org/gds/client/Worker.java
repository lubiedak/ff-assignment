package org.gds.client;


public class Worker implements Runnable{

    private final GdsApiClient client;

    public Worker(GdsApiClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        var response = client.createSession();
        System.out.println("Created session: " + response);
    }
}
