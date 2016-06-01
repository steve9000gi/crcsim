package org.renci.epi.util.stats;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Scanner;

/**
 * Abstracts the set of possible betas that will vary from agent to agent in the compliance model.
 * Also provides a static lookup function for determining appropriate betas based on insurance category.
 */
class Betas {
    //*Modified for the OR Model to only have the betas that we need
    double compliance_intercept;
    double sex_female;
    double geography_urban; 
    double race_black;
    double race_hispanic; 
    double race_other;
    double dist_to_endo_fac_0_5; 

    /**
     * Private constructor - only this class will create betas.
     */
    //*The arguments of Betas modified for the OR Model
    Betas (double intercept,
	   double female,
	   double urbanGeo, 
	   double black,
	   double hispanic, 
	   double other,
	   double distToFac)
    {
	//*The below modified for OR Model 
	this.compliance_intercept = intercept;
	this.sex_female = female;
	this.geography_urban = urbanGeo; 
	this.race_black = black;
	this.race_hispanic = hispanic; 
	this.race_other = other;
	this.dist_to_endo_fac_0_5 = distToFac; 
	
	System.out.println ("===> intercept:" + intercept + ",female:" + female + ",geography_urban:" + urbanGeo +
				",black:" + black + ",hispanic:" + hispanic +",other:" + other + 
				",Dist_to_endo_fac_0_5:" + distToFac + "\n");
				
    } 

    /**
     * Lookup appropriate compliance betas for a given insurance category.
     * @param insuranceCategory An insurance category.
     * @return Returns the betas for this insurance category.
    final static Betas getComplianceBetas (InsuranceCategory insuranceCategory) {
	Betas.initialize ();
	assert complianceBetas.containsKey (insuranceCategory) : "Unknown insuranceCategory: " + insuranceCategory;
	return complianceBetas.get (insuranceCategory);
    }
    final static Betas getModalityBetas (InsuranceCategory insuranceCategory) {
	Betas.initialize ();
	if (! modalityBetas.containsKey (insuranceCategory)) {
	    throw new RuntimeException ("Unknown insuranceCategory: " + insuranceCategory);
	}
	return modalityBetas.get (insuranceCategory);
    }

    // Map of insurance categories to betas for compliance model.
    private static EnumMap<InsuranceCategory, Betas> complianceBetas = 
	new EnumMap<InsuranceCategory, Betas> (InsuranceCategory.class);

    // Map of insurance categories to betas for modality model.
    private static EnumMap<InsuranceCategory, Betas> modalityBetas = 
	new EnumMap<InsuranceCategory, Betas> (InsuranceCategory.class);

    static void initialize () {
	if (modalityBetas.size () > 0) {
	    return;
	}
	initialize (complianceBetas, "data/compliance_model_betas_OR.txt");
	initialize (modalityBetas, "data/modality_model_betas_OR.txt");
    }

    static void initialize (EnumMap<InsuranceCategory, Betas> betaMap, String resourcePath) {
	try {
	    
	    URL url = Resources.getResource (resourcePath);
	    String text = Resources.toString (url, Charsets.UTF_8);

	    Scanner scanner = new Scanner (text);
	    while (scanner.hasNextLine ()) {
		String line = scanner.nextLine ();
		System.out.println ("<<= " + line);
		if ( !line.startsWith ("#") && (line.length () > 0) ) {
		    String [] part = line.split (",");
		    int c = 0;
		    System.out.println ("line: " + line);
		    System.out.println ("------- Populating betas for insurance category: [" + part [c] + "]");
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
     */
}
