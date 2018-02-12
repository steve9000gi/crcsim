package org.renci.epi.util;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for generating iterations. 
 * 
 * An iteration is a layer of model parameterization in which we draw from 
 * random distributions for a set of parameters. 
 *
 */
public class IterationUtil {

    private static Log logger = LogFactory.getLog (IterationUtil.class); 

    public static List<String> generateIterations (String fileName) {
	List<String> iterations = new ArrayList<String> ();
	try {
	    File file = new File (fileName);
	    logger.debug ("Iteration config matrix: " + fileName);
	    JavaScriptEngine engine = new JavaScriptEngine ();
	    
	    if (engine.getScriptText (fileName) != null) {

		Map<String, Object> parameters = new HashMap <String, Object> ();
		parameters.put ("iterations", iterations);
		String [] libs = new String [] {
		    "/script/librandom.js",
		    fileName
		};
		String script = "randomUtils.generateIterations (numIterations, iterationParameters, iterations);";
		engine.invoke (script, parameters, libs);

	    } else {
		iterations.add ("");
	    }

	} catch (Exception e) {
	    throw new RuntimeException (e);
	}
	return iterations;
    }

}
