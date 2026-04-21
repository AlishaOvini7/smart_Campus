package com.smartcampus.exception;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper 
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("hint", "Sensor must have status ACTIVE to accept new readings.");

        return Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}