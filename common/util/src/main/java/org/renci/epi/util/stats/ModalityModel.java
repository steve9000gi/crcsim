package org.renci.epi.util.stats;

import org.renci.epi.util.Geography;
import org.renci.epi.util.GeographyFactory;

/**
 * Encapsulates characteristics of the modality model unique to this model.
 * Largely, it inherits functionality from the Compliance model.
 * It uses a different data file for beta values used in the calculation.
 */
public class ModalityModel extends ComplianceModel {
    
    /* Default ctor */
    public ModalityModel () {}

    /**
     * Create a new modality model object, initializing betas appropriately.
     */
    public ModalityModel (String complianceFilename, String distanceFilename) {
        GeographyFactory.setComplianceAndDistanceFilenames(complianceFilename, distanceFilename);
	this.setBetas (BetaMapFactory.getModality ());
    } 

    public ModalityModel (String distanceFilename) {
        GeographyFactory.setDistanceFilename(distanceFilename); 
	this.setBetas (BetaMapFactory.getModality ());
    } 

    /**
     * Get geography appropriate for the modality statistical model.
     * @return Returns a county intercept lookup table appropriate for the modality model.
     */
    protected Geography getGeography () {
	return GeographyFactory.getModality ();
    } 

    /**
     *
     * Calculate the probability that a specific agent will select colonoscopy as a screening modality.
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
    public final double getProbabilityOfColonoscopy (boolean person_sex_male,
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
						     boolean insure_none,
                                                     boolean insure_private_orig,
                                                     boolean insure_none_orig)
    {
	return this.getProbabilityOfCompliance (person_sex_male,
						person_race_black,
						person_race_other,
						person_zipcode,
						person_stcotrbg,
						person_married,
						person_SEHP,
						insure_private,
						insure_medicaid,
						insure_medicare,
						insure_dual,
						insure_none,
                                                insure_private_orig,
                                                insure_none_orig);
    }

}
