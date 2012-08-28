package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.apache.commons.io.IOUtils;

/**
 * <p>Import the contents of a delimited text file and provide iterative access
 * to the rows and name-based access to the fields. The current implementation
 * imports the entire file into memory, so this class may not be appropriate for
 * very large input files.</p>
 *
 * <p>The first line of the text file is treated as a field name row. All
 * subsequent lines are treated as data rows. The number of fields in the field
 * name row defines the number of fields imported from each data row. Extra
 * fields in the data rows are discarded, while missing fields in the data rows
 * are set to the empty string.</p>
 *
 * <p>This class does not directly support the ability to ignore rows that are
 * "commented out". Such rows are imported just like all others, however, so the
 * calling code could implement its own commenting feature by, for example,
 * ignoring all rows where the first field value begins with a "#"
 * character.</p>
 *
 * <p>The class operates by keeping track of the "active row". At any given
 * time, either no rows are active or exactly one row is active. Methods that
 * return data values apply to the active row only. Methods are provided to
 * change the active row.</p>
 *
 * <p>Example of usage:</p>
 *
 * <pre>
 *   DelimitedFileImporter d = null;
 *
 *   try {
 *     d = new DelimitedFileImporter("myfile.txt", "\t", -1);
 *   }
 *   catch (IOException e) {
 *     // handle the exception
 *   }
 *
 *   // traverse all the rows in order
 *   while (d.hasMoreRows()) {
 *     // make the next row active
 *     d.nextRow();
 *
 *     // get values from the active row
 *     String id = d.getString("ID");
 *     double score = d.getDouble("Score");
 *
 *     // ... do something with id and score ...
 *   }
 *
 *   // make a row with a particular attribute active
 *   d.findRow("ID", "A07");
 *
 *   // get values from the active row
 *   double score = d.getDouble("Score");
 *
 *   // ... do something with the score for the person with ID=A07 ...
 * </pre>
 */

public class DelimitedFileImporter {

    public static final int ALL = -1;

    /**
     * Creates a {@code DelimitedFileImporter} from the given file, using the
     * given field delimiter and reading only the given number of rows. Does not
     * set an active row.
     *
     * @param filename name of the delimited file to import
     * @param delimiter the field delimiter, interpreted as a regular expression
     * @param numRows the maximum number of rows to read from the file, or -1 to
     *        read all rows
     * @throws IOException if an I/O error occurs
     */
    public DelimitedFileImporter(String filename, String delimiter, int numRows)
        throws IOException
    {
        if (numRows < -1) {
            throw new IllegalArgumentException("Invalid number of rows specfied [" + numRows + "]");
        }

        BufferedReader in = null;
	try {
	    in = new BufferedReader(new FileReader(filename));

	    // Read the field names from the first line.
	    fieldNames = in.readLine().split(delimiter);
	    
	    // Read data lines until numRows or the end of the file is reached. For
	    // each line, construct a map of the field name to the data value. Add
	    // this map to a list of row maps.
	    
	    rows = new ArrayList<Map<String,String>>();
	    
	    while (rows.size() < numRows || numRows == -1) {
		String line = in.readLine();
		
		// Check for end of file.
		if (line == null) {
		    if (numRows == -1) {
			// The user asked for all rows, so simply break the loop.
			break;
		    }
		    else {
			// The user asked for a specific number of rows. This means
			// we have reached the end of the file before finding that
			// many rows.
			throw new IOException("Not enough lines in file " + filename + " ; expected=" + numRows + " found=" + rows.size());
		    }
		}
		
		// Create a map of field names to data values from the active row.
		// Excess fields are ignored. Missing fields are set to the empty
		// string.
		String[] fields = line.split(delimiter);
		Map<String,String> row = new HashMap<String,String>();
		for (int i = 0; i < fieldNames.length; i++) {
		    if (i < fields.length) {
			row.put(fieldNames[i], fields[i]);
		    }
		    else {
			row.put(fieldNames[i], "");
		    }
		}
		
		rows.add(row);
	    }
	} catch (IOException e) {
	    throw e;
	} finally {
	    IOUtils.closeQuietly (in);
	}

        // Go ahead and set up the row iterator to point to the beginning of the
        // list. This way the user will not need to call this method manually the
        // first time he iterates over the rows.
        reset();
    }


