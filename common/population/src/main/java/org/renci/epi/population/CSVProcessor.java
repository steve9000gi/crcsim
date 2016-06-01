package org.renci.epi.population;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory; 
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.renci.epi.population.insurance.BasicInsuranceStrategy;
import org.renci.epi.population.insurance.InsuranceStatus;
import org.renci.epi.population.insurance.InsuranceStrategy;
import org.renci.epi.population.insurance.Person;

/**
 *
 * Generic interface for processing an input data line into an output data line.
 * 
 */
interface Processor {
  public String [] process (String [] data, HashMap<String,String> record);
  public void setHeader (String [] header);
  public String [] getOutputKeys ();
}

/**
 * A mapping processor to output the RTI ABM model input format.
 */
class SynthPopAnnotationProcessor implements Processor {

  private String [] _header = new String [0];
  private String [] _outputKeys = null;
  private static Log logger = LogFactory.getLog (SynthPopAnnotationProcessor.class); 
  private static int _id = 0;
  Random _randomGenerator = new Random ( 19580427 );
  private HashMap<String, String> _urbanRuralMap; // SAC 2016/05/24
  
  //static Random r = new Random (625345);
  static Random r = new Random (987654);
  static double random = r.nextDouble();

  //////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Return the same pseudorandom sequence in each build:
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////
  public double getRandom() {
    return r.nextDouble();  // Same sequence each build
    //return Math.random(); // Returns a different sequence for each build
  }

  private InsuranceStrategy _insuranceStrategy = new BasicInsuranceStrategy ();

  /**
   * Construct a mapping processor.
   */
  public SynthPopAnnotationProcessor (String [] outputKeys, HashMap<String, String> urbanRuralMap) {
	assert (outputKeys != null) : "Output keys must be non-null";
	this._outputKeys = outputKeys;
        this._urbanRuralMap = urbanRuralMap;
  }
  
  public String [] getOutputKeys () {
	return this._outputKeys;
  }
  
  /**
   * Set the input header row.
   */
  public void setHeader (String [] header) {
	assert (header != null) : "Header row must be non-null";
	if (logger.isDebugEnabled ()) {
	  this.logger.debug ("setting header: " + StringUtils.join (header, ","));
	}
    this._header = header;
  }

  /**
   * Map a row into its new form.
   */
  public String [] process (String [] data, HashMap<String,String> record) {
	assert (data != null) : "Data must be non null";
	assert (data.length == this._header.length) : "Data length is not " + this._header.length;

	StringBuilder buffer = new StringBuilder (100);
	for (int c = 0; c < this._header.length; c++) {
	  String key = this._header [c];
	  String datum = data [c];
	  record.put (key, datum);
	}
	
	translateOutputLine (record);
	
    String [] output = new String [_outputKeys.length];

    for (int c = 0; c < this._outputKeys.length; c++) {
      String key = _outputKeys [c];
      String value = record.get (key);
      output [c] = value;
      if (logger.isDebugEnabled ()) {
        logger.debug ("key=>[" + key + "] <= [" + value + "]");
      }
    }

	return output;
  }   
  
  /**
   *  http://www.census.gov/acs/www/Downloads/data_documentation/pums/DataDict/PUMS_Data_Dictionary_2009-2011.pdf
   */
  class PUMS {
	static final short RAC1P_WHITE = 1;
	static final short RAC1P_BLACK = 2;
	static final short HISP_NOTHISPANIC = 1;

    static final short MAR_MARRIED = 1;
  }

  private enum InsuranceStatusEnum { NOINS, PRIVA, MEDICARE, MEDICAID, DUAL, UNKNOWN }
				

