package org.renci.epi.util.stats;

import java.util.EnumMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.renci.epi.util.CountyIntercepts;
import org.renci.epi.util.Geography;
import org.renci.epi.util.GeographyFactory;

/**
 *
 * Encapsulates the calculation of the probability that an agent will be compliant with screening.
 * Incorporates geographic and insurance information.
 * Uses betas appropriate for the insurance category supplied.
 *
 */
public class ComplianceModel {

    private static Log logger = LogFactory.getLog (ComplianceModel.class); 

    // Default value to use for compliance probability.
    // SAC 2016/05/31 private static final double defaultComplianceProbability = 0.20;
    // 1-(1-p)^(n/10) to reflect the 6 year probability of screening (where n=4, p=.25 for Oregon)
    // - rmt on 05/31/16:
    private static final double defaultComplianceProbability = 0.1087;
    

    // A map of insurance category to beta values.
    private BetaMap betas;

    /* Default ctor */
    public ComplianceModel() {}

    /**
     * Create a new compliance model object, initializing appropriately for
     * this model.
     * @param distanceFilename The name of the distance file.
     */
    public ComplianceModel (String complianceFilename, String distanceFilename) {
        GeographyFactory.setComplianceAndDistanceFilenames(complianceFilename, distanceFilename);
	this.setBetas (BetaMapFactory.getCompliance ());
    }

    public ComplianceModel (String distanceFilename) {
        GeographyFactory.setDistanceFilename(distanceFilename);
	this.setBetas (BetaMapFactory.getCompliance ());
    }

    /**
     * Set the beta values to use for this model.
     * @param betas The beta values to use when executing this model.
     */
    protected void setBetas (BetaMap betas) {
	this.betas = betas;
    }

    /**
     * Get geography appropriate for the compliance statistical model.
     * @return Returns a county intercept lookup table appropriate for the compliance model.
     */
    protected Geography getGeography () {
	return GeographyFactory.getCompliance ();
    } 

