package org.renci.epi.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

public class JavaScriptCalibrate implements Calibrate {
    
    private static Log logger = LogFactory.getLog (JavaScriptCalibrate.class); 
    private JavaScriptEngine engine = new JavaScriptEngine (true); // one engine.
    
    public JavaScriptCalibrate (String fileName) {
	assert engine.getScriptText (fileName) != null : "No such calibration script found: " + fileName;
	engine.invoke (engine.getScriptText (fileName), null);
	onInitExperiment ();
    }
    public void onInitExperiment () {
	invoke ("calibrate.onInitExperiment ();", "logger", logger);
    }
    public void onInitRun (Object root) {
	invoke ("calibrate.onInitRun (root);", "root", root, "logger", logger);
    }
    public void onRunComplete (Object root) {
	invoke ("calibrate.onRunComplete (root);", "root", root, "logger", logger);
    }
    public void onIterationComplete () {
        invoke ("calibrate.onIterationComplete ();", "logger", logger);
    }
    public Map<String, Object> getOptimalProperties () {
	Map<String, Object> optimal = new Hashtable<String, Object> ();
	invoke ("calibrate.getOptimalParameters (optimal);", "optimal", optimal, "logger", logger);
	return optimal;
    }
    private Map<String, Object> getArgs (Object... args) {
	Map<String, Object> parameters = new Hashtable<String, Object> ();
	assert (args.length % 2) == 0 : "Args length must be even";
	for (Object x : args) {
	    System.out.println ("arg: " + x);
	}
        for (int c = 0; c < args.length; c += 2) {
	    String name = (String)args [c];
	    Object arg = args [c + 1];
	    parameters.put (name, arg);
	}
	return parameters;
    }
    private void invoke (String text, Object... args) {
	engine.invoke (text, getArgs (args));
    }
}
