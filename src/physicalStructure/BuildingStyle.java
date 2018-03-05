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
public class BuildingStyle implements SurfaceShapeStyle<Building>{
	
	@Override
	public SurfaceShape getSurfaceShape(Building object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(Building zone) {
		return Color.gray;
	}

	@Override
	public double getFillOpacity(Building obj) {
		return 0.25;
	}

	@Override
	public Color getLineColor(Building zone) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(Building obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(Building obj) {
		return 3;
	}

}
