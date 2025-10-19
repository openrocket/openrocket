package info.openrocket.core.simulation;

import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataBranchViewTest extends BaseTestCase {

	@Test
	void valuesViewReflectsBranchMutations() {
		FlightDataBranch branch = new FlightDataBranch("test", FlightDataType.TYPE_TIME);

		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.1);

		List<Double> view = branch.getValuesView(FlightDataType.TYPE_TIME);
		assertEquals(1, view.size());
		assertEquals(0.1, view.get(0));

		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.2);

		assertEquals(2, view.size(), "View should reflect newly appended samples");
		assertEquals(0.2, view.get(1));
	}

	@Test
	void valuesViewIsUnmodifiable() {
		FlightDataBranch branch = new FlightDataBranch("test", FlightDataType.TYPE_TIME);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 1.0);

		List<Double> view = branch.getValuesView(FlightDataType.TYPE_TIME);

		assertThrows(UnsupportedOperationException.class, () -> view.add(2.0));
	}

	@Test
	void valuesViewForMissingTypeIsEmptyAndUnmodifiable() {
		FlightDataBranch branch = new FlightDataBranch("test", FlightDataType.TYPE_TIME);

		List<Double> view = branch.getValuesView(FlightDataType.TYPE_VELOCITY_TOTAL);
		assertNull(view);
	}
}
