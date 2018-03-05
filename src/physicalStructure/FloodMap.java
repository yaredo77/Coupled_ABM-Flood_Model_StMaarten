/**
 * 
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
 * @author abebe8
 * 
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
