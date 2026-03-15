package info.openrocket.core.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.MockPreferences;
import info.openrocket.core.util.MathUtil;

public class ApplicationPreferencesTest {
    private static final double EPSILON = MathUtil.EPSILON;
    private MockPreferences prefs;

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
        prefs = new MockPreferences();
    }

    @Test
    @DisplayName("getLaunchRodDirection returns LAUNCH_ROD_DIRECTION when LAUNCH_INTO_WIND is false")
    public void testGetLaunchRodDirectionWhenNotIntoWind() {
        // Set launch into wind to false
        prefs.setLaunchIntoWind(false);
        
        // Set a specific launch rod direction
        double expectedDirection = Math.PI / 4; // 45 degrees
        prefs.setLaunchRodDirection(expectedDirection);
        
        // Set a different wind direction to ensure it's not used
        prefs.getAverageWindModel().setDirection(Math.PI); // 180 degrees
        
        // Verify getLaunchRodDirection returns the launch rod direction, not wind direction
        double actualDirection = prefs.getLaunchRodDirection();
        assertEquals(expectedDirection, actualDirection, EPSILON,
                "getLaunchRodDirection should return LAUNCH_ROD_DIRECTION when LAUNCH_INTO_WIND is false");
    }

    @Test
    @DisplayName("getLaunchRodDirection returns WIND_DIRECTION and syncs when LAUNCH_INTO_WIND is true")
    public void testGetLaunchRodDirectionWhenIntoWind() {
        // Set launch into wind to true
        prefs.setLaunchIntoWind(true);
        
        // Set a specific wind direction
        double expectedWindDirection = Math.PI / 3; // 60 degrees
        prefs.getAverageWindModel().setDirection(expectedWindDirection);
        
        // Set a different launch rod direction initially
        prefs.setLaunchRodDirection(Math.PI / 4); // 45 degrees
        
        // Verify getLaunchRodDirection returns the wind direction and syncs
        double actualDirection = prefs.getLaunchRodDirection();
        assertEquals(expectedWindDirection, actualDirection, EPSILON,
                "getLaunchRodDirection should return WIND_DIRECTION when LAUNCH_INTO_WIND is true");
        
        // Verify that the launch rod direction was synced
        double syncedDirection = prefs.getDouble(ApplicationPreferences.LAUNCH_ROD_DIRECTION, 0);
        assertEquals(expectedWindDirection, syncedDirection, EPSILON,
                "LAUNCH_ROD_DIRECTION should be synced with WIND_DIRECTION when LAUNCH_INTO_WIND is true");
    }

    @Test
    @DisplayName("getLaunchRodDirection uses default value when preferences are not set")
    public void testGetLaunchRodDirectionDefaultValue() {
        // Don't set any preferences
        // When LAUNCH_INTO_WIND is false, should return default LAUNCH_ROD_DIRECTION
        prefs.setLaunchIntoWind(false);
        double defaultDirection = prefs.getLaunchRodDirection();
        assertEquals(Math.PI / 2, defaultDirection, EPSILON,
                "getLaunchRodDirection should return default Math.PI/2 when not set");
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

