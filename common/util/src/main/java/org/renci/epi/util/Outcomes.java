package org.renci.epi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Collections;
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

public class Outcomes implements Comparable {

    private static Log logger = LogFactory.getLog (Outcomes.class); 

    public String name = "";
    public double cost_routine = 0.0;
    public double cost_diagnostic = 0.0;
    public double cost_surveillance = 0.0;
    public double cost_treatment = 0.0;
    public double lost_years = 0.0;
    public double total = 0.0;

    public double d_cost_routine = 0.0;
    public double d_cost_diagnostic = 0.0;
    public double d_cost_surveillance = 0.0;
    public double d_cost_treatment = 0.0;
    public double d_lost_years = 0.0;
    public double d_total = 0.0;

    public double prob_crc = 0.0;
    public double prob_dead_crc = 0.0;
	
    public double ce_over_usual_care = 0.0;	
    public double avg_cost_per_crc_averted = 0.0;
    public double avg_cost_per_crc_death_averted = 0.0;

    public double d_ce_over_usual_care = 0.0;	
    public double d_avg_cost_per_crc_averted = 0.0;
    public double d_avg_cost_per_crc_death_averted = 0.0;

    private int replications = 0;

    /**
     * Create a new outcomes object, initializing only the name.
     * This allows for reading the results of multiple replications.
     */
    public Outcomes (String name) {
	this.name = name;
    }

    public Outcomes (String name,
		     double cost_routine,
		     double cost_diagnostic,
		     double cost_surveillance,
		     double cost_treatment,
		     double lost_years,

		     double d_cost_routine,
		     double d_cost_diagnostic,
		     double d_cost_surveillance,
		     double d_cost_treatment,
		     double d_lost_years,

		     double prob_crc,
		     double prob_dead_crc)
    {
	this.name = name;
	this.cost_routine = cost_routine;
	this.cost_diagnostic = cost_diagnostic;
	this.cost_surveillance = cost_surveillance;
	this.cost_treatment = cost_treatment;
	this.lost_years = lost_years;

	this.total =
	    cost_routine +
	    cost_diagnostic +
	    cost_surveillance +
	    cost_treatment;

	this.d_cost_routine = d_cost_routine;
	this.d_cost_diagnostic = d_cost_diagnostic;
	this.d_cost_surveillance = d_cost_surveillance;
	this.d_cost_treatment = d_cost_treatment;
	this.d_lost_years = d_lost_years;

	this.d_total =
	    d_cost_routine +
	    d_cost_diagnostic +
	    d_cost_surveillance +
	    d_cost_treatment;

	this.prob_crc = prob_crc;
	this.prob_dead_crc = prob_dead_crc;

    }

    public int compareTo (Object other) throws ClassCastException {
	if (! (other instanceof Outcomes)) {
	    throw new ClassCastException ("Expected a " + getClass().getName () + " object.");
	}
	Outcomes otherOutcomes = (Outcomes)other;
	return name.compareTo (otherOutcomes.name);
    }

    /**
     * Read values, adding to running totals.
     */
    public synchronized void read (double cost_routine,
				   double cost_diagnostic,
				   double cost_surveillance,
				   double cost_treatment,
				   double lost_years,
				   
				   double d_cost_routine,
				   double d_cost_diagnostic,
				   double d_cost_surveillance,
				   double d_cost_treatment,
				   double d_lost_years,
				   
				   double prob_crc,
				   double prob_dead_crc)
    {
	this.replications++;

	this.cost_routine += cost_routine;
	this.cost_diagnostic += cost_diagnostic;
	this.cost_surveillance += cost_surveillance;
	this.cost_treatment += cost_treatment;
	this.lost_years += lost_years;

	this.d_cost_routine += d_cost_routine;
	this.d_cost_diagnostic += d_cost_diagnostic;
	this.d_cost_surveillance += d_cost_surveillance;
	this.d_cost_treatment += d_cost_treatment;
	this.d_lost_years += d_lost_years;

	this.prob_crc = prob_crc;
	this.prob_dead_crc = prob_dead_crc;

    }

    /**
     * For each value, rou
     */
    public void calculateValues () {

	cost_routine = cost_routine / replications;
	cost_diagnostic = cost_diagnostic / replications;
	cost_surveillance = cost_surveillance / replications;
	cost_treatment = cost_treatment / replications;
	lost_years = lost_years / replications;

	d_cost_routine = d_cost_routine / replications;
	d_cost_diagnostic = d_cost_diagnostic / replications;
	d_cost_surveillance = d_cost_surveillance / replications;
	d_cost_treatment = d_cost_treatment / replications;
	d_lost_years = d_lost_years / replications;

	prob_crc = prob_crc / replications;
	prob_dead_crc = prob_dead_crc / replications;

	total =
	    cost_routine +
	    cost_diagnostic +
	    cost_surveillance +
	    cost_treatment;

	d_total =
	    d_cost_routine +
	    d_cost_diagnostic +
	    d_cost_surveillance +
	    d_cost_treatment;
    }

