package org.renci.epi.util.stats;

import java.util.List;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.renci.epi.util.Geography;

public class ComplianceModelTest {
	
    private static Log logger = LogFactory.getLog (ComplianceModelTest.class); 

    @Test
    public void testComplianceModel () {

/* TEMP OR
	ComplianceModel model = new ComplianceModel ("compliance_county_intercepts.csv",
                                                     "nearest_dist_simulation_OR.csv");
	List<Person> people = Person.scan ("data/stats_model_test_OR.txt");
	for (Person person : people) {
	    logger.debug ("-----> Testing person: " + person);
	    double xbeta =
		model.getProbabilityOfCompliance (person.sex_male,           // male
                                                  true,                      // dummy urban_geography changed args SAC 
						  person.is_black,           // race black
                                                  false,                     // dummy race_hispanic changed args SAC 
						  person.is_other,           // race other
						  person.is_married,         // married
						  person.SEHP,               // SEHP
						  person.zipcode,            // zipcode
						  person.stcotrbg,           // stcotrbg
						  person.insurance_private,  // insure_private
						  person.insurance_medicaid, // insure_medicaid
						  person.insurance_medicare, // insure_medicare
						  person.insurance_dual,     // insure_dual
						  person.insurance_none,     // insure_none
                                                  person.insurance_private_orig,
                                                  person.insurance_none_orig);

	    logger.debug (xbeta + " == " + person.compliance_xbeta + "?");

	    // TEMP SAC OR assert Math.abs (xbeta - person.compliance_xbeta) < 0.00000001;

	    logger.debug ("FOBT adjusted probability: " + model.getTestAdjustedComplianceProbability (xbeta, "FOBT"));

	}
*/
    }
    
}

