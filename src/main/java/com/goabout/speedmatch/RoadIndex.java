package com.goabout.speedmatch;

import java.io.File;
import java.util.Collections;
import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.distance.GeometryLocation;

public class RoadIndex {

    private final String SPEED_ATTR = "OMSCHR";
    private static String INPUT = "/home/abyrd/maxspeed/max_snelheden.shp";
    private static final Logger LOG = LoggerFactory.getLogger(RoadIndex.class);

    // Example input file uses Rijksdriehoekstelsel_New
    // which is already in meters
    CoordinateReferenceSystem sourceCRS;
    CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
    MathTransform fromWGS84;
    GeometryBuilder builder;

    // STRtree is a Packed R-Tree that cannot be modified once built
    STRtree tree;

    public void loadFile() {

        tree = new STRtree();

        LOG.debug("Loading road segments from shapefile {}", INPUT);
        try {
            File file = new File(INPUT);
            if (!file.exists()) {
                throw new RuntimeException("Input file does not exist.");
            }
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();

            sourceCRS = featureSource.getInfo().getCRS();
            // CoordinateReferenceSystem WGS84 = CRS.decode("EPSG:4326", true);
            fromWGS84 = CRS.findMathTransform(WGS84, sourceCRS, true); // lenient
            builder = new GeometryBuilder(); // this one from JTS needs no CRS

            Query query = new Query();
            query.setCoordinateSystem(sourceCRS);
            // no need to reproject, source is already in meters.
            // query.setCoordinateSystemReproject(internalCRS);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);

            SimpleFeatureIterator it = featureCollection.features();
            int n = 0;
            try {
                while (it.hasNext()) {
                    SimpleFeature feature = it.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    int maxSpeedKph = (int) feature.getAttribute(SPEED_ATTR);
                    // example input file contains single-entry multilinestrings
                    // rather than linestrings
                    MultiLineString mls = (MultiLineString) geom;
                    LOG.trace("geom: {}", mls);
                    for (int i = 0; i < mls.getNumGeometries(); i++) {
                        LineString ls = (LineString) mls.getGeometryN(i);
                        RoadChunk rc = new RoadChunk(ls, maxSpeedKph);
                        Envelope env = ls.getEnvelopeInternal();
                        tree.insert(env, rc);
                    }
                    n += 1;
                }
            } catch (ClassCastException cce) {
                throw new RuntimeException("Shapefile must contain multilinestrings.");
            }
            LOG.info("Loaded {} features", n);
            it.close();
            LOG.info("Indexing features");
            tree.build();
        } catch (Exception ex) {
            LOG.error("Error loading roads from shapefile: {}", ex.toString());
            throw new RuntimeException(ex);
        }
        LOG.debug("Done loading shapefile.");
    }

    public RoadIndex() {
        loadFile();
    }

    /**
     * Heading is in degrees. Speed is in meters/sec.
     */
    public List<RoadMatch> getMatches(double lat, double lon, double radius, double heading,
            double speed) {
        List<RoadMatch> matches = Lists.newArrayList();
        Coordinate coord = new Coordinate(lon, lat);
        try {
            // use GeoTools JTS bridge class
            Coordinate proj = JTS.transform(coord, null, fromWGS84);
            Point point = builder.point(proj.x, proj.y);
            Envelope env = new Envelope(proj);
            env.expandBy(radius, radius);
            LOG.debug("Envelope is: {}", env.toString());
            @SuppressWarnings("unchecked")
            List<RoadChunk> chunks = (List<RoadChunk>) tree.query(env);
            for (RoadChunk chunk : chunks) {
                double distance = point.distance(chunk.geom);
                DistanceOp dop = new DistanceOp(chunk.geom, point);
                GeometryLocation[] locs = dop.nearestLocations();
                int segIdx = locs[0].getSegmentIndex();
                Coordinate c0 = chunk.geom.getCoordinateN(segIdx);
                Coordinate c1 = chunk.geom.getCoordinateN(segIdx + 1);
                LineSegment seg = new LineSegment(c0, c1);
                double segAngleRad = seg.angle();
                segAngleRad += (Math.PI / 2.0); // Relative to North, not East
                double segAngleDeg = Angle.toDegrees(segAngleRad);
                double reqAngleRad = Angle.toRadians(heading);
                double relAngleDeg = Angle.toDegrees(Angle.diff(segAngleRad, reqAngleRad));
                matches.add(new RoadMatch(chunk, (int) distance, (int) segAngleDeg,
                        (int) relAngleDeg));
            }
        } catch (TransformException e) {
            e.printStackTrace();
        }
        Collections.sort(matches);
        return matches;
    }

}