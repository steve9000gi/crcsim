<jsp:include page="include/header.jsp" />
<html>
  <head>
    <meta name="viewport"  content="initial-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="resources/css/styles.css" type="text/css" media="all" />
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.8.22/themes/base/jquery-ui.css" type="text/css" media="all" />
  </head>
  <body onload="initialize()">
    <div id="menu">
      <div>Animate</div>
    </div>
    <div id="progress"></div>
    <div id="status"></div>
    <div id="map_canvas" style="width:100%; height:100%"></div>
  </body>  
  <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyB0s6Ao4hTmGq9qcVhuRZ2ecldLL_68Zd4&sensor=false&libraries=visualization"> </script>
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.22/jquery-ui.min.js"></script>
  <script type="text/javascript" src="resources/js/app.js"></script>  
</html>
