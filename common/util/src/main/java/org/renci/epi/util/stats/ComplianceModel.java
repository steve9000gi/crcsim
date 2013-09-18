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
    private static final double defaultComplianceProbability = 0.20;

    // A map of insurance category to beta values.
    private BetaMap betas;

    /**
     * Create a new compliance model object, initializing appropriately for this model.
     */
    public ComplianceModel () {
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
     * @param person_sex_male   True if the agent is male.
     * @param person_race_black True if the agents race is black.
     * @param person_race_other True if the agents race is other.
     * @param person_zipcode    The agents zipcode.
     * @param person_stcotrbg   The state-county code for the agent.
     * @param insure_private    True if the agent has private insurance.
     * @param insure_medicaid   True if the agent has medicaid.
     * @param insure_medicare   True if the agent has medicare.
     * @param insure_dual       True if the agent has dual insurance.
     * @param insure_none       True if the agent has no insurance.
     *
     * @return Returns a double - the probability the agent will be compliant with screening.   
     *
     */
    public final double getProbabilityOfCompliance (boolean person_sex_male,
						    boolean person_race_black,
						    boolean person_race_other,
						    String person_zipcode,
						    String person_stcotrbg,
						    boolean person_married,
						    boolean person_SEHP,
						    boolean insure_private,
						    boolean insure_medicaid,
						    boolean insure_medicare,
						    boolean insure_dual,
						    boolean insure_none)
    {
	double result = defaultComplianceProbability;
	
	// Determine the agent's insurance category.
	InsuranceCategory insuranceCategory = getInsuranceCategory (insure_private,
								    insure_medicaid,
								    insure_medicare,
								    insure_dual,
								    insure_none);

	if (insuranceCategory != InsuranceCategory.UNINSURED) { // Return the default compliance probability for the uninsured.
	    
	    // Determine person's distance to a colonoscopy facility.
	    double distance = getGeography().getDistanceToNearestEndoscopyFacilityByZipCode (person_zipcode);
	    
	    // Determine distance range.
	    boolean distance_05_10 = distance >=  5 && distance < 10;
	    boolean distance_10_15 = distance >= 10 && distance < 15;
	    boolean distance_15_20 = distance >= 15 && distance < 20;
	    boolean distance_20_25 = distance >= 20 && distance < 25;
	    boolean distance_gt_25 = distance >= 25;
	    
	    // Get the appropriate betas based on the agent's insurance category.
	    Betas betas = this.betas.getBetas (insuranceCategory);

	    // Look up county intercepts.
	    CountyIntercepts countyIntercepts = getGeography().getCountyInterceptsByStcotrbg (person_stcotrbg);

	    // Get insurance betas based on county intercepts.
	    double insuranceBeta = getInsuranceBeta (insuranceCategory, countyIntercepts);

	    // Execute the statistical model.
	    result = 
		betas.compliance_intercept
		+ (1 - (person_sex_male   ? 1 : 0)) * betas.sex_female
		+      (person_race_black ? 1 : 0)  * betas.race_black
		+      (person_race_other ? 1 : 0)  * betas.race_other
		+ (distance_05_10 ? 1 : 0) * betas.distance_05_10
		+ (distance_10_15 ? 1 : 0) * betas.distance_10_15
		+ (distance_15_20 ? 1 : 0) * betas.distance_15_20
		+ (distance_20_25 ? 1 : 0) * betas.distance_20_25
		+ (distance_gt_25 ? 1 : 0) * betas.distance_gt_25
		+ betas.year_turned_50
		+      (person_married ? 1 : 0) * betas.married
		+      (person_SEHP ? 1 : 0)    * betas.SEHP
		+ insuranceBeta;

	    // Logging.
	    if (logger.isDebugEnabled ()) {
		StringBuffer buffer = new StringBuffer ().
		    append ("sex_male=").    append (person_sex_male).
		    append (" race_black="). append (person_race_black).
		    append (" race_other="). append (person_race_other).
		    append (" zipcode=").    append (person_zipcode).
		    append (" stcotrbg=").   append (person_stcotrbg).
		    
		    append (" beta_compliance_intercept="). append (betas.compliance_intercept).
		    append (" beta_sex_female=").           append (betas.sex_female).
		    append (" beta_race_black=").           append (betas.race_black).
		    append (" beta_race_other=").           append (betas.race_other).
		    append (" year_turned_50=").            append (betas.year_turned_50).

		    append (" beta_distance_05_10=").  append (betas.distance_05_10).
		    append (" beta_distance_10_15=").  append (betas.distance_10_15).
		    append (" beta_distance_15_20=").  append (betas.distance_15_20).
		    append (" beta_distance_20_25=").  append (betas.distance_20_25).
		    append (" beta_distance_gt_25=").  append (betas.distance_gt_25).
		    
		    append (" insurance_beta="). append (insuranceBeta).
		    
		    append (" result="). append (result);
		
		logger.debug (buffer.toString ());
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
