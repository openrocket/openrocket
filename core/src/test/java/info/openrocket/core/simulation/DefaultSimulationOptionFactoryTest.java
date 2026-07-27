package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.MockPreferences;
import info.openrocket.core.util.MathUtil;

class DefaultSimulationOptionFactoryTest {
	private static final double EPSILON = MathUtil.EPSILON;

	private MockPreferences preferences;
	private DefaultSimulationOptionFactory factory;

	@BeforeEach
	void setUp() {
		Module applicationModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ApplicationPreferences.class).to(MockPreferences.class);
				bind(Translator.class).toProvider(ServicesForTesting.TranslatorProviderForTesting.class);
				bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
			}
		};
		Application.setInjector(Guice.createInjector(
				Modules.override(applicationModule).with(new PluginModule())));
		preferences = (MockPreferences) Application.getPreferences();
		factory = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
	}

	@Test
	void defaultsRoundTripThroughLaunchPreferences() {
		// Verify that defaults are loaded from the launch preferences, not the
		// obsolete SimCondition preference keys.
		preferences.setISAAtmosphere(false);
		preferences.setLaunchLatitude(50.8791);
		preferences.setLaunchLongitude(4.7025);
		preferences.setLaunchAltitude(25.0);
		preferences.setLaunchTemperature(293.15);
		preferences.setLaunchPressure(100500.0);
		preferences.setLaunchRelativeHumidity(0.65);
		preferences.setLaunchIntoWind(false);
		preferences.setLaunchRodLength(1.8);
		preferences.setLaunchRodAngle(0.2);
		preferences.setLaunchRodDirection(0.7);
		preferences.getAverageWindModel().setAverage(4.2);
		preferences.getAverageWindModel().setStandardDeviation(0.8);
		preferences.getAverageWindModel().setDirection(1.2);
		preferences.putDouble(DefaultSimulationOptionFactory.SIMCONDITION_SITE_ALT, 999.0);

		SimulationOptions loaded = factory.getDefault();

		assertConditions(loaded, 50.8791, 4.7025, 25.0, 293.15, 100500.0,
				0.65, 1.8, 0.2, 0.7, 4.2, 0.8, 1.2);

		SimulationOptions saved = new SimulationOptions();
		saved.setISAAtmosphere(false);
		saved.setLaunchLatitude(-33.8688);
		saved.setLaunchLongitude(151.2093);
		saved.setLaunchAltitude(80.0);
		saved.setLaunchTemperature(301.15);
		saved.setLaunchPressure(99000.0);
		saved.setLaunchRelativeHumidity(0.35);
		saved.setLaunchIntoWind(false);
		saved.setLaunchRodLength(2.4);
		saved.setLaunchRodAngle(0.7);
		saved.setLaunchRodDirection(2.1);
		saved.getAverageWindModel().setAverage(7.5);
		saved.getAverageWindModel().setStandardDeviation(1.1);
		saved.getAverageWindModel().setDirection(2.8);

		factory.saveDefault(saved);

		assertEquals(-33.8688, preferences.getLaunchLatitude(), EPSILON);
		assertEquals(151.2093, preferences.getLaunchLongitude(), EPSILON);
		assertEquals(80.0, preferences.getLaunchAltitude(), EPSILON);
		assertEquals(301.15, preferences.getLaunchTemperature(), EPSILON);
		assertEquals(99000.0, preferences.getLaunchPressure(), EPSILON);
		assertEquals(0.35, preferences.getLaunchRelativeHumidity(), EPSILON);
		assertFalse(preferences.getLaunchIntoWind());
		assertEquals(2.4, preferences.getLaunchRodLength(), EPSILON);
		assertEquals(0.7, preferences.getLaunchRodAngle(), EPSILON);
		assertEquals(2.1, preferences.getLaunchRodDirection(), EPSILON);
		assertEquals(7.5, preferences.getAverageWindModel().getAverage(), EPSILON);
		assertEquals(1.1, preferences.getAverageWindModel().getStandardDeviation(), EPSILON);
		assertEquals(2.8, preferences.getAverageWindModel().getDirection(), EPSILON);

		assertConditions(factory.getDefault(), -33.8688, 151.2093, 80.0, 301.15,
				99000.0, 0.35, 2.4, 0.7, 2.1, 7.5, 1.1, 2.8);
	}

	@Test
	void copyingConditionsNotifiesNestedWindModels() {
		SimulationOptions source = new SimulationOptions();
		SimulationOptions target = new SimulationOptions();
		source.getAverageWindModel().setAverage(target.getAverageWindModel().getAverage() + 1.0);
		source.getMultiLevelWindModel().addWindLevel(100.0, 5.0, 0.5);
		AtomicInteger averageWindEvents = new AtomicInteger();
		AtomicInteger multiLevelWindEvents = new AtomicInteger();
		target.getAverageWindModel().addChangeListener(event -> averageWindEvents.incrementAndGet());
		target.getMultiLevelWindModel().addChangeListener(event -> multiLevelWindEvents.incrementAndGet());

		target.copyConditionsFrom(source);

		assertEquals(1, averageWindEvents.get());
		assertEquals(1, multiLevelWindEvents.get());
	}

	/**
	 * Asserts all launch-condition values managed by the default factory.
	 */
	private static void assertConditions(SimulationOptions options, double latitude, double longitude,
			double altitude, double temperature, double pressure, double relativeHumidity,
			double rodLength, double rodAngle, double rodDirection, double windAverage,
			double windStandardDeviation, double windDirection) {
		assertEquals(latitude, options.getLaunchLatitude(), EPSILON);
		assertEquals(longitude, options.getLaunchLongitude(), EPSILON);
		assertEquals(altitude, options.getLaunchAltitude(), EPSILON);
		assertFalse(options.isISAAtmosphere());
		assertEquals(temperature, options.getLaunchTemperature(), EPSILON);
		assertEquals(pressure, options.getLaunchPressure(), EPSILON);
		assertEquals(relativeHumidity, options.getLaunchRelativeHumidity(), EPSILON);
		assertFalse(options.getLaunchIntoWind());
		assertEquals(rodLength, options.getLaunchRodLength(), EPSILON);
		assertEquals(rodAngle, options.getLaunchRodAngle(), EPSILON);
		assertEquals(rodDirection, options.getLaunchRodDirection(), EPSILON);
		assertEquals(windAverage, options.getAverageWindModel().getAverage(), EPSILON);
		assertEquals(windStandardDeviation, options.getAverageWindModel().getStandardDeviation(), EPSILON);
		assertEquals(windDirection, options.getAverageWindModel().getDirection(), EPSILON);
	}
}
