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
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyTubeExporterTest {

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
        BodyTube bodyTube = createBodyTube(0.05, 0.0, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(bodyTube);

        newExporter(obj, config, bodyTube, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate body tube should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Degenerate body tube should emit no faces");
        assertEquals(1, warnings.size(), "Zero-thickness body tube should emit a warning");
    }

    @Test
    void exportsDiskWhenLengthZero() {
        BodyTube bodyTube = createBodyTube(0.05, 0.01, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(bodyTube);

        newExporter(obj, config, bodyTube, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-length body tube should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-length body tube should emit faces");
        assertEquals(0, warnings.size(), "Finite thickness disk should not emit warning");

        for (int i = 0; i < obj.getNumVertices(); i++) {
            assertEquals(0f, obj.getVertex(i).getX(), FLOAT_EPSILON, "Disk vertices should lie in a plane");
        }
    }

    @Test
    void exportsZeroThicknessTubeWhenLengthPositive() {
        BodyTube bodyTube = createBodyTube(0.05, 0.0, 0.05);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(bodyTube);

        newExporter(obj, config, bodyTube, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-thickness tube should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-thickness tube should emit faces");
        assertEquals(1, warnings.size(), "Zero-thickness tube should emit warning");
    }

    @Test
    void skipsGeometryWhenOuterRadiusZeroButLengthPositive() {
        BodyTube bodyTube = createBodyTube(0.0, 0.0, 0.05);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(bodyTube);

        newExporter(obj, config, bodyTube, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-diameter body tube should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-diameter body tube should emit no faces");
        assertEquals(1, warnings.size(), "Zero-diameter body tube retains zero-thickness warning");
    }

    private static BodyTubeExporter newExporter(DefaultObj obj, FlightConfiguration config, BodyTube bodyTube, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new BodyTubeExporter(obj, config, transformer, bodyTube, "bodyTubeGroup", ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(BodyTube bodyTube) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(bodyTube, 0, Transformation.IDENTITY);
        return config;
    }

    private static BodyTube createBodyTube(double outerRadius, double thickness, double length) {
        BodyTube tube = new BodyTube();
        tube.setOuterRadius(outerRadius);
        tube.setThickness(thickness);
        tube.setLength(length);
        tube.setFilled(false);
        return tube;
    }
}
