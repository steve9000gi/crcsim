package org.renci.epi.population.insurance;

/**
 * Implement an insurance strategy.
 */
public class BasicInsuranceStrategy implements InsuranceStrategy {

    /**
     * The table of insurance status statistical distributions.
     */
    private InsuranceStatusTable insuranceStatusTable = new InsuranceStatusTable ();

    /**
     * Determine a person's insurance status.
     */
    public InsuranceStatus getInsuranceStatus (Person person) {
	return insuranceStatusTable.getInsuranceStatus (person);
    }

    public boolean hasNoInsurance (InsuranceStatus status) {
	return insuranceStatusTable.hasNoInsurance (status);
    }
    
    public boolean hasPrivateInsurance (InsuranceStatus status) {
	return insuranceStatusTable.hasPrivateInsurance (status);
    }

    public boolean hasMedicaidOnly (InsuranceStatus status) {
	return insuranceStatusTable.hasMedicaidOnly (status);
    }
    
    public boolean hasMedicareOnly (InsuranceStatus status) {
	return insuranceStatusTable.hasMedicareOnly (status);
    }

    public boolean hasDual (InsuranceStatus status) {
	return insuranceStatusTable.hasDual (status);        
    }

/* debug
    public InsuranceStatus getInsStatus() {
        return insuranceStatusTable.insStatus;
    }

    public double getInsRandom() {
        return insuranceStatusTable.insRandom;
    }

    public String getPersonKey() {
        return insuranceStatusTable.personKey;
    }
*/
}