    //--------------------------------------------------------------------------
    // Methods for accessing field names.
    //

    /**
     * Returns the number of fields found in this object. The number of fields is
     * taken from the first row.
     *
     * @return the number of fields in this object
     */
    public int numFields() {
        return fieldNames.length;
    }

    /**
     * Returns all the field names found in this object.
     *
     * @return the field names found in this object
     */
    public String[] fieldNames() {
        String[] namesCopy = new String[fieldNames.length];
        System.arraycopy(fieldNames, 0, namesCopy, 0, fieldNames.length);
        return namesCopy;
    }

    /**
     * Returns {@code true} if this object contains a field with the given name.
     *
     * @return {@code true} if this object contains a field with the given name
     */
    public boolean hasField(String fieldName) {
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i].equals(fieldName)) {
                return true;
            }
        }

        return false;
    }


    //--------------------------------------------------------------------------
    // Row access methods.
    //

    /**
     * Returns the number of data rows read from this object.
     *
     * @return the number of data rows read from this object
     */
    public int numRows() {
        return rows.size();
    }

    /**
     * Returns {@code true} if this object contains additional rows beyond the
     * active row.
     *
     * @return {@code true} if this object contains additional rows beyond the
     *         active row.
     */
    public boolean hasMoreRows() {
        return rowIterator.hasNext();
    }

    /**
     * Resets the active row pointer so that no row is active.
     */
    public void reset() {
        rowIterator = rows.iterator();
        activeRow = null;
    }

    /**
     * Sets the active row pointer to the next row in this object, or the first
     * row if no row is currently active.
     */
    public void nextRow() {
        activeRow = rowIterator.next();
    }

    /**
     * Sets the active row pointer to the first row in this object where the
     * field with the given name has the given value.
     *
     * @param fieldName the name of the field in which to search
     * @param fieldValue the value to look for
     */
    public void findRow(String fieldName, String fieldValue) {
        if (!hasField(fieldName)) {
            throw new IllegalArgumentException("No field named " + fieldName + " exists");
        }

        reset();
        while (hasMoreRows()) {
            nextRow();
            if (getString(fieldName).equals(fieldValue)) {
                return;
            }
        }

        throw new NoSuchElementException("Cannot find value " + fieldValue + " in field " + fieldName);
    }


    //--------------------------------------------------------------------------
    // Methods for accessing a data value on the active row.
    //

    /**
     * Returns the value in the active row in the field with the given name,
     * expressed as a boolean. Valid boolean values are described in {@link
     * BooleanParser#parse(String)}.
     *
     * @param fieldName name of the field whose value should be returned
     */
    public boolean getBoolean(String fieldName) {
        return BooleanParser.parse(getString(fieldName));
    }

    /**
     * Returns the value in the active row in the field with the given name,
     * expressed as a double.
     *
     * @param fieldName name of the field whose value should be returned
     */
    public double getDouble(String fieldName) {
        return Double.parseDouble(getString(fieldName));
    }

    /**
     * Returns the value in the active row in the field with the given name,
     * expressed as an int.
     *
     * @param fieldName name of the field whose value should be returned
     */
    public int getInt(String fieldName) {
        return Integer.parseInt(getString(fieldName));
    }

    /**
     * Returns the value in the active row in the field with the given name,
     * expressed as a String.
     *
     * @param fieldName name of the field whose value should be returned
     */
    public String getString(String fieldName) {
        if (!hasField(fieldName)) {
            throw new IllegalArgumentException("No field named " + fieldName + " exists");
        }
        if (activeRow == null) {
            throw new IllegalStateException("No row is active");
        }

        return activeRow.get(fieldName);
    }


    //--------------------------------------------------------------------------
    // Member variables.
    //

    private String[] fieldNames;
    private List<Map<String,String>> rows;
    private Iterator<Map<String,String>> rowIterator;
    private Map<String,String> activeRow;
}
