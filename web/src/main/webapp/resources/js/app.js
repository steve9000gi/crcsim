var epiController = null;

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};
// http://stackoverflow.com/questions/646628/javascript-startswith
if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.slice(0, str.length) == str;
  };
}
// get url parameter
function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}
// http://stackoverflow.com/questions/2998784/how-to-output-integers-with-leading-zeros-in-javascript
function zeropad(num, size) {
    var s = num+"";
    while (s.length < size) s = "0" + s;
    return s;
}
var totalValue = 0;

/**
 *
 * Epi Map - renders polygons on a Google map.
 * 
 * Applies counts for each polygon across a timeframe.
 *
 */
function EpiMap (mapId, plugin) { //, scenario) {
    this.gradient = new Rainbow (); // by default, range is 0 to 100
    this.frame = 0;
    this.occurrences = null;
    //    this.loadScenario (this, scenario);
    this.plugin = plugin;
    this.mapId = mapId;
    this.polygons = [];
};
// set the matrix of occurrences. this is a list of frames per observation with values per polygon.
EpiMap.prototype.setOccurrences = function (occurrences) {
    this.occurrences = occurrences;
    var max = 60000;
    this.gradient.setNumberRange (0, max);
    this.gradient.setSpectrum ('blue', 'green', '#33FF33', 'yellow', '#FF0000', 'red');
};
EpiMap.prototype.setTitle = function (title) {
    if (title.startsWith ('prod/')) {
	title = title.substring (prefix.length - 1);
    }
    $('#' + this.mapId + '_title').html (title);
};
// Initialize the map. load frame matrix, polygon index and render individual polygons.
EpiMap.prototype.loadScenario = function (map, scenario) {
    var path = "resources/prod/" + scenario.name + "-occurrences.json";
    $.getJSON (path, function (occurrences) {
	    map.setOccurrences (occurrences);
	});
};
// Render the next frame.
EpiMap.prototype.renderFrame = function (frame) {
    if (this.occurrences != null && frame < this.occurrences.counts.length) {
	for (var c = 0; c < this.polygons.length; c++) {
	    var polygon = this.polygons [c];
	    var value = this.occurrences.counts [polygon.index][frame];

	    var polygonId = polygon.index + '';
 	    this.plugin.polygonValue [polygonId] = value;

	    var fillColor = this.gradient.colorAt (value);
	    polygon.polygon.setStyle ({
		    fillColor    : '#' + fillColor,
			fillOpacity  : 0.4,
			stroke   : 1
			});

	    totalValue += value;

	}
    }
};
// Process a polygon
EpiMap.prototype.drawPolygon = function (polygon, value, plugin) {
    this.polygons.push ({
	    polygon : plugin.createPolygon (polygon, value, this.gradient),
	    index   : parseInt (polygon['id']) //polygon.count
	});
    this.polygons.sort (function (a, b) {
	    return a.index - b.index;
	});
};

/**
 *
 * Cloudmade Leaflet Plugin
 *
 */
