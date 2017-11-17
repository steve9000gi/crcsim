
/**
 *
 * Drawing random iteration parameters. Design considerations:
 *   (a) Allow this to happen outside of the framework of a specific modeling tool such as AnyLogic.
 *   (b) Provide access to a large number of predefined distributions.
 *   (c) Provide the flexibility to add custom distributions if needed.
 *   (d) Provide a simple approach to configuring iterations.
 *
 * Approach:
 *   (a) Provide a Java interface for creating a list of iteration parameter sets.
 *   (b) In its implementation, embed a JavaScript interpreter.
 *   (c) Provide a JavaScript library that
 *       i) Imports the apache commons math3 distribution package
 *      ii) Provides a utility for drawing values based on a matrix of distributions
 *   (d) Allow the user to create a matrix defining parameters and their distributions
 *
 */
load("nashorn:mozilla_compat.js"); // See https://bugs.openjdk.java.net/browse/JDK-8025132

importPackage (org.apache.commons.math3.distribution); 

/**
 * Utility function for capitalizing the first letter of a string.
 */
function capitaliseFirstLetter (string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

/**
 * Utilities for dealing with stochasticity.
 *
 */
function RandomUtils () {
    this.distributions = {};
};

/**
 *
 * Draw a random number given a distribution type, a parameter name and arguments.
 *
 */
RandomUtils.prototype.draw = function (distributionType, parameterName, args) { 
    if ( ! (parameterName in this.distributions)) { 
	var codeText = [ 'new ', capitaliseFirstLetter (distributionType), 'Distribution (' ];
	for (var c = 2; c < args.length; c++) {
	    codeText.push (args [c]);
	    if (c < (args.length - 1)) {
		codeText.push (',');
	    }
	}
	codeText.push (')');
	var code = codeText.join ('');
	print (code + newline); 
	this.distributions [parameterName] = eval (code); 
    } 
    return this.distributions [parameterName].sample (); 
}; 

/**
 *
 * Generate iteration parameter sets given an configuratoin matrix.
 * @param numIterations The number of parameter sets to generate.
 * @param variableParameters The matrix of parameters. Example matrix:
 *
 *   var iterationParameters = [ 
 *       [ 'param-A', 'gamma', 0.9, 2.9 ], 
 *       [ 'param-B', 'beta', 2, 0.9 ]
 *   ];
 *
 * @param iterations A java.util.List into which the resulting parameter sets will be inserted as strins.
 *
 */
RandomUtils.prototype.generateIterations = function (numIterations, variableParameters, iterations) { 
    /**
     * Validate the input parameter set. 
     * Disallow duplicates.
     */
    var parameterNames = {};
    for (var c = 0; c < variableParameters.length; c++) {
	var row = variableParameters [c];
	if (row != null && row.length > 0) {
	    var parameterName = row [0];
	    print ('validating parameter: ' + parameterName + '\n');
	    if (parameterName in parameterNames) {
		throw 'Parameter: ' + parameterName + ' appears twice in the configuration matrix. ';
	    }
	    parameterNames [parameterName] = parameterName;
	}
    }
    /**
     * Calclate the iterations, generating parameter sets with values 
     * drawn from the specified distributions.
     */
    for (var i = 0; i < numIterations; i++) { 
	var iterationValues = []; 
	for (var c = 0; c < variableParameters.length; c++) { 
            //print ('c: ' + c + ' vp[c] ' + variableParameters [c] + ' vp: ' + variableParameters + newline); 
            var parameterName = variableParameters [c][0]; 
            var distributionMode = variableParameters [c][1]; 
            var value = this.draw (distributionMode, parameterName, variableParameters[c]);
            iterationValues.push ([ parameterName, '=', value ].join ('')); 
	}
	iterations.add (iterationValues.join (newline)); 
    } 
};

var randomUtils = new RandomUtils ();
