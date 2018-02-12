package org.renci.epi.util.stats;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.util.Scanner;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

// SEXC BLACK HISP OTHER NOINS PRIVA MEDICARE MEDICAID DUAL county stcotrbg zipcode MARRIED compliancePFromKristen
class Person {

    public boolean sex_male;
    public boolean is_black;
    public boolean is_hispanic;
    public boolean is_other;
    public boolean insurance_none;
    public boolean insurance_private;
    public boolean insurance_medicare;
    public boolean insurance_medicaid;
    public boolean insurance_dual;
    public boolean insurance_private_orig;
    public boolean insurance_none_orig;
    public String stcotrbg;
    public String zipcode;
    public boolean is_married;
    public boolean SEHP = false;

    public double compliance_xbeta;
    public double modality_xbeta;

    private static final String TRUE_DIGIT = "1";
    private static final String FALSE_DIGIT = "0";

    private Person (String line) {

	String [] part = line.split (" ");
	int c = 0;
	sex_male                = TRUE_DIGIT.equals (part [c++]);
	is_black                = TRUE_DIGIT.equals (part [c++]);
	is_hispanic             = TRUE_DIGIT.equals (part [c++]);
	is_other                = TRUE_DIGIT.equals (part [c++]);
	insurance_none          = TRUE_DIGIT.equals (part [c++]);
	insurance_private       = TRUE_DIGIT.equals (part [c++]);
	insurance_medicare      = TRUE_DIGIT.equals (part [c++]);
	insurance_medicaid      = TRUE_DIGIT.equals (part [c++]);
	insurance_dual          = TRUE_DIGIT.equals (part [c++]);
	insurance_private_orig  = TRUE_DIGIT.equals (part [c++]);
	insurance_none_orig     = TRUE_DIGIT.equals (part [c++]);
	//c++; // county
	stcotrbg           = part [c++];
	zipcode            = part [c++];
	is_married         = TRUE_DIGIT.equals (part [c++]);

	compliance_xbeta = Double.valueOf (part [c++]);
	modality_xbeta = Double.valueOf (part [c++]);
	
	System.out.println ("{--> line: " + line);
	System.out.println (" --> prsn: " + this + "}");

    }
    public String toString () {
	StringBuffer buffer = new StringBuffer ();
	buffer.
	    append ("sex_male: ").append (sex_male).append (", ").
	    append ("black: ").append (is_black).append (", ").
	    append ("hisp: ").append (is_hispanic).append (", ").
	    append ("other: ").append (is_other).append (", ").
	    append ("none: ").append (insurance_none).append (", ").
	    append ("priv: ").append (insurance_private).append (", ").
	    append ("medicare: ").append (insurance_medicare).append (", ").
	    append ("medicaid: ").append (insurance_medicaid).append (", ").
	    append ("dual: ").append (insurance_dual).append (", ").
	    append ("private_orig: ").append (insurance_dual).append (", ").
	    append ("none_orig: ").append (insurance_dual).append (", ").
	    append ("stcotrbg: ").append (stcotrbg).append (", ").
	    append ("zipcode: ").append (zipcode).append (", ").
	    append ("married: ").append (is_married).append (", ").
	    append ("compliance_xbeta: ").append (compliance_xbeta).
	    append ("modality_xbeta: ").append (modality_xbeta);
	return buffer.toString ();
    }

    public static final List<Person> scan (String resourcePath) {
	List<Person> result = new ArrayList<Person> ();
	try {
	    URL url = Resources.getResource (resourcePath);
	    String text = Resources.toString (url, Charsets.UTF_8);
	    Scanner scanner = new Scanner (text);
	    while (scanner.hasNextLine ()) {
		String line = scanner.nextLine ();
		System.out.println ("<<= " + line);
		if ( !line.startsWith ("#") && (line.trim().length () > 0) ) {
		    result.add (new Person (line));
		}
	    }
	    scanner.close();	    
	} catch (IOException e) {
	    throw new RuntimeException (e);
	}
	return result;
    }

}
