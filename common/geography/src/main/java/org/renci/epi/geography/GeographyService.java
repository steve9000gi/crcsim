package org.renci.epi.geography;

import org.renci.epi.util.DataLocator;

public interface GeographyService {

    public void setDataLocator (DataLocator dataLocator);
    public DataLocator getDataLocator ();
    public void getPolygons (String fileName);
    public void getPolygons (String fileName, PolygonOperator [] operator);

}
