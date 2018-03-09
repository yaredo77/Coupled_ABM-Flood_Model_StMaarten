/**
 * Copyright (c) [2018] [Yared Abayneh Abebe]
 *
 * This file is part of Coupled_ABM-Flood_Model.
 * Coupled_ABM-Flood_Model is free software licensed under the CC BY-NC-SA 4.0
 * You are free to:
 *	 Share — copy and redistribute the material in any medium or format
 *   Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *	 Attribution — You must give appropriate credit, provide a link to the license, 
 *				  and indicate if changes were made. You may do so in any reasonable 
 *				  manner, but not in any way that suggests the licensor endorses you 
 *				  or your use.
 *	 NonCommercial — You may not use the material for commercial purposes.
 *	 ShareAlike — If you remix, transform, or build upon the material, you must distribute 
 *				 your contributions under the same license as the original. 
 *   Full license description: https://creativecommons.org/licenses/by-nc-sa/4.0/
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
