package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CADomainDataTypeTest extends ComponentAnalysisTestBase {

	private static final double EPSILON = 1e-9;

	@Test
	void domainTypeRangeCanBeAdjustedAndRestored() {
		CADomainDataType domain = CADomainDataType.MACH;

		double originalMin = domain.getMin();
		double originalMax = domain.getMax();
		double originalDelta = domain.getDelta();

		try {
			domain.setMin(0.1);
			domain.setMax(2.5);
			domain.setDelta(0.05);

			assertEquals(0.1, domain.getMin(), EPSILON);
			assertEquals(2.5, domain.getMax(), EPSILON);
			assertEquals(0.05, domain.getDelta(), EPSILON);
		} finally {
			domain.setMin(originalMin);
			domain.setMax(originalMax);
			domain.setDelta(originalDelta);
		}
	}

	@Test
	void domainTypesExposeMinimumStepSizeAndAreUnique() {
		Set<CADomainDataType> uniqueTypes = new HashSet<>();
		for (CADomainDataType type : CADomainDataType.ALL_DOMAIN_TYPES) {
			assertNotNull(type.getName());
			assertTrue(type.getMinDelta() > 0);
			uniqueTypes.add(type);
		}

		assertEquals(CADomainDataType.ALL_DOMAIN_TYPES.length, uniqueTypes.size());
	}

	@Test
	void dataTypeGroupsAreOrderedByPriority() {
		assertTrue(CADataTypeGroup.STABILITY.compareTo(CADataTypeGroup.DRAG) < 0);
		assertTrue(CADataTypeGroup.DRAG.compareTo(CADataTypeGroup.ROLL) < 0);
		assertEquals(CADataTypeGroup.DRAG.getPriority(), 20);
	}
}