    /**
     *
     * Calculate the probability that a specific agent will be compliant with screening.
     * 
     * @param person_sex_male     True if the agent is male.
     * @param person_race_black   True if the agents race is black.
     * @param person_race_other   True if the agents race is other.
     * @param person_zipcode      The agents zipcode.
     * @param person_stcotrbg     The state-county code for the agent.
     * @param insure_private      True if the agent has private insurance.
     * @param insure_medicaid     True if the agent has medicaid.
     * @param insure_medicare     True if the agent has medicare.
     * @param insure_dual         True if the agent has dual insurance.
     * @param insure_none         True if the agent has no insurance.
     * @param insure_private_orig True if the agent originally had private insurance.
     * @param insure_none_orig    True if the agent originally had no insurance.
     *
     * @return Returns a double - the probability the agent will be compliant with screening.   
     *
     */
	 //* Arguments of getProbability may need to be modified for the OR model. However, not certain what arguments are being passed to this function.
    public final double getProbabilityOfCompliance (boolean person_sex_male,
		                                    boolean person_urban_geography,
						    boolean person_race_black,
						    boolean person_race_hispanic,
						    boolean person_race_other,
						    boolean person_married,
						    boolean person_SEHP,
						    String person_zipcode,
						    String person_stcotrbg,
						    boolean insure_private,
						    boolean insure_medicaid,
						    boolean insure_medicare,
						    boolean insure_dual,
						    boolean insure_none,
                                                    boolean insure_private_orig,
                                                    boolean insure_none_orig)
    {
	double result = defaultComplianceProbability;
	
	// Determine the agent's insurance category.
	InsuranceCategory insuranceCategory = getInsuranceCategory (insure_private,
								    insure_medicaid,
								    insure_medicare,
								    insure_dual,
								    insure_none,
                                                                    insure_private_orig,
                                                                    insure_none_orig);

	if (insuranceCategory != InsuranceCategory.UNINSURED) { // Return the default compliance probability for the uninsured.
	    
	    // Determine person's distance to a colonoscopy facility.
	    double distance = getGeography().getDistanceToNearestEndoscopyFacilityByZipCode (person_zipcode);
	    
	    // Determine distance range.
		boolean distance_00_05 = distance >= 0 && distance < 5; //*Added for OR
	    //*boolean distance_05_10 = distance >=  5 && distance < 10;
	    //*boolean distance_10_15 = distance >= 10 && distance < 15;
	    //*boolean distance_15_20 = distance >= 15 && distance < 20;
	    //*boolean distance_20_25 = distance >= 20 && distance < 25;
	    //*boolean distance_gt_25 = distance >= 25;
	    
	    // Get the appropriate betas based on the agent's insurance category.
	    Betas betas = this.betas.getBetas (insuranceCategory);

	    // Look up county intercepts.
	    CountyIntercepts countyIntercepts = getGeography().getCountyInterceptsByStcotrbg (person_stcotrbg);

	    // Get insurance betas based on county intercepts.
	    double insuranceBeta = getInsuranceBeta (insuranceCategory, countyIntercepts);

	    // Execute the statistical model.
		//*The result function modified for the OR model 
	    result = 
		betas.compliance_intercept
		+ (1 - (person_sex_male   ? 1 : 0)) * betas.sex_female
		+ (person_urban_geography ? 1 : 0) * betas.geography_urban
		+ (person_race_black ? 1 : 0)  * betas.race_black
		+ (person_race_hispanic ? 1 : 0) * betas. race_hispanic
		+ (person_race_other ? 1 : 0)  * betas.race_other
		+ (distance_00_05 ? 1 : 0)*betas.dist_to_endo_fac_0_5
		+ insuranceBeta;

	    result = Math.exp (result) / ( 1 + Math.exp (result));

	    // Logging.
	    if (logger.isDebugEnabled ()) {
		StringBuffer buffer = new StringBuffer ().
		    append ("sex_male=").    append (person_sex_male).append ("\n").
    		    append ("urban_Geography="). append(person_urban_geography).append("\n").
		    append (" race_black="). append (person_race_black).append ("\n").
		    append (" race_hispanic="). append(person_race_hispanic).append("\n").
		    append (" race_other="). append (person_race_other).append ("\n").
		    append (" zipcode=").    append (person_zipcode).append ("\n").
		    append (" stcotrbg=").   append (person_stcotrbg).append ("\n").
		    
		    append (" insurance_category="). append (insuranceCategory).append ("\n").

		    append (" beta_compliance_intercept="). append (betas.compliance_intercept).append ("\n").
		    append ("beta_urban_geography="). append(betas.geography_urban).append("\n").
		    append (" beta_sex_female=").           append (betas.sex_female).append ("\n").
		    append (" beta_race_black=").           append (betas.race_black).append ("\n").
			append (" beta_race_hispanic=").        append (betas.race_hispanic).append ("\n").
		    append (" beta_race_other=").           append (betas.race_other).append ("\n").
		    //*append (" beta_year_turned_50=").       append (betas.year_turned_50).append ("\n").
		    //*append (" beta_married=").              append (betas.married).append ("\n").
		    //*append (" beta_SEHP=").                 append (betas.SEHP).append ("\n").
			append ("beta_distance_00_05="). append (betas.dist_to_endo_fac_0_5).append("\n").
		    //*append (" beta_distance_05_10=").  append (betas.distance_05_10).append ("\n").
		    //*append (" beta_distance_10_15=").  append (betas.distance_10_15).append ("\n").
		    //*append (" beta_distance_15_20=").  append (betas.distance_15_20).append ("\n").
		    //*append (" beta_distance_20_25=").  append (betas.distance_20_25).append ("\n").
		    //*append (" beta_distance_gt_25=").  append (betas.distance_gt_25).append ("\n").
			
		    append (" distance_00_05=").  append (distance_00_05).append("\n").
		    //*append (" distance_05_10=").  append (distance_05_10).append ("\n").
		    //*append (" distance_10_15=").  append (distance_10_15).append ("\n").
		    //*append (" distance_15_20=").  append (distance_15_20).append ("\n").
		    //*append (" distance_20_25=").  append (distance_20_25).append ("\n").
		    //*append (" distance_gt_25=").  append (distance_gt_25).append ("\n").
		    
		    //*append (" married="). append (person_married).append ("\n").
		    //*append (" SEHP="). append (person_SEHP).append ("\n").
		    append (" insurance_beta="). append (insuranceBeta).append ("\n").
		    
		    append (" result="). append (result);
		
		logger.debug (buffer.toString ());
	    }
	}
	return result;
    }

