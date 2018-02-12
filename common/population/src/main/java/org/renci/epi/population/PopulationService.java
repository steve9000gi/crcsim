package org.renci.epi.population;

import org.renci.epi.population.dao.PopulationDAO;
import org.renci.epi.util.DataLocator;

public interface PopulationService {
    public void setPopulationDAO (PopulationDAO populationDao);
    public DataLocator getDataLocator ();

    public void compileModelInput (char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys);
    public void compileMultipleModelInputs (char inputSeparator,
					   char outputSeparator,
					   String [] outputKeys);
    
    public Object getPopulation (String [] query);

    public void geocodePopulation (String polygonFileName);
}
