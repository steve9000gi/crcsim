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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
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

/**
 *
 * A polygon operator. Polygon operators are plugins to the GeographyService invoked for each 
 * polygon in a list.
 *
 * This operator
 *   Reads each of a set of output files from a model.
 *   For each line in the file, it extracts longitude and latitude data
 *   It then calculates containment of the point by the polygon
 *   A secondary predicate is also calculated
 *   If (a) the point is contained by the polygon and
 *      (b) the second predicate evaluates to true
 *      then a counter is incremented and added to a matrix of values 
 *
 */
class PopulationPolygonOperator implements PolygonOperator {

    private static Log logger = LogFactory.getLog (PopulationPolygonOperator.class); 

    private static final int BLOCK = 2048;
    private final char delimiter = '\t';

    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private final String NUM_LESIONS = "num_lesions";
    private final String NEVER_COMPLIANT = "never_compliant";
    
    private GeometryFactory geometryFactory = new GeometryFactory ();

    private String _modelOutputPath;
    private String _outputPath;
    private String _outputFileNamePrefix;

    private HashMap<File, ArrayList<Integer>> _counts = new HashMap<File, ArrayList<Integer>> ();



    /**
     * Construct a new operator.
     */
    PopulationPolygonOperator (String outputPath, String outputFileNamePrefix, String modelOutputPath) {
	_outputPath = outputPath;
	_outputFileNamePrefix = outputFileNamePrefix;
	_modelOutputPath = modelOutputPath;
    }

    /**
     * Execute the operator, iterating over a set of observations, processing each in the context of the polygon.
     * <p>
     * Updates a matrix of values by observation and polygon.
     * <p>
     * {@link GeographyService} defines the plugin API this implements.
     *
     * @param polygon   the polygon to describe.
     * @param points    the list of points in the polygon.
     * @param hasNext   there are more polygons to process after this one.
     * @see             PolygonOperator
     * @since           1.0
     */
    public void execute (MultiPolygon polygon, Point [] points, boolean hasNext) {
	try {
	    File modelOutputDir = new File (_modelOutputPath);
	    if (! modelOutputDir.isDirectory ()) {
		throw new RuntimeException ("Model output directory: " + _modelOutputPath + " does not exist.");
	    }
	    logger.debug ("Event counter: processing polygon for output dir; " + modelOutputDir.getCanonicalPath ());
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
    
    /**
     * Analyzes an observation in the context of the given polygon. Determines containment and calculates additional predicates.
     * <p>
     * Updates a matrix of values by observation and polygon.
     *
     * @param polygon           the polygon to describe.
     * @param points            the list of points in the polygon.
     * @param hasNext           there are more polygons to process after this one.
     * @param modelOutputFile   observation data to process in the context of this polygon.
     * @param outputLevelCounts matrix row of values pertaining to this observation.
     * @since           1.0
     */
    private void processModelOutputFile (MultiPolygon polygon,
					 Point [] points,
					 boolean hasNext,
					 File modelOutputFile,
					 ArrayList<Integer> outputLevelCounts) 
    {
	Reader reader = null;
	try {
	    int count = 0;

	    logger.debug ("processing model output file: " + modelOutputFile.getCanonicalPath ());
	    /*
	    DelimitedFileImporter input = new DelimitedFileImporter (modelOutputFile.getCanonicalPath (),
								     new String (new char [] { delimiter }),
								     DelimitedFileImporter.ALL);
	    */
	    String fileName = modelOutputFile.getCanonicalPath ();
	    reader = fileName.endsWith (".gz") ?
	        new InputStreamReader (new GZIPInputStream (new FileInputStream (fileName))) :
		new FileReader (fileName);

	    DelimitedFileImporter input = new DelimitedFileImporter (fileName,
								     reader,
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

    /**
     * Close the polygon operator. Called when all polygons have been processed.
     * <p>
     * Writes output index and matrix data.
     *
     */
    public void close () {
	PrintWriter writer = null;
	try {
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
