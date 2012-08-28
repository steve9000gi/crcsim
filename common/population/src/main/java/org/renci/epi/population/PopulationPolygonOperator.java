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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.renci.epi.geography.PolygonOperator;
import org.renci.epi.util.DelimitedFileImporter;

class PopulationPolygonOperator implements PolygonOperator {

    private static Log logger = LogFactory.getLog (PopulationPolygonOperator.class); 

    private static final int BLOCK = 2048;
    private final char delimiter = '\t';

    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private final String NUM_LESIONS = "num_lesions";
    private final String NEVER_COMPLIANT = "never_compliant";
    
    private boolean _firstLine = true;
    private int _latitudePosition = -1;
    private int _longitudePosition = -1;

    private GeometryFactory geometryFactory = new GeometryFactory ();

    private String _modelOutputPath;
    private String _outputPath;
    private String _outputFileNamePrefix;

    private HashMap<File, ArrayList<Integer>> _counts = new HashMap<File, ArrayList<Integer>> ();

    PopulationPolygonOperator (String outputPath, String outputFileNamePrefix, String modelOutputPath) {
	_outputPath = outputPath;
	_outputFileNamePrefix = outputFileNamePrefix;
	_modelOutputPath = modelOutputPath;
    }

    public void execute (MultiPolygon polygon, Point [] points, boolean hasNext) {

	try {
	    File modelOutputDir = new File (_modelOutputPath);
	    if (! modelOutputDir.isDirectory ()) {
		throw new RuntimeException ("Model output directory: " + _modelOutputPath + " does not exist.");
	    }
	    
	    logger.debug ("Event counter: processing polygon for output dir; " + modelOutputDir.getCanonicalPath ());
	    
	    // second parameter - filename filter. third, recursive.
	    Collection<File> fileCollection = FileUtils.listFiles (modelOutputDir, null, false);
	    File [] files = (File [])fileCollection.toArray (new File [fileCollection.size ()]);
	    for (int c = 0; c < files.length; c++) {	    
		File file = files [c];
		if (file.getName ().startsWith ("person.")) {
		    ArrayList<Integer> outputLevelCounts = _counts.get (file);
		    if (outputLevelCounts == null) {
			outputLevelCounts = new ArrayList<Integer> ();
			_counts.put (file, outputLevelCounts);
		    }		
		    this.processModelOutputFile (polygon, points, hasNext, file, outputLevelCounts);
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }
    
    private void processModelOutputFile (MultiPolygon polygon,
					 Point [] points,
					 boolean hasNext,
					 File modelOutputFile,
					 ArrayList<Integer> outputLevelCounts) 
    {
	BufferedReader reader = null;
	try {
	    int count = 0;

	    logger.debug ("processing model output file: " + modelOutputFile.getCanonicalPath ());
	    DelimitedFileImporter input = new DelimitedFileImporter (modelOutputFile.getCanonicalPath (),
								     new String (new char [] { delimiter }),
								     DelimitedFileImporter.ALL);

	    for (input.nextRow (); input.hasMoreRows (); input.nextRow ()) {
		Coordinate coordinate = new Coordinate (input.getDouble (LONGITUDE),
							input.getDouble (LATITUDE));
		Point point = this.geometryFactory.createPoint (coordinate);
		
		int numLesions = input.getInt (NUM_LESIONS);
		boolean neverCompliant = input.getBoolean (NEVER_COMPLIANT);
		
		if (polygon.contains (point) && numLesions > 0 && neverCompliant) {
		    count++;
		}
	    }
	    outputLevelCounts.add (count);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
    }


    private void processModelOutputFile0 (MultiPolygon polygon, Point [] points, boolean hasNext, File modelOutputFile, ArrayList<Integer> outputLevelCounts) {
	BufferedReader reader = null;
	try {
	    int count = 0;
	    logger.debug ("processing model output file: " + modelOutputFile.getCanonicalPath ());

	    reader = new BufferedReader (new FileReader (modelOutputFile.getCanonicalPath ()), BLOCK);
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
						    LATITUDE + " " + LONGITUDE + " in file: " + modelOutputFile.getCanonicalPath ());
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

			int numLesions = Integer.parseInt (fields [4]);
			boolean neverCompliant = Boolean.parseBoolean (fields [5]);
			

			if (polygon.contains (point) && numLesions > 0 && neverCompliant) {
			    count++;
			}

		    } catch (NumberFormatException e) {
			//logger.error ("Error parsing doubles: lat(" + latitudeText + ") and lon(" + longitudeText + ")");
		    }
		}
	    }
	    logger.error ("adding count: " + count);
	    outputLevelCounts.add (count);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
    }

    public void close () {
	PrintWriter writer = null;
	try {
	    // http://labs.carrotsearch.com/hppc-api-and-code-examples.html
	    File [] keys = (File [])_counts.keySet ().toArray (new File [_counts.size ()]);
	    Arrays.sort (keys);
	    JSONArray matrix = new JSONArray ();
	    for (File key : keys) {
		ArrayList<Integer> vector = _counts.get (key);
		Integer [] integers = (Integer [])vector.toArray (new Integer [vector.size ()]);
		JSONArray countsPerPolygon = new JSONArray ();
		for (Integer item : integers) {
		    countsPerPolygon.put (item.intValue ());
		}
		matrix.put (countsPerPolygon);
	    }
	    JSONObject jsonObject = new JSONObject ();
	    jsonObject.put ("counts", matrix);

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

interface ObservationMatrix {
    public void addObservation (String keyA, String keyB, int value);
    public int [] getObservations (String keyA, String keyB);
}
