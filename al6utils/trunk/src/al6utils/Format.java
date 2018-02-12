package al6utils;

import java.text.DecimalFormat;


/**
 * Utility class providing methods involving text formats.
 */

public class Format {
    /**
     * Empty private constructor to prevent instantiation of this class.
     */
    private Format() {}

    /**
     * Returns a {@code String} representation of the specified {@code double}
     * value, formatted to display the specified number of decimal places.
     *
     * @param value value to be formatted
     * @param places number of decimal places to include in the formatted value
     * @return the formatted representation of the input value
     */
    static public String decimalFormat(double value, int places) {
        if (places >= 0) {
            DecimalFormat fmt = null;

            if (places < Format.decimalPlaces.length) {
                fmt = Format.decimalPlaces[places];
            }
            else {
                String pattern = "0.";
                for (int i = 0; i < places; i++) {
                    pattern += "0";
                }

                fmt = new DecimalFormat(pattern);
            }

            return fmt.format(value);
        }
        else {
            throw new IllegalArgumentException("places is not positive");
        }
    }

    /**
     * Returns a {@code String} representation of the specified {@code double}
     * value, formatted using the specifed format string. The format string should
     * be a pattern recognized by {@code java.text.DecimalFormat}.
     *
     * @param value value to be formatted
     * @param pattern format pattern to apply to the value
     * @return the formatted representation of the input value
     */
    static public String decimalFormat(double value, String pattern) {
        DecimalFormat fmt = new DecimalFormat(pattern);
        return fmt.format(value);
    }

    /**
     * Array of {@code DecimalFormat} instances, where the <i>i</i><sup>th</sup>
     * element defines a format with <i>i</i> decimal places. The format pattern
     * takes the form "0.000", where the number of zeroes after the decimal is
     * equal to the format's index in the array.
     */
    public static final DecimalFormat[] decimalPlaces = {
        new DecimalFormat("0"),
        new DecimalFormat("0.0"),
        new DecimalFormat("0.00"),
        new DecimalFormat("0.000"),
        new DecimalFormat("0.0000"),
        new DecimalFormat("0.00000"),
        new DecimalFormat("0.000000"),
        new DecimalFormat("0.0000000"),
        new DecimalFormat("0.00000000")
    };
}
