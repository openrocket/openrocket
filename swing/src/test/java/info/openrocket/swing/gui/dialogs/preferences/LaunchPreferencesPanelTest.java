package info.openrocket.swing.gui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.prefs.BackingStoreException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.simulation.DefaultSimulationOptionFactory;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.util.SwingPreferences;

/**
 * Verifies that the launch preferences edited on the Launch preferences panel are the
 * conditions that new simulations start from. The panel binds its controls directly to
 * the preferences, so editing the panel is simulated by writing the preferences.
 */
public class LaunchPreferencesPanelTest {
    private static final double EPSILON = MathUtil.EPSILON;

    private static final double LATITUDE = 50.8791;             // Leuven
    private static final double LONGITUDE = 4.7025;
    private static final double ALTITUDE = 10.0;                // m
    private static final boolean ISA_ATMOSPHERE = false;
    private static final double TEMPERATURE = 293.15;           // K
    private static final double PRESSURE = 101000.0;            // Pa
    private static final boolean LAUNCH_INTO_WIND = false;
    private static final double ROD_LENGTH = 1.5;               // m
    private static final double ROD_ANGLE = 0.1;                // rad
    private static final double ROD_DIRECTION = Math.PI / 4;    // rad
    private static final double WIND_AVERAGE = 3.0;             // m/s
    private static final double WIND_TURBULENCE = 0.15;

    private SwingPreferences prefs;

    @BeforeAll
    public static void setUp() throws Exception {
        Module applicationModule = new PreferencesModule();
        Module debugTranslator = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }
        };
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator),
                pluginModule);
        Application.setInjector(injector);
    }

    @BeforeEach
    public void setUpTest() throws BackingStoreException {
        prefs = (SwingPreferences) Application.getPreferences();
        // The testing preferences are backed by a single node shared by the tests in this JVM,
        // so clear it to keep the launch conditions of one test out of the next one.
        prefs.getPreferences().clear();
    }

    @Test
    @DisplayName("The panel can be built on top of the launch preferences")
    public void testPanelCreation() {
        assertNotNull(new LaunchPreferencesPanel(), "Panel should be created");
    }

    @Nested
    @DisplayName("New simulations start from the launch preferences")
    class DefaultsFromLaunchPreferences {
        private SimulationOptions defaults;
        // The standard deviation is derived from the turbulence intensity and the average
        private double windStandardDeviation;

        @BeforeEach
        public void editLaunchPreferences() {
            prefs.setLaunchLatitude(LATITUDE);
            prefs.setLaunchLongitude(LONGITUDE);
            prefs.setISAAtmosphere(ISA_ATMOSPHERE);
            prefs.setLaunchAltitude(ALTITUDE);
            prefs.setLaunchTemperature(TEMPERATURE);
            prefs.setLaunchPressure(PRESSURE);
            prefs.setLaunchIntoWind(LAUNCH_INTO_WIND);
            prefs.setLaunchRodLength(ROD_LENGTH);
            prefs.setLaunchRodAngle(ROD_ANGLE);
            prefs.setLaunchRodDirection(ROD_DIRECTION);
            prefs.getAverageWindModel().setAverage(WIND_AVERAGE);
            prefs.getAverageWindModel().setTurbulenceIntensity(WIND_TURBULENCE);
            windStandardDeviation = prefs.getAverageWindModel().getStandardDeviation();

            DefaultSimulationOptionFactory factory = Application.getInjector()
                    .getInstance(DefaultSimulationOptionFactory.class);
            defaults = factory.getDefault();
            assertNotNull(defaults, "Default options should not be null");
        }

        @Test
        public void usesLatitude() {
            assertEquals(LATITUDE, defaults.getLaunchLatitude(), EPSILON);
        }

        @Test
        public void usesLongitude() {
            assertEquals(LONGITUDE, defaults.getLaunchLongitude(), EPSILON);
        }

        @Test
        public void usesAltitude() {
            assertEquals(ALTITUDE, defaults.getLaunchAltitude(), EPSILON);
        }

        @Test
        public void usesISAAtmosphereFlag() {
            assertEquals(ISA_ATMOSPHERE, defaults.isISAAtmosphere());
        }

        @Test
        public void usesTemperature() {
            assertEquals(TEMPERATURE, defaults.getLaunchTemperature(), EPSILON);
        }

        @Test
        public void usesPressure() {
            assertEquals(PRESSURE, defaults.getLaunchPressure(), EPSILON);
        }

        @Test
        public void usesLaunchIntoWind() {
            assertEquals(LAUNCH_INTO_WIND, defaults.getLaunchIntoWind());
        }

        @Test
        public void usesRodLength() {
            assertEquals(ROD_LENGTH, defaults.getLaunchRodLength(), EPSILON);
        }

        @Test
        public void usesRodAngle() {
            assertEquals(ROD_ANGLE, defaults.getLaunchRodAngle(), EPSILON);
        }

        @Test
        public void usesRodDirection() {
            assertEquals(ROD_DIRECTION, defaults.getLaunchRodDirection(), EPSILON);
        }

        @Test
        public void usesWindAverage() {
            assertEquals(WIND_AVERAGE, defaults.getAverageWindModel().getAverage(), EPSILON);
        }

        @Test
        public void usesWindStandardDeviation() {
            assertEquals(windStandardDeviation, defaults.getAverageWindModel().getStandardDeviation(), EPSILON);
        }

        @Test
        public void usesWindTurbulenceIntensity() {
            assertEquals(WIND_TURBULENCE, defaults.getAverageWindModel().getTurbulenceIntensity(), EPSILON);
        }
    }

    private static class PreferencesModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ApplicationPreferences.class).to(ServicesForTesting.PreferencesForTesting.class);
            bind(Translator.class).toProvider(ServicesForTesting.TranslatorProviderForTesting.class);
            bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
        }
    }
}
