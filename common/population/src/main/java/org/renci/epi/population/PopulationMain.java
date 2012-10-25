package org.renci.epi.population;

import java.io.FilenameFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Run the population processor.
 */ 
public class PopulationMain {

    private static ApplicationContext appCtx;

    {
        appCtx = new ClassPathXmlApplicationContext ( new String [] {
                "/spring/population-context.xml"
	    });
    }
    
    public static final ApplicationContext getApplicationContext() {
        return appCtx;
    }

    public void compileModelInput () {
	char inputSeparator   = '|';
	char outputSeparator  = '\t';
	String [] outputKeys  = {
	    "sex", "race", "SEXC", "INCOME", "FRISK", "VITALE", "AGE_G2",
	    "AGE_G3", "AGE_G4", "FLU", "BLACK", "HISP", "OTHER", "FORMER",
	    "NEVER", "ALONE", "MW", "SO", "WE", "USUAL", "NOINS",
	    "PRIVA", "EDU", "id", "LAT", "LON"
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

