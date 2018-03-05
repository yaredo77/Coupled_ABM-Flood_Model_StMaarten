/**
 * 
 */
package operationalStructure;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;

import collectiveStructure.Household;
import physicalStructure.House;
import physicalStructure.Plan;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import stMaarten.GlobalVariables;
import stMaarten.StMaartenBuilder;

/**
 * @author abebe8
 * 
 * This class implements the Urban Development Action situation. This action situation consists of the following entity actions:
 * 			- Determine number of new buildings
 * 			- Make plan
 * 			- Cancel plan (based on Beach Policy)
 * 			- Update plan (based on NDP flood zone - elevate house by 50cm to 150cm)
 * 			- Update plan (based on Building Ordinance - elevate house by 20cm)
 * 			- Build house
 * 
 * In this model, we assume that household agents have only one type of development (residential) because of lack of data. There 
 * is no limit to the number of developments a household makes. This is just the programmers decision to make new developments from  
 * this class. However, the total annual number of developments in the city is limited. The main aim in this case is just to replicate 
 * how new developments affect (drive) flood risk, i.e., drivers of hazard and exposure. Another aspect is that the households also
 * implement individual flood risk management measures (assuming that they know about FRM measures, afford and are willing to
 * implement these measures). In this case, too, behaviour and decisions of households are drivers of vulnerability. 
 * 
 * In this particular case study, economic situations of households are not considered because of lack of data. 
 * However, incorporating economic model (including insurance) would enrich the model. 
 *
 * @assumptions: - Households are static. 
 *               - A household owns one house. The reverse is also true.
 *               - If a household decides to implement measures, the assumption is that it has enough financial resource.
 *               - All houses are assumed to be of the same use/function (i.e., all are residential).
 *               - Only new houses apply measures such as elevated floors and green roofs.
 * 
 * @param beachPolicyCompliance
 * @param distanceFromSea
 * @param floodZoningPolicyCompliance
 * @param buildingOrdinanceCompliance
 * @param buildingOrdinanceFloorElevation
 * 
 * @measures zoning
 * @measures beachPolicy
 * @measures building ordinance
 * 
 */
public class UrbanDevelopmentActionSituation {
	
	private static int index;  // index of new development location. See makePlan method.
	private static final String YES = "yes";
	private static final String NO = "no";
	private static final String NA = "NA";  // Not Applicable
	
	@ScheduledMethod(start = 1, interval = 1, priority = 44)
	public static void urbanDevelopmentActionSituation() {

		// TODO consider asset (or saving) dynamics
		// TODO consider insurance policies

		// set isFlooded to default at the beginning of every tick
		for (int i = 0; i < GlobalVariables.getExistingHouseholdList().size(); i++) {
			GlobalVariables.getExistingHouseholdList().get(i).getHouse().setIsFlooded(0);  
			GlobalVariables.getExistingHouseholdList().get(i).getHouse().setFloodDepth(0);  
			GlobalVariables.getExistingHouseholdList().get(i).getPlan().setPlanComplianceBP(NA);
		}
		
		/** urbanBuildingGrowthEquation
		 * The equation used to fit urban building growth is one of the dynamic variables (a discreet time series).
		 * We use a half-life exponential fitting curve (see http://mycurvefit.com/). This curve is based on the number of 
		 * housing permits issued by the Permit Department of St Maarten VROMI. (Source: St Maarten Statistics Agency Yearbook 2014, pp 22). 
		 * 
		 * As a time series, this curve is a function of the tick count.
		 * 
		 * This variable may reflect the economic situation on the island. A trend of higher number of new houses may show economic boom,
		 * or a lower trend may be seen after a major disaster event that hurts the economy (e.g. tourism and associated businesses).
		 *   
		 * TODO Does a flood event have an impact on the number of new buildings? Perhaps, according to the magnitude of flood event,
		 * economy of the island may slow down that is reflected on the number of new buildings.
		 */

		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); // get tick count from the Runtime Environment
		//a half-life exponential fitting curve with number of permits issued between 2009 and 2013 given as 45, 127, 214, 198, 200
		int numberOfNewBuildings = (int)(213.1676+(-430.6214/(Math.pow(2.0, tick/0.7523272)))); 
		/*//a half-life exponential fitting curve with number of permits issued between 2009 and 2013 given as 48, 120, 179, 171, 306
		numberOfNewBuildings = (int)(253.978+(-419.7313/(Math.pow(2.0, tick/0.9589492))));*/
		
