package org.renci.epi.population.insurance;

public interface InsuranceStrategy {
   /**
     * Determine a person's insurance status.
     */
    public InsuranceStatus getInsuranceStatus (Person person);

    public boolean hasNoInsurance (InsuranceStatus status);
    public boolean hasPrivateInsurance (InsuranceStatus status);
    public boolean hasMedicaidOnly (InsuranceStatus status);
    public boolean hasMedicareOnly (InsuranceStatus status);
    public boolean hasDual (InsuranceStatus status);

/* debug
    public InsuranceStatus getInsStatus();
    public double getInsRandom();
    public String getPersonKey();
*/

}
