package org.renci.epi.util.stats;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Scanner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Models a map of beta values indexed by insurance category.
 */
public class BetaMap {

    private static Log logger = LogFactory.getLog (BetaMap.class); 

    // Constants for data files used by known models.
    static final String COMPLIANCE_BETAS = "data/compliance_model_betas.txt";
    static final String MODALITY_BETAS = "data/modality_model_betas.txt";

    // Map of insurance categories to betas.
    private EnumMap<InsuranceCategory, Betas> betaMap = 
	new EnumMap<InsuranceCategory, Betas> (InsuranceCategory.class);

    /**
     * Construct a beta map given a resource file to read data from.
     * @param resourceName The file or classpath resource containing the beta data.
     */
    BetaMap (String resourceName) {
	initialize (betaMap, resourceName);
    }

    /**
     * Lookup appropriate compliance betas for a given insurance category.
     * @param insuranceCategory An insurance category.
     * @return Returns the betas for this insurance category.
     */
    final Betas getBetas (InsuranceCategory insuranceCategory) {
	return betaMap.get (insuranceCategory);
    }

    /**
     * Read a beta data file.
     * @param betaMap Map of insurance cateogry to beta values.
     * @param resourcePath Path to the dtat file.
     */
    static void initialize (EnumMap<InsuranceCategory, Betas> betaMap, String resourcePath) {
	try {
	    URL url = Resources.getResource (resourcePath);
	    String text = Resources.toString (url, Charsets.UTF_8);
	    Scanner scanner = new Scanner (text);
	    while (scanner.hasNextLine ()) {
		String line = scanner.nextLine ();
		logger.debug ("<<= " + line);
		if ( !line.startsWith ("#") && (line.trim().length () > 0) ) {
		    String [] part = line.split (",");
		    int c = 0;
		    logger.debug ("line: " + line);
		    logger.debug ("------- Populating betas for insurance category: [" + part [c] + "]");
		    betaMap.put (InsuranceCategory.valueOf (part [c++]),
				 new Betas (Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c++]),
					    Double.valueOf (part [c])));
		}
	    }
	    scanner.close();	    
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }
}