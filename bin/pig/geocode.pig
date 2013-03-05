/**
 *
 *
 *
 */

DEFINE renci_geocoder org.renci.epi.pig.GEOCODE ('timeslice', 'longitude', 'latitude');

-- load data.
all_rows = LOAD '$input' USING PigStorage ('\t') 
      AS (cancer_free_years : float,
          cost_diagnostic   : float,
	  death_age	    : float,
	  lost_years	    : float,
	  num_lesions	    : int,
	  never_compliant   : chararray,
	  onset_age_clin    : chararray,
	  onset_age_polyp   : chararray,
	  onset_stage_clin  : chararray,
	  surveillance_negatives : int,
	  tot_lesions		 : int,
	  value_life		 : float,
	  longitude 		 : float,
	  latitude 		 : float);

-- filter
target_rows = FILTER all_rows BY (num_lesions > 0) AND (never_compliant == 'true');

-- transform data
output_polygons = FOREACH target_rows GENERATE renci_geocoder ($timeslice, longitude, latitude);

-- group output polygons 
polygons = GROUP output_polygons BY $0;

-- count polygons per timeslice
polygon_count = FOREACH polygons GENERATE group, '$input', COUNT (output_polygons);
DUMP polygon_count;
DESCRIBE polygon_count;


/*
subtotals = FOREACH polygon_count GENERATE group, SUM ($2);
DUMP subtotals;
DESCRIBE subtotals;
*/

-- store output
--STORE polygon_count INTO 'output' USING JsonStorage ();


