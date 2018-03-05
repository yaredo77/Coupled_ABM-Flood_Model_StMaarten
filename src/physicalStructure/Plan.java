/**
 * 
 */
package physicalStructure;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author abebe8
 *
 */
public class Plan {
	private double plan_elevated;  // if the house is located in flood-prone area and elevated according to the development plan
	private Geometry plan_location;
	private double plan_elevation;
	// these attributes are related to agents' behaviours. They have values of "yes" (for complying agents), "no" (for non-complying agents) and 
	// "NA" (for agents that were instantiated before the institutions) 
	private String plan_complianceBP; // compliance Beach Policy
	private String plan_complianceFZP; // compliance Flood Zoning Policy
	private String plan_complianceBO; // compliance Building Ordinance
	
	public Plan(Geometry location, double elevated, double elevation, String complianceBP, String complianceFZP, String complianceBO) {
		super();
		this.plan_location = location;
		this.plan_elevated = elevated;
		this.plan_elevation = elevation;
		this.plan_complianceBP = complianceBP;
		this.plan_complianceFZP = complianceFZP;
		this.plan_complianceBO = complianceBO;
	}
	
	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public double getPlanElevated() {
		return plan_elevated;
	}
	
	public void setPlanElevated (double elevated) {
		this.plan_elevated = elevated;
	}
	
	public Geometry getPlanLocation() {
		return plan_location;
	}
	
	public void setPlanLocation(Geometry location) {
		this.plan_location = location;
	}

	public double getPlanElevation() {
		return plan_elevation;
	}
	
	public void setPlanElevation(double elevation) {
		this.plan_elevation = elevation;
	}

	public String getPlanComplianceBP() {
		return plan_complianceBP;
	}

	public void setPlanComplianceBP(String complianceBP) {
		this.plan_complianceBP = complianceBP;
	}

	public String getPlanComplianceFZP() {
		return plan_complianceFZP;
	}

	public void setPlanComplianceFZP(String complianceFZP) {
		this.plan_complianceFZP = complianceFZP;
	}

	public String getPlanComplianceBO() {
		return plan_complianceBO;
	}

	public void setPlanComplianceBO(String complianceBO) {
		this.plan_complianceBO = complianceBO;
	}
}
