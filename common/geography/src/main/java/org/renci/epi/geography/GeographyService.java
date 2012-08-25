package org.renci.epi.geography;

public interface GeographyService {

    public void getPolygons (String fileName);
    public void getPolygons (String fileName, PolygonOperator [] operator);

}
