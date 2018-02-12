package org.renci.epi.population.insurance;

import java.util.*;

/**
 * Representation of a population member for purposes
 * of insurance status assignment.
 */
public class Person {
    // Return the same pseudorandom sequence in each build:
    static Random r = new Random (625345);
    static double random = r.nextDouble();

    short ageCat = -1;
    short householdIncomeCat = -1;
    short householdSizeCat = -1;
    short raceCat = -1;
    short sexCat = -1;

    public double getRandom () {
	return r.nextDouble();  // Same sequence each build
        //return Math.random(); // Returns a different sequence for each build
    }
    public short getAgeCat () {
	return ageCat;
    }
    public short getHouseholdIncomeCat () {
	return householdIncomeCat;
    }
    public short getHouseholdSizeCat () {
	return householdSizeCat;
    }
    public short getRaceCat () {
	return raceCat;
    }
    public short getSexCat () {
	return sexCat;
    }

    /**
     * Construct a new person.
     */
    Person () {
    }
    Person (short ageCat,
	    short householdIncomeCat,
	    short householdSizeCat,
	    short raceCat,
	    short sexCat) {
	this.ageCat = ageCat;
	this.householdIncomeCat = householdIncomeCat;
	this.householdSizeCat = householdSizeCat;
	this.raceCat = raceCat;
	this.sexCat = sexCat;
    }
    
    /**
     * Create a new insured person.
     */
    public static final Person getPerson (int age, 
					  int householdIncome,
					  int householdSize,
					  int white,
					  int black,
					  int sex)
    {
	//assert (age > 0) : "Age must be greater than zero.";
	assert (householdSize > 0) : "Household size must be greater than zero.";
	assert (sex == 1 || sex == 2) : "Sex must be 1 or 2.";
	
	// age
	short ageCat = -1;
	if (age < 25) {
	    ageCat = 1;
	} else if (age < 35) {
	    ageCat = 2;
	} else if (age < 45) {
	    ageCat = 3;
	} else if (age < 55) {
	    ageCat = 4;
	} else if (age < 65) {
	    ageCat = 5;
	} else {
	    ageCat = 6;
	}

	// household income
	short householdIncomeCat = -1;
	if (householdIncome < 25000) {
	    householdIncomeCat = 1;
	} else if (householdIncome <= 50000) {
	    householdIncomeCat = 2;
	} else {
	    householdIncomeCat = 3;
	}

	// household size
	short householdSizeCat = -1;
	switch (householdSize) {
	case 1:
	case 2:
	    householdSizeCat = 1;
	    break;
	case 3:
	case 4:
	case 5:
	    householdSizeCat = 2;
	    break;
	default:
	    householdSizeCat = 3;
	}

	// race
	short raceCat = -1;
	if (white == 1) {
	    raceCat = 1;
	} else if (black == 1) {
	    raceCat = 2;
	} else {
	    raceCat = 3;
	}

	// sex
	short sexCat = (short)sex;

	return new Person (ageCat, householdIncomeCat, householdSizeCat, raceCat, sexCat);
    }
}
