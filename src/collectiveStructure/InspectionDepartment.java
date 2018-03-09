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

/*import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import stMaarten.GlobalVariables;*/

/**
 * The Inspection Department is responsible for the inspection and enforcement of laws 
 * in all areas concerning spatial planning and development, such as control of domain 
 * lands, building, (public) properties, environment and work safety to maintain an 
 * environmentally safe, structured and save living and work environment for the public.
 * 
 * The inspector checks if new buildings have permits and demolish houses (?) if they are 
 * built illegally. The inspector can also be corrupt and allow buildings to carry on 
 * without legal permit.
 */
public class InspectionDepartment {
	private boolean isInspectionCorrupt;
	
	/*public void inspectionCorruption() {
		double corruptionRate = 0.1; // TODO assume that 90% of inspection department officers are not corrupt
		if (RandomHelper.nextDoubleFromTo(0, 1) < corruptionRate) {
			isInspectionCorrupt = true; // TODO update inspection department corruption every time step?
		} else {
			isInspectionCorrupt = false;
		}
		
		// TODO improve this code and where to call the method?
		if (!isInspectionCorrupt) {
			int index = RandomHelper.nextIntFromTo(0, GlobalVariables.householdList.size() - 1);
			Object obj = GlobalVariables.householdList.get(index);
			Context<Object> context = ContextUtils.getContext(obj);
			context.remove(obj);
		}
	}*/

	public boolean getIsInspectionCorrupt() {
		return isInspectionCorrupt;
	}

	public void setInspectionCorrupt(boolean isInspectionCorrupt) {
		this.isInspectionCorrupt = isInspectionCorrupt;
	}
}
