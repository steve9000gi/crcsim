package org.renci.epi.population.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import org.renci.epi.population.dao.PopulationDAO;

public class PopulationDAOImpl implements PopulationDAO {
    
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    public void setDataSource (DataSource dataSource) {
	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate (dataSource);
    }

    public Object getPopulation (String [] query) {
	int populationId = Integer.parseInt (query [0]);

	String queryText = "select * from people, pumsp, households POPULATION_ID=:populationId";

	SqlParameterSource namedParameters =
	    new MapSqlParameterSource ("populationId",
				       Integer.valueOf (populationId));
	

	return new Object ();

	/*
	return (Object)namedParameterJdbcTemplate.
	    queryForObject (queryText,
			    namedParameters,
			    new RowMapper () {
				public Object mapRow (ResultSet resultSet, int rowNum)
				    throws SQLException {
				    return new String ("");
				    // new Something (resultSet.getInt ("x"), ...);
				}
			    });
	*/
    }
    
}
