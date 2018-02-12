package al6utils;

import java.util.regex.Pattern;

/**
 * Utility class providing methods for creating booleans from other types.
 */

public class BooleanParser {
    /**
     * Empty private constructor to prevent instantiation of this class.
     */
    private BooleanParser() {}

    /**
     * Converts a {@code String} value to a {@code boolean} value. The following
     * {@code String} values are converted to {@code true}: "t", "true", "yes",
     * "y", "1". The following are converted to {@code false}: "f", "false",
     * "no", "n", "0". The conversion is case insensitive.
     *
     * @param value the {@code String} value
     * @return the {@code boolean} value associated with the {@code String}
     *         value
     */
    public static boolean parse(String value) {
        if (truePattern.matcher(value).matches()) {
            return true;
        }
        else if (falsePattern.matcher(value).matches()) {
            return false;
        }
        else {
            throw new RuntimeException(value + " is not a boolean");
        }
    }


    //--------------------------------------------------------------------------
    // Member variables.
    //

    private static final Pattern truePattern = Pattern.compile("t|true|y|yes|1", Pattern.CASE_INSENSITIVE);
    private static final Pattern falsePattern = Pattern.compile("f|false|n|no|0", Pattern.CASE_INSENSITIVE);
}
