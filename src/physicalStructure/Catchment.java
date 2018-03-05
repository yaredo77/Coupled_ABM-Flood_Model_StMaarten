/**
 * 
 */
package physicalStructure;

/**
 * @author abebe8
 * 
 * This class is used just for visualisation purpose.
 *
 */
public class Catchment {
	private String catchmentName;
	
	public Catchment(String catchmentName) {
		super();
		this.catchmentName = catchmentName;
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public String getCatchmentName() {
		return catchmentName;
	}
	
	public void setCatchmentName (String catchmentName) {
		this.catchmentName = catchmentName;
	}
	
	
}
