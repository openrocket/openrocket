package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import info.openrocket.core.rocketcomponent.RocketComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CADataBranchTest {

	private static final double EPSILON = 1e-9;

	private CADataBranch branch;
	private RocketComponent component;

	@BeforeEach
	void setUp() {
		branch = new CADataBranch("test-branch", CADataType.CP_X);
		component = mock(RocketComponent.class);
	}

	@Test
	void setValueTracksPerComponentSeriesAndMinMax() {
		branch.addPoint();
		branch.setValue(CADataType.CP_X, component, 5.0);

		assertEquals(5.0, branch.getLast(CADataType.CP_X, component), EPSILON);
		assertEquals(5.0, branch.getMinimum(CADataType.CP_X, component), EPSILON);
		assertEquals(5.0, branch.getMaximum(CADataType.CP_X, component), EPSILON);

		branch.addPoint();
		branch.setValue(CADataType.CP_X, component, 3.0);

		assertEquals(3.0, branch.getLast(CADataType.CP_X, component), EPSILON);
		assertEquals(3.0, branch.getMinimum(CADataType.CP_X, component), EPSILON);
		assertEquals(5.0, branch.getMaximum(CADataType.CP_X, component), EPSILON);

		assertEquals(5.0, branch.getByIndex(CADataType.CP_X, component, 0), EPSILON);
		assertEquals(3.0, branch.getByIndex(CADataType.CP_X, component, 1), EPSILON);
	}

	@Test
	void getReturnsDefensiveCopyOfComponentData() {
		branch.addPoint();
		branch.setValue(CADataType.CP_X, component, 7.5);

		List<Double> values = branch.get(CADataType.CP_X, component);
		values.set(values.size() - 1, 99.0);

		assertEquals(7.5, branch.getLast(CADataType.CP_X, component), EPSILON);
	}

	@Test
	void domainValuesAreStoredThroughSetDomainValue() {
		branch.addType(CADomainDataType.MACH);
		branch.addPoint();
		branch.setDomainValue(CADomainDataType.MACH, 0.85);

		assertEquals(0.85, branch.getLast(CADomainDataType.MACH), EPSILON);
		assertEquals(0.85, branch.getByIndex(CADomainDataType.MACH, 0), EPSILON);
	}

	@Test
	void setValueRejectsDomainTypes() {
		branch.addType(CADomainDataType.MACH);

		assertThrows(IllegalArgumentException.class,
				() -> branch.setValue(CADomainDataType.MACH, component, 1.0));
	}

	@Test
	void getByIndexValidatesBounds() {
		assertThrows(IllegalArgumentException.class,
				() -> branch.getByIndex(CADataType.CP_X, component, 0));

		branch.addPoint();
		assertThrows(IllegalArgumentException.class,
				() -> branch.getByIndex(CADataType.CP_X, component, -1));
	}

	@Test
	void missingComponentDataReturnsNaN() {
		assertTrue(Double.isNaN(branch.getMinimum(CADataType.CP_X, component)));
		assertTrue(Double.isNaN(branch.getMaximum(CADataType.CP_X, component)));
		assertTrue(Double.isNaN(branch.getLast(CADataType.CP_X, component)));
	}
}
