package al6utils;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;


/**
 * <p>Construct a data set that can be arranged as rows of fields and export it
 * to a delimited text file.</p>
 *
 * <p>The number of data rows held in memory before writing to the output file
 * is controlled by the calling code. The calling code could construct all rows
 * in memory before writing to the output file. It could also write each row to
 * the output file immediately after constructing it, maintaining only one row in
 * memory at all times. Scenarios falling between these two extremes are also
 * possible.</p>
 *
 * <p>Data is flushed to the output file after every write operation. As a
 * result, if the application crashes before the {@code close} method is called,
 * the output file should contain all the rows that had been written prior to
 * the crash.</p>
 * 
 * <p>Example of usage:</p>
 *
 * <pre>
 *   DelimitedFileExporter exporter = null;
 *
 *   try {
 *       exporter = new DelimitedFileExporter("myfile.txt", false, "\t", null);
 *   }
 *   catch (IOException e) {
 *       // handle the exception
 *   }
 *
 *   // tell the exporter about all the fields in the data
 *   exporter.addField("ID");
 *   exporter.addField("Name");
 *   exporter.addField("Score");
 *
 *   // write a header row containing the field names to the file
 *   exporter.writeHeaderRow();
 *
 *   // add a data row and write it to the file
 *   exporter.startDataRow();
 *   exporter.addDataValue("ID", 1);
 *   exporter.addDataValue("Name", "Charlie Delta");
 *   exporter.addDataValue("Score", 0.267);
 *   exporter.writeDataRows();
 *
 *   // add several data rows and write them all to the file in a group
 *   double[] scores = { 0.150, 0.212, 0.284, 0.313 };
 *   for (int i = 0; i < scores.length; i++) {
 *       exporter.startDataRow();
 *       exporter.addDataValue("ID", 2);
 *       exporter.addDataValue("Name", "Victor Tango");
 *       exporter.addDataValue("Score", scores[i]);
 *   }
 *   exporter.writeDataRows();
 *
 *   // close the file
 *   exporter.close();
 * </pre>
 */

public class DelimitedFileExporter {
    /**
     * Creates a {@code DelimitedFileExporter} that writes to the given file.
     *
     * @param filename the delimited text file to create
     * @param append @{code true} if the data should be appended to the given file
     * @param fieldDelimiter the text to be written between consecutive fields
     * @param lineTerminator the text to be written at the end of each row. Set
     *        to {@code null} to use the default end-of-line text for the
     *        current platform.
     * @throws IOException if an I/O error occurs while opening the file
     */
    public DelimitedFileExporter(String filename, boolean append, String fieldDelimiter, String lineTerminator)
        throws IOException
    {
        out = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
        fieldNames = new ArrayList<String>();
        rows = new ArrayList<Map<String,String>>();
        this.fieldDelimiter = fieldDelimiter;
        this.lineTerminator = lineTerminator;
    }


    //--------------------------------------------------------------------------
    // Methods for defining the fields.
    //

    /**
     * Adds a field to this exporter object. The field will be written to all
     * subsequent rows of the output file. Fields are written to the output file
     * in the order in which they are added.
     *
     * @param fieldName name of the field to add. The name will appear in the
     *        output file's header row (if any) and is the name by which the
     *        field is accessed in calls to {@code addDataValue}.
     */
    public void addField(String fieldName) {
        fieldNames.add(fieldName);
    }

    /**
     * Removes a field from this exporter object. The field will not be written
     * to subsequent rows in the output file.
     *
     * @param fieldName name of the field to remove
     */
    public void removeField(String fieldName) {
        boolean foundField = fieldNames.remove(fieldName);
        if (!foundField) {
            throw new IllegalArgumentException("Undefined field name: " + fieldName);
        }
    }


    //--------------------------------------------------------------------------
    // Methods for adding data values.
    //

    /**
     * Creates a new, empty data row and makes it active. Subsequent calls to
     * {@code addDataValue} will add data items to this new row.
     */
    public void startDataRow() {
        currentRow = new HashMap<String,String>();
        rows.add(currentRow);
    }

