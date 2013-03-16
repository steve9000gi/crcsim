package org.renci.epi.pig;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.util.WrappedIOException;
import org.renci.epi.geography.GeographyService;
import org.renci.epi.geography.GeographyServiceImpl;

/**
 * Geocode points to polygons.
 */
public class GEOCODE extends EvalFunc<String> {

    private static Map<String, List <PreparedGeometry>> geometryLists =
	new HashMap<String, List<PreparedGeometry>> ();
    private static GeometryFactory geometryFactory = new GeometryFactory ();

    /**
     * Pig.define passes parameter names to public constructor.
     */
    public GEOCODE (String [] names) {	
    }

    private final static int findPolygon (List<PreparedGeometry> geometries, float longitude, float latitude) {
	int polygon_index = -1;
	int total_polygons = geometries.size ();
	Coordinate coordinate = new Coordinate (longitude, latitude);
	Point point = geometryFactory.createPoint (coordinate);
	if (point != null) {
	    PreparedGeometry geometry = null;
	    for (int current = 0; current < total_polygons; current++) {
		try {
		    synchronized (geometries) {
			geometry = geometries.get (current);
			if (geometry != null && geometry.contains (point)) {
			    polygon_index = current;
			    break;
			}
		    }
		} catch (NoSuchElementException e) {
		    System.err.println ("-----------> " + e.getMessage ());
		}
	    }
	}
	return polygon_index;
    }

    public String exec (Tuple input) throws IOException {
	String result = null;
	if (input != null && input.size () == 4) {
	    try {
		String shapefile = (String)input.get (0);

		//Get the list of polygons.
		List<PreparedGeometry> geometries = null;
		synchronized (geometryLists) {
		    geometries = geometryLists.get (shapefile);
		    if (geometries == null) {
			geometries = new ArrayList<PreparedGeometry> ();
			GeographyService geographyService = new GeographyServiceImpl ();
			geometries = geographyService.getPreparedPolygons (shapefile);
			geometryLists.put (shapefile, geometries);
		    }
		}

		// Determine the polygon containing the given point
		Integer timeslice = (Integer)input.get (1);
		Float longitude = (Float)input.get (2);
		Float latitude = (Float)input.get (3);
		result = String.valueOf (findPolygon (geometries, longitude, latitude));
	    } catch (Exception e) {
		//e.printStackTrace ();
		throw WrappedIOException.wrap ("Caught exception processing input row ", e);
	    }
	}
	return result;
    }
}