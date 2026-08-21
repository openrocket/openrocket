package info.openrocket.swing.gui.dialogs.scaledialog;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.dialogs.ScaleDialog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;

/**
 * Base class for all component scaling unit tests.
 * This sets up the Guice environment and uses reflection to access the private
 * scaling methods in ScaleDialog, decoupling tests from the Swing UI dependencies.
 */
public abstract class ScaleDialogBaseTest {

    protected ScaleDialog dialogInstance;
    protected Method scaleMethod;
    protected Method scaleOffsetMethod;

    /**
     * Sets up the Guice Injector statically using @BeforeAll.
     * This ensures the application services (like preferences) are initialized
     * before any component constructors are called, solving initialization order errors.
     */
    @BeforeAll
    public static void globalSetup() throws Exception {
        // --- 1. Guice/Application Setup ---
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();

        Module debugTranslator = new AbstractModule() {
            @Override
            protected void configure() {
                // Use a non-null translator instance for required services
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }
        };

        Injector injector = Guice.createInjector(
                Modules.override(applicationModule).with(debugTranslator),
                pluginModule);

        // Set the injector globally before component constructors execute
        Application.setInjector(injector);
    }

    /**
     * Sets up the test instance and reflection handles for each individual test.
     */
    @BeforeEach
    public void instanceSetup() throws Exception {
        // Mock the dialog instance. The scale methods are static-map driven.
        dialogInstance = mock(ScaleDialog.class);

        // --- UNLOCK PRIVATE METHODS VIA REFLECTION ---

        // Reflection for scale(RocketComponent c, double multiplier, boolean scaleMass)
        scaleMethod = ScaleDialog.class.getDeclaredMethod("scale",
                RocketComponent.class, double.class, boolean.class);
        scaleMethod.setAccessible(true);

        // Reflection for scaleOffset(RocketComponent c, double multiplier, boolean scaleMass)
        scaleOffsetMethod = ScaleDialog.class.getDeclaredMethod("scaleOffset",
                RocketComponent.class, double.class, boolean.class);
        scaleOffsetMethod.setAccessible(true);
    }
}