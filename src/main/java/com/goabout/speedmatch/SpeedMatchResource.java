package com.goabout.speedmatch;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

@Path("/match")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SpeedMatchResource {

    // for some reason Jersey will only inject if this is
    // Application, not a subclass of Application.
    @Context
    Application app;

    @GET
    public Response getMatches(@QueryParam("lat") Double lat, @QueryParam("lon") Double lon,
            @QueryParam("heading") double heading, @QueryParam("speed") double speed,
            @QueryParam("radius") @DefaultValue("200") double radius) {

        List<RoadMatch> matches = ((SpeedMatchApplication) app).ridx.getMatches(lat, lon, radius,
                heading, speed);
        return Response.status(200).entity(matches).build();
    }
}
