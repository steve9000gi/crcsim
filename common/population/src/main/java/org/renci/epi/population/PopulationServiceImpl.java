package org.renci.epi.population;

import au.com.bytecode.opencsv.CSVReader;
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


/**
 *
 * A service for manipulating population data.
 *
 */
public class PopulationServiceImpl implements PopulationService {

    private PopulationDAO populationDao;

    private HashMap<String, String> INS2014Map;    // SAC 2016/07/21

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
    /**
     * Translate model data from a raw export format emitted from the RTI synthetic model.
     * Add insurance and other data.
     * Generate an output file suitable for input to the RTI CRC model.
     *@param inputSeparator The separator character used in the input.
     *@param outputSeparator The separator characgter used in the output.
     *@param outputKeys The keys (names and order) in the output.
     */
    public void compileModelInput (char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys)
    {
	File inputFile = new File (getDataLocator().getSyntheticPopulationPath ("syntheticpopulation.out"));
	File outputFile = new File (getDataLocator().getModelInputFileName ("population.tsv"));
	try {
	    System.out.println ("Input: " + inputFile.getCanonicalPath ());
	    System.out.println ("Output: " + outputFile.getCanonicalPath ());
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
	this.createModelInput (inputSeparator,
			       outputSeparator,
			       outputKeys,
			       inputFile,
			       outputFile);
    }

    /**
     * Translate model data from a raw export format emitted from the RTI synthetic model.
     * Add insurance and other data.
     * Generate an output file suitable for input to the RTI CRC model.
     *
     * This version operates on multiple export files, generating multiple import files.
     *
     *@param inputSeparator The separator character used in the input.
     *@param outputSeparator The separator characgter used in the output.
     *@param outputKeys The keys (names and order) in the output.
     */
    public void compileMultipleModelInputs (char inputSeparator,
					    char outputSeparator,
					    String [] outputKeys)
    {
	InputStream input = null;
	Reader reader = null;
	Writer writer = null;
	CSVProcessor syntheticPopulation = null;

        try {
            createINS2014Map ();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create INS2014 map. Status code: ", e);
        }

	File outputPath = new File (getDataLocator().getModelInputPath ());
	if (! outputPath.exists ()) {
	    outputPath.mkdirs ();
	}
	File [] inputFiles = getDataLocator().getSyntheticPopulationExports ();
	try {
	    if (inputFiles != null) {
		for (File inputFile : inputFiles) {
		    String extension = StringUtils.substringAfter (inputFile.getPath (), ".");
		    File outputFile = new File (getDataLocator().getModelInputFileName ("population.tsv." + extension));
		    System.out.println ("input: " + inputFile.getCanonicalPath ());
		    System.out.println ("output: " + outputFile.getCanonicalPath ());
		    this.createModelInput (inputSeparator,
					   outputSeparator,
					   outputKeys,
					   inputFile,
					   outputFile);
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }

    /**
     * Translate model data from a raw export format emitted from the RTI synthetic model.
     * Add insurance and other data.
     * Generate an output file suitable for input to the RTI CRC model.
     *@param inputSeparator The separator character used in the input.
     *@param outputSeparator The separator characgter used in the output.
     *@param outputKeys The keys (names and order) in the output.
     *@param inputFile An input file to convert.
     *@param outputFile The output file to create.
     */
    private void createModelInput (char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys,
				   File inputFile,
				   File outputFile)
    {
	InputStream input = null;
	Reader reader = null;
	Writer writer = null;
	CSVProcessor syntheticPopulation = null;

	try {
	    String inputFileName = inputFile.getCanonicalPath ();
	    String outputFileName = outputFile.getCanonicalPath ();
	    input = new BufferedInputStream (new FileInputStream (inputFileName));
	    reader = new BufferedReader (new InputStreamReader (input));
	    writer = new BufferedWriter (new FileWriter (outputFileName));
	    Processor processor = new SynthPopAnnotationProcessor (outputKeys, this.INS2014Map);
	    syntheticPopulation = new CSVProcessor (reader,
						    inputSeparator,
						    processor,
						    writer,
						    outputSeparator);
	    syntheticPopulation.parse ();	    
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

    /**
     * Analyze a series of model output files applying a series of operators.
     *@param polygonFileName The polygon shapefile to apply.
     */
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
    
    private void createINS2014Map() throws IOException {
        InputStream input = new BufferedInputStream (new FileInputStream (
            "c:/dev-NC/crcsim/common/util/src/main/resources/data/INS2014_NC_072116.csv"));
        Reader reader = new BufferedReader (new InputStreamReader (input));
        this.INS2014Map = new CSVToINS2014HashMap(reader, ',').getMap();
        /* DEBUG
        for (String key:this.INS2014Map.keySet()) {
            System.out.println(key + "," + this.INS2014Map.get(key));
        }
        */
    }
}

/**
 *
 * Create a HashMap from a CSV file. Each key is a string concatenated from the first six
 * columns for the corresponding row: integer values for sex, ageCat, raceCat, HISP,
 * houdeholdIncomeCat_NEW, and MARRIED, respectively. The associated value is from the
 * seventh "increase" column for that row: a probability such that 0 <= increase <= 1.
 *
 */
class CSVToINS2014HashMap {

  private CSVReader _input;
  private HashMap<String, String> map;

  public CSVToINS2014HashMap (Reader input, char inputSeparator) throws IOException {
      this._input = new CSVReader (input, inputSeparator);
      this.map = new HashMap<String, String> ();

      String key = null;
      this._input.readNext (); // Skip over the header row.
      for (String [] line = this._input.readNext (); line != null;
        line = this._input.readNext ()) {
          key = line[0] + line[1] + line[2] + line[3] + line[4] + line[5];
          map.put(key, line[6]);
      }
  }

  public HashMap<String, String> getMap() {
      return this.map;
  }
}

