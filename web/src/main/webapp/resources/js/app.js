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
    this.frame = 0;
    this.occurrences = null;
    this.polygons = [];
    this.initializeMap ();

    $("#menu").click (this.animateMap);

};

EpiMap.prototype.animateMap = function () {
    var map = epiMap;
    map.frame = 38; //0;
    map.nextFrame ();
};

EpiMap.prototype.initializeMap = function (map) {
    $.getJSON ("resources/occurrences.json", function (occurrences) {
	epiMap.occurrences = occurrences;
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
    $("#status").html ("   frame: " + map.frame);
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
    if (map.frame < map.occurrences.counts.length) {
	setTimeout ("epiMap.nextFrame ()", 200);
    }
};

EpiMap.prototype.getColorForValue = function (value) {
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
	color = "lightred";
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
