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
import org.apache.commons.io.IOUtils;
import org.renci.epi.population.dao.PopulationDAO;

public class PopulationServiceImpl implements PopulationService {

    private PopulationDAO populationDao;

    public void setPopulationDAO (PopulationDAO populationDao) {
	this.populationDao = populationDao;
    }

    public void compileModelInput (String inputFileName,
				   String outputFileName,
				   char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys)
    {
	InputStream input = this.getClass().getResourceAsStream ("/sample.out");
	if (input != null) {
	    Reader reader = null;
	    Writer writer = null;
	    File outputFile = null;
	    CSVProcessor syntheticPopulation = null;
	    try {
		reader = new BufferedReader (new InputStreamReader (input));
		writer = new BufferedWriter (new FileWriter (outputFileName));
		Processor processor = new SynthPopAnnotationProcessor (outputKeys);
		syntheticPopulation = new CSVProcessor (reader,
							inputSeparator,
							processor,
							writer,
							outputSeparator);
		syntheticPopulation.parse ();
		
		// verification steps
		
		outputFile = new File (outputFileName);
		//outputFile.delete ();
		
	    } catch (IOException e) {
		e.printStackTrace ();
	    } finally {
		if (syntheticPopulation != null) {
		    try {
			syntheticPopulation.close ();
		    } catch (IOException e) {
			e.printStackTrace ();
		    }
		}
		IOUtils.closeQuietly (reader);
		IOUtils.closeQuietly (writer);
	    }	
	}
    }

    public Object getPopulation (String [] query) {
	System.out.println ("==========================================================");
	return this.populationDao.getPopulation (query);
    }

}
