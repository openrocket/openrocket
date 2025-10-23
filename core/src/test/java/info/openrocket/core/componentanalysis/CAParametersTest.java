package info.openrocket.core.componentanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.atomic.AtomicReference;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CAParametersTest extends ComponentAnalysisTestBase {

	private static final double EPSILON = 1e-9;

	private Rocket rocket;

	@BeforeEach
	void setUp() {
		rocket = createRocket();
	}

	@Test
	void constructorInitializesValuesAndConfiguration() {
		double initialTheta = Math.toRadians(30);
		CAParameters parameters = new CAParameters(rocket, initialTheta);

		assertEquals(initialTheta, parameters.getTheta(), EPSILON);
		assertEquals(initialTheta, parameters.getInitialTheta(), EPSILON);
		assertEquals(0.0, parameters.getAOA(), EPSILON);
		assertEquals(0.0, parameters.getRollRate(), EPSILON);

		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		assertSame(configuration, parameters.getSelectedConfiguration());
		assertTrue(parameters.isMutable());
	}

	@Test
	void listenersReceiveUpdatesForAllParameters() {
		CAParameters parameters = new CAParameters(rocket, 0.0);

		AtomicReference<Double> thetaRef = new AtomicReference<>();
		AtomicReference<Double> aoaRef = new AtomicReference<>();
		AtomicReference<Double> machRef = new AtomicReference<>();
		AtomicReference<Double> rollRateRef = new AtomicReference<>();

		CAParameters.CAParametersListener listener = new CAParameters.CAParametersListener() {
			@Override
			public void onThetaChanged(double theta) {
				thetaRef.set(theta);
			}

			@Override
			public void onAOAChanged(double aoa) {
				aoaRef.set(aoa);
			}

			@Override
			public void onMachChanged(double mach) {
				machRef.set(mach);
			}

			@Override
			public void onRollRateChanged(double rollRate) {
				rollRateRef.set(rollRate);
			}
		};

		parameters.addListener(listener);

		parameters.setTheta(0.2);
		parameters.setAOA(0.3);
		parameters.setMach(0.8);
		parameters.setRollRate(1.5);

		assertEquals(0.2, thetaRef.get(), EPSILON);
		assertEquals(0.3, aoaRef.get(), EPSILON);
		assertEquals(0.8, machRef.get(), EPSILON);
		assertEquals(1.5, rollRateRef.get(), EPSILON);
	}

	@Test
	void immutePreventsFurtherUpdates() {
		CAParameters parameters = new CAParameters(rocket, 0.0);
		parameters.immute();

		assertFalse(parameters.isMutable());
		assertThrows(IllegalStateException.class, () -> parameters.setTheta(0.1));
		assertThrows(IllegalStateException.class, () -> parameters.setAOA(0.2));
		assertThrows(IllegalStateException.class, () -> parameters.setMach(0.3));
		assertThrows(IllegalStateException.class, () -> parameters.setRollRate(0.4));
	}

	@Test
	void cloneProducesIndependentStateForValues() {
		CAParameters parameters = new CAParameters(rocket, 0.25);
		parameters.setAOA(0.35);
		parameters.setMach(0.65);
		parameters.setRollRate(0.12);
		parameters.setTheta(0.25);

		CAParameters clone = parameters.clone();
		clone.setTheta(0.4);
		clone.setAOA(0.45);
		clone.setMach(0.9);
		clone.setRollRate(0.22);

		assertEquals(0.25, parameters.getTheta(), EPSILON);
		assertEquals(0.35, parameters.getAOA(), EPSILON);
		assertEquals(0.65, parameters.getMach(), EPSILON);
		assertEquals(0.12, parameters.getRollRate(), EPSILON);

		assertEquals(0.4, clone.getTheta(), EPSILON);
		assertEquals(0.45, clone.getAOA(), EPSILON);
		assertEquals(0.9, clone.getMach(), EPSILON);
		assertEquals(0.22, clone.getRollRate(), EPSILON);
	}
}
