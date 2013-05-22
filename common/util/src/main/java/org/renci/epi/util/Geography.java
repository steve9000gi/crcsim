package org.renci.epi.util;

import java.util.Hashtable;

public class Geography {

    private Hashtable<String, Double> zipcode =
	new Hashtable<String, Double> ();

    private Hashtable<String, CountyIntercepts> county =
	new Hashtable<String, CountyIntercepts> ();

    /**
     *
     * Construct a new geography object and initialize maps.
     *
     */
    public Geography () {
	try {
	    String fileName = "nearest_dist_simulation.2.csv";
	    int unlimited = -1;
	    DelimitedFileImporter in = new DelimitedFileImporter (fileName, ",", unlimited);
	    while (in.hasMoreRows ()) {
		in.nextRow ();
		String zipcodeText = in.getString ("zip");
		Double distance = new Double (in.getDouble ("nearest_distance"));
		zipcode.put (zipcodeText, distance);
	    }
	} catch (Exception e) {
	    throw new RuntimeException (e);
	}
	try {
	    String fileName = "county_intercept_values.csv";
	    int unlimited = -1;
	    DelimitedFileImporter in = new DelimitedFileImporter (fileName, ",", unlimited);
	    while (in.hasMoreRows ()) {
		in.nextRow ();
		CountyIntercepts countyIntercepts =
		    new CountyIntercepts (in.getString ("county"),
					  in.getString ("FIPS"),
					  getDouble (in, "totalPubliclyInsured"), //in.getDouble ("totalPubliclyInsured"),
					  getDouble (in, "medicareOnly"), //in.getDouble ("medicareOnly"),
					  getDouble (in, "medicaidOnly"), //in.getDouble ("medicaidOnly"),
					  getDouble (in, "dual"), //in.getDouble ("dual"),
					  getDouble (in, "BCBS")); //in.getDouble ("BCBS"));
		county.put (countyIntercepts.getFIPS (), countyIntercepts);
	    }
	} catch (Exception e) {
	    throw new RuntimeException (e);
	}
    }
    private double getDouble (DelimitedFileImporter importer, String field) {
	String value = importer.getString (field);
	double result = -Double.MAX_VALUE;
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
