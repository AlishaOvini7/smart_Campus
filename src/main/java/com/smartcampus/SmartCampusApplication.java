package com.smartcampus;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Scan the entire com.smartcampus package for
        // @Path, @Provider, @ExceptionMapper, filters etc.
        packages("com.smartcampus");
    }
}