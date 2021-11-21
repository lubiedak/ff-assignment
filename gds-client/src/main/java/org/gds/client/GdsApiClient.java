package org.gds.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "gds", url = "http://127.0.0.1:5000")
public interface GdsApiClient {
    @RequestMapping(method = RequestMethod.POST, value = "/create-session")
    String createSession();

    @RequestMapping(method = RequestMethod.DELETE, value = "/delete-session")
    String deleteSession(String id);

    @RequestMapping(method = RequestMethod.PUT, value = "/keep-alive")
    String keepAlive(String id);
}
