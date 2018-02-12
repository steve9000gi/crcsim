package org.renci.epi.population.dao;

import javax.sql.DataSource;

public interface PopulationDAO {

    public void setDataSource (DataSource dataSource);

    public Object getPopulation (String [] query);

}
