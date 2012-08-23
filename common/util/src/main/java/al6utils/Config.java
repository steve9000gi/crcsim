package al6utils;

/**
 * An object that provides read access to configuration data. The data is
 * assumed to be accessible as a set of key-value pairs, where the value may be a
 * single value or an array of values.
 */

public interface Config {

    /**
     * Returns {@code true} if the given key exists.
     *
     * @param key a key name
     * @return {@code true} if the given key exists.
     */
    boolean hasKey(String key);


    //----------------------------------------------------------------------
    // Methods returning a single value.

    /**
     * Returns the String value associated with the specified key.
     *
     * @param key key whose associated value is to be returned
     * @return the String value associated with the given key
     */
    String getString(String key);

    /**
     * Returns the int value associated with the specified key.
     *
     * @param key key whose associated value is to be returned
     * @return the int value associated with the given key
     */
    int getInt(String key);

    /**
     * Returns the double value associated with the specified key.
     *
     * @param key key whose associated value is to be returned
     * @return the double value associated with the given key
     */
    double getDouble(String key);

    /**
     * Returns the boolean value associated with the specified key.
     *
     * @param key key whose associated value is to be returned
     * @return the boolean value associated with the given key
     */
    boolean getBoolean(String key);


    //----------------------------------------------------------------------
    // Methods returning an array of values.

    /**
     * Returns the array of String values associated with the specified key.
     *
     * @param key key whose associated array is to be returned
     * @return the String array associated with the given key
     */
    String[] getStringArray(String key);

    /**
     * Returns the array of int values associated with the specified key.
     *
     * @param key key whose associated array is to be returned
     * @return the int array associated with the given key
     */
    int[] getIntArray(String key);

    /**
     * Returns the array of double values associated with the specified key.
     *
     * @param key key whose associated array is to be returned
     * @return the double array associated with the given key
     */
    double[] getDoubleArray(String key);

    /**
     * Returns the array of boolean values associated with the specified key.
     *
     * @param key key whose associated array is to be returned
     * @return the boolean array associated with the given key
     */
    boolean[] getBooleanArray(String key);
}
