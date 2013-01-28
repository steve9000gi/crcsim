var epiMaps = null;

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

// get url parameter
function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}

/**
 *
 * Epi Map - renders polygons on a Google map.
 * 
 * Applies counts for each polygon across a timeframe.
 *
 */
function EpiMap (mapCanvasId) {
    this.mapPlugins = {
	'google'     : new EpiMapGooglePlugin (),
	'openlayers' : new EpiMapOpenLayersPlugin (),
	'leaflet'    : new EpiMapLeafletPlugin ()
    };

    var mapType = getURLParameter ('maptype');
    if (mapType === "null") {
	mapType = 'leaflet';
    }
    this.mapPlugin = this.mapPlugins [mapType];
    this.map = this.mapPlugin.createMap (mapCanvasId);
    $.ajaxSetup({
	"error" : function (XMLHttpRequest, textStatus, errorThrown) {
	    console.log (textStatus + ' ' + errorThrown + ' ' + XMLHttpRequest.responseText);
	}
    });

    this.gradient = new Rainbow (); // by default, range is 0 to 100
    this.frame = 0;
    this.occurrences = null;
    this.polygons = [];
    this.initializeMap (this);
};
// set the matrix of occurrences. this is a list of frames per observation with values per polygon.
EpiMap.prototype.setOccurrences = function (map, occurrences) {
    map.occurrences = occurrences;
    var max = 0;
    var endpoint = map.occurrences.counts [map.occurrences.counts.length - 1];
    for (var c = 0; c < endpoint.length; c++) {
	var value = endpoint [c];
	max = Math.max (value, max);
    }
    map.gradient.setNumberRange (0, max);
    map.gradient.setSpectrum ('blue', 'green', '#33FF33', 'yellow', '#FF0000', 'red');
};
EpiMap.prototype.initializeMap = function (map) {
    $.getJSON ("resources/occurrences.json", function (occurrences) {
	map.setOccurrences (map, occurrences);
	$.getJSON ("resources/polygon-index.json", function (obj) {
	    for (var c = 0; c < obj.index.length; c++) {
		var polygonName = obj.index [c];
		$.getJSON ("resources/" + polygonName, function (polygon) {
		    map.drawPolygon (map, polygon, 0, map);
		});
	    }
	});
	
    });
};
// render the next frame.
EpiMap.prototype.nextFrame = function (map, index) {
    var controller = epiController;
    if (controller.frame < map.occurrences.counts.length) {
	for (var c = 0; c < map.polygons.length; c++) {
	    var polygon = map.polygons [c];
	    polygon.polygon.setStyle ({
		fillColor    : map.getColorForValue (map, map.occurrences.counts [controller.frame][polygon.index]),
		fillOpacity  : 0.4,
		strokeWeight : 0.1
	    });
	}
    }
    controller.update ();
    if (controller.frame < map.occurrences.counts.length) {
	setTimeout ("epiController.nextFrame (" + index + ")", 200);
    }
};
// get the color from the calculated gradient associated with the value.
EpiMap.prototype.getColorForValue = function (epiMap, value) {
    var color = '#' + epiMap.gradient.colorAt (value);
    //console.log ('color: ' + value + " " + color);
    return color;
};
// process a polygon
EpiMap.prototype.drawPolygon = function (epiMap, polygon, value) {
    this.polygons.push ({
	polygon : epiMap.mapPlugin.createPolygon (epiMap, polygon, value),
	index   : polygon.count
    });
    this.polygons.sort (function (a, b) {
	return a.index - b.index;
    });
};

/**
 *
 * Google Plugin
 *
 */
