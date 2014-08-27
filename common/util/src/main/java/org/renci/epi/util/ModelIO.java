package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;


/**
 *
 * Standard utilities for IO from the model.
 *
 */
public class ModelIO {

    private static Log logger = LogFactory.getLog (ModelIO.class); 

    private int BLOCK_SIZE = 2048;

    private static final String DEFAULT_OUTPUT = "out";
    private String _outputRoot = DEFAULT_OUTPUT;

    public void setOutputDir (String outputDir) {
	_outputRoot = outputDir;
	ensureOutputDir ();
    }

    static {
	//BasicConfigurator.configure ();
	//Logger.getRootLogger().setLevel (Level.DEBUG);
    }

    private void ensureOutputDir () {
	File root = new File (_outputRoot);
	if (! root.isDirectory ()) {
	    root.mkdirs ();
	}
    }

    public File [] getFilesByPattern (final String pattern, File directory) {
	FilenameFilter filenameFilter = new FilenameFilter () {
		public boolean accept(File dir, String name) {
		    return name.toLowerCase().matches (pattern);
		}
	    };
	return directory.listFiles (filenameFilter);
    }
    public File [] getFilesByPattern (final String pattern) {
	return getFilesByPattern (pattern, new File ("."));
    }
    public String getFileAsString (File file) {
	StringWriter writer = new StringWriter ();
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader (new FileReader (file));
	    IOUtils.copy (reader, writer);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
	return writer.toString ().replace ("\r\n", "\n");
    }

