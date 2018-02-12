package org.renci.epi.util.stats;

/**
 * A factory for beta maps.
 * In addition to hiding implementation details, this allows us to do the file IO to load
 * the maps once. The betas are used in frequently executed calculations so we don't need to 
 * be doing the IO every time.
 */
class BetaMapFactory {

    // Compliane betas.
    static final BetaMap compliance = new BetaMap (BetaMap.COMPLIANCE_BETAS);
    
    // Modality betas.
    static final BetaMap modality = new BetaMap (BetaMap.MODALITY_BETAS);

    /**
     * Get compliance beta map
     * @return Returns compliance betas.
     */
    static final BetaMap getCompliance () {
	return compliance;
    }

    /**
     * Get modality beta map
     * @return Returns modality betas.
     */
    static final BetaMap getModality () {
	return modality;
    }
    
}