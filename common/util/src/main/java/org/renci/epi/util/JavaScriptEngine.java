package org.renci.epi.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import javax.script.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for running JavaScript.
 */
public class JavaScriptEngine {

    private static Log logger = LogFactory.getLog (JavaScriptEngine.class); 
    private ScriptEngine theEngine = null;
    public JavaScriptEngine () {
	this (false);
    }
    public JavaScriptEngine (boolean oneEngine) {
	if (oneEngine) {
	    theEngine = createEngine ();
	}
    }
    private ScriptEngine createEngine () {
	ScriptEngineManager manager = new ScriptEngineManager ();
	return manager.getEngineByName ("JavaScript");
    }
    private ScriptEngine getEngine () {
	return theEngine == null ? createEngine () : theEngine;
    }

    /**
     * Invoke a script given parameters.
     */
    public void invoke (String script, Map<String, Object> parameters) {
	try {
	    ScriptEngine engine = getEngine ();

	    if (parameters != null) {
		parameters.put ("newline", "\n");
		Set<Map.Entry<String,Object>> entries = parameters.entrySet ();
		for (Map.Entry<String, Object> entry : entries) {
		    engine.put (entry.getKey (), entry.getValue ());
		}
	    }
	    //logger.debug ("=====> " + script);
	    engine.eval (script);
	} catch (Exception e) {
	    throw new RuntimeException ("Exception caught", e);
	}
    }

    /**
     * Invoke a script given parameters and a set of libraries to load as context.
     */
    public void invoke (String script, Map<String, Object> parameters, String [] libs) {
	try {
	    StringBuffer buffer = new StringBuffer ();
	    for (String resourceName : libs) {
		buffer.append (getScriptText (resourceName));
	    }
	    buffer.append (script);
	    invoke (buffer.toString (), parameters);
	} catch (Exception e) {
	    throw new RuntimeException ("Exception caught", e);
	}
    }

    /**
     * Get script text, first by trying it as a file.
     * If that doesn't work, load it as a classpath resource.
     */
    String getScriptText (String resourceName) {
	String text = null;
	try {
	    logger.debug ("getting file: " + resourceName);
	    File file = new File (resourceName);
	    if (file.exists ()) {
		text = FileUtils.readFileToString (file);
	    } else {
		URL input = getClass().getResource (resourceName);
		logger.debug ("getting resource: " + resourceName + " " + input);
		if (input != null) {
		    
		    BufferedReader reader = new BufferedReader (new InputStreamReader (input.openStream ()));
		    StringBuilder buffer = new StringBuilder ();
		    for (String line = reader.readLine (); line != null; line = reader.readLine ()) {
			buffer.append (line);
			buffer.append ("\n");
		    }
		    text = buffer.toString ();
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException ("IOException caught", e);
	}
	return text;
    }


}
