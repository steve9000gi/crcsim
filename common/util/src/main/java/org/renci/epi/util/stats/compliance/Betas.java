package org.renci.epi.util.stats.compliance;

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

    double distance_05_10;
    double distance_10_15;
    double distance_15_20;
    double distance_20_25;
    double distance_gt_25;

    /**
     * Private constructor - only this class will create betas.
     */
    private Betas (double intercept,
		   double female,
		   double black,
		   double other,
		   double year_turned_50,
		   double distance_05_10,
		   double distance_10_15,
		   double distance_15_20,
		   double distance_20_25,
		   double distance_gt_25)
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
	
	System.out.println ("===> " + intercept + "," + female + "," + black + "," + other + "," + year_turned_50 + "," + 
			    distance_05_10 + "," + distance_10_15 + "," + distance_15_20 + "," + distance_20_25 +
			    distance_gt_25 + "\n");
    }

    /**
     * Lookup appropriate betas for a given insurance category.
     * @param insuranceCategory An insurance category.
     * @return Returns the betas for this insurance category.
     */
    final static Betas getBetas (InsuranceCategory insuranceCategory) {
	Betas.initialize ();
	if (! insuranceBetas.containsKey (insuranceCategory)) {
	    throw new RuntimeException ("Unknown insuranceCategory: " + insuranceCategory);
	}
	return insuranceBetas.get (insuranceCategory);
    }

    // Map of insurance categories to betas.
    private static EnumMap<InsuranceCategory, Betas> insuranceBetas = 
	new EnumMap<InsuranceCategory, Betas> (InsuranceCategory.class);

    static void initialize () {

	if (insuranceBetas.size () > 0) {
	    return;
	}

	/**
	 *
	 */
	try {
	    URL url = Resources.getResource ("data/compliance_model_insurance_betas.txt");
	    String text = Resources.toString (url, Charsets.UTF_8);

	    Scanner scanner = new Scanner (text);
	    while (scanner.hasNextLine ()) {
		String line = scanner.nextLine ();
		System.out.println ("<<= " + line);
		if ( !line.startsWith ("#") && (line.length () > 0) ) {
		    String [] part = line.split (",");
		    int c = 0;
		    System.out.println ("line: " + line);
		    System.out.println ("------- Populating betas for insurance category: " + part [c]);
		    insuranceBetas.put (InsuranceCategory.valueOf (part [c++]),
					new Betas (Double.valueOf (part [c++]),
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