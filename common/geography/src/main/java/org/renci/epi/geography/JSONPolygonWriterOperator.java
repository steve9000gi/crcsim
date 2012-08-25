package org.renci.epi.geography;

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
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
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

public class JSONPolygonWriterOperator implements PolygonOperator {

    private static Log logger = LogFactory.getLog (JSONPolygonWriterOperator.class); 

    private static final int BLOCK = 2048;
    private GeometryFactory geometryFactory = new GeometryFactory ();
    private String _outputPath;
    private String _outputFileNamePrefix;
    private int _outputFileCount = 0;
    private List<String> _index = new ArrayList<String> ();
    private int _count = 0;

    public JSONPolygonWriterOperator (String outputPath, String outputFileNamePrefix) {
	_outputPath = outputPath;
	_outputFileNamePrefix = outputFileNamePrefix;
    }

    public void execute (MultiPolygon polygon, Point [] points, boolean hasNext) {

	JSONObject jsonObject = new JSONObject ();

	BufferedReader reader = null;
	PrintWriter writer = null;
	try {

	    long startTime = System.currentTimeMillis ();

	    JSONArray jsonPolygon = new JSONArray ();
	    for (int c = 0; c < points.length; c++) {
		Point point = points [c];
		JSONArray aPoint = new JSONArray ();
		aPoint.put (point.getX ());
		aPoint.put (point.getY ());
		jsonPolygon.put (aPoint);
	    }
	    jsonObject.put ("points", jsonPolygon);
	    jsonObject.put ("count", _count++);

	    String outputFileName = new StringBuffer ().
		append (_outputFileNamePrefix).
		append ("-").
		append (_outputFileCount++).
		append (".json").toString ();

	    String outputFilePath = new StringBuffer ().
		append (_outputPath).
		append (File.separatorChar).
		append (outputFileName).toString ();	    

	    writer = new PrintWriter (new BufferedWriter (new FileWriter (outputFilePath)));
	    jsonObject.write (writer);

	    _index.add (outputFileName);

	    /*
	    String indexEntry = new StringBuffer ().
		append ("\"").
		append (outputFileName).
		append ("\"").
		append (hasNext ? "," : "").toString ();
	    _writer.println (indexEntry);
	    */
	    long endTime = System.currentTimeMillis ();
	    if (logger.isDebugEnabled ()) {
		logger.debug ("   --(end record points) " + ((endTime - startTime) / 1000));
	    }

	} catch (IOException e) {
	    throw new RuntimeException (e);
	} catch (JSONException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }

    public void close () {
	PrintWriter writer = null;
	try {
	    JSONObject indexObject = new JSONObject ();
	    JSONArray jsonArray = new JSONArray ();
	    for (int c = 0; c < _index.size (); c++) {
		String entry = _index.get (c);
		jsonArray.put (entry);
	    }
	    indexObject.put ("index", jsonArray);
	    String outputFileName = new StringBuffer ().
		append (_outputFileNamePrefix).
		append ("-index.json").toString ();
	    String outputFilePath = new StringBuffer ().
		append (_outputPath).
		append (File.separatorChar).
		append (outputFileName).toString ();
	    writer = new PrintWriter (new BufferedWriter (new FileWriter (outputFilePath), BLOCK));
	    indexObject.write (writer);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} catch (JSONException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }
}