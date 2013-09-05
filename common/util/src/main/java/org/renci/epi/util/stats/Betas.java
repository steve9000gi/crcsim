package org.renci.epi.util.stats;

import java.util.EnumMap;

/**
 * Abstracts the set of possible betas that will vary from agent to agent in the compliance model.
 * Also provides a static lookup function for determining appropriate betas based on insurance category.
 */
class Betas {

    double compliance_intercept;
    double sex_female;
    double race_black;
    double race_other;

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
	
	this.distance_05_10 = distance_05_10;
	this.distance_10_15 = distance_10_15;
	this.distance_15_20 = distance_15_20;
	this.distance_20_25 = distance_20_25;
	this.distance_gt_25 = distance_gt_25;
    }

    /**
     * Lookup appropriate betas for a given insurance category.
     * @param insuranceCategory An insurance category.
     * @return Returns the betas for this insurance category.
     */
    final static Betas getBetas (InsuranceCategory insuranceCategory) {
	return insuranceBetas.get (insuranceCategory);
    }

    // Map of insurance categories to betas.
    private static EnumMap<InsuranceCategory, Betas> insuranceBetas = 
	new EnumMap<InsuranceCategory, Betas> (InsuranceCategory.class);

    static {

	/**
	 *
	 * TODO: consider reading these from an input file.
	 *
	 * These are derived from the stats model spreadsheet at:
	 * https://app.box.com/files/0/f/876129186/1/f_9239777459

                  total        medicare medicaid dual    bcbs 
       intercept: -0.9698	-1.0385	-1.1883	-0.9355	-0.6034
       female   : 0.7635	0.9736	0.6807	0.6899	0.6581
       black    : -0.1479	-0.183	-0.1307	-0.1144	0
       other    : -0.09868	-0.1923	-0.05079	-0.03621	0


                    Total       Medicaire      Medicaid      Dual      BCBS
       >5-10 Miles  -0.02845	-0.08839	-0.07334     0.04391	-0.04829
       >10-15 Miles -0.01567	-0.00458	-0.0749	     0.02924	-0.06208
       >15-20 Miles -0.07815	-0.01507	-0.2275	     0.01985	-0.1097
       >20-25 Miles 0.1292	0.2816	        0.1259	     0.02571	 0.01381
       25+ Miles    -0.2473	-0.3594	        -0.3291	     -0.2046	-0.1219

       *
       *
       */
	insuranceBetas.put (InsuranceCategory.MEDICARE,
			    new Betas (-1.0385,   // intercept
				       0.9736,    // female
				       -0.183,    // black
				       -0.1923,   // other
				       -0.08839,  // 5-10
				       -0.00458,  // 10-15
				       -0.01507,  // 15-20
				       0.2816,    // 20-25
				       -0.3594)); // >25
	
	insuranceBetas.put (InsuranceCategory.MEDICAID,
			    new Betas (-1.1883,   // intercept
				       0.6807,    // female
				       -0.1307,   // black
				       -0.05079,  // other
				       -0.07334,  // 5-10
				       -0.0749,   // 10-15
				       -0.2275,   // 15-20
				       0.1259,    // 20-25
				       -0.3291)); // >25
	
	insuranceBetas.put (InsuranceCategory.DUAL,
			    new Betas (-0.9355,   // intercept
				       -0.1307,   // female
				       -0.1144,   // black
				       -0.03621,  // other
				       0.04391,   // 5-10
				       0.02924,   // 10-15
				       0.01985,   // 15-20
				       0.02571,   // 20-25
				       -0.2046)); // >25
	
	insuranceBetas.put (InsuranceCategory.PRIVATE,
			    new Betas (-0.6034,   // intercept
				       -0.05079,  // female
				       0.0,       // black
				       0.0,       // other
				       -0.04829,  // 5-10
				       -0.06208,  // 10-15
				       -0.1097,   // 15-20
				       0.01381,   // 20-25
				       -0.1219)); // >25
    }
}