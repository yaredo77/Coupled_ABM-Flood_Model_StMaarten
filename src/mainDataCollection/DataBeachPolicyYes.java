/**
 * 
 */
package mainDataCollection;

import physicalStructure.Plan;
import repast.simphony.data2.AggregateDataSource;

/**
 * @author abebe8
 *
 */
public class DataBeachPolicyYes implements AggregateDataSource {
	@Override
	public String getId() {
		return "BP Yes (cum)";
	}

	@Override
	public Class<?> getDataType() {
		return Integer.class;
	}

	@Override
	public Class<?> getSourceType() {
		return Plan.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		int count = 0;
		for (Object o: objs) {
			if (((Plan) o).getPlanComplianceBP().equals("yes")) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void reset() {
	}
}