function EpiMapLeafletPlugin () {
}
// create a leaflet map
EpiMapLeafletPlugin.prototype.createMap = function (mapId) {
    this.map = L.map (mapId).setView ([ 35.0131, -79.9061], 6);
    var layer = L.tileLayer('http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/997/256/{z}/{x}/{y}.png', {
	attribution : [ 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> ',
			' contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ',
			' Imagery © <a href="http://cloudmade.com">CloudMade</a>' ].join (''),
	maxZoom : 18,
	}).addTo (this.map);

    /**
     * geojson layer.
     */
    var popup = new L.Popup ();
    var polygonIndex = 0;
    var theMap = this.map;
    var plugin = this;
    this.featureLayer = L.geoJson (null,  {
	    style : function (feature) {
		return {
		    "z-index"   : 10,
		    strokeColor : "#ff0000" 
		};
	    },
	    onEachFeature: function (feature, layer) {
		(function (layer, feature) {
		    layer.on ("mouseover", function (e) {
			//layer.setStyle(highlightStyle);
			var popup = $("<div></div>", {
			    id: "popup-" + feature.properties.NAME10,
			    css: {
				position : "absolute",
				bottom   : "5px",
				left     : "10px",
				zIndex   : 1002,
				backgroundColor : "white",
				padding  : "8px",
				border   : "1px solid #ccc"
			    }
			});
			var info = [ 'County: ' + feature.properties.NAME10,
				     'FIPS  : ' + feature.properties.COUNTYFP10,
				     'Value : ' + plugin.polygonValue [feature.id],
				     'Total : ' + totalValue ].join (', ');
			var header = $("<div></div>", {
			    text : info,
			    css : {
				fontSize     : "16px",
				marginBottom : "3px"
			    }
			}).appendTo (popup);
			
			$('.popup').remove ();
			popup.
			    addClass ('popup').
			    appendTo ('#' + mapId); //"#map");
		    });
		    layer.on ("mouseout", function (e) {
			//layer.setStyle(defaultStyle); 
			$("#popup-" + feature.properties.NAME10).remove ();
		    });
		    // Close the "anonymous" wrapper function, and call it while passing
		    // in the variables necessary to make the events work the way we want.
		})(layer, feature);
	    }
	});
    this.map.addLayer (this.featureLayer);

    this.polygonValue = {};
};
EpiMapLeafletPlugin.prototype.getDefaultPolygonStyle = function () {
    return {
	fillColor     : '#ececec',
	'z-index'     : 0,
	weight        : 0.1,
	strokeColor   : "#111",
	strokeOpacity : 0.5,
	strokeWeight  : 0.1,
	fillOpacity   : 0.2
    };
};
// create a polygon
EpiMapLeafletPlugin.prototype.createPolygon = function (polygon, count, gradient) {
    var coordinates = [];
    var points = polygon.geometry.coordinates [0];
    for (var q = 0; q < points.length; q++) {
	var point = points [q];
	coordinates.push ([ point [1], point [0] ]);
    }
    var fillColor = gradient.colorAt (count);
    var vPolygon = L.polygon (coordinates).
    addTo (this.map).
    setStyle (this.getDefaultPolygonStyle ());
    this.polygonValue [polygon.id] = count;
    return new LeafletPolygon (vPolygon);
};
// wrap a leaflet polygon
function LeafletPolygon (polygon) {
    this.polygon = polygon;
};
// set a leaflet polygon's style
LeafletPolygon.prototype.setStyle = function (style) {
    this.polygon.setStyle (style);
};

/**
 * 
 * Control a set of maps.
 *
 */
function EpiController () {
    $.ajaxSetup({
	"error" : function (XMLHttpRequest, textStatus, errorThrown) {
	    console.log (textStatus + ' ' + errorThrown + ' ' + XMLHttpRequest.responseText);
	}
    });
    this.mapPlugins = {
	'google'     : EpiMapGooglePlugin,
	'openlayers' : EpiMapOpenLayersPlugin,
	'leaflet'    : EpiMapLeafletPlugin
    };

    var mapType = getURLParameter ('maptype');
    if (mapType === "null") {
	mapType = 'leaflet';
    }
    this.mapPluginType = mapType; //this.mapPlugins [mapType];

    this.maps = [];
    this.frame = 38;
    this.animationHandle = null;

    $("#animate").click (this.animate);
    this.polygons = [];
};
/**
 * Render polygons.
 */
EpiController.prototype.renderPolygons = function () {
    var polys = epiController.polygons;
    $.getJSON ("resources/prod/polygon-index.json", function (obj) {
	    for (var c = 0; c < obj.index.length; c++) {
		var polygonName = obj.index [c];
		$.getJSON ("resources/prod/" + polygonName, function (polygon) {
			polys.push (polygon);
			for (var k = 0; k < epiController.maps.length; k++) {
			    var map = epiController.maps [k];
			    // order matters here...
			    map.drawPolygon (polygon, 0, map.plugin);
			    map.plugin.featureLayer.addData (polygon);
			}
		    });
	    }
	});
};
/**
 * Create a new plugin object.
 */
EpiController.prototype.createPlugin = function () {
    return new epiController.mapPlugins [epiController.mapPluginType] ();
};
/**
 * Construct scenarios.
 */
