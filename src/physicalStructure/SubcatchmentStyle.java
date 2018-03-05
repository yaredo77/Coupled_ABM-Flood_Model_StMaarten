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
public class SubcatchmentStyle implements SurfaceShapeStyle<Subcatchment>{
	
	@Override
	public SurfaceShape getSurfaceShape(Subcatchment object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(Subcatchment zone) {
		return Color.BLUE;
	}

	@Override
	public double getFillOpacity(Subcatchment obj) {
		return 0.25;
	}

	@Override
	public Color getLineColor(Subcatchment zone) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(Subcatchment obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(Subcatchment obj) {
		return 3;
	}


}
