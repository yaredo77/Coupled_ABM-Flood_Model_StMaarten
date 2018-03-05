package physicalStructure;

/**
 * @author abebe8
 * 
 * the Subcatchment class is created to update the Curve Number (CN) of subcatchments in Rainfall-Runoff analysis. Once a new development
 * is built in the Household class, the subcatchment where this building is located is identified. Then, depending on the number of 
 * developments in a given subcatchment, the CN number will be updated. 
 *
 */

public class Subcatchment {
	private String subcatchmentName;
	
	public Subcatchment(String subcatchmentName) {
		super();
		this.subcatchmentName = subcatchmentName;
	}
	
	
	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public String getSubcatchmentName() {
		return subcatchmentName;
	}
	
	public void setSubcatchmentName (String subcatchmentName) {
		this.subcatchmentName = subcatchmentName;
	}
	

}
