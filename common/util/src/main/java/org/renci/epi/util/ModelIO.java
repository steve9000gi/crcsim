package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * Standard utilities for IO from the model.
 *
 */
public class ModelIO {

    private int BLOCK_SIZE = 2048;

    private static final String DEFAULT_OUTPUT = "out";
    private String _outputRoot = DEFAULT_OUTPUT;

    public void setOutputDir (String outputDir) {
	_outputRoot = outputDir;
	ensureOutputDir ();
    }

    private void ensureOutputDir () {
	File root = new File (_outputRoot);
	if (! root.isDirectory ()) {
	    root.mkdirs ();
	}
    }

    /**
     * Create a Writer object.
     */
    private BufferedWriter getModelOutputWriterInternal (String fileName, boolean append, boolean compress) 
	throws IOException
    {
	Writer writer = null;
	try {
	    if (compress) {
		writer = new OutputStreamWriter (new GZIPOutputStream (new FileOutputStream (fileName + ".gz", append)));
	    } else {
		writer = new FileWriter (fileName, append);
	    }
	} catch (IOException e) {
	    IOUtils.closeQuietly (writer);
	}
	return new BufferedWriter (writer, BLOCK_SIZE); //new FileWriter (fileName, append));
    }

    /**
     * Create a Writer object.
     */
    public BufferedWriter getModelOutputWriter (String fileName, boolean overwrite, boolean compress) {
	ensureOutputDir ();
	BufferedWriter writer = null;
	try {
	    fileName = StringUtils.join (new String [] { _outputRoot, fileName }, File.separator);
	    writer = getModelOutputWriterInternal (fileName, !overwrite, compress);
	    if (overwrite) {
		String text = StringUtils.join (new String [] {
			"description",
			"person",
			"age",
			"statechart",
			"new_state"
		    }, '\t');
		writer.write (text);
		writer.write ("\r\n");
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
	return writer;
    }

    /**
     * Close a writer.
     */
    public void closeWriter (Writer writer) {
	IOUtils.closeQuietly (writer);
    }

    /**
     * Output a row of data to the writer.
     */
    public void outputRow (Writer writer, String [] values, char separator, String newline) {
	try {
	    String row = StringUtils.join (values, separator);
	    writer.write (row);
	    writer.write (newline);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }

    /**
     *
     * Output a row of data read, via reflection, from an object.
     *
     */
    public void outputDataRow (Writer writer, String className, Object object, String [] fieldNames, char separator, String newline) {
	String [] values = this.getFieldValues (className, object, fieldNames);
	this.outputRow (writer, values, separator, newline);
    }

    /**
     *
     * Get the value of a field from an object given an instance and the field name.
     *
     */
    public Object getFieldValue (String className, Object object, String fieldName) {
	Object value = null;
	try {
	    Class c = object.getClass (); //Class.forName (className);
	    Field field = c.getField (fieldName);
	    value = field.get (object);
	} catch (Exception e) {
	    e.printStackTrace ();
	}
	return value;
    }

   /**
     *
     * Set the value of a field from an object given an instance, the field name and a value.
     *
     */
    public Object setFieldValue (Object object, String fieldName, String value) {
	try {
	    Class c = object.getClass (); //Class.forName (className);
	    Field field = c.getField (fieldName);
	    Object val = value;
	    Class type = field.getType ();
	    if (type.equals (double.class)) {
		val = Double.parseDouble (value);
	    } else if (type.equals (float.class)) {
		val = Float.parseFloat (value);
	    } else if (type.equals (int.class)) {
		val = Integer.parseInt (value);
	    } else if (type.equals (boolean.class)) {
		val = Boolean.parseBoolean (value);
	    }
	    field.set (object, val);
	} catch (Exception e) {
	    e.printStackTrace ();
	}
	return value;
    }

    /**
     *
     * Get values for the specified fields.
     *
     */
    public String [] getFieldValues (String className, Object object, String [] fieldNames) {
	ArrayList<String> values = new ArrayList<String> (fieldNames.length);
	for (int c = 0; c < fieldNames.length; c++) {
	    String fieldName = fieldNames [c];
	    Object value = this.getFieldValue (className, object, fieldName);
	    values.add (String.valueOf (value));
	}
	return (String [])values.toArray (new String [values.size ()]);
    }

}

