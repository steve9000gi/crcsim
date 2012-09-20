package org.renci.epi.util;

import java.io.IOException;
import java.io.Reader;
import com.csvreader.CsvReader;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;


import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class CsvValueReader {

    private static Log logger = LogFactory.getLog (CsvValueReader.class); 

    private static final int BLOCK = 4096;
    private BufferedReader _reader = null;
    private Map<String,Integer> _headers = new HashMap<String,Integer> ();
    private String [] _current = null;
    private char _delimiter = '\t';

    public CsvValueReader (Reader reader, char delimiter) throws IOException {
	_reader = new BufferedReader (reader, BLOCK);
	_delimiter = delimiter;

	String [] header = readNext ();
	for (int c = 0; c < header.length; c++) {
	    _headers.put (header [c], new Integer (c));
	}
    }
    private String [] readNext () throws IOException  {
	return StringUtils.split (_reader.readLine (), _delimiter);
    }

    public boolean readRecord () throws IOException  {
	_current = readNext ();
	return _current != null;
    }

    private String getValue (String header) throws IOException {
	return _current [ _headers.get (header).intValue () ];
    }

    public double getDouble (String header) throws IOException {
	return Double.parseDouble (this.getValue (header));
    }

    public int getInt (String header) throws IOException {
	return Integer.parseInt (this.getValue (header));
    }

    public boolean getBoolean (String header) throws IOException {
	return Boolean.parseBoolean (this.getValue (header));
    }
}


/*
public class CsvValueReader extends CsvReader {

    private static Log logger = LogFactory.getLog (CsvValueReader.class); 

    public CsvValueReader (Reader reader, char delimiter) throws IOException {
	super (reader, delimiter);
	this.readHeaders ();
    }

    private String getValue (String header) throws IOException {
	String value = this.get (header);
	logger.debug (header + "=" + value); 
	return value;
    }

    public double getDouble (String header) throws IOException {
	return Double.parseDouble (this.getValue (header));
    }

    public int getInt (String header) throws IOException {
	return Integer.parseInt (this.getValue (header));
    }

    public boolean getBoolean (String header) throws IOException {
	return Boolean.parseBoolean (this.getValue (header));
    }
}
 */