  private void translateOutputLine (HashMap<String,String> record) {
        Person person = null;

	/** A unique id */
	record.put ("id", String.valueOf (_id++));

	/** Synthetic population id */
	record.put ("p_id", record.get ("people.p_id"));

	/**
	 * In the synthetic population data, sex is
	 *   1 - male
	 *   2 - female
	 * And in the RTI input file
	 *   SEXC is true if male
	 */
	String sex = record.get ("people.sex");
	record.put ("sex", record.get ("people.sex"));
	record.put ("SEXC", String.valueOf (sex.charAt (0) == '1' ? 1 : 0));

	/** age */
	record.put ("AGE_G2", record.get ("people.age"));

    /** marital status */
	String maritalStatusString = record.get ("pumsp.mar");
	try {
	  int maritalStatusCode = Integer.parseInt (maritalStatusString);
	  record.put ("MARRIED", maritalStatusCode == PUMS.MAR_MARRIED ? "1" : "0");
	} catch (NumberFormatException e) {
	  throw new RuntimeException ("unable to parse marital status code: " + maritalStatusString, e);
	}

	/** income */
	try {
	  int pumsIncomeCode = Integer.parseInt (record.get ("pumsp.semp"));
	  record.put ("INCOME", pumsIncomeCode >= 20000 ? "1" : "0");
	} catch (NumberFormatException e) {
	  record.put ("INCOME", "0");
	  if (logger.isDebugEnabled ()) {
		logger.debug (" semp ===> " + record.get ("pumsp.semp"));
	  }
	}

	//record.put ("INCOME", record.get ("hh_income"));
        String incomeAsStr = record.get ("households.hh_income");
        int income = Integer.parseInt (incomeAsStr);
        record.put ("NEW_INCOME", incomeAsStr);

        // Need five categories for householdIncomeCat_NEW (2016/04/29 SAC):
        String incomeCatNew;
        if (income < 15000) {
          incomeCatNew = "1";
        } else if (income < 25000) {
          incomeCatNew = "2";
        } else if (income < 35000) {
          incomeCatNew = "3";
        } else if (income < 50000) {
          incomeCatNew = "4";
        } else {
          incomeCatNew = "5";
        }
    
        record.put("householdIncomeCat_NEW", incomeCatNew);


	/** alone status */
	String householdSizeString = record.get ("households.hh_size");
	try {
	  int householdSize = Integer.parseInt (householdSizeString);
	  record.put ("ALONE", householdSize == 1 ? "1" : "0");
	} catch (NumberFormatException e) {
	  throw new RuntimeException ("unable to parse household size: " + householdSizeString, e);
	}

	/** State and county FIPS codes **/
	record.put ("stcotrbg", record.get ("households.stcotrbg"));

	/** Zip codes **/
	record.put ("zipcode", record.get ("zipcode.zipcode"));

	/** Everyone's in North Carolina, therefore in the South. */
	record.put ("SO", "1");
	record.put ("MW", "0");
	record.put ("WE", "0");

	/** Race
	  Pums Data Dict: 1 .White alone, 2 .Black or African American alone
	*/
        int pumsRaceCode = Integer.parseInt (record.get ("pumsp.rac1p"));
        record.put ("BLACK", pumsRaceCode == PUMS.RAC1P_BLACK ? "1" : "0");
        record.put ("OTHER",
            pumsRaceCode != PUMS.RAC1P_WHITE && pumsRaceCode != PUMS.RAC1P_BLACK ? "1" : "0");

	/**
	   PUMS Data Dict: 01 .Not Spanish/Hispanic/Latino, 02 .Mexican, 03 .Puerto Rican
	*/
	int pumsHispanicCode = Integer.parseInt (record.get ("pumsp.hisp"));
	record.put ("HISP", pumsHispanicCode == PUMS.HISP_NOTHISPANIC ? "0" : "1");


	/** Incorporate insurance data 
	 *  Note: There is randomness in the selection algorithm, so 
	 *      expect different output files given the same inputs.
	 */
	record.put ("NOINS", "0");
	record.put ("PRIVA", "0");
        record.put ("MEDICARE", "0");
        record.put ("MEDICAID", "0");
        record.put ("DUAL", "0");
  
        InsuranceStatusEnum eInsStatus = InsuranceStatusEnum.UNKNOWN;

	try {
	  person = Person.getPerson (Integer.parseInt (record.get ("people.age")),
	                             Integer.parseInt (record.get ("households.hh_income")),
				     Integer.parseInt (record.get ("households.hh_size")),
				     pumsRaceCode == PUMS.RAC1P_WHITE ? 1 : 0,
				     Integer.parseInt (record.get ("BLACK")),
				     Integer.parseInt (record.get ("people.sex")));
	  InsuranceStatus status = _insuranceStrategy.getInsuranceStatus (person);

       // person.getHouseholdIncomeCat () and getHouseholdSizeCat () must be called *after*
       // person.getPerson (...):
       record.put ("NEW_INCOME_CAT", Integer.toString (person.getHouseholdIncomeCat ()));
       record.put ("householdSizeCat", Integer.toString (person.getHouseholdSizeCat ())); // New 2016/04/05 SAC
       record.put ("householdSize", record.get ("households.hh_size")); // New 2016/04/05 SAC

/* debug
    record.put ("pumsp_rac1p", record.get ("pumsp.rac1p")); // one more added 2013/01/17
	record.put ("people_race", record.get ("people.race"));
	record.put ("households_hh_income", record.get ("households.hh_income"));
	record.put ("people_age", record.get ("people.age"));
	record.put ("households_hh_size", record.get ("households.hh_size"));
    record.put ("insStatus", _insuranceStrategy.getInsStatus().toString());
    record.put ("insRandom", Double.toString(_insuranceStrategy.getInsRandom()));
    record.put ("personKey", _insuranceStrategy.getPersonKey());
    record.put ("outBlack", record.get ("BLACK"));
    record.put ("outINCOME", record.get ("INCOME"));
*/

      if (_insuranceStrategy.hasPrivateInsurance (status)) {
        record.put ("PRIVA", "1");
        eInsStatus = InsuranceStatusEnum.PRIVA; 
      } else if (_insuranceStrategy.hasMedicareOnly (status)) {
        record.put ("MEDICARE", "1");
        eInsStatus = InsuranceStatusEnum.MEDICARE;
      } else if (_insuranceStrategy.hasMedicaidOnly (status)) {
        record.put ("MEDICAID", "1");
        eInsStatus = InsuranceStatusEnum.MEDICAID;
      } else if (_insuranceStrategy.hasDual (status)) {
        record.put ("DUAL", "1");
        eInsStatus = InsuranceStatusEnum.DUAL;
      } else if (_insuranceStrategy.hasNoInsurance (status)) {
        record.put ("NOINS", "1");
        eInsStatus = InsuranceStatusEnum.NOINS;
      }
	} catch (NumberFormatException e) {
	  e.printStackTrace ();
	  throw new RuntimeException ("Error e");
	}

	addNewInsuranceStatusValues(eInsStatus, record);

        addINS_NEW_2014_OR(record, person); // Oregon values

        addUrbanRuralDesignation(record); // Oregon only 

	/**
	   RTI model docs: EDU	boolean	education level; true implies some college or higher
	   PUMS Data Dict: 10 .Some college, but less than 1 year 11 .One or more years of college...
	*/
	try {
	  int pumsEducationCode = Integer.parseInt (record.get ("pumsp.schl"));
	  record.put ("EDU", pumsEducationCode >= 10 ? "1" : "0");
	} catch (NumberFormatException e) {
	  record.put ("EDU", "0");
	  if (logger.isDebugEnabled ()) {
		logger.debug ("pums.schl: ===> " + record.get ("pumsp.schl"));
	  }
	}

	record.put ("LAT", record.get ("households.latitude"));
	record.put ("LON", record.get ("households.longitude"));

 	record.put ("FRISK",   getStateWithProbability (0.2));
 	record.put ("VITALE",  getStateWithProbability (0.35));
 	record.put ("FLU",   getStateWithProbability (0.5));
 	record.put ("FORMER",  getStateWithProbability (0.26));
 	record.put ("NEVER",   getStateWithProbability (0.56));
	record.put ("CURRENT", getStateWithProbability (0.18));
 	record.put ("USUAL",   getStateWithProbability (0.80));
  }

