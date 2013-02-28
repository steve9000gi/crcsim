package org.renci.epi.pig;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import java.io.IOException;
import java.util.List;
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

    private static final String POLYGON_FILE = "/home/scox/dev/var/crcsim/census2010/tl_2010_37_county10.shp";
    private static List <PreparedGeometry> geometries = null;    
    private static GeometryFactory geometryFactory = new GeometryFactory ();

    /**
     * Pig.define passes parameter names to public constructor.
     */
    public GEOCODE (String [] names) {	
    }

    private static final void initialize () {
	if (geometries == null) {
	    GeographyService geographyService = new GeographyServiceImpl ();
	    geometries = geographyService.getPreparedPolygons (POLYGON_FILE);
	}
    }

    private final static int findPolygon (float longitude, float latitude) {
	initialize ();
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
	if (input != null && input.size () == 3) {
	    try {
		Integer timeslice = (Integer)input.get (0);
		Float longitude = (Float)input.get (1);
		Float latitude = (Float)input.get (2);
		result = String.valueOf (findPolygon (longitude, latitude));
		/*
		Tuple output = TupleFactory.getInstance ().newTuple (2);
		output.set (0, input.get (1));
		output.set (1, input.get (0));
		*/
	    } catch (Exception e) {
		e.printStackTrace ();
		//throw WrappedIOException.wrap ("Caught exception processing input row ", e);
	    }
	}
	return result;
    }
    /*
    public Schema outputSchema (Schema input) {
	Schema result = null;
        try {
            Schema tupleSchema = new Schema ();
            tupleSchema.add (input.getField (1));
            result = new Schema (new Schema.FieldSchema (getSchemaName (this.getClass().getName().toLowerCase(), input),
							 tupleSchema,
							 DataType.TUPLE));
        } catch (Exception e){
	    throw new RuntimeException (e);
        }
	return result;
    }
    */

}