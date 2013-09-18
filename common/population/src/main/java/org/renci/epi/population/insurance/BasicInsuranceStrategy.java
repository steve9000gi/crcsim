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

    /**
     * Is this person insured via Medicaid only?
     */
    public boolean hasMedicaidOnly (Person person, InsuranceStatus status) {
	return insuranceStatusTable.hasMedicaidOnly (person, status);
    }
    
    /**
     * Is this person insured via Medicare only?
     */
    public boolean hasMedicareOnly (Person person, InsuranceStatus status) {
	return insuranceStatusTable.hasMedicareOnly (person, status);
    }

    /**
     * Is this person insured via dual means?
     */
    public boolean hasDual (Person person, InsuranceStatus status) {
	return insuranceStatusTable.hasDual (person, status);        
    }
}
