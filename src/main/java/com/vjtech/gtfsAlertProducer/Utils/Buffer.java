package com.vjtech.gtfsAlertProducer.Utils;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Take a Linestring in WGS84, convert to a local projection and buffer at 1km
 * then reproject to WGS84 and print.
 * 
 * 
 * @author ian
 *
 */
public class Buffer {
	
	//private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);
	    
    public Geometry buffer(Geometry geometry, double distanceInMeters) throws FactoryException, TransformException {
        String code = "AUTO:42001," + geometry.getCentroid().getCoordinate().x + "," + geometry.getCentroid().getCoordinate().y;
        CoordinateReferenceSystem auto = CRS.decode(code);

        MathTransform toTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
        MathTransform fromTransform = CRS.findMathTransform(auto, DefaultGeographicCRS.WGS84);
        
        Geometry pGeom = JTS.transform(geometry, toTransform);
        Geometry pBufferedGeom = pGeom.buffer(distanceInMeters);
        return JTS.transform(pBufferedGeom, fromTransform);
   }

   
}