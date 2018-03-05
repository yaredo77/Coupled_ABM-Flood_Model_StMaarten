/**
 * 
 */
package collectiveStructure;

import physicalStructure.House;
import physicalStructure.Plan;

/**
 * @author abebe8
 *
 * This class implements the household agent. This agent owns a house and also engage in new developments. in the later case, the
 * household need to make a plan (i.e., location of the new house). The word development is used to show that household agents can 
 * be property owners of different type (e.g., residential, small business, hotels, real estates, etc). In this model, we assume that 
 * household agents have only one type of development (residential). There is no limit to the number of developments a household makes. 
 * This is just the programmers decision to make new developments from this class. However, the total annual number of developments in 
 * the city is limited. 
 *  
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
