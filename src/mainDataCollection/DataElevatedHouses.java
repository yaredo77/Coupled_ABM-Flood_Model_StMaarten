/**
 * 
 */
package mainDataCollection;

import physicalStructure.House;
import repast.simphony.data2.AggregateDataSource;

/**
 * @author abebe8
 *
 */
public class DataElevatedHouses implements AggregateDataSource{
	@Override
	public String getId() {
		return "Elevated houses (cum)";
	}

	@Override
	public Class<?> getDataType() {
		return Integer.class;
	}

	@Override
	public Class<?> getSourceType() {
		return House.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		int count = 0;
		for (Object o: objs) {
			if (((House) o).getElevated() > 0) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void reset() {
	}
}
