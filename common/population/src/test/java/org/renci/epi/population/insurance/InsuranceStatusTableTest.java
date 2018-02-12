package org.renci.epi.population.insurance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 

/**
 * Unit test for simple App.
 */
public class InsuranceStatusTableTest extends TestCase {

    private static Log logger = LogFactory.getLog (InsuranceStatusTableTest.class); 

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public InsuranceStatusTableTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( InsuranceStatusTableTest.class );
    }

    /**
     * 
     */
    public void testApp () {
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);
  
	InsuranceStrategy insuranceStrategy = new BasicInsuranceStrategy ();
	
	Person person = new Person ();
	person.random = 0.22;
	person.ageCat = 1;
	person.householdIncomeCat = 1;
	person.householdSizeCat = 1;
	person.raceCat = 1;
	person.sexCat = 1;

	double [] randoms = {
	    0.26904,
	    0.53384,
	    0.65005,
	    0.87648,
	    0.88551,
	    0.91224,
	    0.96073
	};
	for (int c = 0; c < randoms.length; c++) {
	    person.random = randoms [c] - 0.0001;
	    InsuranceStatus status = insuranceStrategy.getInsuranceStatus (person);
	    logger.info ("  category: " + status);
	    logger.info ("  noins: " + insuranceStrategy.hasNoInsurance (status));
	    logger.info ("  private: " + insuranceStrategy.hasPrivateInsurance (status));
	}

    }
}