    /**
     * Calculate deltas from prior iterations for this iteration.
     */
    public void calculateDeltas (Outcomes control) {
	double delta_prob_crc = control.prob_crc - this.prob_crc;
	double delta_prob_dead_crc = control.prob_dead_crc - this.prob_dead_crc;
			
	// non-discounted
	double delta_total = this.total - control.total;
	double delta_lifelost = control.lost_years - this.lost_years;
	this.ce_over_usual_care = delta_lifelost == 0.0 ?
	    0.0 :
	    delta_total / delta_lifelost;
		
	this.avg_cost_per_crc_averted = delta_prob_crc == 0.0 ? 
	    0.0 :
	    delta_total / delta_prob_crc;
		
	this.avg_cost_per_crc_death_averted = delta_prob_dead_crc == 0.0 ? 
	    0.0 :
	    delta_total / delta_prob_dead_crc;

	// discounted
	double delta_d_total = this.d_total - control.d_total;
	double delta_d_lifelost = control.d_lost_years - this.d_lost_years;
	this.d_ce_over_usual_care = delta_d_lifelost == 0.0 ?
	    0.0 :
	    delta_d_total / delta_d_lifelost;
		
	this.d_avg_cost_per_crc_averted = delta_prob_crc == 0.0 ? 
	    0.0 :
	    delta_d_total / delta_prob_crc;
		
	this.d_avg_cost_per_crc_death_averted = delta_prob_dead_crc == 0.0 ? 
	    0.0 :
	    delta_d_total / delta_prob_dead_crc;
    }

    /**
     * Calculate deltas from prior iterations for this iteration.
     */
    public void calculateDeltas0 (Outcomes control) {
	double delta_prob_crc = control.prob_crc - this.prob_crc;
	double delta_prob_dead_crc = control.prob_dead_crc - this.prob_dead_crc;
			
	// non-discounted
	double delta_total = this.total - control.total;
	double delta_lifelost = control.lost_years - this.lost_years;
	this.ce_over_usual_care = delta_lifelost == 0.0 ?
	    0.0 :
	    delta_total / delta_lifelost;
		
	this.avg_cost_per_crc_averted = delta_prob_crc == 0.0 ? 
	    0.0 :
	    delta_total / delta_prob_crc;
		
	this.avg_cost_per_crc_death_averted = delta_prob_dead_crc == 0.0 ? 
	    0.0 :
	    delta_total / delta_prob_dead_crc;

	// discounted
	double delta_d_total = this.d_total - control.d_total;
	double delta_d_lifelost = control.d_lost_years - this.d_lost_years;
	this.d_ce_over_usual_care = delta_d_lifelost == 0.0 ?
	    0.0 :
	    delta_d_total / delta_d_lifelost;
		
	this.d_avg_cost_per_crc_averted = delta_prob_crc == 0.0 ? 
	    0.0 :
	    delta_d_total / delta_prob_crc;
		
	this.d_avg_cost_per_crc_death_averted = delta_prob_dead_crc == 0.0 ? 
	    0.0 :
	    delta_d_total / delta_prob_dead_crc;
    }

    public static void writeOutcomes0 (List<Outcomes> outcomesList, String fileName, String separator, String [][] views) {
	PrintWriter writer = null;
	try {
	    File file = new File (fileName);
	    writer = new PrintWriter (new BufferedWriter (new FileWriter (file)));
	    for (String [] view : views) {
		writer.println ();
		String header = StringUtils.join (view, separator);
		writer.println (header);
		for (Outcomes outcomes : outcomesList) {		
		    String [] fieldValues = Util.getFieldValues (outcomes, view);
		    String line = StringUtils.join (fieldValues, separator);
		    writer.println (line);
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }
    public static void writeOutcomes (List<Outcomes> outcomesList, String fileName, String separator, String [][] views) {
	PrintWriter writer = null;
	try {
	    File file = new File (fileName);
	    writer = new PrintWriter (new BufferedWriter (new FileWriter (file)));
	    writeOutcomes (outcomesList, writer, separator, views);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} finally {
	    IOUtils.closeQuietly (writer);
	}
    }
    public static void writeOutcomes (List<Outcomes> outcomesList, PrintWriter writer, String separator, String [][] views) 
	throws IOException
    {
	Collections.sort (outcomesList);
	for (String [] view : views) {
	    writer.println ();
	    String header = StringUtils.join (view, separator);
	    writer.println (header);
	    for (Outcomes outcomes : outcomesList) {		
		String [] fieldValues = Util.getFieldValues (outcomes, view);
		String line = StringUtils.join (fieldValues, separator);
		writer.println (line);
	    }
	}
    }

    public static final void writeOutcomes (List<Outcomes> outcomesList, PrintWriter writer, String separator) throws IOException {
	writeOutcomes (outcomesList, writer, separator, getView ());
    }
    public static final void writeOutcomes (List<Outcomes> outcomesList, String fileName, String separator) {
	writeOutcomes (outcomesList, fileName, separator, getView ());
    }
    private static final String [][] getView () {
	return new String [][] {
	    new String [] {
		"name",
		"cost_routine",
		"cost_diagnostic",
		"cost_surveillance",
		"cost_treatment",
		"total",
		"lost_years",
		"ce_over_usual_care",
		"prob_crc",
		"avg_cost_per_crc_averted",
		"prob_dead_crc",
		"avg_cost_per_crc_death_averted"
	    },
	    new String [] {
		"name",
		"d_cost_routine",
		"d_cost_diagnostic",
		"d_cost_surveillance",
		"d_cost_treatment",
		"d_total",
		"lost_years",
		"d_ce_over_usual_care",
		"prob_crc",
		"d_avg_cost_per_crc_averted",
		"prob_dead_crc",
		"d_avg_cost_per_crc_death_averted"
	    }
	};
    }
}
