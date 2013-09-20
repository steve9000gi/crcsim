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

    double compliance_intercept;
    double sex_female;
    double race_black;
    double race_other;

    double year_turned_50;

    double married;
    double SEHP; // state employee health plan.

    double distance_05_10;
    double distance_10_15;
    double distance_15_20;
    double distance_20_25;
    double distance_gt_25;

    /**
     * Private constructor - only this class will create betas.
     */
    Betas (double intercept,
	   double female,
	   double black,
	   double other,
	   double year_turned_50,
	   
	   double distance_05_10,
	   double distance_10_15,
	   double distance_15_20,
	   double distance_20_25,
	   double distance_gt_25,

	   double married,
	   double SEHP)
    {
	this.compliance_intercept = intercept;
	this.sex_female = female;
	this.race_black = black;
	this.race_other = other;
	this.year_turned_50 = year_turned_50;

	this.distance_05_10 = distance_05_10;
	this.distance_10_15 = distance_10_15;
	this.distance_15_20 = distance_15_20;
	this.distance_20_25 = distance_20_25;
	this.distance_gt_25 = distance_gt_25;
	
	this.married = married;
	this.SEHP = SEHP;

	System.out.println ("===> intercept:" + intercept + ",female:" + female + ",black:" + black + ",other:" + other + ",year50:" + year_turned_50 + 
			    ",married:" + married + ",SEHP:" + SEHP + 
			    ",distance_5_10:" + distance_05_10 + ",distance_10_15:" + distance_10_15 + 
			    ",distance_15_20:" + distance_15_20 + ",distance_20_25:" + distance_20_25 +
			    ",distance_gt_25:" + distance_gt_25 + "\n");
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
	initialize (complianceBetas, "data/compliance_model_betas.txt");
	initialize (modalityBetas, "data/modality_model_betas.txt");
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