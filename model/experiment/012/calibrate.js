importPackage (com.xj.anylogic.engine);

function Calibration () {
    this.max_lifespan = 0;
    this.optimal_settings = {};
};

/**
 *
 */
Calibration.prototype.onInitExperiment = function () {
    logger.debug ("--> calibration.onInitExperiment() called.\n");
};
Calibration.prototype.onInitRun = function (root) {
    logger.debug ("--> calibration.onInitRun(root) called.\n");
    /**
     * Example of configuring AnyLogic solvers.
     * Sets the DAE solver to Euler (the default).
     * See: http://www.anylogic.com/anylogic/help/index.jsp?topic=/com.xj.anylogic.help/html/javadoc/com/xj/anylogic/engine/Engine.html
     */
    root.getEngine().setSolverDAE (Engine.SOLVER_DAE_EULER_NEWTON);
    root.model_initial_compliance = false;
    root.use_conditional_compliance = false;
};
Calibration.prototype.onRunComplete = function (root) {
    logger.debug ("--> calibration.onRunComplete(root) called.\n");
    var lifespan = root.population.people.average ('lifespan');
    if (lifespan > this.max_lifespan) {
	this.max_lifespan = lifespan;
	this.optimal_settings = {
	    diagnostic_compliance_rate : root.diagnostic_compliance_rate,
	    cost_treatment             : root.cost_treatment
	};
	print ("optimal => " + this.optimal_settings);
    }
};
Calibration.prototype.onIterationComplete = function () {
    print ("--> calibration.onIterationComplete(root) called.\n");
};
Calibration.prototype.getOptimalSettings = function (settings) {
    print ("--> calibration.getOptimalSettings(settings) called\n");
    for (key in this.optimal_settings) {
	settings.put (key, this.optimal_settings [key]);
    }
}

var calibrate = new Calibration ();