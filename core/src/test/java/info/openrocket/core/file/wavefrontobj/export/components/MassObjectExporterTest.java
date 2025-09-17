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
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MassObjectExporterTest {

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void skipsGeometryWhenLengthZero() {
        MassComponent massComponent = createMassComponent(0.0, 0.02);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(massComponent);

        newExporter(obj, config, massComponent, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-length mass object should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-length mass object should emit no faces");
    }

    @Test
    void skipsGeometryWhenRadiusZero() {
        MassComponent massComponent = createMassComponent(0.05, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(massComponent);

        newExporter(obj, config, massComponent, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-radius mass object should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-radius mass object should emit no faces");
    }

    @Test
    void exportsNominalMassObject() {
        MassComponent massComponent = createMassComponent(0.05, 0.02);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(massComponent);

        newExporter(obj, config, massComponent, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Nominal mass object should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Nominal mass object should emit faces");
    }

    private static MassObjectExporter newExporter(DefaultObj obj, FlightConfiguration config, MassComponent component, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new MassObjectExporter(obj, config, transformer, component, "massComponentGroup",
                ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(MassComponent component) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(component, 0, Transformation.IDENTITY);
        return config;
    }

    private static MassComponent createMassComponent(double length, double radius) {
        MassComponent component = new MassComponent();
        component.setLength(length);
        component.setRadius(radius);
        component.setComponentMass(0.1);
        return component;
    }
}
