package com.goabout.speedmatch;

import java.util.Set;

import javax.ws.rs.core.Application;

import jersey.repackaged.com.google.common.collect.Sets;

import org.glassfish.jersey.jackson.JacksonFeature;

public class SpeedMatchApplication extends Application {

    public RoadIndex ridx;

    public SpeedMatchApplication() {
        ridx = new RoadIndex();
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = Sets.newHashSet();
        // Add web resource classes.
        classes.add(SpeedMatchResource.class);
        // Add Jackson POJO JSON serialization.
        classes.add(JacksonFeature.class);
        return classes;
    }

}
