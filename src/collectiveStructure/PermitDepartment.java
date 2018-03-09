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
 
package collectiveStructure;

//import physicalStructure.Plan;
//import repast.simphony.engine.environment.RunEnvironment;
//import repast.simphony.parameter.Parameters;
//import repast.simphony.random.RandomHelper;

/**
 * The Department of Permits deals with the processing and issuance of building related permits 
 * and business environment related permits within the work-sphere of the ministry of VROMI.
 * 
 * Once households submit their building plan, the permit department gives (or declines) permit
 * based on the location and type of building. This also refers to the zoning and building codes.
 * However, if the permit department is corrupt, households may build houses in areas designated 
 * for, for example, green area or culture or in flood prone areas.
 * 
 * In this case, the focus is mainly on permits given to developments close to the sea.
 * 
 * @param corruptionRate
 */
public class PermitDepartment {
	//private static boolean isPermitCorrupt;
		
	/*public static boolean isPermitCorrupt() {
		*//** @param corruptionRate 
		 * Corruption rate is one of the MODEL PARAMETERS.
		 * The default value is 0.1, i.e., 90% of the times, permit department is not corrupt. 
         *//*
		Parameters params = RunEnvironment.getInstance().getParameters();
		double corruptionRate = (double)params.getValue("corruption_Rate"); 
		if (RandomHelper.nextDoubleFromTo(0, 1) < corruptionRate) {
			isPermitCorrupt = true; // 
		} else {
			isPermitCorrupt = false;
		}
		//System.out.println("corrupt = " + isPermitCorrupt);
		return isPermitCorrupt;
	}*/

	/***********************************************************************************************
	***********************************************************************************************/
	/*public static boolean grantPermit(Plan plan) {
		if (isPermitCorrupt()) {
			return true;
		} else {
			return false;
		}
	}*/
	
	/***********************************************************************************************
	***********************************************************************************************/
	/*public static void setPermitCorrupt(boolean isPermitCorrupt) {
		PermitDepartment.isPermitCorrupt = isPermitCorrupt;
	}*/

	// TODO how to punish corrupt permit department?
	// monitoring and sanctioning of permit department corruption is taken into
	// account by varying the percentage of corruption
}
