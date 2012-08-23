package al6utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;

public class FileIO {

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
	    e.printStackTrace ();
	    //error (e.getMessage());
	}
	return writer;
    }

    public void closeWriter (Writer writer) {
	IOUtils.closeQuietly (writer);
    }
    
    public void outputRow (Writer writer, String [] values) {
	String text = StringUtils.join (values, '\t');
    }

    public String [] getPeopleFields () {
	return new String [] {
	    "description",
	    "replication",
	    "population",
	    "age",
	    "n",
	    "num_state_healthy",
	    "tot_state_healthy",
	    "num_state_polyp",
	    "tot_state_polyp", 
	    "num_state_cancer",
	    "tot_state_cancer",
	    "num_state_pre",
	    "tot_state_pre",
	    "num_state_clin",
	    "tot_state_clin",
	    "tot_state_dead",
	    "tot_state_dead_crc",
	    "tot_polyps_removed",
	    "tot_lesions_cured",
	    "tot_discounted_cost_routine",
	    "tot_discounted_cost_diagnostic",
	    "tot_discounted_cost_surveillance",
	    "tot_discounted_cost_treatment",
	    "tot_test0_performed",
	    "tot_test1_performed",
	    "tot_test2_performed",
	    "longitude",
	    "latitude"
	};
    }
}

