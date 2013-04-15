package org.renci.epi.population;

import java.io.FilenameFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.apache.commons.logging.LogFactory; 
import org.apache.commons.logging.Log;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Run the population processor.
 */ 
public class PopulationMain {

    private static Log logger = LogFactory.getLog (PopulationMain.class); 

    private static ApplicationContext appCtx;

    public static final ApplicationContext getApplicationContext() {
	if (appCtx == null) {
	    appCtx = new ClassPathXmlApplicationContext ( new String [] {
		    "spring/population-context.xml"
		});
	}
        return appCtx;
    }

    public PopulationMain () {
    }

    public void compileModelInput () {
	char inputSeparator   = '|';
	char outputSeparator  = '\t';
	String [] outputKeys  = {
	    "sex", "race", "SEXC", "INCOME", "FRISK", "VITALE", "AGE_G2",
	    "AGE_G3", "AGE_G4", "FLU", "BLACK", "HISP", "OTHER", "FORMER",
	    "NEVER", "ALONE", "MW", "SO", "WE", "USUAL", "NOINS",
	    "PRIVA", "EDU", "id", "LAT", "LON", "stcotrbg"
	};

	PopulationService populationService =
	    (PopulationService)getApplicationContext().getBean ("populationService");

	populationService.compileMultipleModelInputs (inputSeparator,
						      outputSeparator,
						      outputKeys);
    }

    public static final void main (String [] args) {
        System.out.println ( "Starting..." );
	PopulationMain app = new PopulationMain ();
	app.compileModelInput ();
        System.out.println ( "loaded" );
    }
}