function EpiMapGooglePlugin () {
}
// create map
EpiMapGooglePlugin.prototype.createMap = function () {
    var center = new google.maps.LatLng (35.9131, -79.0561);
    var mapOptions = {
	center: center,
	zoom: 7,
	mapTypeId: google.maps.MapTypeId.TERRAIN //ROADMAP
    };
    this.map = new google.maps.Map ($("#map_canvas")[0], mapOptions);
};
// create polygon
EpiMapGooglePlugin.prototype.createPolygon = function (epiMap, polygon, value) {
    var coordinates = [];
    for (var q = 0; q < polygon.points.length; q++) {
	var point = polygon.points [q];
	var latlng = new google.maps.LatLng (point [1], point [0]);
	coordinates.push (latlng);
    }
    var polygon = new google.maps.Polygon ({
	paths         : coordinates,
	strokeColor   : "#222",
	strokeOpacity : 0.5,
	strokeWeight  : 0.5,
	fillColor     : epiMap.getColorForValue (epiMap, value),
	fillOpacity   : 0.7
    });
    polygon.setMap (this.map);
    return new GooglePolygon (polygon);
};
// wrap a google polygon
function GooglePolygon (polygon) {
    this.polygon = polygon;
};
// set a google polygon's style
GooglePolygon.prototype.setStyle = function (style) {
    this.polygon.setOptions (style);
};


/**
 *
 * Cloudmade Leaflet Plugin
 *
 */
