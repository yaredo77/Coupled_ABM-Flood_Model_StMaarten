/**
 * 
 */
package physicalStructure;

/**
 * @author abebe8
 * 
 * this class is not used!
 *
 */
public class FRMMeasure {
	private String type; // types of flood risk management(FRM) options
	private String status; // status of FRM existing measures (good or bad)
	
	public FRMMeasure(String type, String status, double dimension) {
		super();
		this.type = type;
		this.status = status;
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public String getType() {
		return type;
	}
	
	public void setType (String type) {
		this.type = type;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus (String status) {
		this.status = status;
	}
	
}
