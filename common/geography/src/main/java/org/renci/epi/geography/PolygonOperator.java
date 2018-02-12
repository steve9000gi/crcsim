package org.renci.epi.geography;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;

public interface PolygonOperator {

    public void execute (MultiPolygon polygon, Point [] points, boolean hasNext);

    public void close ();

}
