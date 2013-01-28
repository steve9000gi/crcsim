package org.renci.epi.util;

import java.util.ArrayList;
import java.util.Map;
import java.lang.reflect.Field;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

class Util {

    private static Log logger = LogFactory.getLog (Util.class); 

    /**
     * Get the value of a field from an object given an instance and the field name.
     *@param object An instance of some class
     *@param fieldName The field to get a value for.
     */
    public static final Object getFieldValue (Object object, String fieldName) {
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
     * Set the value of a field from an object given an instance, the field name and a value.
     *@param object An instance of some class.
     *@param fieldName Name of the field to set a value for
     *@param value The string value to use.
     */
    public static final void setFieldValue (Object object, String fieldName, String value) {
	try {
	    Class c = object.getClass ();
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
    }

    /**
     * Set field values
     * @param instance an object instance to configure.
     * @param args arguments formatted as [name value]+
     */
    public static void setFieldValues (Object instance, String [] args, Map<String, String> skip) {
	for (int c = 0; c < args.length; c++) {
	    String fieldName = args [c];
	    if (++c < args.length && !skip.containsKey (fieldName)) {
		String fieldValue = args [c];
		Util.setFieldValue (instance, fieldName, fieldValue);
		logger.debug ("  " + fieldName + "=" + fieldValue);
	    }
	}
    }

    /**
     * Get values for the specified fields.
     *@param object An instance of some class.
     *@param fieldNames The names of fields to get values for
     *@return Returns a list of field values.
     */
    public static final String [] getFieldValues (Object object, String [] fieldNames) {
	ArrayList<String> values = new ArrayList<String> (fieldNames.length);
	for (int c = 0; c < fieldNames.length; c++) {
	    String fieldName = fieldNames [c];
	    Object value = Util.getFieldValue (object, fieldName);
	    values.add (String.valueOf (value));
	}
	return (String [])values.toArray (new String [values.size ()]);
    }
}