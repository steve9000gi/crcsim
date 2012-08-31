package org.renci.epi.population;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.renci.epi.population.dao.PopulationDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.renci.epi.geography.GeographyService;
import org.renci.epi.geography.PolygonOperator;
import org.renci.epi.geography.JSONPolygonWriterOperator;
import org.renci.epi.util.DataLocator;

public class PopulationServiceImpl implements PopulationService {

    private PopulationDAO populationDao;

    private GeographyService geographyService;

    public void setPopulationDAO (PopulationDAO populationDao) {
	this.populationDao = populationDao;
    }
    public void setGeographyService (GeographyService geographyService) {
	this.geographyService = geographyService;
    }
    public DataLocator getDataLocator () {
	return this.geographyService.getDataLocator ();
    }

    public void compileModelInput (char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys)
    {
	InputStream input = null;
	Reader reader = null;
	Writer writer = null;
	CSVProcessor syntheticPopulation = null;
	try {
	    String inputFileName = getDataLocator().getSyntheticPopulationPath ("syntheticpopulation.out");
	    String outputFileName = getDataLocator().getModelInputFileName ();
	    input = new BufferedInputStream (new FileInputStream (inputFileName));
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
	    
	    File outputFile = new File (outputFileName);
	    
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

    public Object getPopulation (String [] query) {
	return this.populationDao.getPopulation (query);
    }

    public void geocodePopulation (String polygonFileName) {
	String modelFileDirectory = getDataLocator ().getModelOutputPath ();
	String outputFilePath = getDataLocator ().getGeocodedOutputPath ();
	this.geographyService.
	    getPolygons (polygonFileName,
			 new PolygonOperator [] {
			     new JSONPolygonWriterOperator (outputFilePath, "polygon"),
			     new PopulationPolygonOperator (outputFilePath, "occurrences", modelFileDirectory)
			 });
    }
    
}

