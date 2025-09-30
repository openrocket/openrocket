package info.openrocket.core.file.wavefrontobj.export.components;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.file.wavefrontobj.Axis;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitionExporterTest {

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void exportsDiskWhenLengthZero() {
        Transition transition = createTransition(0.06, 0.04, 0.01, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(transition);

        newExporter(obj, config, transition, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-length transition should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-length transition should emit faces");
        assertEquals(0, warnings.size(), "No zero-thickness warning expected");
    }

    @Test
    void skipsGeometryWhenLengthAndThicknessZero() {
        Transition transition = createTransition(0.05, 0.05, 0.0, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(transition);

        newExporter(obj, config, transition, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate transition should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Degenerate transition should emit no faces");
        assertEquals(1, warnings.size(), "Zero-thickness transition should emit warning");
    }

    @Test
    void exportsZeroThicknessTransition() {
        Transition transition = createTransition(0.06, 0.03, 0.0, 0.05);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(transition);

        newExporter(obj, config, transition, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-thickness transition should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-thickness transition should emit faces");
        assertEquals(1, warnings.size(), "Zero-thickness transition should emit warning");
    }

    private static TransitionExporter newExporter(DefaultObj obj, FlightConfiguration config, Transition transition, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new TransitionExporter(obj, config, transformer, transition, "transitionGroup",
                ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(Transition transition) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(transition, 0, Transformation.IDENTITY);
        return config;
    }

    private static Transition createTransition(double foreRadius, double aftRadius, double thickness, double length) {
        Transition transition = new Transition();
        transition.setForeRadius(foreRadius);
        transition.setAftRadius(aftRadius);
        transition.setThickness(thickness);
        transition.setLength(length);
        transition.setFilled(false);
        transition.setForeShoulderLength(0);
        transition.setAftShoulderLength(0);
        return transition;
    }
}
