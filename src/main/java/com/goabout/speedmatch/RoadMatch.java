package com.goabout.speedmatch;

public class RoadMatch implements Comparable<RoadMatch> {

	private RoadChunk chunk;
	public int distance;
	public int segAngle;
	public int relAngle;
	public int quality;
	
	@Override
	public int compareTo(RoadMatch that) {
		return this.quality - that.quality;
	}

	public RoadMatch(RoadChunk chunk, int distance, int segAngle, int relAngle) {
		this.chunk = chunk;
		this.distance = distance;
		this.segAngle = segAngle;
		this.relAngle = relAngle;
		this.quality = (int) (distance + Math.abs(relAngle * 4));
	}

	public float getMaxSpeed() {
		return chunk.maxSpeedKph;
	}
}
