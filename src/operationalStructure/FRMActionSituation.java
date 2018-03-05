/**
 * 
 */
package operationalStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import physicalStructure.Flood;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import stMaarten.GlobalVariables;
import stMaarten.StMaartenBuilder;

/**
 * @author abebe8
 * 
 * This class implements decision making process regarding FRM measures. The decisions include which measure to implement and
 * where to implement. The measures can be structural or non-structural (resilient measures). These measures can be implemented 
 * per catchment (e.g., most structural measures such as detention basin and widening channel cross-section are implemented per
 * catchment) or be applied overall the island (e.g., most resilient or adaptation measures such as beach policy, zoning policy
 * and green roofs are applied in the whole island). The structural measures are usually constructed by the Department of New 
 * Projects & Management of the Sint Maarten Ministry of Public Housing, Spatial Planning, Environment and Infrastructure. On the
 * other hand, even if the non-structural measures such as policies are issued by the same Ministry, their implementation happens 
 * per household.
 * 
 * Assumptions: - One type of structural measure is implemented in a catchment only once.  
 * 				- If there is a decision to implement a measure, it will be implemented in the same year. But, there will not be measures
 * 				  in the next 3 years (showing limited budget)
 * 
 * @param measureInterval
 * @param FRM_where_scenario
 * @param floodedThreshold
 * 
 * @scenario where/in which catchment a measure is implemented?
 * 
 */
public class FRMActionSituation {
	
	private static String selectedFloodedCatchment; // this string holds the catchment name where a structural measure is implemented
	
	@ScheduledMethod(start=1, interval=1, priority = 25)
	public static void floodRiskManagementActionSituation() {
		// remove catchmentWithSM object from selectedCatchmentsSM list every tick
		for (Object o : ABMFloodModelCouple.getSelectedCatchmentsSM()) {
			StMaartenBuilder.context.remove(o);
		}
		// then, clear the selectedCatchmentsSM list
		ABMFloodModelCouple.getSelectedCatchmentsSM().clear();

		// every time step, instantiate the selected catchment where measures are implemented as null 
		selectedFloodedCatchment = null;
		// get the tick count
		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		// get the number of flooded houses in catchments and perform structural measure decision-making
		HashMap<String, Integer> h = GlobalVariables.countCatchmentFloodedHouses();

		/** @param measureInterval
		 * Considering budget constraints and years of construction a structural measure takes, measures are not implemented every year (tick).
		 * In that case, they are implemented, at least, every some years. 
		 * This parameter defines the interval in years.
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Measures interval (in years)" and is an integer value.
		 */
		// In that case, they are implemented, at least, every some years.
		// get the interval defined in the Parameter Tab in Repast Simphony Runtime Environment
		Parameters params = RunEnvironment.getInstance().getParameters();
		int measureInterval = (int)params.getValue("measureInterval");  

		// structural measures occur either in the first tick provided that there is flood at tick=1 or at least 3 years after the last structural measure is implemented  
		// or if return period in a given tick is greater than or equal to 50 years 
		if (tick == 1 || tick > ABMFloodModelCouple.getTickLastStructuralMeasure() + measureInterval || Flood.getReturnPeriod() >= 50) {
			if (!h.isEmpty()) {
				/** @scenario structural measure?
				 * 		- scenario-1 no structural measure
				 * 		- scenario-2 catchment with the highest number of flooded houses get priority
				 * 
				 ** @param FRM_where_scenario
				 * The scenario is set in Repast Simphony Runtime Environment as "FRM_where_scenario" and has an integer value (1 or 2).
				 */
				
				// get the scenario from the Parameter Tab in Repast Simphony Runtime Environment
				int whereStructuralMeasure = (int)params.getValue("FRM_where_scenario");
				
				if (whereStructuralMeasure == 1) {
					// No structural measure!
					
				} else if (whereStructuralMeasure == 2) {
					// divide the number of flooded houses in the catchment 'SALTPOND' and add one half to catchment 'B' and the other to catchment 'D' 
					if (h.containsKey("SALTPOND")) {
						if (h.containsKey("B")) {
							h.replace("B", (h.get("B") + h.get("SALTPOND")/2));
						} else {
							h.put("B", h.get("SALTPOND")/2);
						}
						if (h.containsKey("D")) {
							h.replace("D", (h.get("D") + h.get("SALTPOND")/2));
						} else {
							h.put("D", h.get("SALTPOND")/2);
						}
						h.remove("SALTPOND");
					}
					// remove catchment 'ZE' because it is one property (beach resort with multiple houses) owned by a single agent
					if (h.containsKey("ZE")) {
						h.remove("ZE");
					}
					// remove catchment 'AA' because it only has a coastal flood
					if (h.containsKey("AA")) {
						h.remove("AA");
					}
					
					//this hashmap<catchment name, number of flooded houses> stores flooded catchment greater than threshold. 
					HashMap<String, Integer> floodedCatchmentgtThreshold = new HashMap<String, Integer>(); 
					
					/** @param floodedThreshold
					 * The minimum number of flooded houses that triggers a FRM measure implementation is one of the MODEL PARAMETERS.
					 * 
					 * The parameter is set in Repast Simphony Runtime Environment as "minimum number of flooded houses" and is an integer value.
					 * 
			         */
					int floodedThreshold = (int)params.getValue("flooded_threshold"); 

					// identify which catchments satisfies the threshold criteria and store them in another hashmap
					for (Map.Entry<String, Integer> e : h.entrySet()) {
						if (e.getValue() >= floodedThreshold) {
							floodedCatchmentgtThreshold.put(e.getKey(), e.getValue());  
							//System.out.println(e.getKey() + " " + e.getValue());
						}
					}
					
					if (!floodedCatchmentgtThreshold.isEmpty()) {
						// first identify the max number of flooded houses registered in a catchment (from floodedCatchmentgtThreshold hashmap)
						int maxVal = Collections.max(floodedCatchmentgtThreshold.values());  // maximum value of flooded catchments in a sub-catchment
						// then store all the flooded catchments names with number of flooded houses equal to maxVal
						List<String> catchmentsWithEqualNumberOfFloodedHouses = new ArrayList<String>();  // this stores all catchments with number of flooded houses equal to maxVal
						for (Map.Entry<String, Integer> e : floodedCatchmentgtThreshold.entrySet()) {
							if (e.getValue() == maxVal) {
								catchmentsWithEqualNumberOfFloodedHouses.add(e.getKey());
							}
						}
						// if there is only one catchment in equalNumberOfFloodedCatchment list, select that catchment. If there are more than one catchment in that list, 
						// choose one randomly (uniform distribution)
						if (catchmentsWithEqualNumberOfFloodedHouses.size() == 1) { 
							selectedFloodedCatchment = catchmentsWithEqualNumberOfFloodedHouses.get(0); 
						} else if (catchmentsWithEqualNumberOfFloodedHouses.size() > 1) {
							int index1 = RandomHelper.nextIntFromTo(0, catchmentsWithEqualNumberOfFloodedHouses.size() - 1); 
							selectedFloodedCatchment = catchmentsWithEqualNumberOfFloodedHouses.get(index1);
						}
					}
				} else {
					System.out.println("Please define scenarioWhere. It should be a positive integer.");
					// in this case, selectedFloodedCatchment stays null!
				}
			}
		}
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public static String getSelectedFloodedCatchment() {
		return selectedFloodedCatchment;
	}

	public static void setSelectedFloodedCatchment(String selectedFloodedCatchment) {
		FRMActionSituation.selectedFloodedCatchment = selectedFloodedCatchment;
	}
}