  private String getStateWithProbability (double probability) {
	return String.valueOf (_randomGenerator.nextDouble () < probability);
  }

  ///addNewInsuranceStatusValues////////////////////////////////////////////////////////////////////
  //
  // Add 10 boolean values (represented by "1" or "0"), five for insurance status at age < 65 and
  // the other five for insurance status at age >= 65. This code has been modified from code that
  // was previously in the AnyLogic model.
  //
  // Algorithm:
  //  if age < 65:
  //    if no insurance, then insNoneLt65 
  //      if low income then
  //        if random < constant0 then insDualGte65 (act like dual >= 65)
  //        else insMedicareGte65 (act like Medicare >= 65)
  //      else insMedicareGte65 (act like Medicare >= 65)
  //    else if private, then insPrivaLt65 and insMedicareGte65 (act like Medicare >= 65)
  //    else if Medicare, then insMedicareLt65 and insMedicareGte65 (Medicare stays Medicare)
  //    else if dual, then insDualLt65 and insDualGte65 (dual stays dual)
  //    else if Medicaid, then insMedicaidLt65 and insDualGte65 (Medicaid goes dual) 
  //  else (if age >= 65: we know what they are now,  and need to work out insurance < 65)
  //    if no insurance, then insNoneLt65 and insNoneGte65
  //    else if private, then insPrivaLt65 and insPrivaGte65
  //    else if Medicare, then insMedicareGte65
  //      if low income then
  //        if random < constant1 then insMedicareLt65 (disabled)
  //        else if (same?) random < constant2 then insNoneLt65 (no insurance < 65)
  //        else insPrivaLt65
  //      else if mid income then
  //        if random < constant3 then insMedicareLt65
  //        else if random < constant4 then insNoneLt65 (no insurance < 65)
  //        else insPrivaLt65 (private < 65)
  //      else (high income) 
  //        if random < constant5 then insMedicareLt65
  //        else insPrivaLt65
  //    else if dual
  //      if random < constant6 then insDualLt65
  //      else if random < constant7 then insMedicaidLt65
  //      else insNoneLt65
  //    else if Medicaid, then insMedicaidGte65 and insMedicaidLt65
  //  end age >= 65
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////
  private void addNewInsuranceStatusValues(InsuranceStatusEnum eInsStatus,
                       HashMap<String,String> record) {

    int age = Integer.parseInt(record.get("people.age"));
    int incomeCategory = Integer.parseInt(record.get("NEW_INCOME_CAT"));
    final int LOW_INCOME = 1;
    final int MID_INCOME = 2;
    final int HIGH_INCOME = 3;

    final String T = "1";
    final String F = "0";

    record.put("insNoneLt65", F);
    record.put("insPrivaLt65", F);
    record.put("insMedicareLt65", F);
    record.put("insMedicaidLt65", F);
    record.put("insDualLt65", F);
    record.put("insNoneGte65", F);
    record.put("insPrivaGte65", F);
    record.put("insMedicareGte65", F);
    record.put("insMedicaidGte65", F);
    record.put("insDualGte65", F);

    // Transition parameter for insurance at age 50 for those < 65:
    final double lt65LoIncomeNone2Dual = 1.0; // constant0

    // Transition parameters for the Insurance at age 50 for those >=65:

    // Low income (should sum to 1.0):
    final double GTE65_LOW_INCOME_MCARE_WAS_MCARE = 0.035; // constant1
    final double GTE65_LOW_INCOME_MCARE_WAS_NONE = 0.36; // constant 2
    // Not used:    double gte65_cat_1_mcare2private = 0.605;

    // Mid income (should sum to 1.0):
    final double GTE65_MID_INCOME_MCARE_WAS_MCARE = 0.035; // constant3
    final double GTE65_MID_INCOME_MCARE_WAS_NONE = 0.17; // constant4
    // Not used:    double gte65_cat_2_mcare2private = 0.795; 

    // High income (should sum to 1.0):
    final double GTE65_HIGH_INCOME_MCARE_WAS_MCARE = 0.06; // constant5
    // Not used:    double gte65_cat_3_mcare2none = 0.94;

    // For Dual patients in 2007 data set (should sum to 1.0):
    // Not used:    double gte65_dual2none = 0.5;
    final double GTE65_DUAL_WAS_DUAL = 1.0 / 3.0; // constant6 (HACK SAC set to 1/3 to get 1/4)
    final double GTE65_DUAL_WAS_MAID = 0.25; // constant7

    if (age < 65) {
      if (eInsStatus == InsuranceStatusEnum.NOINS) {
        record.put("insNoneLt65", T);
        if (incomeCategory == LOW_INCOME) {
          if (getRandom() < lt65LoIncomeNone2Dual) {
            record.put("insDualGte65", T);
          } else {
             record.put("insMedicareGte65", T);
          }
        } else { // not LOW_INCOME
          record.put("insMedicareGte65", T);
        }
      } else if (eInsStatus == InsuranceStatusEnum.PRIVA) {
        record.put("insPrivaLt65", T);
        record.put("insMedicareGte65", T);
      } else if (eInsStatus == InsuranceStatusEnum.MEDICARE) {
        record.put("insMedicareLt65", T);
        record.put("insMedicareGte65", T);
      } else if (eInsStatus == InsuranceStatusEnum.DUAL) {
        record.put("insDualLt65", T);
        record.put("insDualGte65", T);
      } else if (eInsStatus == InsuranceStatusEnum.MEDICAID) {
        record.put("insMedicaidLt65", T);
        record.put("insDualGte65", T);
      }
    } else { // age >= 65: we know what they are now and need to work out insurance < 65
      if (eInsStatus == InsuranceStatusEnum.NOINS) {
        record.put("insNoneLt65", T);
        record.put("insNoneGte65", T);
      } else if (eInsStatus == InsuranceStatusEnum.PRIVA) {
        record.put("insPrivaLt65", T);
        record.put("insPrivaGte65", T);
      } else if (eInsStatus == InsuranceStatusEnum.MEDICARE) {
        record.put("insMedicareGte65", T);
        if (incomeCategory == LOW_INCOME) {
          if (getRandom() < GTE65_LOW_INCOME_MCARE_WAS_MCARE) {
            record.put("insMedicareLt65", T);
          } else if (getRandom() < GTE65_LOW_INCOME_MCARE_WAS_NONE) {
            record.put("insNoneLt65", T);
          } else {
            record.put("insPrivaLt65", T);
          }
        } else if (incomeCategory == MID_INCOME) {
          if (getRandom() < GTE65_MID_INCOME_MCARE_WAS_MCARE) {
            record.put("insMedicareLt65", T);
          } else if (getRandom() < GTE65_MID_INCOME_MCARE_WAS_NONE) {
            record.put("insNoneLt65", T);
          } else {
            record.put("insPrivaLt65", T);
          }
        } else { // HIGH_INCOME
          if (getRandom() < GTE65_HIGH_INCOME_MCARE_WAS_MCARE) {
            record.put("insMedicareLt65", T);
          } else {
            record.put("insPrivaLt65", T);
          }
        } // HIGH_INCOME
      } else if (eInsStatus == InsuranceStatusEnum.DUAL) {
        record.put("insDualGte65", T);
        if (getRandom() < GTE65_DUAL_WAS_MAID) {
          record.put("insMedicaidLt65", T);
        } else if (getRandom() < GTE65_DUAL_WAS_DUAL) {
          record.put("insDualLt65", T);
        } else {
          record.put("insNoneLt65", T);
        }
      } else if (eInsStatus == InsuranceStatusEnum.MEDICAID) {
        record.put("insMedicaidLt65", T);
        record.put("insMedicaidGte65", T);
      }
    } // end age >= 65
  } // end addNewInsuranceStatusValues(...)


