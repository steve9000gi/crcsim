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

    private HashMap<String, String> urbanRuralMap; // SAC 2016/05/24
    private HashMap<String, String> INS2014Map;    // SAC 2016/06/10
    private HashMap<String, String> INS2015Map;    // SAC 2017/11/16

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
            createUrbanRuralMap ();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create urban/rural map. Status code: ", e);
        }

        String inPath = null;
        try {
            inPath = "g:/dev-OR/crcsim/common/util/src/main/resources/data/INS2014_Probs_All_OR.csv";
            createINSMap (inPath, "2014");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create INS 2014 map. Status code: ", e);
        }
        try {
            inPath = "g:/dev-OR/crcsim/common/util/src/main/resources/data/INS2015_NC-OR_041017.csv";
            createINSMap (inPath, "2015");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create INS 2015 map. Status code: ", e);
        }


	File outputPath = new File (getDataLocator().getModelInputPath ());
	if (! outputPath.exists ()) {
	    outputPath.mkdirs ();
	}
	File [] inputFiles = getDataLocator().getSyntheticPopulationExports ();
        System.out.println("PopulationServiceImpl.compileMultipleModelInputs: " + inputFiles.length
           + " input files");
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
	    Processor processor = new SynthPopAnnotationProcessor (outputKeys,
                                                                   this.urbanRuralMap,
                                                                   this.INS2014Map,
                                                                   this.INS2015Map);
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

    private void createUrbanRuralMap() throws IOException {
        InputStream input = new BufferedInputStream (new FileInputStream (
            "c:/dev/crcsim/common/util/src/main/resources/data/Zip_to_Urban_Rural_Frontier.csv"));
        Reader reader = new BufferedReader (new InputStreamReader (input));
        this.urbanRuralMap = new CSVToUrbanRuralHashMap(reader, ',').getMap();
        /* DEBUG SAC 
        for (String key:this.urbanRuralMap.keySet()) {
            System.out.println(key + ": " + this.urbanRuralMap.get(key));
        }
        */
    }

    private void createINSMap(String inputFilePath, String year)
      throws IOException {
        InputStream input = new BufferedInputStream (new FileInputStream (inputFilePath));
        Reader reader = new BufferedReader (new InputStreamReader (input));
        HashMap<String, String> map = new CSVToINSHashMap(reader, ',').getMap();
        if (year == "2014") {
          this.INS2014Map = map;
        } else if (year == "2015") {
          this.INS2015Map = map;
        } else {
          System.out.println("PopulationServiceImpl.createINSMap(...): Unknown year " + year);
        }
        /* DEBUG
        for (String key:map.keySet()) {
            System.out.println(key + "," + map.get(key));
        }
        */
    }
}

/**
 *
 * Create a HashMap from a CSV file in which the keys are zipcodes, and the values are
 * strings "Rural" or "Urban".
 *
 */
class CSVToUrbanRuralHashMap {

  private CSVReader _input;
  private HashMap<String, String> map;

  public CSVToUrbanRuralHashMap (Reader input, char inputSeparator) throws IOException {
      this._input = new CSVReader (input, inputSeparator);
      this.map = new HashMap<String, String> ();

      this._input.readNext (); // Skip over header row.
      for (String [] line = this._input.readNext (); line != null;
        line = this._input.readNext ()) {
	  map.put(line[0], line[4]);
      }
  }

  public HashMap<String, String> getMap() {
      return this.map;
  }
}

/**
 *
 * Create a HashMap of insurance probabilities from a CSV file. Each key is a string concatenated
 * from the first six columns for the corresponding row: integer values for sex, ageCat, raceCat,
 * HISP, householdIncomeCat_NEW, and MARRIED, respectively. The associated value is from the
 * seventh "increase" column for that row: a probability such that 0 <= increase <= 1.
 *
 */
class CSVToINSHashMap {

  private CSVReader _input;
  private HashMap<String, String> map;

  public CSVToINSHashMap (Reader input, char inputSeparator) throws IOException {
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

