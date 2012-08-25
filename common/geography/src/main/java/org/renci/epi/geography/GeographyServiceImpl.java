package org.renci.epi.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.Geometry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GeographyServiceImpl implements GeographyService {

    private static Log logger = LogFactory.getLog (GeographyServiceImpl.class); 

    private GeometryFactory geometryFactory = new GeometryFactory ();

    public void getPolygons (String fileName) {
	getPolygons (fileName, null);
    }

    public void getPolygons (String fileName, PolygonOperator [] operators) {
	File file = new File (fileName);
	try {
	    Map connect = new HashMap ();
	    connect.put ("url", file.toURL ());
	    DataStore dataStore = DataStoreFinder.getDataStore (connect);
	    String[] typeNames = dataStore.getTypeNames ();
	    String typeName = typeNames [0];
	    FeatureSource featureSource = dataStore.getFeatureSource (typeName);
	    FeatureCollection collection = featureSource.getFeatures ();
	    FeatureIterator iterator = collection.features ();
	    try {
		while (iterator.hasNext ()) {
		    Feature feature = iterator.next ();
		    GeometryAttribute geometryAttribute = feature.getDefaultGeometryProperty (); 
		    GeometryType geometryType = geometryAttribute.getType ();
		    StringBuilder builder = new StringBuilder ().
			append ("feature: ").
			append (geometryAttribute.getName ()).
			append (geometryType.getName ());
		    //logger.debug (builder.toString ());
		    MultiPolygon multiPolygon = (MultiPolygon)geometryAttribute.getValue ();
		    Point [] points = this.decodeMultiPolygon (geometryAttribute);
		    if (operators != null) {
			for (int c = 0; c < operators.length; c++) {
			    PolygonOperator operator = operators [c];
			    if (operator != null) {
				try {
				    operator.execute (multiPolygon, points, iterator.hasNext ());
				} catch (Exception e) {
				    logger.error (e);
				}
			    }
			}
		    }
		}
	    } finally {
		iterator.close ();
		if (operators != null) {
		    for (int c = 0; c < operators.length; c++) {
			PolygonOperator operator = operators [c];
			if (operator != null) {
			    try {
				operator.close ();
			    } catch (Exception e) {
				logger.error (e);
			    }
			}
		    }
		}
	    }
	} catch (Throwable e) {
	    e.printStackTrace ();
	}
    }

    private Point [] decodeMultiPolygon (GeometryAttribute geometryAttribute) {
	List<Point> points = new ArrayList<Point> ();
	MultiPolygon multiPolygon = (MultiPolygon)geometryAttribute.getValue ();
	for (int c = 0; c < multiPolygon.getNumGeometries (); c++) {
	    Polygon polygon = (Polygon)multiPolygon.getGeometryN (c);
	    LinearRing linearRing = (LinearRing)polygon.getExteriorRing ();
	    Coordinate [] coordinates = linearRing.getCoordinates ();

	    // Skipping last coordinate since JTD defines a shell as a LineString that start with same first and last coordinate
	    for (int j = 0; j < coordinates.length-1; j++) {
		Point point = this.geometryFactory.createPoint (coordinates [j]);
		points.add (point);
	    }
	}
	return (Point [])points.toArray (new Point [points.size ()]);
    }
}