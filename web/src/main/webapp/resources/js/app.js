var epiMap = null;

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

/**
 *
 * Epi Map - renders polygons on a Google map.
 * 
 * Applies counts for each polygon across a timeframe.
 *
 */
function EpiMap () {
    var center = new google.maps.LatLng (35.9131, -79.0561);
    var mapOptions = {
	center: center,
	zoom: 7,
	mapTypeId: google.maps.MapTypeId.TERRAIN //ROADMAP
    };
    this.map = new google.maps.Map ($("#map_canvas")[0], mapOptions);

    $.ajaxSetup({
	"error" : function (XMLHttpRequest, textStatus, errorThrown) {
	    console.log (textStatus + ' ' + errorThrown + ' ' + XMLHttpRequest.responseText);
	}
    });

    this.gradient = new Rainbow (); // by default, range is 0 to 100
    this.frame = 0;
    this.occurrences = null;
    this.polygons = [];
    this.initializeMap ();

    $("#menu").click (this.animateMap);

};

EpiMap.prototype.setOccurrences = function (occurrences) {
    var map = epiMap;
    map.occurrences = occurrences;
    var max = 0;
    var endpoint = map.occurrences.counts [map.occurrences.counts.length - 1];
    for (var c = 0; c < endpoint.length; c++) {
	var value = endpoint [c];
	max = Math.max (value, max);
    }
    console.log ("configuring max: " + max);
    map.gradient.setNumberRange (0, max);
    map.gradient.setSpectrum ('blue', 'green', 'yellow', 'red');
};
EpiMap.prototype.animateMap = function () {
    var map = epiMap;
    map.frame = 35; //0;
    map.nextFrame ();
};

EpiMap.prototype.initializeMap = function (map) {
    $.getJSON ("resources/occurrences.json", function (occurrences) {
	epiMap.setOccurrences (occurrences);
	$.getJSON ("resources/polygon-index.json", function (obj) {
	    for (var c = 0; c < obj.index.length; c++) {
		var polygonName = obj.index [c];
		$.getJSON ("resources/" + polygonName, function (polygon) {		    
		    epiMap.processPolygons (polygon,
					    occurrences.counts [epiMap.frame][polygon.count],
					    map);
		});
	    }
	});
    });
};

EpiMap.prototype.nextFrame = function () {
    var map = epiMap;
    $("#status").html (map.frame + "%");
    $("#progress").progressbar ({
	value : map.frame + 1
    });
    if (map.frame < map.occurrences.counts.length) {
	//console.log (map.frame + ' => ' + map.occurrences.counts [map.frame]);
	for (var c = 0; c < map.polygons.length; c++) {
	    var polygon = map.polygons [c];
	    polygon.polygon.setOptions ({
		fillColor     : map.getColorForValue (map.occurrences.counts [map.frame][polygon.index])
	    });
	}
    }
    map.frame++;
    if (map.frame <= map.occurrences.counts.length) {
	setTimeout ("epiMap.nextFrame ()", 200);
    }
};

EpiMap.prototype.getColorForValue = function (value) {
    var color = '#' + epiMap.gradient.colorAt (value);
    console.log ('color: ' + value + " " + color);
    return color;
};
EpiMap.prototype.getColorForValue0 = function (value) {

    //http://techxplorer.com/2010/01/29/building-a-colour-gradient-in-javascript/
    var color = "YellowGreen";
    if (value > 0) {
	color = "lightblue";
    }
    if (value > 5) {
	color = "blue";
    }
    if (value > 10) {
	color ="green";
    }
    if (value > 15) {
	color ="yellow";
    }
    if (value > 20) {
	color = "orange";
    }
    if (value > 25) {
	color = "crimson";
    }
    return color;

};

EpiMap.prototype.processPolygons = function (polygon, count) {
    var coordinates = [];
    for (var q = 0; q < polygon.points.length; q++) {
	var point = polygon.points [q];
	var latlng = new google.maps.LatLng (point [1], point [0]);
	coordinates.push (latlng);
    }
    var visualPolygon = epiMap.renderPolygon (coordinates, count);
    this.polygons.push ({
	polygon : visualPolygon,
	index   : polygon.count
    });
    this.polygons.sort (function (a, b) {
	return a.index - b.index;
    });
};

EpiMap.prototype.renderPolygon = function (points, count, map) {    
    var fillColor = epiMap.getColorForValue (count);
    var polygon = new google.maps.Polygon ({
	paths         : points,
	strokeColor   : "#000", //"#FF0000",
	strokeOpacity : 0.5,
	strokeWeight  : 0.5,
	fillColor     : fillColor,
	fillOpacity   : 0.7
    });    
    polygon.setMap (this.map);
    return polygon;
};


function initialize() {
    //    $("#dialog").dialog ();
    epiMap = new EpiMap ();
}


/**
 *
 *
 *
 */
/*
RainbowVis-JS 
Released under MIT License
*/

