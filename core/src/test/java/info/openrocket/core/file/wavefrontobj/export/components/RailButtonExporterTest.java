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
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Transformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RailButtonExporterTest {

    @BeforeAll
    static void setup() {
        Module applicationModule = new ServicesForTesting();
        Module pluginModule = new PluginModule();
        Injector injector = Guice.createInjector(applicationModule, pluginModule);
        Application.setInjector(injector);
    }

    @Test
    void skipsGeometryWhenOuterDiameterZero() {
        RailButton railButton = createRailButton(0.0, 0.0, 0.01, 0.005, 0.002);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(railButton);

        newExporter(obj, config, railButton, warnings).addToObj();

        assertEquals(0, obj.getNumVertices(), "Zero-diameter rail button should emit no vertices");
        assertEquals(0, obj.getNumFaces(), "Zero-diameter rail button should emit no faces");
    }

    @Test
    void exportsNominalRailButton() {
        RailButton railButton = createRailButton(0.02, 0.01, 0.01, 0.005, 0.002);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(railButton);

        newExporter(obj, config, railButton, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Nominal rail button should emit vertices");
        assertTrue(obj.getNumFaces() > 0, "Nominal rail button should emit faces");
    }

    @Test
    void handlesZeroBaseHeight() {
        RailButton railButton = createRailButton(0.02, 0.0, 0.0, 0.004, 0.0);

        DefaultObj obj = new DefaultObj();
        WarningSet warnings = new WarningSet();
        FlightConfiguration config = prepareConfiguration(railButton);

        newExporter(obj, config, railButton, warnings).addToObj();

        assertTrue(obj.getNumVertices() > 0, "Rail button with zero base height should still emit geometry");
        assertTrue(obj.getNumFaces() > 0, "Rail button with zero base height should still emit faces");
    }

    private static RailButtonExporter newExporter(DefaultObj obj, FlightConfiguration config, RailButton railButton, WarningSet warnings) {
        CoordTransform transformer = CoordTransform.generateUsingAxialAndForwardAxes(Axis.X, Axis.Y, 0, 0, 0);
        return new RailButtonExporter(obj, config, transformer, railButton, "railButtonGroup",
                ObjUtils.LevelOfDetail.NORMAL_QUALITY, true, warnings);
    }

    private static FlightConfiguration prepareConfiguration(RailButton railButton) {
        Rocket rocket = new Rocket();
        FlightConfiguration config = rocket.getSelectedConfiguration();
        InstanceMap map = config.getActiveInstances();
        map.emplace(railButton, 0, Transformation.IDENTITY);
        return config;
    }

    private static RailButton createRailButton(double outerDiameter, double innerDiameter, double baseHeight,
                                               double flangeHeight, double screwHeight) {
        RailButton railButton = new RailButton();
        double cylindricalHeight = Math.max(0, baseHeight) + Math.max(0, flangeHeight);
        double totalHeight = Math.max(cylindricalHeight + Math.max(0, screwHeight), 0.001);

        railButton.setOuterDiameter(outerDiameter);
        railButton.setInnerDiameter(innerDiameter);
        railButton.setBaseHeight(baseHeight);
        railButton.setFlangeHeight(flangeHeight);
        railButton.setTotalHeight(totalHeight);
        railButton.setScrewHeight(screwHeight);
        railButton.setInstanceCount(1);
        return railButton;
    }
}
