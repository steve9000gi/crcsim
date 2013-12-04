package org.renci.epi.util;

/**
 * Initialize geography objects for both statistical models at the outset.
 * The resultant lookup tables are used in calculations that happen frequently
 * and need to be fast.
 *
 */
public class GeographyFactory {
    
    /**
     * Assign distance filename.
     * NOTE: This method *must* be called prior to calling getCompliance() and
     * getModality() in order to instantiate compliance and modality.
     */
    public static void setDistanceFilename(String distanceFilename) {
        if (compliance == null) {
            compliance 
                = new Geography (Geography.COMPLIANCE, distanceFilename);
        }
        if (modality == null) {
            modality = new Geography (Geography.MODALITY, distanceFilename);
        }
    }

    // Compliance model geography
    private static Geography compliance;

    // Modality model geography
    private static Geography modality;

    /**
     * Get the geography for the compliance model.
     */
    public static Geography getCompliance () {
        assert compliance != null;
	return compliance;
    }

    /**
     * Get the geography for the modality model.
     */
    public static Geography getModality () {
        assert modality != null;
	return modality;
    }
}
