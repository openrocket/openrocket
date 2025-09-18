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
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchLugExporterTest {

    private static final float FLOAT_EPSILON = 1.0e-6f;

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void skipsGeometryWhenLengthAndThicknessZero() {
        LaunchLug launchLug = createLaunchLug(0.01, 0.0, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(launchLug);

        newExporter(obj, config, launchLug, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate launch lug should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Degenerate launch lug should emit no faces");
        assertEquals(1, warnings.size(), "Zero-thickness launch lug should emit warning");
    }

    @Test
    void exportsRingWhenLengthZeroButThicknessPositive() {
        LaunchLug launchLug = createLaunchLug(0.01, 0.002, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(launchLug);

        newExporter(obj, config, launchLug, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-length launch lug should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-length launch lug should emit faces");
        assertEquals(0, warnings.size(), "Finite-thickness launch lug should not emit warning");

        for (int i = 0; i < obj.getNumVertices(); i++) {
            assertEquals(0f, obj.getVertex(i).getX(), FLOAT_EPSILON, "Ring vertices should lie in a plane");
        }
    }

    @Test
    void skipsGeometryWhenOuterRadiusZeroButLengthPositive() {
        LaunchLug launchLug = createLaunchLug(0.0, 0.0, 0.05);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(launchLug);

        newExporter(obj, config, launchLug, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-diameter launch lug should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-diameter launch lug should emit no faces");
        assertEquals(1, warnings.size(), "Zero-diameter launch lug retains zero-thickness warning");
    }

    private static LaunchLugExporter newExporter(DefaultObj obj, FlightConfiguration config, LaunchLug launchLug, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new LaunchLugExporter(obj, config, transformer, launchLug, "launchLugGroup", ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(LaunchLug launchLug) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(launchLug, 0, Transformation.IDENTITY);
        return config;
    }

    private static LaunchLug createLaunchLug(double outerRadius, double thickness, double length) {
        LaunchLug launchLug = new LaunchLug();
        launchLug.setInstanceCount(1);
        launchLug.setOuterRadius(outerRadius);
        launchLug.setThickness(thickness);
        launchLug.setLength(length);
        return launchLug;
    }
}
