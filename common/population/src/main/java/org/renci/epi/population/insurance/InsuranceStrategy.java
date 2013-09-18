package org.renci.epi.population.insurance;

public interface InsuranceStrategy {
   /**
     * Select the insurance strategy.
     */
    public InsuranceStatus getInsuranceStatus (Person person);

    /**
     * Is this person uninsured?
     */
    public boolean hasNoInsurance (Person person, InsuranceStatus status);
    
    /**
     * Is this person privately insured?
     */
    public boolean hasPrivateInsurance (Person person, InsuranceStatus status);

    /**
     * Is this person insured via medicaid only?
     */
    public boolean hasMedicaidOnly (Person person, InsuranceStatus status);
    
    /**
     * Is this person insured via Medicare only?
     */
    public boolean hasMedicareOnly (Person person, InsuranceStatus status);

    /**
     * Is this person insured via dual means?
     */
    public boolean hasDual (Person person, InsuranceStatus status);
}
