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
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RingComponentExporterTest {

    private static final float FLOAT_EPSILON = 1.0e-6f;

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void exportsZeroThicknessTubeWhenLengthPositive() {
        CenteringRing ring = createRing(0.03, 0.03, 0.02);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(ring);

        newExporter(obj, config, ring, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-thickness tube should emit geometry");
        assertTrue(obj.getNumFaces() > 0, "Zero-thickness tube should emit faces");
        assertEquals(1, warnings.size(), "Zero-thickness tube should emit a warning");
    }

    @Test
    void exportsTubeForValidRing() {
        CenteringRing ring = createRing(0.04, 0.02, 0.01);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(ring);

        newExporter(obj, config, ring, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Valid ring should create vertices");
        assertTrue(obj.getNumFaces() > 0, "Valid ring should create faces");
        assertEquals(0, warnings.size(), "No warning expected for valid ring thickness");
    }

    @Test
    void exportsRingDiskWhenLengthZero() {
        CenteringRing ring = createRing(0.04, 0.02, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(ring);

        newExporter(obj, config, ring, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Ring disk should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Ring disk should emit faces");
        assertEquals(0, warnings.size(), "No warning expected for finite thickness ring disk");

        for (int i = 0; i < obj.getNumVertices(); i++) {
            assertEquals(0f, obj.getVertex(i).getX(), FLOAT_EPSILON, "Ring disk vertices should lie in a plane");
        }
    }

    @Test
    void skipsGeometryWhenLengthAndThicknessZero() {
        CenteringRing ring = createRing(0.03, 0.03, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(ring);

        newExporter(obj, config, ring, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate ring should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Degenerate ring should emit no faces");
        assertEquals(1, warnings.size(), "Degenerate ring should still emit thickness warning");
    }

    private static RingComponentExporter newExporter(DefaultObj obj, FlightConfiguration config, CenteringRing ring, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new RingComponentExporter(obj, config, transformer, ring, "ringGroup", ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(CenteringRing ring) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(ring, 0, Transformation.IDENTITY);
        return config;
    }

    private static CenteringRing createRing(double outerRadius, double innerRadius, double length) {
        CenteringRing ring = new CenteringRing();
        ring.setOuterRadiusAutomatic(false);
        ring.setInnerRadiusAutomatic(false);
        ring.setOuterRadius(outerRadius);
        ring.setInnerRadius(innerRadius);
        ring.setLength(length);
        return ring;
    }
}
