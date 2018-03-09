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

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
//import repast.simphony.random.RandomHelper;

/**
 * Each time step, we decide if there will be flood or not. However, if there is flood, the random 
 * selection of return periods is based on the probability of occurrence inherent in the return periods. 
 * For example, the probability of occurrence of a flood having 100year recurrence interval in any 
 * given year is 1% (0.01). In this case, the modeller considered five return periods (5, 10, 20, 50, 100)  
 * and do not consider the probability of occurrence of other events. Therefore, one minus the sum of the 
 * probabilities of the five events is 'no or zero flood event probability'. In addition, only one flood 
 * event happens in each time step.
 * (http://codetheory.in/weighted-biased-random-number-generation-with-javascript-based-on-probability/)
 * 
 * Assumption 
 *			- Design rainfall magnitudes for the given recurrence intervals are the same throughout the simulation period,
 *			  i.e., no intensification of rainfall due to climate change 
 *
 */
public class Flood {
	private static int returnPeriod = 0; 
	
	@ScheduledMethod(start=1, interval=1, priority = 50)
	public static void makeFlood() {
		/*double[] returnPeriodWeight = {0.2, 0.1, 0.05, 0.02, 0.01, 0.62};
		double weightSum = 0;
		double weightSum1 = returnPeriodWeight[0] + returnPeriodWeight[1];
		double weightSum2 = weightSum1 + returnPeriodWeight[2];
		double weightSum3 = weightSum2 + returnPeriodWeight[3];
		double weightSum4 = weightSum3 + returnPeriodWeight[4];
		for (int i = 0; i < returnPeriodWeight.length; i++) {
			weightSum += returnPeriodWeight[i];
		}
		double randNum = 0 + (weightSum * RandomHelper.nextDouble());
		if (randNum <= returnPeriodWeight[0]) {
			returnPeriod = (int) (1/returnPeriodWeight[0]);
		} else if (returnPeriodWeight[0] < randNum && randNum <= weightSum1) {
			returnPeriod = (int) (1/returnPeriodWeight[1]);
		} else if (weightSum1 < randNum && randNum <= weightSum2) {
			returnPeriod = (int) (1/returnPeriodWeight[2]);
		} else if (weightSum2 < randNum && randNum <= weightSum3) {
			returnPeriod = (int) (1/returnPeriodWeight[3]);
		} else if (weightSum3 < randNum && randNum <= weightSum4) {
			returnPeriod = (int) (1/returnPeriodWeight[4]);
		} else {
			returnPeriod = 0;
		}*/
		
		// alternatively, we can also use a predefined time series of design rainfall. 
		int[] returnPeriodTimeSeries = {0, 100, 0, 5, 0, 5, 5, 0, 5, 0, 0, 0, 0, 0, 0, 20, 0, 0, 50, 0, 0, 5, 10, 0, 0, 0, 0, 0, 5, 0};
		int tick = (int)RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		returnPeriod =  returnPeriodTimeSeries[tick-1];
	}
	
	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/

	public static int getReturnPeriod() {
		return returnPeriod;
	}
	
	public static void setReturnPeriod (int returnPeriod) {
		Flood.returnPeriod = returnPeriod;
	}
}
