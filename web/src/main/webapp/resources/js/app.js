var epiMap = null;

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

/**
 *
 * Epidemiology oriented map.
 * 
 */
function EpiMap (map) {
    this.map = map;
    var center = this.map.getCenter ();
    this.heatmapData = [];	 
    for (var c = -100; c < 100; c++) {
	var data = new google.maps.LatLng (center.lat () + (Math.random () * 10 * Math.random () / 10),
					   center.lng () + (Math.random () * 10 * Math.random () / 10));
	this.heatmapData.push (data);
    }
    this.zoomThreshold = 12; // point at which we render real person markers.
    this.markers = [];
    this.infowindow = new google.maps.InfoWindow ({
            content : "",
            size    : new google.maps.Size (50,50)
	});
};
EpiMap.prototype.getMap = function () {
    return this.map;
};
EpiMap.prototype.getHeatmapData = function () {
    return this.heatmapData;
};
EpiMap.prototype.boundsChanged = function (map) {
    this.removeMarkers ();
    var zoom = map.getZoom ();
    if (zoom > this.zoomThreshold) {
	var bounds = map.getBounds ();
	for (var c = 0; c < this.heatmapData.length; c++) {
	    var point = this.heatmapData [c];
	    if (bounds.contains (point)) {
		var marker = new google.maps.Marker({
		        position : point,
			map      : map,
                        animation: google.maps.Animation.DROP,
			title    : "person id"
		    });

		google.maps.event.addListener(marker, 'mouseover', this.showDetail);
		google.maps.event.addListener(marker, 'mouseout', this.hideDetail);
		this.markers.push (marker);
	    }
	}
    } else if (zoom < this.zoomThreshold) {
	this.removeMarkers ();
    }
};
EpiMap.prototype.removeMarkers = function () {            
    while (this.markers.length > 0) {
	var marker = this.markers [0];
	if (marker) {
	    marker.setMap (null);
	    this.markers.remove (0);
	}
    }
};
EpiMap.prototype.showDetail = function (event) {
    var contentString = "details...";
    epiMap.infowindow.setContent (contentString);
    epiMap.infowindow.setPosition (event.latLng);
    epiMap.infowindow.open (epiMap.getMap ());
};
EpiMap.prototype.hideDetail = function (event) {
    epiMap.infowindow.close ();
};



function initialize() {

    $("#dialog").dialog ();
    var center = new google.maps.LatLng (35.9131, -79.0561);

    var mapOptions = {
	center: center,
	zoom: 5,
	mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"),
				  mapOptions);

    epiMap = new EpiMap (map);


    var heatmap = new google.maps.visualization.HeatmapLayer({
	    data: epiMap.getHeatmapData ()
        });
    heatmap.setMap (map);


    google.maps.event.addListener(map, 'bounds_changed', function() {
	    epiMap.boundsChanged (map);
        });


}