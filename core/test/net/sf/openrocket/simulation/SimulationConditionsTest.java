package net.sf.openrocket.simulation;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.MockPreferences;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.MathUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SimulationConditionsTest {
    private final static double EPSILON = MathUtil.EPSILON;

    @BeforeClass
    public static void setUp() throws Exception {
        Module applicationModule = new PreferencesModule();
        Module debugTranslator = new AbstractModule() {

            @Override
            protected void configure() {
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }

        };
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator), pluginModule);
        Application.setInjector(injector);
    }

    @Test
    public void testDefaultSimulationOptionFactory() {
        Application.getInjector().injectMembers(this);
        DefaultSimulationOptionFactory factory = Application.getInjector().getInstance(DefaultSimulationOptionFactory.class);
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
        assertEquals(Math.PI / 2, options.getWindDirection(), EPSILON);
        assertEquals(0.1, options.getWindTurbulenceIntensity(), EPSILON);
        assertEquals(2.0, options.getWindSpeedAverage(), EPSILON);
        assertEquals(0.2, options.getWindSpeedDeviation(), EPSILON);

        assertEquals(0.05, options.getTimeStep(), EPSILON);
        assertEquals(3 * Math.PI / 180, options.getMaximumStepAngle(), EPSILON);

    }

    private static class PreferencesModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Preferences.class).to(MockPreferences.class);
            bind(Translator.class).toProvider(ServicesForTesting.TranslatorProviderForTesting.class);
            bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
        }
    }
}
