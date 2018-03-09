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
 
package stMaarten;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.table.TableModel;

import physicalStructure.House;
import physicalStructure.Plan;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.ui.table.AgentTableFactory;
import repast.simphony.ui.table.SpreadsheetUtils;
import repast.simphony.ui.table.TablePanel;
import collectiveStructure.Household;

/**
 * This class implements some global variables used in other classes. It is also used to write outputs.
 */

public class GlobalVariables {
	
	private static int index2; 
	private static List<Household> existingHouseholdList = new ArrayList<Household>(); // a list of all existing households
	private static List<Household> fullExistingAndFutureDevelopmentLocations = new ArrayList<Household>(); // a list of all house locations (existing + future locations)
	private static ArrayList<Integer> list = new ArrayList<Integer>(existingHouseholdList.size());
	private static List<String> frequency_SubcatchmentNameList = new ArrayList<String>();  // this stores all the developed subcatchments and number of new houses/buildings
	private static List<String> frequency_floodedCatchmentNameList = new ArrayList<String>();  // this stores all the flooded catchments and number of flooded houses
	private static HashMap<String, Integer> repetitions_Subcatchment = new HashMap<String, Integer>();
	private static HashMap<String, Integer> repetitions_floodedCatchment = new HashMap<String, Integer>();
	
	// save a table of agent layer for Plan.class and House.class per tick
	/*@ScheduledMethod(start=0, interval=1, priority = 5)
	public static void log() {
		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		@SuppressWarnings("rawtypes")
		Context context = RunState.getInstance().getMasterContext();
		
		Map<String,TableModel> models = new HashMap<String,TableModel>();
		
		// Create a tab panel for each agent layer
        for (Object agentType : context.getAgentTypes()) {
        	if (agentType.equals(House.class) || agentType.equals(Plan.class)) {
            	Class<?> agentClass = (Class<?>)agentType; 
    			JPanel agentPanel = AgentTableFactory.createAgentTablePanel(context.getAgentLayer(agentClass), agentClass.getSimpleName());
            	if (agentPanel instanceof TablePanel) {
            		TableModel model = ((TablePanel)agentPanel).getTable().getModel();
            		models.put(agentClass.getSimpleName(), model);
            	}
        	}
        }
        
        SpreadsheetUtils.saveTablesAsExcel(models, new File("output/" + StMaartenBuilder.getResultDirectoryName() + "/tick-"+tick+".csv"));
	}*/

	/***********************************************************************************************
	***********************************************************************************************/
	// Populate the list that is used in listOfBuildings(). It is located here since the method countRichHouses()
	// starts at zero; where as, the list is used starting tick one.
	@ScheduledMethod(start=1, interval=1, priority = 45)
	public static void populateList() {
		for(int i = 0; i < existingHouseholdList.size(); i++) {
            list.add(i);
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	public static int listOfBuildings() {
		Random rand = new Random();
		// index1 is the position in the list and index2 is the element at that position. Therefore, instead of
		// returning index1, return the actual element and then remove it from the list 
		int index1 = rand.nextInt(list.size());
		index2 = list.get(index1);
		list.remove(new Integer(index2));
		
		return index2;
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// This method maps subcatchment name with number of new developments within it.
	// frequency_SubcatchmentNameList gets updated in UrbanDevelopmentActionsituation.developmentSubcatchment method
	public static HashMap<String, Integer> countSubcatchmentFrequency() {
		repetitions_Subcatchment.clear(); // Unless we clear this hashmap every tick, it will accumulate the contents from previous ticks
		for (int i = 0; i < frequency_SubcatchmentNameList.size(); i++) {
			String item = frequency_SubcatchmentNameList.get(i);

			if (repetitions_Subcatchment.containsKey(item))
				repetitions_Subcatchment.put(item, repetitions_Subcatchment.get(item) + 1);
			else
				repetitions_Subcatchment.put(item, 1);
		}
		frequency_SubcatchmentNameList.clear(); // Unless we clear this list every tick, it will accumulate the contents from previous ticks 
		return repetitions_Subcatchment;
	}

	/***********************************************************************************************
	***********************************************************************************************/
	//This method maps catchment name with number of flooded houses within it.
	//frequency_CatchmentNameList gets updated in ABMFloodModelCouple.assesImpact method
	public static HashMap<String, Integer> countCatchmentFloodedHouses() {
		repetitions_floodedCatchment.clear(); // Unless we clear this hashmap every tick, it will accumulate the contents from previous ticks
		for (int i = 0; i < frequency_floodedCatchmentNameList.size(); i++) {
			String item = frequency_floodedCatchmentNameList.get(i);

			if (repetitions_floodedCatchment.containsKey(item))
				repetitions_floodedCatchment.put(item, repetitions_floodedCatchment.get(item) + 1); // if catchment name list exists, increment the frequency by one
			else
				repetitions_floodedCatchment.put(item, 1);
		}
		frequency_floodedCatchmentNameList.clear();  // Unless we clear this list every tick, it will accumulate the contents from previous ticks 
		return repetitions_floodedCatchment;
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public static List<Household> getExistingHouseholdList() {
		return existingHouseholdList;
	}

	public static void setExistingHouseholdList(List<Household> existingHouseholdList) {
		GlobalVariables.existingHouseholdList = existingHouseholdList;
	}

	public static List<Household> getFullExistingAndFutureDevelopmentLocations() {
		return fullExistingAndFutureDevelopmentLocations;
	}

	public static void setFullExistingAndFutureDevelopmentLocations(List<Household> fullExistingAndFutureDevelopmentLocations) {
		GlobalVariables.fullExistingAndFutureDevelopmentLocations = fullExistingAndFutureDevelopmentLocations;
	}
	
	public static List<String> getFrequency_SubcatchmentNameList() {
		return frequency_SubcatchmentNameList;
	}

	public static void setFrequency_SubcatchmentNameList(List<String> frequency_SubcatchmentNameList) {
		GlobalVariables.frequency_SubcatchmentNameList = frequency_SubcatchmentNameList;
	}

	public static List<String> getFrequency_floodedCatchmentNameList() {
		return frequency_floodedCatchmentNameList;
	}

	public static void setFrequency_floodedCatchmentNameList(List<String> frequency_floodedCatchmentNameList) {
		GlobalVariables.frequency_floodedCatchmentNameList = frequency_floodedCatchmentNameList;
	}

	public static ArrayList<Integer> getList() {
		return list;
	}

	public static void setList(ArrayList<Integer> list) {
		GlobalVariables.list = list;
	}
}
