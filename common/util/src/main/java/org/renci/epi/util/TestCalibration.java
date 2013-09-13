package org.renci.epi.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestCalibration implements Calibration {

    private double _value;

    private double _min;
    private double _max;

    public TestCalibration (double min, double max) {
	_min = min;
	_max = max;
	_value = min;
    }

    public double getValue () {
	_value += 1;
	return _value;
    }

}
