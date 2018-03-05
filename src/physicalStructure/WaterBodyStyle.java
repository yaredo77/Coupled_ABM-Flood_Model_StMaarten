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
public class WaterBodyStyle implements SurfaceShapeStyle<WaterBody>{
	
	@Override
	public SurfaceShape getSurfaceShape(WaterBody object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(WaterBody zone) {
		return Color.BLUE;
	}

	@Override
	public double getFillOpacity(WaterBody obj) {
		return 0.25;
	}

	@Override
	public Color getLineColor(WaterBody zone) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(WaterBody obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(WaterBody obj) {
		return 3;
	}

}
