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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.WWTexture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;

import repast.simphony.visualization.gis3D.BufferedImageTexture;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

/**
 * derived from the Repast Simphony Geography model (GisAgentStyle Class) by Eric Tatara
 */
public class HouseStyle implements MarkStyle<House> {

	/**
	 * The gov.nasa.worldwind.render.Offset is used to position the icon from 
	 *   the mark point location.  If no offset is provided, the lower left corner
	 *   of the icon is located at the point (lat lon) position.  Using values of
	 *   (0.5,0.5) will position the icon center over the lat lon location.
	 *   The first two arguments in the Offset constructor are the x and y 
	 *   offset values.  The third and fourth arguments are the x and y units 
	 *   for the offset. AVKey.FRACTION represents units of the image texture 
	 *   size, with 1.0 being one image width/height.  AVKey.PIXELS can be used 
	 *   to specify the offset in pixels. 
	 */
	Offset iconOffset = new Offset(0.5d, 0.5d, AVKey.FRACTION, AVKey.FRACTION);

	@Override
	public PlaceMark getPlaceMark(House house, PlaceMark mark) {
		
		// PlaceMark is null on first call.
		if (mark == null) {
			mark = new PlaceMark();
		}
		mark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

		return mark;
	}

	@Override
	public WWTexture getTexture(House house, WWTexture texture) {
			
		/*// BasicWWTexture is useful when the texture is a non-changing image.
		URL localUrl = WorldWind.getDataFileStore().requestFile("icons/house2.png");
		if (localUrl != null) {
			return new BasicWWTexture(localUrl, false);
		}
		return null;*/
		
		Color color = null;
		
		if (house.getElevated() == 0){
			color = Color.BLUE;
		}
		else{
			color = Color.GREEN;
		}
		BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_SQUARE, new Dimension(10, 10), 0.7f, color);

		return new BufferedImageTexture(image);	
	}
	
	@Override
	public String getLabel(House house) {
		return null;
	}

	@Override
	public Color getLabelColor(House house) {
		return null;
	}

	@Override
	public Font getLabelFont(House house) {
		return null;
	}

	@Override
	public double getScale(House house) {
		return 1;
	}

	@Override
	public Offset getLabelOffset(House house) {
		return null;
	}

	@Override
	public Offset getIconOffset(House house) {
		return iconOffset;
	}

	@Override
	public double getElevation(House house) {
		return 0;
	}

	@Override
	public double getHeading(House house) {
		return 0;
	}

	@Override
	public double getLineWidth(House house) {
		return 0;
	}

	@Override
	public Material getLineMaterial(House house, Material lineMaterial) {
		return null;
	}

}