    /**
     * Some tests may have a probability that is a function of the colonoscopy probability of compliance.
     * We can adjust these here.
     * @param probability The ordinary probability of compliance.
     * @param test The name of the test to adjust for.
     */
// Replaced with Rachel Townsley's version of this method for Oregon 2016/05/31 SAC
/*
    public double getTestAdjustedComplianceProbability (double probability, String test) {
	double result = probability;
	if (test.equals ("FOBT")) {
	    // 1-(1-p)^(1/6)
	    result = 1 - Math.pow (1 - probability, 1.0 / 6.0); 
	    logger.debug ("-------------------> " + result);
	}
	return result;
    }
*/
    public double getTestAdjustedComplianceProbability (double probability, String test) {
	double result = probability;
	if (test.equals ("FOBT")) {
	   // 1-(1-p)^(1/n)
	   result = 1 - Math.pow (1 - probability, 1.0 / 4.0); 
	   logger.debug ("-------------------> " + result);
	} else {                               // Added by rmt on 05/31/16, modified 2016/06/02 SAC
	// 1-(1-p)^(10/n)
	result = 1-Math.pow(1- probability, 10.0 / 4.0);
	logger.debug ("-------------------> " + result);
	}
	return result;
    }

    /**
     * Determine the insurance category to use based on agent attributes.
     * NOTE: If the agent used to have private insurance or no insurance and now has
     * Medicare, we expect his or her future behavior to be similar to that of someone
     * who has private insurance, at least with respect to compliance (SAC 2014/03/14).
     * future 
     * @param insure_private      True if the agent has private insurance.
     * @param insure_medicaid     True if the agent has medicaid.
     * @param insure_medicare     True if the agent has medicare.
     * @param insure_none         True if the agent has no insurance.
     * @param insure_private_orig True if the agent originally had private insurance.
     * @param insure_none_-orig   True if the agent originally had no insurance.
     * @return Returns the agent's insurance category.
     */
    private InsuranceCategory getInsuranceCategory (boolean insure_private,
                                                    boolean insure_medicaid,
                                                    boolean insure_medicare,
                                                    boolean insure_dual,
                                                    boolean insure_none,
                                                    boolean insure_private_orig,
                                                    boolean insure_none_orig)
    {
        InsuranceCategory result = InsuranceCategory.UNINSURED;
        if (insure_private) {
            result = InsuranceCategory.PRIVATE;
        } else if (insure_dual) {
            result = InsuranceCategory.DUAL;
        } else if (insure_medicaid) {
            result = InsuranceCategory.MEDICAID;
        } else if (insure_medicare) {
            if (insure_private_orig || insure_none_orig) { // See NOTE above
                result = InsuranceCategory.PRIVATE;
            } else {
                result = InsuranceCategory.MEDICARE;
            }
        }
        return result;
    }

    /**
     * Determine the insurance category to use based on agent attributes.
     * @param insure_private    True if the agent has private insurance.
     * @param insure_medicaid   True if the agent has medicaid.
     * @param insure_medicare   True if the agent has medicare.
     * @param insure_none       True if the agent has no insurance.     
     * @return Returns the agents insurance category.
     */
    private InsuranceCategory getInsuranceCategory (boolean insure_private,
						    boolean insure_medicaid,
						    boolean insure_medicare,
						    boolean insure_dual,
						    boolean insure_none)
    {
	InsuranceCategory result = InsuranceCategory.UNINSURED;
	if (insure_private) {
	    result = InsuranceCategory.PRIVATE;
	} else if (insure_dual) {
	    result = InsuranceCategory.DUAL;
	} else if (insure_medicaid) {
	    result = InsuranceCategory.MEDICAID;
	} else if (insure_medicare) {
	    result = InsuranceCategory.MEDICARE;
	}
	return result;
    }

    /**
     * Get the appropriate betas to use given the agent's insurance category.
     * This is calculated using the county intercepts table.
     * @param insuranceCategory The agents insurance category.
     * @param countyIntercepts A lookup table able to produce betas for various caregories based on location.
     * @return Returns a beta for an agents insurance category driven by their geographic location.
     */
    private double getInsuranceBeta (InsuranceCategory insuranceCategory, CountyIntercepts countyIntercepts) {
	double result = 0.0;

	switch (insuranceCategory) {

	case MEDICARE:
	    result = countyIntercepts.getMedicareOnly ();
	    break;

	case MEDICAID:
	    result = countyIntercepts.getMedicaidOnly ();
	    break;

	case DUAL:
	    result = countyIntercepts.getDual ();
	    break;

	case PRIVATE:
	    result = countyIntercepts.getBCBS ();
	    break;

	default:
	    break;
	}

	return result;
    }
}
