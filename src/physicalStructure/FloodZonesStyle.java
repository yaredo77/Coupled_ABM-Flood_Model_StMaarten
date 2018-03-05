/**
 * 
 */
package physicalStructure;

import java.awt.Color;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

/**
 * @author abebe8
 *
 */
public class FloodZonesStyle implements SurfaceShapeStyle<FloodZones>{
	
	@Override
	public SurfaceShape getSurfaceShape(FloodZones object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(FloodZones zone) {
		return Color.RED;
	}

	@Override
	public double getFillOpacity(FloodZones obj) {
		return 0.65;
	}

	@Override
	public Color getLineColor(FloodZones zone) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(FloodZones obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(FloodZones obj) {
		return 1;
	}


}