EpiController.prototype.getScenarios = function (intervention) {
    return [
	{ name : 'crc-control',                 desc : 'Cancer Free Years (baseline)' },
	{ name : 'crc-' + intervention,         desc : 'Cancer Free Years > 0.0 (Intervention ' + intervention + ')' },
	{ name : 'colonoscopy-control',         desc : 'Colonoscopies Performed (baseline)' },
	{ name : 'colonoscopy-' + intervention, desc : 'Colonoscopies Performed (Intervention ' + intervention + ')' }
    ]
};
/**
 * Draw the maps.
 */
EpiController.prototype.drawMaps = function (count) {
    for (var c = 0; c < count; c++) {
	epiController.addMap ();
    }
    epiController.renderPolygons ();
};
/**
 * Initialize maps for a run.
 */
EpiController.prototype.initializeMaps = function (count) {
    var interventionId = $('#intervention').val ();
    var scenarios = this.getScenarios (interventionId);
    for (var c = 0; c < epiController.maps.length; c++) {
	var map = epiController.maps[c];
	var scenario = scenarios [c];
	map.loadScenario (map, scenario);
	for (var p = 0; p < map.polygons.length; p++) {
	    var polygon = map.polygons [p];
	    polygon.polygon.setStyle (map.plugin.getDefaultPolygonStyle ());
	}
	$('#' + map.mapId + '_title').html (scenario.desc);
    }
    epiController.resize ();
};
/**
 * Add a map.
 */
EpiController.prototype.addMap = function (scenario) {
    var mapId = "map_canvas" + epiController.maps.length;
    var text = [ "<div class='map_container'>",
		 "   <div class='map_title' id='", mapId, "_title' ></div>",
		 "   <div class='map' id='", mapId, "' ></div>",
		 "</div>" ].join ('');
    $('#maps').append (text);

    var pluginMap = epiController.createPlugin ();
    pluginMap.createMap (mapId);
    var map = new EpiMap (mapId, pluginMap);
    epiController.maps.push (map);
};
/** 
 * Animate the set of maps.
 */
EpiController.prototype.animate = function () {
    totalValue = 0;
    epiController.initializeMaps (4);
    epiController.frame = 38;
    epiController.animationHandle = setInterval (epiController.renderFrame, 500);
};
/**
 * Render a frame in all maps.
 */
EpiController.prototype.renderFrame = function () {
    $("#status").html (epiController.frame + "%");
    $("#progress").progressbar ({
	value : epiController.frame
    });
    for (var c = 0; c < epiController.maps.length; c++) {
	var map = epiController.maps [c];
	if (epiController.frame >= map.occurrences.counts.length - 1) {
	    clearInterval (epiController.animationHandle);
	    if (totalValue > 0) {
		console.log ("animation total: " + totalValue);
	    }
	}
	map.renderFrame (epiController.frame);
    }
    epiController.frame++;
};
EpiController.prototype.resize = function () {
    var maps = $('#maps');
    var parent = maps.parent ();
    var menuHeight =  $('#menu').height ();
    var nWidth = $(parent).width () - 4;
    var nHeight = $(parent).height() - menuHeight - 4;
    $(maps).css ({
	width : nWidth,
	height : nHeight
    });
    $('.map_title').css ({ height : '20px' });

    //$('.map_container').each (function () {
    $('.map').each (function () {
	var parent = $(this).parent();

	nWidth = $(parent).width ();
	nHeight = $(parent).height() - 20;


	/*
	nWidth = ( $(parent).width () / 2 ) - 4;
	nHeight = ( $(parent).height() / 2 ) - 22;
	*/
	//nWidth = p.width() - ($(this).offset().left - p.offset().left);
	//nHeight = p.height() - ($(this).offset().top - p.offset().top);
	$(this).css ({
	    width  : nWidth,
	    height : nHeight
	});
    });
};
/**
 * Initialize.
 */
function initialize() {
    for (var c = 0; c < 13; c++) {
	var value = zeropad (c, 4);
	$('#intervention').append ( $('<option>', { 
            value: value,
            text : 'Intervention ' + value 
	}));
    }
    epiController = new EpiController ();
    $(window).resize (epiController.resize);
    epiController.drawMaps (4);
}

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

$(function () {
    initialize ();
});
