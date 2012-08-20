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
     * Select the insurance strategy.
     */
    public InsuranceStatus getInsuranceStatus (Person person) {
	return insuranceStatusTable.getInsuranceStatus (person);
    }

    /**
     * Is this person uninsured?
     */
    public boolean hasNoInsurance (Person person, InsuranceStatus status) {
	return insuranceStatusTable.hasNoInsurance (person, status);
    }
    
    /**
     * Is this person privately insured?
     */
    public boolean hasPrivateInsurance (Person person, InsuranceStatus status) {
	return insuranceStatusTable.hasPrivateInsurance (person, status);
    }
}