		// this if-statement avoids error in GlobalVariables.listOfBuildings() method if either we start from very FEW houses which might be
		// less than the household size or the urban growth rate is VERY high.
		if (numberOfNewBuildings > GlobalVariables.getExistingHouseholdList().size()) {
			numberOfNewBuildings = GlobalVariables.getExistingHouseholdList().size();
		}
		// this if-statement avoids out of index error when the number of new buildings is greater than development location points
		if (StMaartenBuilder.getDevelopmentLocations().size() <= numberOfNewBuildings) {
			numberOfNewBuildings = StMaartenBuilder.getDevelopmentLocations().size();
		}
		
		// Make sure that a maximum of one building is created under each household, and finally, a total number of exactly numberOfNewBuildings. 
		// To do that, a list with same size as the householdlist (GlobalVariables class) is created and populated with numbers between zero and 
		// the size of the list. Then, randomly select an integer from the list and if the integer is less than numberOfNewBuildings, make new house 
		// and remove that number from the list so that the same household does not make many houses. This implementation creates randomness, if 
		// necessary, with which household creates new one. However, this does not affect the future development pattern (see makePlanEntityAction()).
		ArrayList<Household> selectedHouseholdList = new ArrayList<Household>();
		if (StMaartenBuilder.getDevelopmentLocations().size() > 0) { // this is to avoid out of index exception
			for (int i = 0; i < numberOfNewBuildings; i++) {
				int selectedHouseholdIndex = GlobalVariables.listOfBuildings();
				selectedHouseholdList.add(GlobalVariables.getExistingHouseholdList().get(selectedHouseholdIndex));
			}
		}

