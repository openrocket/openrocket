package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

	/** Launch conditions stored in the preferences, i.e. the input of {@link DefaultSimulationOptionFactory#getDefault()}. */
	private static final boolean PREFERRED_ISA_ATMOSPHERE = false;
	private static final double PREFERRED_LATITUDE = 50.8791;			// Leuven
	private static final double PREFERRED_LONGITUDE = 4.7025;
	private static final double PREFERRED_ALTITUDE = 25.0;				// m
	private static final double PREFERRED_TEMPERATURE = 293.15;			// K
	private static final double PREFERRED_PRESSURE = 100500.0;			// Pa
	private static final double PREFERRED_RELATIVE_HUMIDITY = 0.65;
	private static final boolean PREFERRED_INTO_WIND = false;
	private static final double PREFERRED_ROD_LENGTH = 1.8;				// m
	private static final double PREFERRED_ROD_ANGLE = 0.2;				// rad
	private static final double PREFERRED_ROD_DIRECTION = 0.7;			// rad
	private static final double PREFERRED_WIND_AVERAGE = 4.2;			// m/s
	private static final double PREFERRED_WIND_STANDARD_DEVIATION = 0.8;	// m/s
	private static final double PREFERRED_WIND_DIRECTION = 1.2;			// rad

	/** Launch conditions of the simulation stored by {@link DefaultSimulationOptionFactory#saveDefault(SimulationOptions)}. */
	private static final boolean SAVED_ISA_ATMOSPHERE = false;
	private static final double SAVED_LATITUDE = -33.8688;				// Sydney
	private static final double SAVED_LONGITUDE = 151.2093;
	private static final double SAVED_ALTITUDE = 80.0;					// m
	private static final double SAVED_TEMPERATURE = 301.15;				// K
	private static final double SAVED_PRESSURE = 99000.0;				// Pa
	private static final double SAVED_RELATIVE_HUMIDITY = 0.35;
	private static final boolean SAVED_INTO_WIND = false;
	private static final double SAVED_ROD_LENGTH = 2.4;					// m
	private static final double SAVED_ROD_ANGLE = 0.7;					// rad, beyond the old launch preference limit of PI/6
	private static final double SAVED_ROD_DIRECTION = 2.1;				// rad
	private static final double SAVED_WIND_AVERAGE = 7.5;				// m/s
	private static final double SAVED_WIND_STANDARD_DEVIATION = 1.1;	// m/s
	private static final double SAVED_WIND_DIRECTION = 2.8;				// rad

	/** Obsolete preference key that the factory used to store the launch altitude under. */
	private static final String LEGACY_SITE_ALTITUDE_KEY = "SimConditionSiteAlt";

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

	/**
	 * Stores the PREFERRED_* launch conditions in the launch preferences.
	 */
	private void storeLaunchPreferences() {
		preferences.setISAAtmosphere(PREFERRED_ISA_ATMOSPHERE);
		preferences.setLaunchLatitude(PREFERRED_LATITUDE);
		preferences.setLaunchLongitude(PREFERRED_LONGITUDE);
		preferences.setLaunchAltitude(PREFERRED_ALTITUDE);
		preferences.setLaunchTemperature(PREFERRED_TEMPERATURE);
		preferences.setLaunchPressure(PREFERRED_PRESSURE);
		preferences.setLaunchRelativeHumidity(PREFERRED_RELATIVE_HUMIDITY);
		preferences.setLaunchIntoWind(PREFERRED_INTO_WIND);
		preferences.setLaunchRodLength(PREFERRED_ROD_LENGTH);
		preferences.setLaunchRodAngle(PREFERRED_ROD_ANGLE);
		preferences.setLaunchRodDirection(PREFERRED_ROD_DIRECTION);
		preferences.getAverageWindModel().setAverage(PREFERRED_WIND_AVERAGE);
		preferences.getAverageWindModel().setStandardDeviation(PREFERRED_WIND_STANDARD_DEVIATION);
		preferences.getAverageWindModel().setDirection(PREFERRED_WIND_DIRECTION);
	}

	/**
	 * @return simulation conditions holding the SAVED_* launch conditions
	 */
	private static SimulationOptions savedSimulationOptions() {
		SimulationOptions options = new SimulationOptions();
		options.setISAAtmosphere(SAVED_ISA_ATMOSPHERE);
		options.setLaunchLatitude(SAVED_LATITUDE);
		options.setLaunchLongitude(SAVED_LONGITUDE);
		options.setLaunchAltitude(SAVED_ALTITUDE);
		options.setLaunchTemperature(SAVED_TEMPERATURE);
		options.setLaunchPressure(SAVED_PRESSURE);
		options.setLaunchRelativeHumidity(SAVED_RELATIVE_HUMIDITY);
		options.setLaunchIntoWind(SAVED_INTO_WIND);
		options.setLaunchRodLength(SAVED_ROD_LENGTH);
		options.setLaunchRodAngle(SAVED_ROD_ANGLE);
		options.setLaunchRodDirection(SAVED_ROD_DIRECTION);
		options.getAverageWindModel().setAverage(SAVED_WIND_AVERAGE);
		options.getAverageWindModel().setStandardDeviation(SAVED_WIND_STANDARD_DEVIATION);
		options.getAverageWindModel().setDirection(SAVED_WIND_DIRECTION);
		return options;
	}

	@Nested
	@DisplayName("getDefault() loads the launch preferences")
	class GetDefault {
		private SimulationOptions defaults;

		@BeforeEach
		void loadDefaults() {
			storeLaunchPreferences();
			defaults = factory.getDefault();
		}

		@Test
		void loadsISAAtmosphere() {
			assertEquals(PREFERRED_ISA_ATMOSPHERE, defaults.isISAAtmosphere());
		}

		@Test
		void loadsLatitude() {
			assertEquals(PREFERRED_LATITUDE, defaults.getLaunchLatitude(), EPSILON);
		}

		@Test
		void loadsLongitude() {
			assertEquals(PREFERRED_LONGITUDE, defaults.getLaunchLongitude(), EPSILON);
		}

		@Test
		void loadsAltitude() {
			assertEquals(PREFERRED_ALTITUDE, defaults.getLaunchAltitude(), EPSILON);
		}

		@Test
		void loadsTemperature() {
			assertEquals(PREFERRED_TEMPERATURE, defaults.getLaunchTemperature(), EPSILON);
		}

		@Test
		void loadsPressure() {
			assertEquals(PREFERRED_PRESSURE, defaults.getLaunchPressure(), EPSILON);
		}

		@Test
		void loadsRelativeHumidity() {
			assertEquals(PREFERRED_RELATIVE_HUMIDITY, defaults.getLaunchRelativeHumidity(), EPSILON);
		}

		@Test
		void loadsLaunchIntoWind() {
			assertEquals(PREFERRED_INTO_WIND, defaults.getLaunchIntoWind());
		}

		@Test
		void loadsRodLength() {
			assertEquals(PREFERRED_ROD_LENGTH, defaults.getLaunchRodLength(), EPSILON);
		}

		@Test
		void loadsRodAngle() {
			assertEquals(PREFERRED_ROD_ANGLE, defaults.getLaunchRodAngle(), EPSILON);
		}

		@Test
		void loadsRodDirection() {
			assertEquals(PREFERRED_ROD_DIRECTION, defaults.getLaunchRodDirection(), EPSILON);
		}

		@Test
		void loadsWindAverage() {
			assertEquals(PREFERRED_WIND_AVERAGE, defaults.getAverageWindModel().getAverage(), EPSILON);
		}

		@Test
		void loadsWindStandardDeviation() {
			assertEquals(PREFERRED_WIND_STANDARD_DEVIATION, defaults.getAverageWindModel().getStandardDeviation(),
					EPSILON);
		}

		@Test
		void loadsWindDirection() {
			assertEquals(PREFERRED_WIND_DIRECTION, defaults.getAverageWindModel().getDirection(), EPSILON);
		}

		@Test
		@DisplayName("ignores the obsolete SimCondition preference keys")
		void ignoresObsoletePreferenceKeys() {
			preferences.putDouble(LEGACY_SITE_ALTITUDE_KEY, PREFERRED_ALTITUDE + 999.0);
			assertEquals(PREFERRED_ALTITUDE, factory.getDefault().getLaunchAltitude(), EPSILON);
		}
	}

	@Nested
	@DisplayName("saveDefault() stores the launch preferences")
	class SaveDefault {

		@BeforeEach
		void saveDefaults() {
			factory.saveDefault(savedSimulationOptions());
		}

		@Test
		void savesISAAtmosphere() {
			assertEquals(SAVED_ISA_ATMOSPHERE, preferences.isISAAtmosphere());
		}

		@Test
		void savesLatitude() {
			assertEquals(SAVED_LATITUDE, preferences.getLaunchLatitude(), EPSILON);
		}

		@Test
		void savesLongitude() {
			assertEquals(SAVED_LONGITUDE, preferences.getLaunchLongitude(), EPSILON);
		}

		@Test
		void savesAltitude() {
			assertEquals(SAVED_ALTITUDE, preferences.getLaunchAltitude(), EPSILON);
		}

		@Test
		void savesTemperature() {
			assertEquals(SAVED_TEMPERATURE, preferences.getLaunchTemperature(), EPSILON);
		}

		@Test
		void savesPressure() {
			assertEquals(SAVED_PRESSURE, preferences.getLaunchPressure(), EPSILON);
		}

		@Test
		void savesRelativeHumidity() {
			assertEquals(SAVED_RELATIVE_HUMIDITY, preferences.getLaunchRelativeHumidity(), EPSILON);
		}

		@Test
		void savesLaunchIntoWind() {
			assertEquals(SAVED_INTO_WIND, preferences.getLaunchIntoWind());
		}

		@Test
		void savesRodLength() {
			assertEquals(SAVED_ROD_LENGTH, preferences.getLaunchRodLength(), EPSILON);
		}

		@Test
		void savesRodAngle() {
			assertEquals(SAVED_ROD_ANGLE, preferences.getLaunchRodAngle(), EPSILON);
		}

		@Test
		void savesRodDirection() {
			assertEquals(SAVED_ROD_DIRECTION, preferences.getLaunchRodDirection(), EPSILON);
		}

		@Test
		void savesWindAverage() {
			assertEquals(SAVED_WIND_AVERAGE, preferences.getAverageWindModel().getAverage(), EPSILON);
		}

		@Test
		void savesWindStandardDeviation() {
			assertEquals(SAVED_WIND_STANDARD_DEVIATION, preferences.getAverageWindModel().getStandardDeviation(),
					EPSILON);
		}

		@Test
		void savesWindDirection() {
			assertEquals(SAVED_WIND_DIRECTION, preferences.getAverageWindModel().getDirection(), EPSILON);
		}

		@Test
		@DisplayName("stored conditions are returned by getDefault()")
		void savedConditionsAreLoadedAgain() {
			SimulationOptions reloaded = factory.getDefault();
			assertEquals(SAVED_LATITUDE, reloaded.getLaunchLatitude(), EPSILON);
			assertEquals(SAVED_ROD_ANGLE, reloaded.getLaunchRodAngle(), EPSILON);
			assertEquals(SAVED_WIND_AVERAGE, reloaded.getAverageWindModel().getAverage(), EPSILON);
			assertEquals(SAVED_WIND_STANDARD_DEVIATION, reloaded.getAverageWindModel().getStandardDeviation(), EPSILON);
			assertEquals(SAVED_WIND_DIRECTION, reloaded.getAverageWindModel().getDirection(), EPSILON);
		}
	}

	@Nested
	@DisplayName("A fresh profile falls back to the same defaults on both sides")
	class UnsetPreferences {

		@Test
		void launchIntoWindMatchesTheSimulationDefault() {
			assertEquals(new SimulationOptions().getLaunchIntoWind(), preferences.getLaunchIntoWind());
			assertTrue(factory.getDefault().getLaunchIntoWind());
		}

		@Test
		void rodAngleLimitMatchesTheSimulationLimit() {
			preferences.setLaunchRodAngle(SimulationOptions.MAX_LAUNCH_ROD_ANGLE);
			assertEquals(SimulationOptions.MAX_LAUNCH_ROD_ANGLE, preferences.getLaunchRodAngle(), EPSILON);
		}
	}

	@Test
	@DisplayName("copyConditionsFrom() notifies the listeners of the nested wind models")
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

	@Test
	@DisplayName("Saving does not enable the ISA atmosphere of a simulation that has it disabled")
	void savingKeepsCustomAtmosphere() {
		factory.saveDefault(savedSimulationOptions());
		assertFalse(preferences.isISAAtmosphere());
		assertEquals(SAVED_TEMPERATURE, preferences.getLaunchTemperature(), EPSILON);
	}
}
