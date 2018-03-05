/**
 * 
 */
package physicalStructure;

import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;

import java.awt.Color;

import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

/**
 * @author abebe8
 *
 */
public class RoadStyle implements SurfaceShapeStyle<Road>{
	@Override
	public SurfaceShape getSurfaceShape(Road object, SurfaceShape shape) {
	  return new SurfacePolyline();
	}

	@Override
	public Color getFillColor(Road obj) {
		return null;
	}

	@Override
	public double getFillOpacity(Road obj) {
		return 0;
	}

	@Override
	public Color getLineColor(Road obj) {
		return Color.magenta;
	}

	@Override
	public double getLineOpacity(Road obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(Road obj) {
		return 0.75;
	}
}
