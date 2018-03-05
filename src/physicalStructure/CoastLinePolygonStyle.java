/**
 * 
 */
package physicalStructure;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

/**
 * @author abebe8
 *
 */
public class CoastLinePolygonStyle implements SurfaceShapeStyle<CoastLinePolygon>{
	
	@Override
	public SurfaceShape getSurfaceShape(CoastLinePolygon object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(CoastLinePolygon zone) {
		return Color.YELLOW;
	}

	@Override
	public double getFillOpacity(CoastLinePolygon obj) {
		return 0.25;
	}

	@Override
	public Color getLineColor(CoastLinePolygon zone) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(CoastLinePolygon obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(CoastLinePolygon obj) {
		return 3;
	}

}
