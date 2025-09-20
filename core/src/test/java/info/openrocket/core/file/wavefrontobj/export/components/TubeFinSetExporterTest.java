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
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TubeFinSetExporterTest {

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void skipsGeometryWhenLengthAndThicknessZero() {
        TubeFinSet tubeFinSet = createTubeFinSet(0.05, 0.0, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(tubeFinSet);

        newExporter(obj, config, tubeFinSet, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Degenerate tube fin set should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Degenerate tube fin set should emit no faces");
        assertEquals(1, warnings.size(), "Zero-thickness tube fin set should emit warning");
    }

    @Test
    void exportsRingWhenLengthZeroButThicknessPositive() {
        TubeFinSet tubeFinSet = createTubeFinSet(0.05, 0.01, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(tubeFinSet);

        newExporter(obj, config, tubeFinSet, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Zero-length tube fin set should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Zero-length tube fin set should emit faces");
        assertEquals(0, warnings.size(), "Finite-thickness tube fin set should not emit warning");
    }

    @Test
    void skipsGeometryWhenOuterRadiusZeroButLengthPositive() {
        TubeFinSet tubeFinSet = createTubeFinSet(0.0, 0.0, 0.1);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(tubeFinSet);

        newExporter(obj, config, tubeFinSet, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-diameter tube fin set should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-diameter tube fin set should emit no faces");
        assertEquals(1, warnings.size(), "Zero-diameter tube fin set retains zero-thickness warning");
    }

    private static TubeFinSetExporter newExporter(DefaultObj obj, FlightConfiguration config, TubeFinSet tubeFinSet, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new TubeFinSetExporter(obj, config, transformer, tubeFinSet, "tubeFinSetGroup", ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(TubeFinSet tubeFinSet) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(tubeFinSet, 0, Transformation.IDENTITY);
        return config;
    }

    private static TubeFinSet createTubeFinSet(double outerRadius, double thickness, double length) {
        TubeFinSet tubeFinSet = new TubeFinSet();
        tubeFinSet.setOuterRadiusAutomatic(false);
        tubeFinSet.setOuterRadius(outerRadius);
        tubeFinSet.setThickness(thickness);
        tubeFinSet.setLength(length);
        tubeFinSet.setFinCount(1);
        return tubeFinSet;
    }
}
