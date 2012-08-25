package org.renci.epi.population;

import org.renci.epi.population.dao.PopulationDAO;

public interface PopulationService {
    public void setPopulationDAO (PopulationDAO populationDao);

    public void compileModelInput (String inputFileName,
				   String outputFileName,
				   char inputSeparator,
				   char outputSeparator,
				   String [] outputKeys);
				   
    public Object getPopulation (String [] query);

    public void geocodePopulation (String modelFileDirectory,
				   String polygonsFile,
				   String outputFilePath); 
}
