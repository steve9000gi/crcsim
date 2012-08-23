package al6utils;

/**
 * Skeletal implementation of the Config interface to ease the effort in
 * implementing Config.
 */

public abstract class AbstractConfig implements Config {
    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractConfig() {}


    public abstract boolean hasKey(String key);


    //----------------------------------------------------------------------
    // Methods returning a single value.

    public abstract String getString(String key);

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }

    /**
     * Returns the boolean value associated with the specified key. Valid boolean
     * values are described in {@link BooleanParser#parse(String)}.
     *
     * @param  key  key whose associated value is to be returned
     * @return the boolean value associated with the given key
     */
    public boolean getBoolean(String key) {
        return BooleanParser.parse(getString(key));
    }


    //----------------------------------------------------------------------
    // Methods returning an array of values.

    public abstract String[] getStringArray(String key);

    public int[] getIntArray(String key) {
        String[] stringValues = getStringArray(key);
        int[] values = new int[stringValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Integer.parseInt(stringValues[i]);
        }
        return values;
    }

    public double[] getDoubleArray(String key) {
        String[] stringValues = getStringArray(key);
        double[] values = new double[stringValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Double.parseDouble(stringValues[i]);
        }
        return values;
    }

    /**
     * Returns the array of boolean values associated with the specified
     * key. Valid boolean values are described in {@link
     * BooleanParser#parse(String)}.
     *
     * @param  key  key whose associated array is to be returned
     * @return the boolean array associated with the given key
     */
    public boolean[] getBooleanArray(String key) {
        String[] stringValues = getStringArray(key);
        boolean[] values = new boolean[stringValues.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = BooleanParser.parse(stringValues[i]);
        }
        return values;
    }
}