    /**
     * Adds a {@code String} data value to a field in the active row. If the
     * field already has a value, that value will be overwritten.
     *
     * @param fieldName the field for which a data value is provided
     * @param value the data value
     */
    public void addDataValue(String fieldName, String value) {
        if (fieldNames.contains(fieldName)) {
            if (currentRow == null) {
                throw new RuntimeException("Must call startDataRow() before calling addDataValue()");
            }
            currentRow.put(fieldName, value);
        }
        else {
            throw new IllegalArgumentException("Undefined field name: " + fieldName);
        }
    }

    /**
     * Adds an {@code int} data value to a field in the active row. If the field
     * already has a value, that value will be overwritten.
     *
     * @param fieldName the field for which a data value is provided
     * @param value the data value
     */
    public void addDataValue(String fieldName, int value) {
        addDataValue(fieldName, String.valueOf(value));
    }

    /**
     * Adds a {@code double} data value to a field in the active row. If the
     * field already has a value, that value will be overwritten.
     *
     * @param fieldName the field for which a data value is provided
     * @param value the data value
     */
    public void addDataValue(String fieldName, double value) {
        addDataValue(fieldName, String.valueOf(value));
    }

    /**
     * Adds a {@code boolean} data value to a field in the active row. The value
     * written to the file is either "true" or "false" (without quotes). If the
     * field already has a value, that value will be overwritten.
     *
     * @param fieldName the field for which a data value is provided
     * @param value the data value
     */
    public void addDataValue(String fieldName, boolean value) {
        addDataValue(fieldName, String.valueOf(value));
    }

    /**
     * Adds an {@code Object} data value to a field in the active row. The value
     * written to the file is taken from the {@code Object}'s {@code toString}
     * method. If the field already has a value, that value will be overwritten.
     *
     * @param fieldName the field for which a data value is provided
     * @param value the data value
     */
    public void addDataValue(String fieldName, Object value) {
        addDataValue(fieldName, value.toString());
    }


    //--------------------------------------------------------------------------
    // Methods for writing to the file.
    //

    /**
     * Writes a line containing the field names to the output file. Consecutive
     * field names are delimited by this object's field delimiter, and the line is
     * terminated by this object's line terminator. The fields appear in the same
     * order in which they were defined using {@code addField}.
     */
    public void writeHeaderRow()
    {
        Iterator<String> fieldIterator = fieldNames.iterator();
        while (fieldIterator.hasNext()) {
            String fieldName = fieldIterator.next();
            out.print(fieldName);

            if (fieldIterator.hasNext()) {
                out.print(fieldDelimiter);
            }
        }
        writeLineTerminator();
        out.flush();
    }

    /**
     * For each data row that has been defined since the last call to this
     * method, writes a line containing the row's data values to the output
     * file. Consecutive data values in a row are delimited by this object's
     * field delimiter, and each line is terminated by this object's line
     * terminator.
     */
    public void writeDataRows()
    {
        while (rows.size() > 0) {
            Map<String,String> row = (HashMap<String,String>)rows.get(0);
            Iterator<String> fieldIterator = fieldNames.iterator();
            while (fieldIterator.hasNext()) {
                String fieldName = fieldIterator.next();
                String dataItem = row.get(fieldName);

                if (dataItem != null) {
                    out.print(dataItem);
                }

                if (fieldIterator.hasNext()) {
                    out.print(fieldDelimiter);
                }
            }

            writeLineTerminator();
            rows.remove(0);
        }
        out.flush();
    }

    /**
     * Closes the output file.
     */
    public void close()
    {
        out.close();
    }

 
    //--------------------------------------------------------------------------
    // Internal methods.
    //

    /**
     * Writes the appropriate line terminator to the output file. The appropriate
     * line terminator is the one specified in the constructor, or, if unspecified,
     * the default line terminator for the current platform.
     *
     * @throws IOException if an I/O occurs while writing to the file
     */
    protected void writeLineTerminator()
    {
        if (lineTerminator == null) {
            out.println();
        }
        else {
            out.print(lineTerminator);
        }
    }


    //--------------------------------------------------------------------------
    // Member variables.
    //

    private PrintWriter out;
    private List<String> fieldNames;
    private List<Map<String,String>> rows;
    private Map<String,String> currentRow;
    private String fieldDelimiter;
    private String lineTerminator;
}
