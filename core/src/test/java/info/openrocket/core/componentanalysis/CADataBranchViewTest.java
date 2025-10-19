package info.openrocket.core.componentanalysis;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CADataBranchViewTest extends BaseTestCase {

	@Test
	void valuesViewReflectsComponentDataMutations() {
		CADataBranch branch = new CADataBranch("test", CADomainDataType.MACH, CADataType.TOTAL_CD);
		RocketComponent component = mock(RocketComponent.class);

		branch.addPoint();
		branch.setDomainValue(CADomainDataType.MACH, 0.1);
		branch.setValue(CADataType.TOTAL_CD, component, 0.4);

		List<Double> domainView = branch.getValuesView(CADomainDataType.MACH);
		List<Double> componentView = branch.getValuesView(CADataType.TOTAL_CD, component);

		assertEquals(0.1, domainView.get(0));
		assertEquals(0.4, componentView.get(0));

		branch.addPoint();
		branch.setDomainValue(CADomainDataType.MACH, 0.2);
		branch.setValue(CADataType.TOTAL_CD, component, 0.6);

		assertEquals(2, domainView.size(), "Domain view should grow with new samples");
		assertEquals(2, componentView.size(), "Component view should grow with new samples");
		assertEquals(0.2, domainView.get(1));
		assertEquals(0.6, componentView.get(1));
	}

	@Test
	void componentValuesViewIsUnmodifiable() {
		CADataBranch branch = new CADataBranch("test", CADomainDataType.MACH, CADataType.TOTAL_CD);
		RocketComponent component = mock(RocketComponent.class);

		branch.addPoint();
		branch.setValue(CADataType.TOTAL_CD, component, 0.5);

		List<Double> view = branch.getValuesView(CADataType.TOTAL_CD, component);

		assertThrows(UnsupportedOperationException.class, () -> view.add(0.6));
	}

	@Test
	void componentValuesViewForMissingComponentIsEmptyAndUnmodifiable() {
		CADataBranch branch = new CADataBranch("test", CADomainDataType.MACH, CADataType.TOTAL_CD);

		List<Double> emptyView = branch.getValuesView(CADataType.TOTAL_CD, mock(RocketComponent.class));

		assertTrue(emptyView.isEmpty());
		assertThrows(UnsupportedOperationException.class, () -> emptyView.add(0.1));
	}
}
