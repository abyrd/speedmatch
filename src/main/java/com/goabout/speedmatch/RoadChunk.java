package com.goabout.speedmatch;

import com.vividsolutions.jts.geom.LineString;

class RoadChunk {

    LineString geom;
    int maxSpeedKph;
	int fromHour;
	int toHour;

    public RoadChunk(LineString geom, int maxSpeedKph, int fromHour, int toHour) {
        this.geom = geom;
        this.maxSpeedKph = maxSpeedKph;
		this.fromHour = fromHour;
		this.toHour = toHour;
    }

}