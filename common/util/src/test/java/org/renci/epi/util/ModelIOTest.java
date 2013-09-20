package org.renci.epi.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

class X {
    public String w = "w0";
    public String x = "x0";
    public String y = "y0";
    public String z = "z0";
};

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/spring/population-context.xml"})
public class ModelIOTest { //extends AbstractJUnit4SpringContextTests {
	

    private static Log logger = LogFactory.getLog (ModelIOTest.class); 
    static {
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);
    }

    public ModelIOTest () {
    }

    @Test
    public void testGetFieldValue () throws Exception {
	X x = new X ();
	Object value = Util.getFieldValue (x, "x");
	logger.debug ("==> " + value);
    }
    @Test
    public void test () throws Exception {
	X object = new X ();
	ModelIO modelIO = new ModelIO ();
	BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (System.out));
	String [] fieldNames = new String [] {
	    "w", "x", "y", "z"
	};
	String className = "org.renci.epi.util.X";
	String newline = "\n";

	modelIO.outputRow (writer, fieldNames, '\t', newline);
	for (int c = 0; c < 10; c++) {
	    modelIO.outputDataRow (writer, object, fieldNames, '\t', newline);
	}	
	writer.flush ();
    }

    @Test
    public void testGeography () throws Exception {
	Geography geography = new Geography (Geography.COMPLIANCE);

	double distance = geography.getDistanceToNearestEndoscopyFacilityByZipCode ("27517");
	CountyIntercepts countyIntercepts = geography.getCountyInterceptsByStcotrbg ("3700199999900000");
   
	System.out.println ("zip code -> distance: " + distance + "\n" +
			    "stcotrbg -> county intercepts -> medicaid: " + countyIntercepts.getMedicaidOnly ());

	assert countyIntercepts.getMedicaidOnly () != Geography.UNKNOWN_DOUBLE : "Unexpected unknown value.";

	assert distance == 4.4922130495 : "Distance by zip code failed.";

	assert countyIntercepts.getMedicaidOnly () == 0.348013 : "Medicaid by county FIPS failed";
    }

    @Test
    public void testScriptEngine () throws Exception {
	List<String> iterations = IterationUtil.generateIterations ("/script/testrandom.js");
	assert iterations.size () == 10;
	for (String element : iterations) {
	    String [] parts = element.split ("\n");
	    for (String part : parts) {
		assert part.indexOf ("=") > 0;
		String [] nameValue = part.split ("=");
		double val = Double.parseDouble (nameValue [1]);
	    }
	}
    }
    @Test
    public void testScriptEngineDetectDuplicateParameter () throws Exception {
	try {
	    List<String> iterations = IterationUtil.generateIterations ("/script/testrandom-duplicate.js");
	    assert iterations.size () == 10;
	    for (String element : iterations) {
		String [] parts = element.split ("\n");
		for (String part : parts) {
		    assert part.indexOf ("=") > 0;
		    String [] nameValue = part.split ("=");
		    double val = Double.parseDouble (nameValue [1]);
		}
	    }
	} catch (Exception e) {
	    boolean match = false;
	    for (Throwable cause = e.getCause (); cause != null; cause = cause.getCause ()) {
		System.out.println ("cause: " + cause.getClass().getName () + " : " + cause.getMessage ());
		if (cause.getMessage ().indexOf ("Parameter: param-A appears twice in the configuration matrix.") > -1) {
		    match = true;
		    break;
		}
	    }
	    assert match;
	}
    }


    @Test
    public void testCalibrator () {
	System.out.println ("-------------------------------(calibrate)");

	class People {
	    public double average (String value) {
		return 9.3;
	    }
	};
	class Population {
	    public People people = new People ();
	}
	class Root {
	    public boolean model_initial_compliance = true;
	    public boolean use_conditional_compliance = true;
	    public Population population = new Population ();
	}
	Root object = new Root ();

	try {
	    Calibrate calibrate = new JavaScriptCalibrate ("/script/calibrate.js");
	    calibrate.onInitExperiment ();
	    calibrate.onInitRun (object);
	    calibrate.onRunComplete (object);
	    calibrate.onIterationComplete ();
	} catch (Exception e) {
	    e.printStackTrace ();
	    throw new RuntimeException (e);
	}
    }

}

