package org.renci.epi.util;

import java.util.Map;

public interface Calibrate {
    public void onInitExperiment ();
    public void onInitRun (Object root);
    public void onRunComplete (Object root);
    public void onIterationComplete ();
    public Map<String, Object> getOptimalProperties ();
}

