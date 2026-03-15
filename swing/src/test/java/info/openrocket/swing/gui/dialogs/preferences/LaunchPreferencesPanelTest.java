package info.openrocket.swing.gui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

public class LaunchPreferencesPanelTest {
    private static final double EPSILON = MathUtil.EPSILON;
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
    public void setUpTest() {
        prefs = (SwingPreferences) Application.getPreferences();
    }

    @Test
    @DisplayName("saveLaunchPreferencesToDefaults saves all launch preferences correctly")
    public void testSaveLaunchPreferencesToDefaults() {
        // Set up test preferences
        double testLatitude = 50.8791; // Leuven
        double testLongitude = 4.7025;
        double testAltitude = 10.0;
        boolean testISAAtmosphere = false;
        double testTemperature = 293.15; // 20°C
        double testPressure = 101000.0;
        boolean testLaunchIntoWind = false;
        double testRodLength = 1.5;
        double testRodAngle = 0.1;
        double testRodDirection = Math.PI / 4; // 45 degrees
        double testWindAverage = 3.0;
        double testWindTurbulence = 0.15;
        // Standard deviation is calculated from turbulence intensity: stdDev = turbulence * average
        double testWindStdDev = testWindTurbulence * testWindAverage; // 0.15 * 3.0 = 0.45

        // Set preferences
        prefs.setLaunchLatitude(testLatitude);
        prefs.setLaunchLongitude(testLongitude);
        prefs.setLaunchAltitude(testAltitude);
        prefs.setISAAtmosphere(testISAAtmosphere);
        prefs.setLaunchTemperature(testTemperature);
        prefs.setLaunchPressure(testPressure);
        prefs.setLaunchIntoWind(testLaunchIntoWind);
        prefs.setLaunchRodLength(testRodLength);
        prefs.setLaunchRodAngle(testRodAngle);
        prefs.setLaunchRodDirection(testRodDirection);
        // Set wind model values - note that standard deviation is calculated from turbulence intensity
        prefs.getAverageWindModel().setAverage(testWindAverage);
        prefs.getAverageWindModel().setTurbulenceIntensity(testWindTurbulence);
        // The standard deviation will be calculated as turbulence * average = 0.15 * 3.0 = 0.45
        testWindStdDev = prefs.getAverageWindModel().getStandardDeviation();

        // Create panel and save preferences
        LaunchPreferencesPanel panel = new LaunchPreferencesPanel();
        panel.saveLaunchPreferencesToDefaults();

        // Verify that DefaultSimulationOptionFactory has the saved values
        DefaultSimulationOptionFactory factory = Application.getInjector()
                .getInstance(DefaultSimulationOptionFactory.class);
        assertNotNull(factory, "Factory should not be null");

        SimulationOptions savedOptions = factory.getDefault();
        assertNotNull(savedOptions, "Saved options should not be null");

        assertEquals(testLatitude, savedOptions.getLaunchLatitude(), EPSILON,
                "Launch latitude should be saved correctly");
        assertEquals(testLongitude, savedOptions.getLaunchLongitude(), EPSILON,
                "Launch longitude should be saved correctly");
        assertEquals(testAltitude, savedOptions.getLaunchAltitude(), EPSILON,
                "Launch altitude should be saved correctly");
        assertEquals(testISAAtmosphere, savedOptions.isISAAtmosphere(),
                "ISA atmosphere flag should be saved correctly");
        assertEquals(testTemperature, savedOptions.getLaunchTemperature(), EPSILON,
                "Launch temperature should be saved correctly");
        assertEquals(testPressure, savedOptions.getLaunchPressure(), EPSILON,
                "Launch pressure should be saved correctly");
        assertEquals(testLaunchIntoWind, savedOptions.getLaunchIntoWind(),
                "Launch into wind flag should be saved correctly");
        assertEquals(testRodLength, savedOptions.getLaunchRodLength(), EPSILON,
                "Launch rod length should be saved correctly");
        assertEquals(testRodAngle, savedOptions.getLaunchRodAngle(), EPSILON,
                "Launch rod angle should be saved correctly");
        assertEquals(testRodDirection, savedOptions.getLaunchRodDirection(), EPSILON,
                "Launch rod direction should be saved correctly");
        assertEquals(testWindAverage, savedOptions.getAverageWindModel().getAverage(), EPSILON,
                "Wind average should be saved correctly");
        assertEquals(testWindStdDev, savedOptions.getAverageWindModel().getStandardDeviation(), EPSILON,
                "Wind standard deviation should be saved correctly");
        assertEquals(testWindTurbulence, savedOptions.getAverageWindModel().getTurbulenceIntensity(), EPSILON,
                "Wind turbulence intensity should be saved correctly");
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

