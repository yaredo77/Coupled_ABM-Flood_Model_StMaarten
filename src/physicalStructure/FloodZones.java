/**
 * 
 */
package physicalStructure;


/**
 * @author abebe8
 * 
 * this class is related to the flood prone areas delineated in the National Development Plan.
 *
 */
public class FloodZones {
	private double floorHeight;
	
	public FloodZones(double floorHeight) {
		super();
		this.floorHeight = floorHeight;
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public double getFloorHeight() {
		return floorHeight;
	}

	public void setFloorHeight(double floorHeight) {
		this.floorHeight = floorHeight;
	}

}
