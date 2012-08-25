var epiMap = null;

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

function initialize() {
    //    $("#dialog").dialog ();
    var center = new google.maps.LatLng (35.9131, -79.0561);
    var mapOptions = {
	center: center,
	zoom: 7,
	mapTypeId: google.maps.MapTypeId.TERRAIN //ROADMAP
    };
    var map = new google.maps.Map ($("#map_canvas")[0], mapOptions);
    epiMap = new EpiMap (map);
    $.ajaxSetup({
	"error" : function (XMLHttpRequest, textStatus, errorThrown) {
	    alert (textStatus);
	    alert (errorThrown);
	    alert (XMLHttpRequest.responseText);
	}});
    $.getJSON ("resources/occurrences.json", function (occurrences) {
	$.getJSON ("resources/polygon-index.json", function (obj) {
	    for (var c = 0; c < obj.index.length; c++) {
		var polygonName = obj.index [c];
		$.getJSON ("resources/" + polygonName, function (polygon) {
		    epiMap.processPolygons (polygon, occurrences.counts [polygon.count], map);
		});
	    }
	});
    });
}

function EpiMap (map) {
    this.map = map;
};
EpiMap.prototype.processPolygons = function (polygon, count) {
    var coordinates = [];
    for (var q = 0; q < polygon.points.length; q++) {
	var point = polygon.points [q];
	var latlng = new google.maps.LatLng (point [1], point [0]);
	coordinates.push (latlng);
    }
    epiMap.renderPolygon (coordinates, count);
};

EpiMap.prototype.renderPolygon = function (points, count, map) {    
    var color = "#00FF00";
    if (count > 10) {
	color = "#9f0";
    }
    if (count > 100) {
	color = "#c00";
    }
    var polygon = new google.maps.Polygon ({
	paths         : points,
	strokeColor   : "#FF0000",
	strokeOpacity : 0.8,
	strokeWeight  : 2,
	fillColor     : color,
	fillOpacity   : 0.35
    });    
    polygon.setMap (this.map);
};

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
