package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CADataTypeTest extends ComponentAnalysisTestBase {

	@Mock
	private FlightConfiguration configuration;

	@Mock
	private RocketComponent aerodynamicComponent;

	@Mock
	private RocketComponent nonAerodynamicComponent;

	private Rocket rocket;
	private FinSet finSet;

	@BeforeEach
	void setUp() {
		rocket = createRocket();
		finSet = new TrapezoidFinSet();

		when(aerodynamicComponent.isAerodynamic()).thenReturn(true);
		when(nonAerodynamicComponent.isAerodynamic()).thenReturn(false);

	}

	@Test
	void aerodynamicComponentsAreRelevantForStabilityData() {
		assertTrue(CADataType.isComponentRelevantForType(aerodynamicComponent, CADataType.CP_X));
		assertFalse(CADataType.isComponentRelevantForType(nonAerodynamicComponent, CADataType.CP_X));
	}

	@Test
	void rocketIsExcludedFromPerInstanceDrag() {
		assertFalse(CADataType.isComponentRelevantForType(rocket, CADataType.PER_INSTANCE_CD));
	}

	@Test
	void rollCoefficientsAreLimitedToFinSets() {
		assertTrue(CADataType.isComponentRelevantForType(finSet, CADataType.ROLL_FORCING_COEFFICIENT));
		assertFalse(CADataType.isComponentRelevantForType(aerodynamicComponent, CADataType.ROLL_FORCING_COEFFICIENT));
	}

	@Test
	void calculateComponentsFiltersByRelevanceForRequestedType() {
		when(configuration.getAllActiveComponents()).thenReturn(List.of(
				rocket,
				finSet,
				aerodynamicComponent,
				nonAerodynamicComponent
		));

		List<RocketComponent> components = CADataType.calculateComponentsForType(configuration, CADataType.CP_X);

		assertTrue(components.contains(rocket));
		assertTrue(components.contains(finSet));
		assertTrue(components.contains(aerodynamicComponent));
		assertFalse(components.contains(nonAerodynamicComponent));

		verify(configuration, times(1)).getAllActiveComponents();
	}

	@Test
	void calculateComponentsForRollTypesOnlyReturnsFinSets() {
		when(configuration.getAllActiveComponents()).thenReturn(List.of(
				rocket,
				finSet,
				aerodynamicComponent,
				nonAerodynamicComponent
		));

		List<RocketComponent> components = CADataType.calculateComponentsForType(configuration, CADataType.ROLL_DAMPING_COEFFICIENT);

		assertEquals(1, components.size());
		assertEquals(finSet, components.get(0));
	}
}
