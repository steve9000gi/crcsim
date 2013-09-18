package org.renci.epi.util;

/**
 * Initialize geography objects for both statistical models at the outset.
 *   The resultant lookup tables are used in calculations that happen frequently and need to be fast.
 *
 */
public class GeographyFactory {
    
    // Compliance model geography
    private static Geography compliance = new Geography (Geography.COMPLIANCE);
    
    // Modality model geography
    private static Geography modality = new Geography (Geography.MODALITY);

    /**
     * Get the geography for the compliance model.
     */
    public static Geography getCompliance () {
	return compliance;
    }

    /**
     * Get the geography for the modality model.
     */
    public static Geography getModality () {
	return modality;
    }
}
