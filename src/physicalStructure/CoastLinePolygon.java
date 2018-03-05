/**
 * 
 */
package physicalStructure;

/*import stMaarten.StMaartenBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.DistanceOp;*/

/**
 * @author abebe8
 * This class shows the whole island (a polygon feature CoastLine_polygon.shp).
 * It is created just for visualisation purpose.
 */
public class CoastLinePolygon {

	public CoastLinePolygon() {
		super();
	}
	// this method better works to compute the distance between a polygon feature and a point feature located outside the polygon.
	/*public static double distanceCoastLine(Geometry location) {
		DistanceOp distanceOp = new DistanceOp(location, StMaartenBuilder.getCoastLineGeom().get(0));
		double distanceToSea = distanceOp.distance(location, StMaartenBuilder.getCoastLineGeom().get(0));
		return distanceToSea;
	}*/
}