function EpiMapLeafletPlugin () {
}
// create a leaflet map
EpiMapLeafletPlugin.prototype.createMap = function (mapCanvasId) {


    this.map = L.map (mapCanvasId).setView ([ 35.9131, -79.0561], 6);
    //this.map = L.map ('map_canvas').setView ([ 35.9131, -79.0561], 7);

    //L.tileLayer('http://{s}.tile.cloudmade.com/d21d2ad31cfd4e589c5c8b2d0c6e91ad/997/256/{z}/{x}/{y}.png', {
    L.tileLayer('http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/997/256/{z}/{x}/{y}.png', {
	attribution : [ 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> ',
			' contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ',
			' Imagery © <a href="http://cloudmade.com">CloudMade</a>' ].join (''),
	maxZoom : 18
    }).addTo (this.map);
    L.Control.Zoom ( { position : 'topleft' } );
};
// create a polygon
EpiMapLeafletPlugin.prototype.createPolygon = function (epiMap, polygon, count) {
    var coordinates = [];
    for (var q = 0; q < polygon.points.length; q++) {
	var point = polygon.points [q];
	coordinates.push ([ point [1], point [0] ]);
    }
    var vPolygon = 
	L.polygon (coordinates).
	addTo (this.map).
	setStyle ({
	    fillColor     : epiMap.getColorForValue (epiMap, count),
	    strokeColor   : "#111",
	    strokeOpacity : 0.5,
	    strokeWeight  : 0.1,
	    fillOpacity   : 0.7
	});
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
 * OpenLayers Plugin
 *
 */
function EpiMapOpenLayersPlugin () {
}
EpiMapOpenLayersPlugin.prototype.createMap = function () {
    var baseLayer = new OpenLayers.Layer.XYZ(
	"MapBox Streets",
	[
            "http://a.tiles.mapbox.com/v3/mapbox.mapbox-streets/${z}/${x}/${y}.png",
            "http://b.tiles.mapbox.com/v3/mapbox.mapbox-streets/${z}/${x}/${y}.png",
            "http://c.tiles.mapbox.com/v3/mapbox.mapbox-streets/${z}/${x}/${y}.png",
            "http://d.tiles.mapbox.com/v3/mapbox.mapbox-streets/${z}/${x}/${y}.png"
	],
	{
            attribution: "Tiles &copy; <a href='http://mapbox.com/'>MapBox</a> | " + 
		"Data &copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> " +
		"and contributors, CC-BY-SA",
            sphericalMercator: true,
            wrapDateLine: true,
	    isBaseLayer : true,
            transitionEffect: "resize",
            buffer: 1,
            numZoomLevels: 17
	}
    );
    this.map = new OpenLayers.Map ('map_canvas');
    this.map.addLayer (baseLayer);

    this.projection = new OpenLayers.Projection ("EPSG:4326");
    var point = new OpenLayers.LonLat (-79.0561, 35.9131);
    point.transform (this.projection, this.map.getProjectionObject ());
    this.map.setCenter (point);
    this.map.zoomTo (7);
/*
    var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
    renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;    
*/
    this.layer = new OpenLayers.Layer.Vector ("Simple Geometry", {
        styleMap: new OpenLayers.StyleMap ({'default':{
            fillColor      : "${fillColor}",
            strokeWidth    : 0.1,
            strokeOpacity  : 0.6,
            fillOpacity    : 0.2,
	    /*
            strokeColor: "#020",
            pointRadius: 6,
            pointerEvents: "visiblePainted",
            // label with \n linebreaks
            label : "", //name: ${name}\n\nage: ${age}",
            fontColor: "${favColor}",
            fontSize: "12px",
            fontFamily: "Courier New, monospace",
            fontWeight: "bold",
            labelAlign: "${align}",
            labelXOffset: "${xOffset}",
            labelYOffset: "${yOffset}",
            labelOutlineColor: "white",
            labelOutlineWidth: 3
*/
        }})
/*
,
        renderers: renderer
*/
    });
    this.map.addLayer (this.layer);
};

EpiMapOpenLayersPlugin.prototype.createPolygon = function (polygon, count) {
    var coordinates = [];
    for (var q = 0; q < polygon.points.length; q++) {
	var data = polygon.points [q];
	var point = new OpenLayers.Geometry.Point (data [0], data [1]);
	point.transform (this.projection, this.map.getProjectionObject ());
	coordinates.push (point);
    }
    coordinates.push (coordinates [0]);
    var linearRing = new OpenLayers.Geometry.LinearRing (coordinates);
    var polygon = new OpenLayers.Geometry.Polygon ([linearRing]);
    var polygonFeature = new OpenLayers.Feature.Vector (polygon);
    polygonFeature.attributes = {
        name: "",
	/*
        age: 21,
        favColor: 'purple',
        align: 'lb'
	*/
    };
    this.layer.addFeatures ([ polygonFeature ]);
    return new OpenLayersPolygonProxy (polygonFeature, this.layer);
};
function OpenLayersPolygonProxy (feature, layer) {
    this.feature = feature;
    this.layer = layer;
};
OpenLayersPolygonProxy.prototype.setStyle = function (style) {
    this.layer.drawFeature (this.feature, style);
};




var epiController = null;

function EpiController () {
    this.mapCount = 4;
    this.maps = [];
    this.frame = 0;
    this.updates = 0;
};
EpiController.prototype.update = function () {
    if (++this.updates % this.mapCount == 0) {
	this.frame++;
    }
};
EpiController.prototype.initialize = function () {
    for (var c = 0; c < this.mapCount; c++) {

	var id = "map_canvas" + c;
	var title = "Scenario " + (c + 1);

	$('#maps').append ([ '<div class="map-container">',
			     '   <div class="map-border">',
			     '      <div class="map-title">' + title + '</div>',
			     '      <div class="map" id="' + id + '"></div>',
			     '   </div>',
			     '</div>' ].join ('\n') );

	
	var mapCount = this.mapCount;
	$(".map-container").css ({
	    height : 100 / ( this.mapCount / 2 ) + '%',
	    width  : 100 / ( this.mapCount / 2 ) + '%'
	});
	$(window).resize (function () {
	    $(".map-container").css ({
		height : 100 / ( mapCount / 2 ) + '%',
		width  : 100 / ( mapCount / 2 ) + '%'
	    });
	});

	this.maps [c] = new EpiMap (id);
    }
};



EpiController.prototype.animate = function () {
    var controller = epiController;
    console.debug ("animate map");
    controller.updates = 0;
    controller.frame = 34;
    for (var c = 0; c < controller.mapCount; c++) {
	var map = controller.maps [c];
	map.nextFrame (map, c);
    }
};
EpiController.prototype.nextFrame = function (index) {
    var controller = epiController;
    $("#status").html (controller.frame + "%");
    $("#progress").progressbar ({
	value : controller.frame
    });

    var map = controller.maps [index];
    map.nextFrame (map, index);
};
epiController = new EpiController ();


function EpiView () {
    $("#menu").click (this.animate);
}
EpiView.prototype.animate = function () {
    epiController.animate ();
}
var epiView = new EpiView ();

/**
 *
 * Calculate color gradients.
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

$(function () {
    epiController.initialize ();
});



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
