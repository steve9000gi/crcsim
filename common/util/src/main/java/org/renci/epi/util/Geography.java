package org.renci.epi.util;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

public class Geography {

    private static Log logger = LogFactory.getLog (Geography.class); 

    private Hashtable<String, Double> zipcode =
	new Hashtable<String, Double> ();

    private Hashtable<String, CountyIntercepts> county =
	new Hashtable<String, CountyIntercepts> ();

    private static final String DATA = "data";
    public static final double UNKNOWN_DOUBLE = -Double.MAX_VALUE;

    private Reader getResourceReader (String resourceName) {	
	Reader result = getDataReader (resourceName);
	if (result == null) {
	    InputStream input = getClass().getResourceAsStream ("/" + DATA + "/" + resourceName);
	    if (input != null) {
		result = new InputStreamReader (new BufferedInputStream (input));
		logger.info ("Got stream for resource: " + resourceName);
	    }
	}
	if (result == null) {
	    throw new RuntimeException ("Unable to find resource: " + resourceName);
	}
	return result;
    }

    private Reader getDataReader (String resourceName) {
	Reader result = null;
	try {
	    result = new BufferedReader (new FileReader (DATA + File.separator + resourceName));
	} catch (IOException e) {
	    // throw new RuntimeException (e);
	}
	return result;
    }

    private DelimitedFileImporter getImporter (String resourceName) throws IOException {
	Reader reader = getResourceReader (resourceName);
	int unlimited = -1;
	// constructor closes reader.
	return new DelimitedFileImporter (resourceName, reader, ",", unlimited);
    }

    static final String COMPLIANCE = "compliance_county_intercepts.csv";
    static final String MODALITY = "modality_county_intercepts.csv";

    /**
     *
     * Construct a new geography object and initialize maps.
     *
     */
    Geography (String resourcePath) {
        try {
            DelimitedFileImporter in = getImporter ("nearest_dist_simulation.csv");
            while (in.hasMoreRows ()) {
                in.nextRow ();
                String zipcodeText = in.getString ("zip");
                Double distance = new Double (in.getDouble ("nearest_distance"));
                zipcode.put (zipcodeText, distance);
            }
            in = getImporter (resourcePath);
            while (in.hasMoreRows ()) {
                in.nextRow ();
                CountyIntercepts countyIntercepts =
                    new CountyIntercepts (in.getString ("county"),
                                          in.getString ("FIPS"),
                                          getDouble (in, "medicareOnly"),
                                          getDouble (in, "medicaidOnly"),
                                          getDouble (in, "dual"),
                                          getDouble (in, "BCBS"));
                county.put (countyIntercepts.getFIPS (), countyIntercepts);
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    /**
     *
     * Construct a new geography object and initialize maps, also passing in as
     * a parameter the name of the distance file.
     *
     */
    Geography (String complianceFilename, String distanceFilename) {
	try {
	    DelimitedFileImporter in = getImporter (distanceFilename);
	    while (in.hasMoreRows ()) {
		in.nextRow ();
		String zipcodeText = in.getString ("zip");
		Double distance
                    = new Double (in.getDouble ("nearest_distance"));
		zipcode.put (zipcodeText, distance);
	    }
	    in = getImporter (complianceFilename);
	    while (in.hasMoreRows ()) {
		in.nextRow ();
		CountyIntercepts countyIntercepts =
		    new CountyIntercepts (in.getString ("county"),
					  in.getString ("FIPS"),
					  getDouble (in, "medicareOnly"),
					  getDouble (in, "medicaidOnly"),
					  getDouble (in, "dual"),
					  getDouble (in, "BCBS"));
		county.put (countyIntercepts.getFIPS (), countyIntercepts);
	    }
	} catch (Exception e) {
	    throw new RuntimeException (e);
	}
    }

    /**
     * Parse a double, inserting a special value if the double's value is not present.
     */
    private double getDouble (DelimitedFileImporter importer, String field) {
	String value = importer.getString (field);
	double result = UNKNOWN_DOUBLE;
	if (! value.equals (".") && value.length () > 0 ) {
	    result = Double.parseDouble (value);
	}
	return result;
    }

    /**
     * Get distance to endoscopy facility by zip code.
     *@param zipcode
     *@return Returns distance to nearest endoscopy facility.
     */
    public double getDistanceToNearestEndoscopyFacilityByZipCode (String zipCode) {
	Double value = zipcode.get (zipCode);
	if (value == null) {
	    throw new RuntimeException ("No endoscopy facility distance known for zipcode: " + zipCode);
	}
	return value.doubleValue ();
    }

    /**
     * Get county intercepts by stcotrbg.
     *@param stcotrbg
     *@return Returns a county intercpets object.
     */
    public CountyIntercepts getCountyInterceptsByStcotrbg (String stcotrbg) {
	String FIPS = String.valueOf (Integer.parseInt (stcotrbg.substring (2, 5)));

	CountyIntercepts value = county.get (FIPS);
	if (value == null) {
	    throw new RuntimeException ("No county intercpets found for stcotrbg: " + stcotrbg);
	}
	return value;
    }
}
