package al6utils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;
import java.io.FileReader;
import java.io.IOException;


/**
 * <p>Implementation of the {@code Config} interface that takes its configuration data from a
 * JSON-formatted text file.</p>
 *
 * <p>Suppose a JSON file named myconfig.json.txt contains the following:</p>
 *
 * <pre>
 *   {
 *     "maxTimeSteps": 100,
 *     "enableQuarantine": true,
 *     "exposedPMF": [0.2, 0.8]
 *   }
 * </pre>
 *
 * <p>Then you might use {@code ConfigJSON} like this:</p>
 *
 * <pre>
 *   Config c = new ConfigJSON("myconfig.json.txt");
 *   int endTime = c.getInt("maxTimeSteps");
 *   boolean enableQuarantine = c.getBoolean("enableQuarantine")) {
 *   double[] pmf = c.getDoubleArray("exposedPMF");
 * </pre>
 *
 * <p>To use {@code ConfigJSON} in your model, you must add the json.jar library
 * to your classpath.</p>
 */

public class ConfigJSON extends AbstractConfig implements Config {
    /**
     * Constructs a new ConfigJSON, given the name of a JSON-formatted file.
     *
     * @param configFilename name of the JSON-formatted file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public ConfigJSON(String configFilename) throws IOException {
        try {
            config = new JSONObject(new JSONTokener(new FileReader(configFilename)));
        }
        catch (JSONException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean hasKey(String key) {
        return config.has(key);
    }

    public String getString(String key) {
        try {
            return config.getString(key);
        }
        catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String[] getStringArray(String key) {
        try {
            JSONArray array = config.getJSONArray(key);

            String[] values = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                values[i] = array.getString(i);
            }

            return values;
        }
        catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private JSONObject config;
}
