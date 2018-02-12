package org.renci.epi.geography;

import java.util.List;
import org.renci.epi.util.DataLocator;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;

public interface GeographyService {
    public void setDataLocator (DataLocator dataLocator);
    public DataLocator getDataLocator ();
    public void getPolygons (String fileName);
    public void getPolygons (String fileName, PolygonOperator [] operator);
    public List<PreparedGeometry> getPreparedPolygons (String fileName);
}
