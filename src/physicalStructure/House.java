/**
 * 
 */
package physicalStructure;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author abebe8
 *
 */
public class House {
	private String buildingFunction;  // knowing the function of buildings might be useful 
	private double elevation;
	//private double damage;
	//private boolean isFlooded;
	private int isFlooded;  // 1 refers to flooded and 0 not flooded. using boolean was not easy for data collection (in the runtime environment)
	private double elevated;
	private Coordinate xyCoor;
	// these attributes are related to agents' behaviours. They have values of "yes" (for complying agents), "no" (for non-complying agents) and 
	// "NA" (for agents that were instantiated before the institutions) 
	private String complianceBP; // compliance Beach Policy
	private String complianceFZP; // compliance Flood Zoning Policy
	private String complianceBO; // compliance Building Ordinance
	// these attributes are added to collect them out
	private String xyCoorString;
	private double floodDepth;

	public House(String buildingFunction, double elevation, int isFlooded, double elevated, Coordinate xyCoor, 
			String complianceBP, String complianceZP, String complianceBO, String xyCoorString, double floodDepth) {
		super();
		this.buildingFunction = buildingFunction;
		this.elevation = elevation;
		this.isFlooded = isFlooded;
		this.elevated = elevated;
		this.xyCoor = xyCoor;
		this.complianceBP = complianceBP;
		this.complianceFZP = complianceZP;
		this.complianceBO = complianceBO;
		this.xyCoorString = xyCoorString;
		this.floodDepth = floodDepth;
	}
	
	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public String getBuildingFunction() {
		return buildingFunction;
	}
	
	public void setBuildingFunction (String buildingFunction) {
		this.buildingFunction = buildingFunction;
	}

	public double getElevation() {
		return elevation;
	}
	
	public void setElevation (double elevation) {
		this.elevation = elevation;
	}
	
	public int getIsFlooded() {
		return isFlooded;
	}
	
	public void setIsFlooded (int isFlooded) {
		this.isFlooded = isFlooded;
	}
	
	public double getElevated() {
		return elevated;
	}
	
	public void setElevated (double elevated) {
		this.elevated = elevated;
	}

	public Coordinate getxyCoor() {
		return xyCoor;
	}
	
	public void setxyCoor (Coordinate xyCoor) {
		this.xyCoor = xyCoor;
	}

	public String getComplianceBP() {
		return complianceBP;
	}

	public void setComplianceBP(String complianceBP) {
		this.complianceBP = complianceBP;
	}

	public String getComplianceFZP() {
		return complianceFZP;
	}

	public void setComplianceFZP(String complianceFZP) {
		this.complianceFZP = complianceFZP;
	}

	public String getComplianceBO() {
		return complianceBO;
	}

	public void setComplianceBO(String complianceBO) {
		this.complianceBO = complianceBO;
	}

	public String getXyCoorString() {
		return xyCoorString;
	}

	public void setXyCoorString(String xyCoorString) {
		this.xyCoorString = xyCoorString;
	}

	public double getFloodDepth() {
		return floodDepth;
	}

	public void setFloodDepth(double floodDepth) {
		this.floodDepth = floodDepth;
	}
}
