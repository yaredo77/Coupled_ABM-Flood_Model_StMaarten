/**
 * Copyright (c) [2018] [Yared Abayneh Abebe]
 *
 * This file is part of Coupled_ABM-Flood_Model.
 * Coupled_ABM-Flood_Model is free software licensed under the CC BY-NC-SA 4.0
 * You are free to:
 *	Share — copy and redistribute the material in any medium or format
 *   Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *	Attribution — You must give appropriate credit, provide a link to the license, 
 *				  and indicate if changes were made. You may do so in any reasonable 
 *				  manner, but not in any way that suggests the licensor endorses you 
 *				  or your use.
 *	NonCommercial — You may not use the material for commercial purposes.
 *	ShareAlike — If you remix, transform, or build upon the material, you must distribute 
 *				 your contributions under the same license as the original. 
 *  Full license description: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
 
package mainDataCollection;

import repast.simphony.data2.AggregateDataSource;

/**
 *
 */
public class CatchmentsWithSMData implements AggregateDataSource {
	@Override
	public String getId() {
		return "catchmentName";
	}

	@Override
	public Class<?> getDataType() {
		return String.class;
	}

	@Override
	public Class<?> getSourceType() {
		return CatchmentsWithSM.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		String name = null;
		for (Object o: objs) {
			name = ((CatchmentsWithSM) o).getCatchmentName();
		}
		return name;
	}

	@Override
	public void reset() {
	}
}