		for (int j = 0; j < selectedHouseholdList.size(); j++) {
			/** @measure: Flood Zoning
			 * Zoning (or Land-use Policy or a Development Plan) for urban development is one of the MEASURES.
			 * In this case, zoning relates to only flood zones. If new houses are located in flood zones, they may build an elevated house.
			 */

			/** @measure Beach Policy
			 * St Maarten Beach Policy is another MEASURES.
			 * Though the main objective of this policy is protection of beaches from recreational point of view, it also affects the
			 * exposure (elements at risk) level of new constructions. One aspect of this policy is that "strip of natural sea sand with a width 
			 * of 50 meters along the sea, or, in the absence of sea sand, 25 meters from high water line" should be free from construction.
			 * However, the policy also puts exceptions for construction permits in case of "special circumstances."  
			 * (Source: St Maarten Beach Policy - August 1994). 
			 */

			Household household = selectedHouseholdList.get(j);
			makePlanEntityAction(household);  // sets location and elevation to a plan. elevated stays default (0)
			implementBeachPolicy(household);

			// if household.plan.location is not null, check the other policies and then instantiate (create) new household 
			if (household.getPlan().getPlanLocation() != null) {
				implementFloodZoningPolicy(household); 
				// Building Ordinance can be implemented by any household (no specific zones where the ordinance is applied). However, if a new household already 
				// implemented the flood zoning policy, there is no need to implement this measure again (as both policies are related to elevating house floor height).
				// Houses located in the flood zones but do not comply with the Flood Zoning Policy can still comply with the Building Ordinance.
				if (household.getPlan().getPlanElevated() == 0) {
					implementBuildingOrdinance(household);  
				}

				buildHouseEntityAction(household.getPlan());
				StMaartenBuilder.getDevelopmentLocations().remove(index); // remove the location from developmentLocations list once it is occupied
				StMaartenBuilder.getDevelopmentLocations_elev().remove(index); // remove the elevation from developmentLocations_elev list once it is occupied
				// check in which subcatchment the new development happens
				developmentSubcatchment(household.getPlan());
			} // else (if plan.getlocation() is null), no development. In that case, the location (and corresponding elevation) will still be available for selection.
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	public static void makePlanEntityAction(Household h) {
		// Get a location and corresponding elevation of new houses randomly from a pre-prepared file (based on development plans) and assign those values to the plan.
		index = RandomHelper.nextIntFromTo(0, StMaartenBuilder.getDevelopmentLocations().size() - 1); // this creates randomness in future development plans
		//index = 0; // this results the same urban development pattern in every simulation
		Geometry location = StMaartenBuilder.getDevelopmentLocations().get(index);
		double elevation = StMaartenBuilder.getDevelopmentLocations_elev().get(index);
		//set the above values to the plan attributes
		h.getPlan().setPlanLocation(location);
		h.getPlan().setPlanElevation(elevation);
		h.getPlan().setPlanElevated(0);
		h.getPlan().setPlanComplianceBO(NA);
		h.getPlan().setPlanComplianceBP(NA);
		h.getPlan().setPlanComplianceFZP(NA);
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// Check if location is within "a distance" of the sea. If so, use plan.location as flag and set it to null. This method does not affect 
	// plan.elevated and plan.elevation
	public static void implementBeachPolicy(Household h) {
		
		/** @param beachPolicyCompliance
		 * The implementation of this policy is based on compliance rate of household agents. The assumption here are 
		 * 		- compliance rate also reflects enforcement.
		 * 		- if households comply with the policy, they comply exactly as written in the document.
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Beach Policy compliance" and has a double value.
		 */
		
		/** @param distanceFromSea
		 * The permitted distance from sea for new developments is one of the MODEL PARAMETERS.
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Beach Policy (Distance from sea in m)" and has a double value.
		 */

		// get the beach policy compliance rate from the Parameter Tab in Repast Simphony Runtime Environment
		Parameters params = RunEnvironment.getInstance().getParameters();
		double beachPolicyCompliance = (double)params.getValue("beach_policy_compliance");

		// Check if location is close to sea. 
		// physicalStructure.CoastLine.proximityToSea method returns distance to coast line in decimal degrees. Therefore, we need to convert decimal 
		// degrees to meters. At 18 degree latitude (St Maarten's location), one meter is roughly equivalent to 9.44541x10^-6 decimal degrees. 
		// Or, 1 decimal degree = 105871.5 m
		double distanceFromSea = (double)params.getValue("distance_from_sea");

		if ((physicalStructure.CoastLine.proximityToSea(h.getPlan().getPlanLocation())*105871.5) < distanceFromSea) { // if new location is within distance to the sea
			double rand = RandomHelper.nextDouble();
			if (rand <= beachPolicyCompliance) { 
				h.getPlan().setPlanLocation(null); // This is a kind of flag. If location is null, new development will not happen.
				h.getPlan().setPlanComplianceBP(YES);
				//GlobalVariables.setCountBP_YES(GlobalVariables.getCountBP_YES()+1);
				//System.out.println("BP YES");
			} else { // else, plan.location will not change its value, and hence, the development will happen
				h.getPlan().setPlanComplianceBP(NO);
				//System.out.println("BP NO");
			}
		} else {
			// if new location is not within distance to the sea, plan.location will not change
			h.getPlan().setPlanComplianceBP(NA);
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// Check if plan.location is in flood-prone area. This method may change the value of plan.elevated but it does not affect 
	// plan.location and plan.elevation.
	public static void implementFloodZoningPolicy(Household h) {
		
		/** @param floodZoningPolicyCompliance 
		 * The (behavioural) parameter associated with the flood zoning policy is floodZoningPolicyCompliance.
		 * The implementation of this policy is based on compliance rate of household agents. The assumptions here are: 
		 * 		- compliance rate also reflects enforcement.
		 * 		- if households comply with the policy, they comply exactly as written in the document.
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Zoning Policy compliance" and has a double value.
		 */

		// always instantiate this variable as false
		boolean isLocationInFloodZone = false;

		// get the beach policy scenario from the Parameter Tab in Repast Simphony Runtime Environment
		Parameters params = RunEnvironment.getInstance().getParameters();
		double floodZoningPolicyCompliance = (double)params.getValue("flood_zoning_compliance");
		
		// check if plan.getLocation is in the delineated flood zone
		for (int i = 0; i < StMaartenBuilder.getFloodZonesGeom().size(); i++) { // loop through the collection of flood-zones polygons
			isLocationInFloodZone = h.getPlan().getPlanLocation().within(StMaartenBuilder.getFloodZonesGeom().get(i)); // 'within' method returns boolean
			// if the location is in one of the flood zones and the household comply with the policy, elevate the floor of the new house.
			if (isLocationInFloodZone) {
				double rand = RandomHelper.nextDouble(); 
				if (rand <= floodZoningPolicyCompliance) {
					h.getPlan().setPlanElevated(StMaartenBuilder.getFloodZonesFloorHeight().get(i));
					h.getPlan().setPlanComplianceFZP(YES);
					//System.out.println("FZP YES");
				} else {
					// if the household does not comply with the policy, do not elevate the floor of the new house.
					h.getPlan().setPlanElevated(0);
					h.getPlan().setPlanComplianceFZP(NO); 
					//System.out.println("FZP NO");
				}
				// Go out of the for-loop once we know that plan.getLocation is located in one of the flood zones (i.e., the floor height is known)
				break;
			} else {
				// if plan.getLocation is not located in the flood zones, plan.setElevated remains 0 (default) and plan.setPlanComplianceZP is NA (default)
				h.getPlan().setPlanElevated(0);
				h.getPlan().setPlanComplianceFZP(NA);
			}
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	public static void implementBuildingOrdinance(Household h) {

		/** @param buildingOrdinanceCompliance 
		 * The (behavioural) parameter associated with the building ordinance is buildingOrdinanceCompliance.
		 * The implementation of this policy is based on compliance rate of household agents. The assumptions here are: 
		 * 		- compliance rate also reflects enforcement.
		 * 		- if households comply with the policy, they comply exactly as written in the document.
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Building Ordinance compliance" and has a double value.
		 */

		/** @param buildingOrdinanceFloorElevation 
		 * This parameter defines the the floor elevation value. If agents comply the with the building ordinance, they elevate the 
		 * house by the stated height. 
		 * 
		 * The default value is 0.2m and obtained from the Sint Maarten LANDSBESLUIT ter uitvoering van artikel 19 van de Bouw- en 
		 * woningverordening 1935 (State decree implementing Article 19 of the Building and Housing Ordinance).
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Building Ordinance floor elevation" and has a double value.
		 */

		// get the beach policy scenario from the Parameter Tab in Repast Simphony Runtime Environment
		Parameters params = RunEnvironment.getInstance().getParameters();
		double buildingOrdinanceCompliance = (double)params.getValue("building_ordinance_compliance");
		double buildingOrdinanceFloorElevation = (double)params.getValue("buildingOrdinanceFloorElevation");

		double rand = RandomHelper.nextDouble();
		if (rand <= buildingOrdinanceCompliance) { 
			h.getPlan().setPlanElevated(buildingOrdinanceFloorElevation);
			h.getPlan().setPlanComplianceBO(YES);
			//System.out.println("BO YES");
		} else {
			h.getPlan().setPlanElevated(0);
			h.getPlan().setPlanComplianceBO(NO);
			//System.out.println("BO NO");
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	public static void buildHouseEntityAction(Plan plan) {
		
		// Instantiate a new house and set its attributes. Building function, isFlooded and floodDepth have default values 
		// of "Residential", 0, and 0, respectively. Elevation, elevated, xycoordinates, complianceBP, complianceFZP 
		// and complianceBO are acquired from the plan. xyCoorString is the string form of xycoordinates.
		House newHouse = new House("Residential", plan.getPlanElevation(), 0, plan.getPlanElevated(), plan.getPlanLocation().getCoordinate(),
				plan.getPlanComplianceBP(), plan.getPlanComplianceFZP(), plan.getPlanComplianceBO(), plan.getPlanLocation().getCoordinate().toString(), 0);
		
		// Instantiate a new plan with default values
		Plan newPlan = new Plan(null, 0, 0, NA, NA, NA); 

		// Instantiate a new household with the new house and new plan instantiated above. 
		Household newHousehold = new Household(newHouse, newPlan); 

		// Add the new household and the house in the householdlist, context and geography
		GlobalVariables.getExistingHouseholdList().add(newHousehold);  // update household list

		StMaartenBuilder.context.add(newHousehold);
		StMaartenBuilder.geography.move(newHousehold, plan.getPlanLocation());
		StMaartenBuilder.context.add(newHouse);
		StMaartenBuilder.geography.move(newHouse, plan.getPlanLocation());
		StMaartenBuilder.context.add(newPlan);
	}

	/***********************************************************************************************
	***********************************************************************************************/
	// This for-loop checks in which subcatchment the new development happens and stores the subcatchment name in frequency_SubcatchmentNameList.
	// The list, then, will be used to analyse the change in Curve Number (CN) of subcatchments (see in GlobalVariables and ABMFloodModelCouple classes). 
	public static void developmentSubcatchment(Plan plan) {
		boolean isLocationInSubcatchment = false;
		for (int i = 0; i < StMaartenBuilder.getSubcatchmentGeom().size(); i++) {
			isLocationInSubcatchment = plan.getPlanLocation().within(StMaartenBuilder.getSubcatchmentGeom().get(i)); // 'within' method returns boolean
			if (isLocationInSubcatchment) {
				GlobalVariables.getFrequency_SubcatchmentNameList().add(StMaartenBuilder.getSubcatchmentNameList().get(i));
				//go out of the loop once the subcatchment is known
				break;
			}
		}
	}
	
	/***********************************************************************************************
	 *********************************** Getters and Setters ****************************************
	 ***********************************************************************************************/
}