  ///addINS_NEW_2014////////////////////////////////////////////////////////////////////////////////
  //
  // Maria Mayorga's instructions (edited for context): Create a new insurance variable for the
  // population input file that is read in by AnyLogic. “INS_NEW_2014”. According to the attributes
  // listed in “Pred_New_Ins_2014.csv”(sex, ageCat,HISP, householdIncomeCat_New, MARRIED –- from
  // person.getPerson) use the “increase” number to assign a binary variable “INS_NEW_2014” as
  // follows: Draw a random number from 0 to 1, say the result is “r”.  If r < increase then 1 else
  // 0. If there are individuals for which the attributes are not listed, set “increase” to 0.
  // 
  // For example for a person with sex=1, ageCat=3, raceCat=1, HISP=0, MARRIED=1, set the increase
  // to 0.37, draw a random number “r” if r < 0.37 set INS_NEW_2014 = 1, else 0.
  //
  // Some notes.  For some of the attributes multiple results use the same increase level.  For
  // example, for ageCat = 2 or 3 we use the same row, for raceCat = 1,2,3 if HISP = 1 then we use
  // the same row.
  //
  // NOTE (SAC 2016/05/01): This function must be called AFTER the various values used within
  // ("sex", etc.) have been put in the record parameter.
  // 
  //////////////////////////////////////////////////////////////////////////////////////////////////
  private void addINS_NEW_2014(HashMap<String,String> record, Person person) {
    Double increase = 0.0;
    int sex = Character.getNumericValue(record.get("sex").charAt(0));
    int ageCat = person.getAgeCat();
    int raceCat = person.getRaceCat();
    int HISP =  Character.getNumericValue(record.get("HISP").charAt(0));
    int householdIncomeCat_NEW = Character.getNumericValue(record.get("householdIncomeCat_NEW").
                                   charAt(0));
    int MARRIED =  Character.getNumericValue(record.get("MARRIED").charAt(0));
    
    if (sex == 1) { 
      if (ageCat == 2 || ageCat == 3) {
        if (HISP == 0) {
          if (raceCat == 1) {
            if (householdIncomeCat_NEW == 1) {
              increase = (MARRIED == 1) ? 0.37 : 0.05;
            } else if (householdIncomeCat_NEW == 2) {
              increase = (MARRIED == 1) ? 0.23 : 0.27;
            } else if (householdIncomeCat_NEW == 3) {
              increase = (MARRIED == 1) ? 0.5 : 0.33;
            } else if (householdIncomeCat_NEW == 4) {
              increase = (MARRIED == 1) ? 0.02 : 0.0;
            } else if (householdIncomeCat_NEW == 5) {
              increase = (MARRIED == 1) ? 0.01 : 0.14;
            }
          } else if (raceCat == 2) {
            if (householdIncomeCat_NEW == 1) {
              increase = (MARRIED == 1) ? 0.41 : 0.11;
            } else if (householdIncomeCat_NEW == 2) {
              increase = (MARRIED == 1) ? 0.29 : 0.33;
            } else if (householdIncomeCat_NEW == 3) {
              increase = (MARRIED == 1) ? 0.55 : 0.39;
            } else if (householdIncomeCat_NEW == 4) {
              increase = (MARRIED == 1) ? 0.13 : 0.0;
            } else if (householdIncomeCat_NEW == 5) {
              increase = (MARRIED == 1) ? 0.0 : 0.24;
            }
          } else if (raceCat == 3) {
            // increase = 0.0; // increase is already 0
          } // raceCat 3
        } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.1 : 0.0;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.02 : 0.04;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.2 : 0.07;
	    } else if (householdIncomeCat_NEW == 4 || householdIncomeCat_NEW == 5) {
	      // increase = 0.0;
	    }
	  }
	} else if (ageCat == 4) {
          if (HISP == 0) {
            if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.52 : 0.21;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.4 : 0.44;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.63 : 0.5;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.27 : 0.13;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.27 : 0.36;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.56 : 0.27;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.45 : 0.48;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.67 : 0.54;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.36 : 0.23;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.37 : 0.44;
	      }
            } else if (raceCat == 3) {
	      // increase = 0.0; // increase is already 0
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.2 : 0.01;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.11 : 0.14;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.36 : 0.2;
	    } else if (householdIncomeCat_NEW == 4) { 
	      // increase = 0.0;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.0 : 0.07;
	    }
	  }
	} else if (ageCat == 5) {
          if (HISP == 0) {
            if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.46 : 0.04;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.26 : 0.32;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.53 : 0.36;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.01;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.51 : 0.13;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.33 : 0.38;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.58 : 0.43;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.09 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.09 : 0.21;
	      }
	    } else if (raceCat == 3) {
	      // increase = 0.0; // increase is already 0
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.16 : 0.0;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.0 : 0.05;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.26 : 0.08;
	    } else if (householdIncomeCat_NEW == 4) {
	      // increase = 0.0;
	    } else if (householdIncomeCat_NEW == 5) {
	      // increase = (MARRIED == 1) ? 0.0 : 0.0;
	    }
	  } // HISP 1
	} // ageCat 5
      } else if (sex == 2) {
	if (ageCat == 2 || ageCat == 3) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.21 : 0.03;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.05 : 0.27;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.32 : 0.32;
	      } else if (householdIncomeCat_NEW == 4) {
		// increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.08;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.25 : 0.04;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.06 : 0.3;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.35 : 0.35;
	      } else if (householdIncomeCat_NEW == 4) {
		// increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.1;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.27 : 0.1;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.11 : 0.34;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.39 : 0.39;
	      } else if (householdIncomeCat_NEW == 4) {
		// increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.18;
	      }
	    }
          } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.05 : 0.0;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.0 : 0.08;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.12 : 0.13;
	    } else if (householdIncomeCat_NEW == 4) {
	      // increase = (MARRIED == 1) ? 0.0 : 0.0;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.0 : 0.02;
	    }
	  } // HISP 1
	} else if (ageCat == 4) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.36 : 0.2;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.21 : 0.44;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.48 : 0.49;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.09;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.32;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.4 : 0.23;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.24 : 0.47;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.51 : 0.51;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.1;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.34;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.43 : 0.28;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.29 : 0.5;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.55 : 0.55;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.09 : 0.18;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.09 : 0.43;
	      }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.11 : 0.06;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.06 : 0.19;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.24 : 0.26;
	    } else if (householdIncomeCat_NEW == 4) { 
	      increase = (MARRIED == 1) ? 0.0 : 0.01;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.0 : 0.21;
	    }
	  } // HISP 1
	} else if (ageCat == 5) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.28 : 0.0;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.03 : 0.3;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.34 : 0.33;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.04;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.31 : 0.01;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.05 : 0.32;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.36 : 0.35;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.05;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.35 : 0.09;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.12 : 0.37;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.41 : 0.41;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.0 : 0.15;
	      }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.09 : 0.0;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.0 : 0.13;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.17 : 0.18;
	    } else if (householdIncomeCat_NEW == 4) {
	      // increase = 0.0;
	    } else if (householdIncomeCat_NEW == 5) {
	      // increase = (MARRIED == 1) ? 0.0 : 0.0;
	    }
	  } // HISP 1
	} // ageCat 5
      } // sex 2

      record.put("INS_NEW_2014", getRandom() < increase ? "1" : "0");
      record.put("INS_NEW_2014_PROB", Double.toString(increase));
      String s = sex + ", " + ageCat + ", " + raceCat + ", " + HISP + ", " + householdIncomeCat_NEW
	       + ", " + MARRIED;
      record.put("Det", s);
    }

  ///addINS_NEW_2014_OR/////////////////////////////////////////////////////////////////////////////
  //
  // Maria Mayorga's instructions (edited for context): Create a new insurance variable for the
  // population input file that is read in by AnyLogic. “INS_NEW_2014”. According to the attributes
  // listed in “Pred_New_Ins_2014.csv”(sex, ageCat,HISP, householdIncomeCat_New, MARRIED –- from
  // person.getPerson) use the “increase” number to assign a binary variable “INS_NEW_2014” as
  // follows: Draw a random number from 0 to 1, say the result is “r”.  If r < increase then 1 else
  // 0. If there are individuals for which the attributes are not listed, set “increase” to 0.
  // 
  // For example for a person with sex=1, ageCat=3, raceCat=1, HISP=0, MARRIED=1, set the increase
  // to 0.37, draw a random number “r” if r < 0.37 set INS_NEW_2014 = 1, else 0.
  //
  // Some notes.  For some of the attributes multiple results use the same increase level.  For
  // example, for ageCat = 2 or 3 we use the same row, for raceCat = 1,2,3 if HISP = 1 then we use
  // the same row.
  //
  // NOTE (SAC 2016/05/01): This function must be called AFTER the various values used within
  // ("sex", etc.) have been put in the record parameter.
  //
  // NOTE (SAC 2016/05/17): similar to addINS_NEW_2014 except with Oregon probabilities.
  // 
  //////////////////////////////////////////////////////////////////////////////////////////////////
  private void addINS_NEW_2014_OR(HashMap<String,String> record, Person person) {
    Double increase = 0.0;
    int sex = Character.getNumericValue(record.get("sex").charAt(0));
    int ageCat = person.getAgeCat();
    int raceCat = person.getRaceCat();
    int HISP =  Character.getNumericValue(record.get("HISP").charAt(0));
    int householdIncomeCat_NEW = Character.getNumericValue(record.get("householdIncomeCat_NEW").
                                   charAt(0));
    int MARRIED =  Character.getNumericValue(record.get("MARRIED").charAt(0));
    
    if (sex == 1) { 
      if (ageCat == 2 || ageCat == 3) {
        if (HISP == 0) {
          if (raceCat == 1) {
            if (householdIncomeCat_NEW == 1) {
              increase = (MARRIED == 1) ? 0.53 : 0.47;
            } else if (householdIncomeCat_NEW == 2) {
              increase = (MARRIED == 1) ? 0.51 : 0.45;
            } else if (householdIncomeCat_NEW == 3) {
              increase = (MARRIED == 1) ? 0.54 : 0.51;
            } else if (householdIncomeCat_NEW == 4) {
              increase = (MARRIED == 1) ? 0.11 : 0.1;
            } else if (householdIncomeCat_NEW == 5) {
              increase = (MARRIED == 1) ? 0.28 : 0.27;
            }
          } else if (raceCat == 2) {
            if (householdIncomeCat_NEW == 1) {
              increase = (MARRIED == 1) ? 0.23 : 0.16;
            } else if (householdIncomeCat_NEW == 2) {
              increase = (MARRIED == 1) ? 0.47 : 0.4;
            } else if (householdIncomeCat_NEW == 3) {
              increase = (MARRIED == 1) ? 0.59 : 0.59;
            } else if (householdIncomeCat_NEW == 4) {
              increase = (MARRIED == 1) ? 0.07 : 0.05;
            } else if (householdIncomeCat_NEW == 5) {
              increase = (MARRIED == 1) ? 0.27 : 0.26;
            }
          } else if (raceCat == 3) {
            if (householdIncomeCat_NEW == 1) {
              increase = (MARRIED == 1) ? 0.44 : 0.37;
            } else if (householdIncomeCat_NEW == 2) {
              increase = (MARRIED == 1) ? 0.5 : 0.44;
            } else if (householdIncomeCat_NEW == 3) {
              increase = (MARRIED == 1) ? 0.48 : 0.43;
            } else if (householdIncomeCat_NEW == 4) {
              increase = (MARRIED == 1) ? 0.11 : 0.09;
            } else if (householdIncomeCat_NEW == 5) {
              increase = (MARRIED == 1) ? 0.28 : 0.27;
            }
          } // raceCat 3
        } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.37 : 0.29;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.34 : 0.26;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.47 : 0.41;
	    } else if (householdIncomeCat_NEW == 4) {
	      increase = (MARRIED == 1) ? 0.11 : 0.09;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.26 : 0.24;
	    }
	  }
	} else if (ageCat == 4) {
          if (HISP == 0) {
            if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.7 : 0.65;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.66 : 0.6;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.63 : 0.58;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.42 : 0.39;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.54 : 0.53;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.61 : 0.54;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.73 : 0.7;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.74 : 0.74;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.41 : 0.39;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.55 : 0.55;
	      }
            } else if (raceCat == 3) {
              if (householdIncomeCat_NEW == 1) {
                increase = (MARRIED == 1) ? 0.61 : 0.53;
              } else if (householdIncomeCat_NEW == 2) {
                increase = (MARRIED == 1) ? 0.64 : 0.57;
              } else if (householdIncomeCat_NEW == 3) {
                increase = (MARRIED == 1) ? 0.51 : 0.42;
              } else if (householdIncomeCat_NEW == 4) {
                increase = (MARRIED == 1) ? 0.4 : 0.37;
              } else if (householdIncomeCat_NEW == 5) {
                increase = (MARRIED == 1) ? 0.54 : 0.53;
              }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.58 : 0.49;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.51 : 0.41;
	    } else if (householdIncomeCat_NEW == 3) {
	    } else if (householdIncomeCat_NEW == 4) { 
	      increase = (MARRIED == 1) ? 0.42 : 0.39;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.52 : 0.5;
	    }
	  }
	} else if (ageCat == 5) {
          if (HISP == 0) {
            if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.61 : 0.57;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.61 : 0.56;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.57 : 0.54;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.23 : 0.22;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.37 : 0.36;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.4 : 0.31;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.61 : 0.56;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.0 : 0.64;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.2 : 0.18;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.37 : 0.36;
	      }
	    } else if (raceCat == 3) {
              if (householdIncomeCat_NEW == 1) {
                increase = (MARRIED == 1) ? 0.59 : 0.54;
              } else if (householdIncomeCat_NEW == 2) {
                increase = (MARRIED == 1) ? 0.63 : 0.6;
              } else if (householdIncomeCat_NEW == 3) {
                increase = (MARRIED == 1) ? 0.55 : 0.5;
              } else if (householdIncomeCat_NEW == 4) {
                increase = (MARRIED == 1) ? 0.23 : 0.22;
              } else if (householdIncomeCat_NEW == 5) {
                increase = (MARRIED == 1) ? 0.37 : 0.37;
              }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.51 : 0.44;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.5 : 0.42;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.51 : 0.45;
	    } else if (householdIncomeCat_NEW == 4) {
	      increase = (MARRIED == 1) ? 0.23 : 0.22;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.36 : 0.34;
	    }
	  } // HISP 1
	} // ageCat 5
      } else if (sex == 2) {
	if (ageCat == 2 || ageCat == 3) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.51 : 0.52;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.49 : 0.51;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.53 : 0.54;
	      } else if (householdIncomeCat_NEW == 4) {
		// increase = (MARRIED == 1) ? 0.11 : 0.11;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.28 : 0.28;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.21 : 0.23;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.45 : 0.47;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.59 : 0.59;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.07 : 0.07;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.27 : 0.27;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.41 : 0.43;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.48 : 0.5;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.47 : 0.48;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.1 : 0.11;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.28 : 0.28;
	      }
	    }
          } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.34 : 0.36;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.31 : 0.34;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.45 : 0.46;
	    } else if (householdIncomeCat_NEW == 4) {
	      // increase = (MARRIED == 1) ? 0.1 : 0.11;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.25 : 0.26;
	    }
	  } // HISP 1
	} else if (ageCat == 4) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.68 : 0.69;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.64 : 0.66;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.61 : 0.63;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.41 : 0.42;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.54 : 0.54;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.59 : 0.61;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.72 : 0.73;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.74 : 0.74;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.41 : 0.41;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.55 : 0.55;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.59 : 0.6;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.62 : 0.64;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.48 : 0.51;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.39 : 0.4;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.54 : 0.54;
	      }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.55 : 0.57;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.48 : 0.5;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.49 : 0.52;
	    } else if (householdIncomeCat_NEW == 4) { 
	      increase = (MARRIED == 1) ? 0.41 : 0.41;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.52 : 0.52;
	    }
	  } // HISP 1
	} else if (ageCat == 5) {
	  if (HISP == 0) {
	    if (raceCat == 1) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.6 : 0.61;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.59 : 0.61;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.56 : 0.57;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.22 : 0.23;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.37 : 0.37;
	      }
	    } else if (raceCat == 2) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.37 : 0.39;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.59 : 0.6;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 1.0 : 0.0;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.19 : 0.2;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.36 : 0.37;
	      }
	    } else if (raceCat == 3) {
	      if (householdIncomeCat_NEW == 1) {
		increase = (MARRIED == 1) ? 0.57 : 0.58;
	      } else if (householdIncomeCat_NEW == 2) {
		increase = (MARRIED == 1) ? 0.62 : 0.63;
	      } else if (householdIncomeCat_NEW == 3) {
		increase = (MARRIED == 1) ? 0.54 : 0.55;
	      } else if (householdIncomeCat_NEW == 4) {
		increase = (MARRIED == 1) ? 0.23 : 0.23;
	      } else if (householdIncomeCat_NEW == 5) {
		increase = (MARRIED == 1) ? 0.37 : 0.37;
	      }
	    } 
	  } else if (HISP == 1) {
	    if (householdIncomeCat_NEW == 1) {
	      increase = (MARRIED == 1) ? 0.48 : 0.5;
	    } else if (householdIncomeCat_NEW == 2) {
	      increase = (MARRIED == 1) ? 0.48 : 0.5;
	    } else if (householdIncomeCat_NEW == 3) {
	      increase = (MARRIED == 1) ? 0.49 : 0.5;
	    } else if (householdIncomeCat_NEW == 4) {
	      increase = (MARRIED == 1) ? 0.22 : 0.23;
	    } else if (householdIncomeCat_NEW == 5) {
	      increase = (MARRIED == 1) ? 0.35 : 0.35;
	    }
	  } // HISP 1
	} // ageCat 5
      } // sex 2

      record.put("INS_NEW_2014", getRandom() < increase ? "1" : "0");
      record.put("INS_NEW_2014_PROB", Double.toString(increase));
      String s = sex + ", " + ageCat + ", " + raceCat + ", " + HISP + ", " + householdIncomeCat_NEW
	       + ", " + MARRIED;
      record.put("Det", s);
    }

    private void addUrbanRuralDesignation(HashMap<String, String> record) {
      String zip = record.get("zipcode");
      String desig = this._urbanRuralMap.get(zip);
      record.put("Desig", desig);
    };

  }


  /**
   *
   * Read JSON data. 
   *
   */
  class JSONReader {

    /**
     * Read JSON from string.
     */
    public HashMap readJSONFromString (String text) {
	  return this.readJSON (new StringReader (text));
    }
    
    /**
     * Read JSON from reader.
     */
    public HashMap readJSON (Reader reader) {
	  HashMap<String,String> result = new HashMap<String,String> ();
	  try {
	    JSONObject object = new JSONObject (new JSONTokener (reader));
	    String [] names = JSONObject.getNames (object);
	    for (int c = 0; c < names.length; c++) {
		  String name = names [c];
		  result.put (name, object.getString (name));
	    }
	  } catch (JSONException e) {
	    e.printStackTrace ();
	  }
	  return result;
    }

  }

  class QuotelessWriter extends FilterWriter {
    public QuotelessWriter (Writer writer) {
	  super (writer);
    }
    public void write (int c) throws IOException {
	  if (c != '"') {
	    super.write (c);
	  }
    }
  }

  /**
   * Write to CSV format.
   * Use a buffered writer flushing on a set period and on close.
   */
  class PopulationWriter {

    private CSVWriter _writer;
    private char _separator;
    private long _writeCount = 0;
    private final int flushPeriodicity = 50;

    /**
     * Create a population writer
     */
    public PopulationWriter (Writer writer, char separator) {
	  this._writer = new CSVWriter (new BufferedWriter (new QuotelessWriter (writer)),
				      separator,
				      CSVWriter.NO_QUOTE_CHARACTER);
    }

    /**
     * Write data 
     */
    public void write (String [] data) throws IOException {
	  if (this._writeCount++ % flushPeriodicity == 0) {
	    this._writer.flush ();
	  }
	  this._writer.writeNext (data);
    }
    
    /**
     * Close the CSV writer.
     */
    public void close () throws IOException {
	  this._writer.flush ();
	  this._writer.close ();
    }
  }

  /**
   *
   * Encapsulate semantics of synthetic population and format translation.
   *
   */
  public class CSVProcessor {

    private Processor _processor;
    private CSVReader _input;
    private PopulationWriter _writer;
    private static Log logger = LogFactory.getLog (CSVProcessor.class);
    private HashMap<String, String> urbanRuralMap;

    /**
     * Create a new population processor
     */
    public CSVProcessor (Reader input,
			   char inputSeparator,
			   Processor processor,
			   Writer output,
			   char outputSeparator) throws IOException 
    {
	  assert input != null : "Input reader must be non-null.";
	  //assert inputSeparator != null : "Input separator must be non-null.";
	  assert processor != null : "Data processor must be non-null.";
	  assert output != null : "Output writer must be non-null.";
	  //assert outputSeparator != null : "Output separator must be non-null.";

	  this._input = new CSVReader (input, inputSeparator);
	  this._processor = processor;
	  this._writer = new PopulationWriter (output, outputSeparator);
	  this._writer.write (processor.getOutputKeys ());
    }

    /**
     *
     * Return the map that maps zip codes to urban/rural designations
     *
     *
    public HashMap<String, String>getUrbanRuralMap() {
        return this.urbanRuralMap;
    }
*/

    /**
     *
     * Parse an input CSV file.
     * Process each line using the processor object.
     *
     */
    public void parse () throws IOException {
	  String [] header = null;
	  int c = 0;

	  HashMap<String, String> record = new HashMap<String,String> (20, 0.75f);
	  for (String [] line = this._input.readNext (); line != null; line = this._input.readNext ()) {
	    c++;

	    if (header != null && line.length != header.length)
		  continue;

	    if (header == null) {
		  this.logger.info ("setting header " + StringUtils.join (line, ","));
		  header = line;
		  this._processor.setHeader (line);
	    } else {
		  record.clear ();
		  String [] outputLine = this._processor.process (line, record);
		  if (outputLine != null) {
		    this._writer.write (outputLine);
		  }
	    }
	  }
    }

    /**
     * Close
     */
    public void close () throws IOException {
	  this._writer.close ();
    }
  }

