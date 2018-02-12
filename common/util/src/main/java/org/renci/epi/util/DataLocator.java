package org.renci.epi.util;

import java.io.File;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.commons.lang3.StringUtils;
import java.io.FilenameFilter;
import java.util.Arrays; // DEBUG SAC

/**
 *
 * Provide services for locating data.
 *
 */
public class DataLocator {

    private String _dataRoot = null;

    public DataLocator () {
<<<<<<< HEAD
	this.setDataRoot ( new String [] { "", "dev-OR", "var", "crcsim" } );
=======
        String [] dataRootElts = { "", "dev-OR", "var", "crcsim" };
	//this.setDataRoot ( new String [] { "", "dev-OR", "var", "crcsim" } );
	this.setDataRoot ( dataRootElts );
        System.out.println("DataLocator ctor dataRootElts: " + Arrays.toString(dataRootElts) + "; _dataRoot: " + _dataRoot);
>>>>>>> 1699535e41ab3f8c55eb11bdeca7e3c98c0f09c6
    }

    // set the root folder to which others are relative
    public void setDataRoot (String [] parts) {
	//_dataRoot = StringUtils.join (parts, File.separatorChar);
	_dataRoot = StringUtils.join (parts, '/'); // Does Java 8 give a different result for File.separatorChar? SAC
        System.out.println("DataLocator.setDataRoot _dataRoot: " + _dataRoot);
    }

    // file generated by joining rti synthetic population and pums.
    public String getSyntheticPopulationPath (String fileName) {
	return this.join (new String [] { _dataRoot, "generated", fileName });
    }

    public File [] getSyntheticPopulationExports () {
        _dataRoot = "/dev-OR/var/crcsim"; // This hack remains while I'm unable to figure out why _dataRoot continues to use "dev" rather than "dev-OR".
        System.out.println("DataLocator.getSyntheticPopulationExports 0: _dataRoot: " + _dataRoot);
	final String pattern = "export.*";
        String dirName = join (new String [] { _dataRoot, "generated", "exports-Oregon" });
//        System.out.println("DataLocator.getSyntheticPopulationExports 1: dirName = " + dirName);
   
	File directory = new File (join (new String [] { _dataRoot, "generated", "exports-Oregon" })); //fileName }));
//        System.out.println("DataLocator.getSyntheticPopulationExports 2: directory = " + (join (new String [] { _dataRoot, "generated", "exports-Oregon" })));
	FilenameFilter filenameFilter = new FilenameFilter () {
		public boolean accept (File dir, String name) {
		    return name.toLowerCase().matches (pattern);
		}
	    };
	return directory.listFiles (filenameFilter);
    }
    // input to and output files from the RTI ABM
    public String getModelOutputPath () {
	return this.join (new String [] { _dataRoot, "model", "output" } );
    }
    public String getModelInputFileName () {
	return this.join (new String [] { _dataRoot, "..", "crcsim", "model", "population.tsv" });
    }
    public String getModelInputPath () {
	return this.join (new String [] { _dataRoot, "..", "crcsim", "model", "pops" });
    }    
    public String getModelInputFileName (String inputFileName) {
	return this.join (new String [] { _dataRoot, "..", "crcsim", "model", "pops", inputFileName });
    }

    // geocoding census shapefile data
    public String getCountyPolygonFileName () {
	return this.join (new String [] { _dataRoot, "census2010", "tl_2010_37_county10.shp" });
    }

    // geocoding census shapefile data
    public String getCensusBlockPolygonFileName () {
	return this.join (new String [] { _dataRoot, "census2010", "tl_2010_37_tabblock10.shp" });
    }

    // geocoded output data
    public String getGeocodedOutputPath () {
	return this.join ( new String [] { _dataRoot, "generated", "geocoded" } );
    }

    // join
    private String join (String [] parts) {
	// return StringUtils.join (parts, File.separatorChar); 
	return StringUtils.join (parts, '/'); // Different File.separatorChar for Cygwin in Java 8?
    }


}
