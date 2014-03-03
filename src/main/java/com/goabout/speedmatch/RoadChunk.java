package com.goabout.speedmatch;

import com.vividsolutions.jts.geom.LineString;

class RoadChunk {

    LineString geom;
    int maxSpeedKph;

    public RoadChunk(LineString geom, int maxSpeedKph) {
        this.geom = geom;
        this.maxSpeedKph = maxSpeedKph;
    }

}