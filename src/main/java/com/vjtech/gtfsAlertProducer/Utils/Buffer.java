package com.vjtech.gtfsAlertProducer.Utils;

import java.util.List;
import javax.measure.quantity.Length;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.vjtech.gtfsAlertProducer.GtfsAlertProducerApplication;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;

/**
 * Take a Linestring in WGS84, convert to a local projection and buffer at 1km
 * then reproject to WGS84 and print.
 * 
 * 
 * @author ian
 *
 */
public class Buffer {
	
	private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);
	
	@Bean
	public CommandLineRunner demo(AccessTokenResponse tokenResponse) {
		return (args) -> { log.info("anche io runno");};
	}

	/*
    public SimpleFeature bufferFeature(SimpleFeature feature,
	   Measure<Double, Length> distance) {
	// extract the geometry
	GeometryAttribute gProp = feature.getDefaultGeometryProperty();
	CoordinateReferenceSystem origCRS = gProp.getDescriptor()
		.getCoordinateReferenceSystem();

	Geometry geom = (Geometry) feature.getDefaultGeometry();
	Geometry pGeom = geom;
	MathTransform toTransform,fromTransform = null;
	// reproject the geometry to a local projection
	if (!(origCRS instanceof ProjectedCRS)) {
	    
	    Point c = geom.getCentroid();
	    double x = c.getCoordinate().x;
	    double y = c.getCoordinate().y;
	   
	    String code = "AUTO:42001," + x + "," + y;
	    // System.out.println(code);
	    CoordinateReferenceSystem auto;
	    try {
		auto = CRS.decode(code);
		 toTransform = CRS.findMathTransform(
			DefaultGeographicCRS.WGS84, auto);
		 fromTransform = CRS.findMathTransform(auto,
			DefaultGeographicCRS.WGS84);
		pGeom = JTS.transform(geom, toTransform);
	    } catch (MismatchedDimensionException | TransformException
		    | FactoryException e) {
		e.printStackTrace();
	    }

	}

	// buffer
	Geometry out = buffer(pGeom, distance.doubleValue(SI.METER));
	Geometry retGeom = out;
	// reproject the geometry to the original projection
	if (!(origCRS instanceof ProjectedCRS)) {
	    try {
		retGeom = JTS.transform(out, fromTransform);
	    } catch (MismatchedDimensionException | TransformException e) {
		e.printStackTrace();
	    }
	}
	// return a new feature containing the geom
	SimpleFeatureType schema = feature.getFeatureType();
	SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
	ftBuilder.setCRS(origCRS);
	//ftBuilder.setDefaultGeometry("buffer");
	ftBuilder.addAll(schema.getAttributeDescriptors());
	ftBuilder.setName(schema.getName());
	
	SimpleFeatureType nSchema = ftBuilder.buildFeatureType();
	SimpleFeatureBuilder builder = new SimpleFeatureBuilder(nSchema);
	 List<Object> atts = feature.getAttributes();
	 for(int i=0;i<atts.size();i++) {
	     if(atts.get(i) instanceof Geometry) {
		 atts.set(i, retGeom);
	     }
	 }
	SimpleFeature nFeature = builder.buildFeature(null, atts.toArray() );
	return nFeature;
    }
    */
    
    public Geometry buffer(Geometry geometry, double distanceInMeters) throws FactoryException, TransformException {
        String code = "AUTO:42001," + geometry.getCentroid().getCoordinate().x + "," + geometry.getCentroid().getCoordinate().y;
        CoordinateReferenceSystem auto = CRS.decode(code);

        MathTransform toTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
        MathTransform fromTransform = CRS.findMathTransform(auto, DefaultGeographicCRS.WGS84);
        
        Geometry pGeom = JTS.transform(geometry, toTransform);
        Geometry pBufferedGeom = pGeom.buffer(distanceInMeters);
        return JTS.transform(pBufferedGeom, fromTransform);
   }

   /**
	 * create a buffer around the geometry, assumes the geometry is in the same
	 * units as the distance variable.
	 * 
	 * @param geom
	 *            a projected geometry.
	 * @param dist
	 *            a distance for the buffer in the same units as the projection.
	 * @return
   */
   /*
   private Geometry buffer(Geometry geom, double dist) {
	
	Geometry buffer = geom.buffer(dist);
	
	return buffer;
	
   }
  */
}