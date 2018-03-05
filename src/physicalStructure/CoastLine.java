/**
 * 
 */
package physicalStructure;

/*import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;*/
import com.vividsolutions.jts.geom.Geometry;
/*import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import repast.simphony.context.Context;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;*/
import stMaarten.StMaartenBuilder;

/**
 * @author abebe8
 * This Coast line is for the line feature CoastLine_single.shp. This feature is used to 
 * compute distance from new development points to coast line. It is related to the Beach Policy. 
 */
public class CoastLine {

	public CoastLine() {
		super();
	}
	
	public static double proximityToSea(Geometry location) {
		/** If we would like to get the distance between two geometries with geographic coordinate in SI unit (meter), one option
		 * is to transform the geographic CRS (WGS84) to UTM projection and then compute the distance. 
		 */ 
		/*// if we have (long, lat) points, we need to first change them to geometry objects using geometry factory 
		GeometryFactory geometryFactory = new GeometryFactory();
		Geometry location = geometryFactory.createPoint(new Coordinate(-63.056767, 18.046783));
		// Initialise the current and target coordinate systems and the transformation method
		CoordinateReferenceSystem currentCRS = null; // current CRS
		CoordinateReferenceSystem transformCRS = null;  // target CRS
		MathTransform transform = null;
		try {
			currentCRS = CRS.decode("EPSG:4326");  // WGS84
			transformCRS = CRS.decode("EPSG:32620");  // UTM20
			transform = CRS.findMathTransform(currentCRS, transformCRS, true); // transform method from WGS84 to UTM20
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		// actual transformation of geometries
		Geometry locationUTM = null;
		Geometry geomUTM = null;
		try {
			locationUTM = JTS.transform(location, transform); // transform individual points 
			geomUTM = JTS.transform(StMaartenBuilder.coastLineGeom.get(0), transform); // transform CoastLine polyline
		} catch (MismatchedDimensionException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		}*/
		
		/** The second option is to check the distance of the points to the polygon border using the JTS DistanceOP.closestPoints() 
		 * which will return a pair of points - one will be the original point, and the other will be the closest point on the polygon.
		 * Then use JTS.orthodromicDistance() to calculate the linear distance between the pair of points.
		 */ 
		/*DistanceOp distanceOp = new DistanceOp(location, StMaartenBuilder.coastLineGeom.get(0));
		Coordinate[] coor = distanceOp.closestPoints();
		double distanceToSea = 0;
		try {
			distanceToSea = JTS.orthodromicDistance(coor[0], coor[1], CRS.decode("EPSG:4326"));
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}*/
		
		/** Or, use distance() from the DistanceOp() to calculate (the shortest?) distance between location (point feature) and 
		 * coast line (polyline feature) in decimal degrees. We don't use the polygon feature because, if the points are inside
		 * the polygon, distance() method returns zero. 
		 */
		double distance_locationToSea = location.distance(StMaartenBuilder.getCoastLineGeom().get(0));

		return distance_locationToSea;
	}
}