function Rainbow()
{
	var gradients = null;
	var minNum = 0;
	var maxNum = 100;
	var colours = ['ff0000', 'ffff00', '00ff00', '0000ff']; 
	setColours(colours);

	function setColours (spectrum) 
	{
		if (spectrum.length < 2) {
			throw new Error('Rainbow must have two or more colours.');
		} else {
			var increment = (maxNum - minNum)/(spectrum.length - 1);
			var firstGradient = new ColourGradient();
			firstGradient.setGradient(spectrum[0], spectrum[1]);
			firstGradient.setNumberRange(minNum, minNum + increment);
			gradients = [ firstGradient ];

			for (var i = 1; i < spectrum.length - 1; i++) {
				var colourGradient = new ColourGradient();
				colourGradient.setGradient(spectrum[i], spectrum[i + 1]);
				colourGradient.setNumberRange(minNum + increment * i, minNum + increment * (i + 1)); 
				gradients[i] = colourGradient; 
			}

			colours = spectrum;
		}
	}
	this.setColors = this.setColours;

	this.setSpectrum = function () 
	{
		setColours(arguments);
	}

	this.setSpectrumByArray = function (array)
	{
		setColours(array);
	}

	this.colourAt = function (number)
	{
		if (isNaN(number)) {
			throw new TypeError(number + ' is not a number');
		} else if (gradients.length === 1) {
			return gradients[0].colourAt(number);
		} else {
			var segment = (maxNum - minNum)/(gradients.length);
			var index = Math.min(Math.floor((Math.max(number, minNum) - minNum)/segment), gradients.length - 1);
			return gradients[index].colourAt(number);
		}
	}
	this.colorAt = this.colourAt;

	this.setNumberRange = function (minNumber, maxNumber)
	{
		if (maxNumber > minNumber) {
			minNum = minNumber;
			maxNum = maxNumber;
			setColours(colours);
		} else {
			throw new RangeError('maxNumber (' + maxNumber + ') is not greater than minNumber (' + minNumber + ')');
		}
	}
}

function ColourGradient() 
{
	var startColour = 'ff0000';
	var endColour = '0000ff';
	var minNum = 0;
	var maxNum = 100;

	this.setGradient = function (colourStart, colourEnd)
	{
		startColour = getHexColour(colourStart);
		endColour = getHexColour(colourEnd);
	}

	this.setNumberRange = function (minNumber, maxNumber)
	{
		if (maxNumber > minNumber) {
			minNum = minNumber;
			maxNum = maxNumber;
		} else {
			throw new RangeError('maxNumber (' + maxNumber + ') is not greater than minNumber (' + minNumber + ')');
		}
	}

	this.colourAt = function (number)
	{
		return calcHex(number, startColour.substring(0,2), endColour.substring(0,2)) 
			+ calcHex(number, startColour.substring(2,4), endColour.substring(2,4)) 
			+ calcHex(number, startColour.substring(4,6), endColour.substring(4,6));
	}

	function calcHex(number, channelStart_Base16, channelEnd_Base16)
	{
		var num = number;
		if (num < minNum) {
			num = minNum;
		}
		if (num > maxNum) {
			num = maxNum;
		} 
		var numRange = maxNum - minNum;
		var cStart_Base10 = parseInt(channelStart_Base16, 16);
		var cEnd_Base10 = parseInt(channelEnd_Base16, 16); 
		var cPerUnit = (cEnd_Base10 - cStart_Base10)/numRange;
		var c_Base10 = Math.round(cPerUnit * (num - minNum) + cStart_Base10);
		return formatHex(c_Base10.toString(16));
	}

	formatHex = function (hex) 
	{
		if (hex.length === 1) {
			return '0' + hex;
		} else {
			return hex;
		}
	} 

	function isHexColour(string)
	{
		var regex = /^#?[0-9a-fA-F]{6}$/i;
		return regex.test(string);
	}

	function getHexColour(string)
	{
		if (isHexColour(string)) {
			return string.substring(string.length - 6, string.length);
		} else {
			var colourNames =
			[
				['red', 'ff0000'],
				['lime', '00ff00'],
				['blue', '0000ff'],
				['yellow', 'ffff00'],
				['orange', 'ff8000'],
				['aqua', '00ffff'],
				['fuchsia', 'ff00ff'],
				['white', 'ffffff'],
				['black', '000000'],
				['gray', '808080'],
				['grey', '808080'],
				['silver', 'c0c0c0'],
				['maroon', '800000'],
				['olive', '808000'],
				['green', '008000'],
				['teal', '008080'],
				['navy', '000080'],
				['purple', '800080']
			];
			for (var i = 0; i < colourNames.length; i++) {
				if (string.toLowerCase() === colourNames[i][0]) {
					return colourNames[i][1];
				}
			}
			throw new Error(string + ' is not a valid colour.');
		}
	}
}




/*
	    $.ajax({
		type     : "GET",
		url      : "resources/" + county,
		dataType : "text",
		success  : function (text){
		    var county = $.parseJSON (text);
		    processPolygons (county, map);
		},
		error: function (XMLHttpRequest, textStatus, errorThrown){
		    console.log (textStatus); 
		    console.log (errorThrown); 
		    alert("Error");
		}
	    });




	    $.getJSON ("resources/" + county, function (county) {

	    });
*/



/*
    $.getJSON ("resources/c.json", function (obj) {
	processPolygons (obj, map);
    });

    $.ajax({
	type     : "GET",
	url      : "resources/c.json",
	dataType : "text", //"jsonp",
	success  : function (t){
	    var obj = $.parseJSON (t);
	    processPolygons (obj);
	},
	error: function (XMLHttpRequest, textStatus, errorThrown){
	    console.log (textStatus); 
	    console.log (errorThrown); 
            alert("Error");
	}
    });
*/

    
/*    
    google.maps.event.addListener(map, 'bounds_changed', function() {
	    epiMap.boundsChanged (map);
        });
*/
