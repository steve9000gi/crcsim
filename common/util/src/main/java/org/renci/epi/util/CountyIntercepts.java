package org.renci.epi.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CountyIntercepts {

    private String name;
    private String FIPS;
    private double medicareOnly;
    private double medicaidOnly;
    private double dual;
    private double BCBS;

    public CountyIntercepts (String name,
			     String FIPS,
			     double medicareOnly,
			     double medicaidOnly,
			     double dual,
			     double BCBS)
    {
	this.name = name;
	this.FIPS = FIPS;
	this.medicareOnly = medicareOnly;
	this.medicaidOnly = medicaidOnly;
	this.dual = dual;
	this.BCBS = BCBS;
    }
    public String getName () {
	return name;
    }
    public String getFIPS () {
	return FIPS;
    }
    public double getMedicareOnly () {
	return medicareOnly;
    }
    public double getMedicaidOnly () {
	return medicaidOnly;
    }
    public double getDual () {
	return dual;
    }
    public double getBCBS () {
	return BCBS;
    }    
}
