package al6utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import java.lang.reflect.Field;

public class ModelIO {

    public BufferedWriter getModelOutputWriterAppend (String fileName, boolean append) 
	throws IOException
    {
	return new BufferedWriter (new FileWriter (fileName, append));
    }

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

    public void closeWriter (Writer writer) {
	IOUtils.closeQuietly (writer);
    }
    
    public void outputRow (Writer writer, String [] values, char separator, String newline) {
	try {
	    String row = StringUtils.join (values, separator);
	    writer.write (row);
	    writer.write (newline);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }

    public void outputDataRow (Writer writer, String className, Object object, String [] fieldNames, char separator, String newline) {
	String [] values = this.getFieldValues (className, object, fieldNames);
	this.outputRow (writer, values, separator, newline);
    }

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

    public String [] getFieldValues (String className, Object object, String [] fieldNames) {
	ArrayList<String> values = new ArrayList<String> (fieldNames.length);
	for (int c = 0; c < fieldNames.length; c++) {
	    String fieldName = fieldNames [c];
	    Object value = this.getFieldValue (className, object, fieldName);
	    values.add (String.valueOf (value));
	}
	return (String [])values.toArray (new String [values.size ()]);
    }

    /*
    public String getDataRow (String className, Object object, String [] fieldNames, String separator) {
	StringBuffer buffer = new StringBuffer (fieldNames.length * 20);
	for (int c = 0; c < fieldNames.length; c++) {
	    String fieldName = fieldNames [c];
	    Object value = this.getFieldValue (className, object, fieldName);
	    buffer.
		append (fieldName).
		append (separator).
		append (value);
	    if (c < fieldNames.length - 1) {
		buffer.append (separator);
	    }
	}
	return buffer.toString ();
    }
    */

}

