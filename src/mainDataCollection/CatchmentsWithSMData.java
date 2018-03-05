/**
 * 
 */
package mainDataCollection;

import repast.simphony.data2.AggregateDataSource;

/**
 * @author abebe8
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
