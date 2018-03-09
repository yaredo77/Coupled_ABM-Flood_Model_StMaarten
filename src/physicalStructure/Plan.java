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

import com.vividsolutions.jts.geom.Geometry;

/**
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
