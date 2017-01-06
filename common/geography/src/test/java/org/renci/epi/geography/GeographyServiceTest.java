package org.renci.epi.geography;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
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


import com.vividsolutions.jts.geom.prep.PreparedGeometry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring/geography-context.xml"})
public class GeographyServiceTest extends AbstractJUnit4SpringContextTests {

    private static Log logger = LogFactory.getLog (GeographyServiceTest.class); 

    @Autowired
    protected GeographyService geographyService;

    @Test
    public void testGetPolygons () throws Exception {

/*
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);

	Assert.assertTrue (this.geographyService != null);

	String fileName = this.geographyService.getDataLocator().getCountyPolygonFileName ();
	this.geographyService.getPolygons (fileName);
*/
    }

    @Test
    public void testGetPreparedPolygons () throws Exception {
/*
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);

	Assert.assertTrue (this.geographyService != null);

	String fileName = this.geographyService.getDataLocator().getCountyPolygonFileName ();
	List<PreparedGeometry> prepared = this.geographyService.getPreparedPolygons (fileName);
	for (PreparedGeometry geometry : prepared) {
	    logger.info ("Prepared geometry instance: " + geometry.hashCode ());
	}
*/
    }
}

