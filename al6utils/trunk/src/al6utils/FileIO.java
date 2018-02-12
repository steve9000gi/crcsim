package al5utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileIO {

    public BufferedWriter getModelOutputWriterAppend (String fileName) {
	return new BufferedWriter (new FileWriter (fileName));
    }

    public BufferedWriter getModelOutputWriter (String fileName, boolean overwrite) {
	BufferedWriter writer = null;
	try {
	    writer = getModelOutputWriterAppend (fileName, !overwrite);
	    if (overwrite) {
		writer.write (
			      "description"
			      + "\t" + "person"
			      + "\t" + "age"
			      + "\t" + "statechart"
			      + "\t" + "new_state"
			      );		
		writer.write ("\r\n");
	    }
	} catch (Exception e) {
	    e.printStackTrace ();
	    //error (e.getMessage());
	}
    }
}

