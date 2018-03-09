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

import physicalStructure.House;
import physicalStructure.Plan;

/**
 * This class implements the household agent. This agent owns a house and also engage in new developments. In the later case, the
 * household need to make a plan (i.e., location of the new house). The word development is used to show that household agents can 
 * be property owners of different type (e.g., residential, small business, hotels, real estates, etc). In this model, we assume that 
 * household agents have only one type of development (residential). There is no limit to the number of developments a household makes. 
 * This is just the programmers decision to make new developments from this class. However, the total annual number of developments in 
 * the city is limited. 
 */
public class Household {

	//private int householdSize; 
	//private double asset; // this can also be saving or income!
	//private boolean hasInsurance;
	//private double householdImpact;

	// declaring and putting House object under Household Class guarantees a one to one relationship between them
	private House house; // TODO should be static??
	private Plan plan; // initial values of Plan // TODO should be static??

	public Household(House house, Plan plan) {
		super();
		this.house = house;
		this.plan = plan;
	}

	/***********************************************************************************************
	 *********************************** Getters and Setters ****************************************
	 ***********************************************************************************************/

	public House getHouse() {
		return house;
	}

	public void setHouse(House house) {
		this.house = house;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
}
