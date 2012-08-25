package org.renci.epi.population;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.renci.epi.geography.PolygonOperator;

class PopulationPolygonOperator implements PolygonOperator {

    private static Log logger = LogFactory.getLog (PopulationPolygonOperator.class); 

    private static final int BLOCK = 2048;
    private final char separator = '\t';
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private boolean _firstLine = true;
    private int _latitudePosition = -1;
    private int _longitudePosition = -1;

    private GeometryFactory geometryFactory = new GeometryFactory ();

    private String _modelFileName;
    private String _outputPath;
    private String _outputFileNamePrefix;

    private JSONArray _jsonArray = new JSONArray ();

    PopulationPolygonOperator (String outputPath, String outputFileNamePrefix, String modelFileName) {
	_outputPath = outputPath;
	_outputFileNamePrefix = outputFileNamePrefix;
	_modelFileName = modelFileName;
    }

    public void execute (MultiPolygon polygon, Point [] points, boolean hasNext) {
	BufferedReader reader = null;
	try {
	    int members = 0;
	    reader = new BufferedReader (new FileReader (_modelFileName), BLOCK);
	    for (String line = reader.readLine (); line != null; line = reader.readLine ()) {
		String [] fields = StringUtils.split (line);
		if (_firstLine) { // header
		    _firstLine = false;
		    for (int q = 0; q < fields.length; q++) {
			String value = fields [q];
			if (value.equals (LATITUDE)) {
			    _latitudePosition = q;
			} else if (value.equals (LONGITUDE)) {
			    _longitudePosition = q;
			}
		    }
		    if (_latitudePosition == -1 || _longitudePosition == -1) {
			throw new RuntimeException ("Unable to find latitude and longitude keys " + 
						    LATITUDE + " " + LONGITUDE + " in file: " + _modelFileName);
		    }
		    break;
		} else {
		    String latitudeText = fields [_latitudePosition];
		    String longitudeText = fields [_longitudePosition];
		    try {
			double latitude = Double.parseDouble (latitudeText);
			double longitude = Double.parseDouble (longitudeText);
			Coordinate coordinate = new Coordinate (longitude, latitude); //(latitude, longitude);
			Point point = this.geometryFactory.createPoint (coordinate);
			if (polygon.contains (point)) {
			    members++;
			    logger.debug ("     *******point found in polygon.");
			}
		    } catch (NumberFormatException e) {
			//logger.error ("Error parsing doubles: lat(" + latitudeText + ") and lon(" + longitudeText + ")");
		    }
		}
	    }
	    _jsonArray.put (members);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
    }

    public void close () {
	PrintWriter writer = null;
	try {
	    JSONObject jsonObject = new JSONObject ();
	    jsonObject.put ("counts", _jsonArray);
	    String outputFileName = new StringBuffer ().
		append (_outputFileNamePrefix).
		append (".json").toString ();
	    String outputFilePath = new StringBuffer ().
		append (_outputPath).
		append (File.separatorChar).
		append (outputFileName).toString ();
	    writer = new PrintWriter (new BufferedWriter (new FileWriter (outputFilePath)));
	    jsonObject.write (writer);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} catch (JSONException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }
}