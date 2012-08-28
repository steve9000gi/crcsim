package org.renci.epi.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Utility class providing methods implementing math routines that are not
 * available in {@code java.lang.Math}.
 */

public class Math {
    /**
     * Empty private constructor to prevent instantiation of this class.
     */
    private Math() {}

    /**
     * <p>Returns the {@code double} value that is closest to the specified
     *  value and is equal to a multiple of the specified rounding unit. The
     *  {@code java.lang.Math.round} implementation allows rounding only to the
     *  nearest integer. It is equivalent to round(x, 1). The following table
     *  provides some examples:</p>
     *
     * <table border="1" cellpadding="2">
     *   <thead>
     *     <tr><th>x</th><th>unit</th><th>round(x, unit)</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr><td>13.26</td><td>0.1</td><td>13.3</td></tr>
     *     <tr><td>13.26</td><td>3</td><td>12</td></tr>
     *     <tr><td>1326</td><td>100</td><td>1300</td></tr>
     *     <tr><td>-13.26</td><td>1</td><td>-13</td></tr>
     *   </tbody>
     * </table>
     *
     * @param x value to be rounded
     * @param unit rounding unit. Must be > 0.
     * @return the rounded value
     */
    static public double round(double x, double unit) {
        if (unit > 0) {
            return java.lang.Math.round(x / unit) * unit;
        }
        else {
            throw new IllegalArgumentException("unit is not positive");
        }
    }

    /**
     * <p>Returns the smallest {@code double} value that is larger than the
     * specified value and is equal to a multiple of the specified rounding
     * unit. The {@code java.lang.Math.ceil} implementation returns values that
     * are multiples of integers only. It is equivalent to ceil(x, 1). The
     * following table provides some examples:</p>
     *
     * <table border="1" cellpadding="2">
     *   <thead>
     *     <tr><th>x</th><th>unit</th><th>ceil(x, unit)</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr><td>13.26</td><td>0.1</td><td>13.3</td></tr>
     *     <tr><td>13.26</td><td>3</td><td>15</td></tr>
     *     <tr><td>1326</td><td>100</td><td>1400</td></tr>
     *     <tr><td>-13.26</td><td>1</td><td>-13</td></tr>
     *   </tbody>
     * </table>
     *
     * @param x value whose ceiling should be returned
     * @param unit rounding unit. Must be > 0.
     * @return the ceiling value
     */
    static public double ceil(double x, double unit) {
        if (unit > 0) {
            return java.lang.Math.ceil(x / unit) * unit;
        }
        else {
            throw new IllegalArgumentException("unit is not positive");
        }
    }

    /**
     * <p>Returns the largest {@code double} value that is smaller than the
     * specified value and is equal to a multiple of the specified rounding
     * unit. The {@code java.lang.Math.floor} implementation returns values that
     * are multiples of integers only. It is equivalent to floor(x, 1). The
     * following table provides some examples:</p>
     *
     * <table border="1" cellpadding="2">
     *   <thead>
     *     <tr><th>x</th><th>unit</th><th>floor(x, unit)</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr><td>13.26</td><td>0.1</td><td>13.2</td></tr>
     *     <tr><td>13.26</td><td>3</td><td>12</td></tr>
     *     <tr><td>1326</td><td>100</td><td>1300</td></tr>
     *     <tr><td>-13.26</td><td>1</td><td>-14</td></tr>
     *   </tbody>
     * </table>
     *
     * @param x value whose ceiling should be returned
     * @param unit rounding unit. Must be > 0.
     * @return the ceiling value
     */
    static public double floor(double x, double unit) {
        if (unit > 0) {
            return java.lang.Math.floor(x / unit) * unit;
        }
        else {
            throw new IllegalArgumentException("unit is not positive");
        }
    }

    /**
     * Returns the median of an array of {@code double} values. If the array
     * contains an even number of elements, the median is computed as the
     * arithmetic mean between the two middle elements.
     *
     * @param list array of double values
     * @param sorted {@code true} if the array is already sorted in ascending
     *        numeric order. If {@code false}, the array will be sorted in place
     *        before computing the median.
     * @return the median of the array elements
     */
    static public double median(double[] list, boolean sorted) {
        if (list.length == 0) {
            throw new IllegalArgumentException("list is empty");
        }

        if (!sorted) {
            Arrays.sort(list);
        }

        int midpoint = list.length / 2;

        if (list.length % 2 == 0) {
            double lower = list[midpoint - 1];
            double upper = list[midpoint];
            return (lower + upper) / 2;
        }
        else {
            return list[midpoint];
        }
    }

    /**
     * Returns the median of a {@code List} array of {@code Double} values. If
     * the list contains an even number of elements, the median is computed as
     * the arithmetic mean between the two middle elements.
     *
     * @param list {@code List} of {@code Double} values
     * @param sorted {@code true} if the list is already sorted in ascending
     *        numeric order. If {@code false}, the list will be sorted in place
     *        before computing the median.
     * @return the median of the list elements
     */
    static public Double median(List<Double> list, boolean sorted) {
        if (list.size() == 0) {
            throw new IllegalArgumentException("list is empty");
        }


        if (!sorted) {
            Collections.sort(list);
        }

        int midpoint = list.size() / 2;

        if (list.size() % 2 == 0) {
            double lower = list.get(midpoint - 1);
            double upper = list.get(midpoint);
            return new Double((lower + upper) / 2.0);
        }
        else {
            return list.get(midpoint);
        }
    }
}
