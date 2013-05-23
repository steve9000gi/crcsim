package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

class X {
    public String w = "w0";
    public String x = "x0";
    public String y = "y0";
    public String z = "z0";
};

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/spring/population-context.xml"})
public class ModelIOTest { //extends AbstractJUnit4SpringContextTests {
	
    private static Log logger = LogFactory.getLog (ModelIO.class); 

    public ModelIOTest () {
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);
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
	Geography geography = new Geography ();

	double distance = geography.getDistanceToNearestEndoscopyFacilityByZipCode ("27517");
	CountyIntercepts countyIntercepts = geography.getCountyInterceptsByStcotrbg ("3700199999900000");
   
	System.out.println ("zip code -> distance: " + distance + "\n" +
			    "stcotrbg -> county intercepts -> medicaid: " + countyIntercepts.getMedicaidOnly ());

	assert distance == 4.4922130495 : "Distance by zip code failed.";

	assert countyIntercepts.getMedicaidOnly () == 0.0016 : "Medicaid by county FIPS failed";
    }

}

