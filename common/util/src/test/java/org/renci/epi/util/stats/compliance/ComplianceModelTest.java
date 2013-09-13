package org.renci.epi.util.stats.compliance;

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

    public ComplianceModelTest () {
	BasicConfigurator.configure ();
	Logger.getRootLogger().setLevel (Level.DEBUG);
    }

    @Test
    public void testComplianceModel () {
	ComplianceModel statsModel = new ComplianceModel ();
	Geography geography = new Geography ();
	double complianceProbability =
	    statsModel.getProbabilityOfCompliance (false,          // male
						   true,           // race black
						   false,          // race other
						   "27603",        // zipcode
						   "371830529002", // stcotrbg
						   false,          // insure_private
						   false,          // insure_medicaid
						   true,           // insure_medicare
						   false,          // insure_none
						   geography);
    }

}

