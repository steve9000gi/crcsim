package org.renci.epi.population;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring/population-context.xml"})
public class CSVProcessorTest extends AbstractJUnit4SpringContextTests {

    private static Log logger = LogFactory.getLog (CSVProcessorTest.class); 

    @Autowired
    protected PopulationService populationService;

    @Test
    public void testModelInputCompiler () throws Exception {

	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);
	//Logger.getRootLogger().setLevel (Level.ERROR);
  
	String inputFileName  = "/sample.out";
	String outputFileName = "target/out.txt";
	char inputSeparator   = '|';
	char outputSeparator  = '\t';
	String [] outputKeys  = {
	    "sex", "race", "SEXC", "INCOME", "FRISK", "VITALE", "AGE_G2",
	    "AGE_G3", "AGE_G4", "FLU", "BLACK", "HISP", "OTHER", "FORMER",
	    "NEVER", "ALONE", "MW", "SO", "WE", "USUAL", "NOINS",
	    "PRIVA", "EDU", "id", "LAT", "LON"
	};
	Assert.assertTrue (this.populationService != null);
	this.populationService.compileModelInput (inputFileName,
						  outputFileName,
						  inputSeparator,
						  outputSeparator,
						  outputKeys);
    }
    @Test
    public void testGetPopulation () throws Exception {
	this.populationService.getPopulation (new String [] { "0" });
    }
}

