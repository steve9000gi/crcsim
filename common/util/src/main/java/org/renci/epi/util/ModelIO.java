package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import java.lang.reflect.Field;

/**
 *
 * Standard utilities for IO from the model.
 *
 */
public class ModelIO {

    /**
     * Create a Writer object.
     */
    public BufferedWriter getModelOutputWriterAppend (String fileName, boolean append) 
	throws IOException
    {
	return new BufferedWriter (new FileWriter (fileName, append));
    }

    /**
     * Create a Writer object.
     */
    public BufferedWriter getModelOutputWriter (String fileName, boolean overwrite) {
	BufferedWriter writer = null;
	try {
	    writer = getModelOutputWriterAppend (fileName, !overwrite);
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
	    Class c = Class.forName (className);
	    Field field = c.getField (fieldName);
	    value = field.get (object);
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

