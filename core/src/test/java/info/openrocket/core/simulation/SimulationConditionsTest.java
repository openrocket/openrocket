package info.openrocket.core.simulation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.MockPreferences;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulationConditionsTest {
    private final static double EPSILON = MathUtil.EPSILON;

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

    @Test
    public void testDefaultSimulationOptionFactory() {
        Application.getInjector().injectMembers(this);
        DefaultSimulationOptionFactory factory = Application.getInjector()
                .getInstance(DefaultSimulationOptionFactory.class);
        SimulationOptions options = factory.getDefault();
        assertNotNull(options);

        assertEquals(28.61, options.getLaunchLatitude(), EPSILON);
        assertEquals(0.0, options.getLaunchAltitude(), EPSILON);
        assertEquals(-80.60, options.getLaunchLongitude(), EPSILON);
        assertTrue(options.isISAAtmosphere());
        assertEquals(288.15, options.getLaunchTemperature(), EPSILON);
        assertEquals(101325, options.getLaunchPressure(), EPSILON);
        assertEquals(1.0, options.getLaunchRodLength(), EPSILON);
        assertEquals(Math.PI / 2, options.getLaunchRodDirection(), EPSILON);
        assertEquals(0.0, options.getLaunchRodAngle(), EPSILON);
        assertTrue(options.getLaunchIntoWind());
        assertEquals(Math.PI / 2, options.getAverageWindModel().getDirection(), EPSILON);
        assertEquals(0.1, options.getAverageWindModel().getTurbulenceIntensity(), EPSILON);
        assertEquals(2.0, options.getAverageWindModel().getAverage(), EPSILON);
        assertEquals(0.2, options.getAverageWindModel().getStandardDeviation(), EPSILON);

        assertEquals(0.05, options.getTimeStep(), EPSILON);
        assertEquals(1200, options.getMaxSimulationTime(), EPSILON);
        assertEquals(3 * Math.PI / 180, options.getMaximumStepAngle(), EPSILON);
    }

    @Test
    @DisplayName("Compare PinkNoiseWindModel and MultiLevelPinkNoiseWindModel in SimulationConditions")
    public void testWindModelComparison() {
        SimulationConditions conditions = new SimulationConditions();

        // Test PinkNoiseWindModel
        PinkNoiseWindModel pinkNoiseModel = new PinkNoiseWindModel();
        pinkNoiseModel.setAverage(5.0);
        pinkNoiseModel.setStandardDeviation(1.0);
        pinkNoiseModel.setDirection(Math.PI / 4); // 45 degrees

        conditions.setWindModel(pinkNoiseModel);

        Coordinate pinkNoiseVelocity = conditions.getWindModel().getWindVelocity(0, 100);
        assertNotNull(pinkNoiseVelocity);
        assertTrue(pinkNoiseVelocity.length() > 0);

        // Test MultiLevelPinkNoiseWindModel
        MultiLevelPinkNoiseWindModel multiLevelModel = new MultiLevelPinkNoiseWindModel();
        multiLevelModel.clearLevels();
        multiLevelModel.addWindLevel(0, 5.0, Math.PI / 4, 1d);
        multiLevelModel.addWindLevel(1000, 10.0, Math.PI / 2, 2d);

        conditions.setWindModel(multiLevelModel);

        Coordinate multiLevelVelocity = conditions.getWindModel().getWindVelocity(0, 100);
        assertNotNull(multiLevelVelocity);
        assertTrue(multiLevelVelocity.length() > 0);

        // Compare behaviors
        assertNotEquals(pinkNoiseVelocity, multiLevelVelocity);
    }

    @Test
    @DisplayName("Test wind velocity consistency for MultiLevelPinkNoiseWindModel")
    public void testMultiLevelWindModelConsistency() {
        SimulationConditions conditions = new SimulationConditions();
        MultiLevelPinkNoiseWindModel multiLevelModel = new MultiLevelPinkNoiseWindModel();
        multiLevelModel.clearLevels();
        multiLevelModel.addWindLevel(0, 5.0, Math.PI / 4, 2d);
        multiLevelModel.addWindLevel(1000, 10.0, Math.PI / 2, 1d);

        conditions.setWindModel(multiLevelModel);

        Coordinate velocity1 = conditions.getWindModel().getWindVelocity(0, 500);
        Coordinate velocity2 = conditions.getWindModel().getWindVelocity(0, 500);

        assertEquals(velocity1, velocity2);
    }

    @Test
    @DisplayName("Test wind velocity variation for PinkNoiseWindModel")
    public void testPinkNoiseWindModelVariation() {
        SimulationConditions conditions = new SimulationConditions();
        PinkNoiseWindModel pinkNoiseModel = new PinkNoiseWindModel();
        pinkNoiseModel.setAverage(5.0);
        pinkNoiseModel.setStandardDeviation(1.0);
        pinkNoiseModel.setDirection(Math.PI / 4);

        conditions.setWindModel(pinkNoiseModel);

        Coordinate velocity1 = conditions.getWindModel().getWindVelocity(0, 100);
        Coordinate velocity2 = conditions.getWindModel().getWindVelocity(1, 100);

        assertNotEquals(velocity1, velocity2);
    }

    @Test
    @DisplayName("Test altitude dependence of MultiLevelPinkNoiseWindModel")
    public void testMultiLevelWindModelAltitudeDependence() {
        SimulationConditions conditions = new SimulationConditions();
        MultiLevelPinkNoiseWindModel multiLevelModel = new MultiLevelPinkNoiseWindModel();
        multiLevelModel.clearLevels();
        multiLevelModel.addWindLevel(0, 5.0, 0, 0d);
        multiLevelModel.addWindLevel(1000, 10.0, Math.PI / 2, 0d);

        conditions.setWindModel(multiLevelModel);

        Coordinate velocityLow = conditions.getWindModel().getWindVelocity(0, 0);
        Coordinate velocityHigh = conditions.getWindModel().getWindVelocity(0, 1000);
        Coordinate velocityMid = conditions.getWindModel().getWindVelocity(0, 500);

        assertNotEquals(velocityLow, velocityHigh);
        assertTrue(velocityMid.length() > velocityLow.length() && velocityMid.length() < velocityHigh.length());
    }


    private static class PreferencesModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ApplicationPreferences.class).to(MockPreferences.class);
            bind(Translator.class).toProvider(ServicesForTesting.TranslatorProviderForTesting.class);
            bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
        }
    }
}
