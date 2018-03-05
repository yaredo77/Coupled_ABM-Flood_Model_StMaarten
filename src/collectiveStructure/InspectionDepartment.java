/**
 * 
 */
package collectiveStructure;

/*import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import stMaarten.GlobalVariables;*/

/**
 * @author abebe8
 * 
 * The Inspection Department is responsible for the inspection and enforcement of laws 
 * in all areas concerning spatial planning and development, such as control of domain 
 * lands, building, (public) properties, environment and work safety to maintain an 
 * environmentally safe, structured and save living and work environment for the public.
 * 
 * The inspector checks if new buildings have permits and demolish houses (?) if they are 
 * built illegally. The inspector can also be corrupt and allow buildings to carry on 
 * without legal permit.
 *  
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
