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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This class loads and reads computed flood maps at a given tick and returns the flood depth  
 * at (x, y) coordinate of a house.
 */
public class FloodMap {
	
	private static double floodDepth;
	
	// Load raster (flood map) file
	public static double getFloodDepth(Coordinate coor) {
		double x = coor.x;
		double y = coor.y;
		double[] value = null;
		try {
			// Locate the raster file
			URL url = null;
			try {
				url = new File("data/hydrodynamic_data/MIKE21/Result/flood_map.tif").toURI().toURL();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			// Get the format (in this case Geotiff)
			AbstractGridFormat format = GridFormatFinder.findFormat(url);
		    GridCoverage2DReader reader = format.getReader(url);
		    	    
		    if (reader != null) {
		    	GridCoverage2D coverage = reader.read(null);
		    	DirectPosition pos = new DirectPosition2D(x, y);
		    	//Object obj = coverage.evaluate(pos);
		    	value = (double[])coverage.evaluate(pos); // extract value of cell that contain the (x,y) coordinate
		    } else {
		    	throw new IOException("No reader");
		    }
		} catch (IOException e) {
			System.out.println("flood map is not available");
			e.printStackTrace();
		}
		floodDepth = value[0];
		return floodDepth;
	}
}
