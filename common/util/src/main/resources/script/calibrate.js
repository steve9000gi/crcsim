function Calibration () {
    this.max_lifespan = 0;
    this.optimal_settings = {};
};
Calibration.prototype.onInitExperiment = function (root) {
    logger.info ("------> calibration.onInitExperiment (root) called.\n");
};
Calibration.prototype.onInitRun = function (root) {
    logger.info ("------> calibration.onInitRun(root) called.\n");
};
Calibration.prototype.onRunComplete = function (root) {
    logger.info ("------> calibration.onRunComplete(root) called.\n");
    var lifespan = 4;
    if (lifespan > this.max_lifespan) {
	this.max_lifespan = lifespan;
	this.optimal_settings = {
	    diagnostic_compliance_rate : 3,
	    cost_treatment             : 2
	}
    }
};
Calibration.prototype.onIterationComplete = function (root) {
    logger.info ("------> calibration.onIterationComplete(root) called.\n");
};
Calibration.prototype.getOptimalSettings = function (settings) {
    loggger.info ("------> calibration.getOptimalSettings(settings) called\n");
    for (key in this.optimal_settings) {
	settings.put (key, this.optimal_settings [key]);
    }
}
var calibrate = new Calibration ();