    /**
     * Create a Writer object.
     */
    private BufferedWriter getModelOutputWriterInternal (String fileName, boolean append, boolean compress) 
	throws IOException
    {

/* DEBUG
    Writer w = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream("C:/dev/crcsim/getModelOutputWriterInternal.txt", true),  "UTF-8"));
        w.write(Thread.currentThread().getName() + ": " + fileName + "\n");
        w.flush();
        w.close();
*/

	Writer writer = null;

	try {
	    File outputFile = new File (fileName);
	    if (outputFile.exists ()) {
                //System.out.println("! Deleting/replacing " + fileName + "; "
                //    + Thread.currentThread().getName());
                //Thread.dumpStack();
		outputFile.delete ();
	    }	    
	    if (compress) {
		writer = new OutputStreamWriter (new GZIPOutputStream (new FileOutputStream (fileName + ".gz", append)));
	    } else {
		writer = new FileWriter (fileName, append);
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e + " : " + fileName);
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
	    fileName = StringUtils.join (new String [] { _outputRoot, fileName }, "");
	    logger.debug ("writing " + fileName);

/* DEBUG
    Writer w = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream("C:/dev/crcsim/getModelOutputWriter.txt", true),  "UTF-8"));
        w.write(Thread.currentThread().getName() + ": " + fileName + "\n");
        w.flush();
        w.close();
*/

	    writer = getModelOutputWriterInternal (fileName, !overwrite, compress);
            /*
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
            */
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
	return writer;
    }

    /**
     * Close a writer.
     *@param writer A writer to close.
     */
    public void closeWriter (Writer writer) {
	IOUtils.closeQuietly (writer);
    }

    /**
     * Write a population to a writer.
     *@param population A list of people objects
     *@param writer A writer.
     */
    public synchronized void writePeople (Iterator people, String outputDir, String fileName) {
	setOutputDir (outputDir);
	BufferedWriter writer = null;

	try {

/* DEBUG
            Writer w = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream("C:/dev/crcsim/writePeople.txt", true),  "UTF-8"));
            w.write(Thread.currentThread().getName() + ": " + outputDir + fileName + "\n");
            w.flush();
            w.close();
*/

	    writer = getModelOutputWriter (fileName, true, false);
	    writePeople (people, writer);
	/* DEBUG 
        } catch (IOException e) {
            throw new RuntimeException (e);
        */
        } finally {
	    IOUtils.closeQuietly (writer);
	}
    }

    public void writePeople (Iterator people, BufferedWriter writer) {
        //System.out.println("ModelIO.writePeople(" + people + ", " + writer+ ");");
	String [] fieldNames = new String [] {
            "person_idx",
            "rep_idx",
            "intervention",
            "age",
            "current_year",
	    "cancer_free_years",
	    "cost_diagnostic",
	    "death_age",
	    "lost_years",
	    "num_lesions",
	    "never_compliant",
	    "onset_age_clin",
	    "onset_age_polyp",
	    "onset_stage_clin", 
	    "surveillance_negatives",
	    "tot_lesions",
	    "num_colonoscopies",
	    "value_life",
	    "longitude",
	    "latitude",
	    "stcotrbg",
	    "zipcode"
	};
	String newline = "\n";
	char separator = '\t';
	this.outputRow (writer, fieldNames, separator, newline);
	if (people.hasNext ()) {
	    for (Object person = people.next (); people.hasNext (); person = people.next ()) {
		 this.outputDataRow (writer,
				     person,
				     fieldNames,
				     separator,
				     newline);
	    }
	}
    }	
	
	
    /**
     * Output a row of data to the writer.
     *@param writer The output writer.
     *@param values Values to write.
     *@param separator The separator character.
     *@param newline The newline sequence.
     */
    public void outputRow (BufferedWriter writer, String [] values, char separator, String newline) {
	try {
	    synchronized (writer) {
		String row = StringUtils.join (values, separator);
		writer.write (row);
		writer.newLine ();
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
    }

    /**
     * Output a row of data read, via reflection, from an object.
     *@param writer The output writer to send this data to.
     *@param object An instance of some class
     *@param fieldNames The names of fields to write
     *@param separator The separator to use between fields
     *@param newline The newline sequence to use.
     */
    public void outputDataRow (BufferedWriter writer, Object object, String [] fieldNames, char separator, String newline) {
	String [] values = Util.getFieldValues (object, fieldNames);
	this.outputRow (writer, values, separator, newline);
    }

    /**
     *
     */
    public final void configureModel (Object object, String [] args, Map<String,String> skip) {
	Util.setFieldValues (object, args, skip);
    }

    public final void setFieldValue (Object object, String fieldName, String value) {
	Util.setFieldValue (object, fieldName, value);
    }

    /**
     * Return a list of field values for the given instance.
     *@param instance A model object.
     *@return Returns a list of strings where each is a name value pair
     */
    public String [] printConfiguration (Object instance) {
	String [] fieldNames = new String [] {
	    "output_dir",
	    "model_description",
	    "num_lesion_types",
	    "num_tests",
	    "max_age",
	    "population_size",
	    "infile_population_name",
	    "outfile_replication",
	    "outfile_replication_name",
	    "outfile_replication_overwrite",
	    "outfile_year",
	    "outfile_year_name",
	    "outfile_year_overwrite",
	    "outfile_year_start_age",
	    "outfile_year_end_age",
	    "outfile_state",
	    "outfile_state_name",
	    "outfile_state_overwrite",
	    "mean_duration_polyp1_polyp2",
	    "mean_duration_polyp2_polyp3",
	    "mean_duration_polyp1_pre",
	    "mean_duration_polyp2_pre",
	    "mean_duration_polyp3_pre",
	    "mean_duration_pre1_pre2",
	    "mean_duration_pre2_pre3",
	    "mean_duration_pre3_pre4",
	    "mean_duration_pre1_dead",
	    "mean_duration_pre2_dead",
	    "mean_duration_pre3_dead",
	    "mean_duration_pre4_dead",
	    "mean_duration_clin1_dead",
	    "mean_duration_clin2_dead",
	    "mean_duration_clin3_dead",
	    "mean_duration_clin4_dead",
	    "mean_duration_pre1_clin1",
	    "mean_duration_pre2_clin2",
	    "mean_duration_pre3_clin3",
	    "mean_duration_pre4_clin4",
	    "mean_duration_clin1_clin2",
	    "mean_duration_clin2_clin3",
	    "mean_duration_clin3_clin4",
	    "proportion_survive_clin1",
	    "proportion_survive_clin2",
	    "proportion_survive_clin3",
	    "proportion_survive_clin4",
	    "cost_polypectomy",
	    "cost_polyp_pathology",
	    "cost_treatment1",
	    "cost_treatment2",
	    "cost_treatment3",
	    "cost_treatment4",
	    "proportion_treatment_cure",
	    "lifespan_multiplier_no_treatment",
	    "value_loss_cancer",
	    "value_life_year_ages",
	    "value_life_year_dollars",
	    "model_test_probability",
	    "duration_screen_low_risk",
	    "surveillance_interval",
	    "max_surveillance_negatives",
	    "use_conditional_compliance",
	    "model_initial_compliance",
	    "initial_compliance_rate",
	    "never_compliant_rate",
	    "diagnostic_compliance_rate",
	    "surveillance_compliance_rate",
	    "treatment_compliance_rate",
	    "routine_tests",
	    "lesion_initial_state",
	    "lesion_incidence_black_female_ages",
	    "lesion_incidence_black_female_rates",
	    "lesion_incidence_black_male_ages",
	    "lesion_incidence_black_male_rates",
	    "lesion_incidence_white_female_ages",
	    "lesion_incidence_white_female_rates",
	    "lesion_incidence_white_male_ages",
	    "lesion_incidence_white_male_rates",
	    "lesion_risk_alpha",
	    "lesion_risk_beta",
	    "test_name",
	    "test_routine_start",
	    "test_routine_end",
	    "test_routine_freq",
	    "test_specificity",
	    "test_sensitivity_polyp1",
	    "test_sensitivity_polyp2",
	    "test_sensitivity_polyp3",
	    "test_sensitivity_cancer",
	    "test_cost",
	    "test_proportion_lethal",
	    "test_proportion_perforation",
	    "test_cost_perforation",
	    "test_proportion",
	    "test_compliance_rate_given_prev_compliant",
	    "test_compliance_rate_given_not_prev_compliant",
	    "compute_population_rates",
	    "population_rates_sample_size",
	    "num_population_rates",
	    "cost_discount_age",
	    "cost_discount_rate",
	    "lifespan_discount_age",
	    "lifespan_discount_rate",
	    "num_tests_displayed"
	};
	List<String> result = new ArrayList<String> (fieldNames.length);
	for (String fieldName : fieldNames) {
	    result.add ("   " + fieldName + " = " + Util.getFieldValue (instance, fieldName));
	}
	return (String [])result.toArray (new String [result.size ()]);
    }

    /**
     * Return a list of field values for the given instance.
     *@param instance A model object.
     *@param fileName Name of file to write to.
     */
    public synchronized void writeConfiguration (Object instance, String directoryName, String fileName, List<String> parameters) {
	BufferedWriter writer = null;
	setOutputDir (directoryName);
	File dir = new File (directoryName);
	dir.mkdirs ();
	try {
	    writer = new BufferedWriter (new FileWriter (directoryName + File.separator + fileName));
	    List config = Arrays.asList (printConfiguration (instance));
	    String lineSeparator = System.getProperty ("line.separator");
	    IOUtils.writeLines (parameters, lineSeparator, writer);
	    IOUtils.writeLines (config, lineSeparator, writer);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }
    public int getAge (File input) {
	int value = -1;
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader (new FileReader (input));
	    reader.readLine ();
	    String line = reader.readLine ();
	    String [] parts = StringUtils.split (line, '\t');
	    if (parts != null && parts.length > 6) {
		value = Integer.parseInt (parts [5]);
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
	return value;
    }
    public int countFileLines (File input) {
	int lines = 0;
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(input));
	    while (reader.readLine() != null) {
		lines++;
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (reader);
	}
	return lines;
    }
}

