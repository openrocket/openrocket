/*
 * NoseConeHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimNoseConeCode;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * NoseConeHandler Tester.
 *
 */
public class NoseConeHandlerTest extends RockSimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testConstructor() throws Exception {

        try {
            new NoseConeHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        AxialStage stage = new AxialStage();
        NoseConeHandler handler = new NoseConeHandler(null, stage, new WarningSet());
        NoseCone component = (NoseCone) getField(handler, "noseCone");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes,
     * WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testOpenElement() throws Exception {
        Assertions.assertEquals(PlainTextHandler.INSTANCE,
                new NoseConeHandler(null, new AxialStage(), new WarningSet()).openElement(null, null, null));
        Assertions.assertNotNull(
                new NoseConeHandler(null, new AxialStage(), new WarningSet()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes,
     * String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testCloseElement() throws Exception {

        AxialStage stage = new AxialStage();
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();

        NoseConeHandler handler = new NoseConeHandler(null, stage, warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("ShapeCode", attributes, "0", warnings);
        Assertions.assertEquals(Transition.Shape.CONICAL, component.getShapeType());
        handler.closeElement("ShapeCode", attributes, "1", warnings);
        Assertions.assertEquals(Transition.Shape.OGIVE, component.getShapeType());
        handler.closeElement("ShapeCode", attributes, "17", warnings);
        Assertions.assertEquals(RockSimNoseConeCode.PARABOLIC.asOpenRocket(), component.getShapeType()); // test of default
        handler.closeElement("ShapeCode", attributes, "foo", warnings);
        Assertions.assertNotNull(component.getShapeType());
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("BaseDia", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getAftRadius(), 0.001);
        handler.closeElement("BaseDia", attributes, "100", warnings);
        Assertions.assertEquals(100.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius(),
                0.001);
        handler.closeElement("BaseDia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        final double aft = 100.0d;
        component.setAftRadius(aft);

        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(component.getAftRadius(), component.getThickness(), 0.001);
        Assertions.assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(aft, component.getThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(0.0d, component.getThickness(), 0.001);
        Assertions.assertEquals(0.0d, component.getAftShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(1.1d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness(),
                0.001);
        Assertions.assertEquals(1.1d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                component.getAftShoulderThickness(), 0.001);

        handler.closeElement("ShoulderLen", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getAftShoulderLength(), 0.001);
        handler.closeElement("ShoulderLen", attributes, "10", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(),
                0.001);
        handler.closeElement("ShoulderLen", attributes, "10.0", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(),
                0.001);
        handler.closeElement("ShoulderLen", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShoulderOD", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("ShoulderOD", attributes, "100", warnings);
        Assertions.assertEquals(100.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS,
                component.getAftShoulderRadius(), 0.001);
        handler.closeElement("ShoulderOD", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        component.setShapeType(Transition.Shape.HAACK);
        handler.closeElement("ShapeParameter", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getShapeParameter(), 0.001);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assertions.assertEquals(Transition.Shape.HAACK.maxParameter(), component.getShapeParameter(), 0.001);
        // Both out-of-range values are clamped with a warning
        Assertions.assertEquals(2, warnings.size());
        warnings.clear();
        handler.closeElement("ShapeParameter", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        Assertions.assertEquals(warnings.iterator().next().toString(), "Could not convert ShapeParameter value of foo.  It is expected to be a number.");

        warnings.clear();

        component.setShapeType(Transition.Shape.CONICAL);
        component.setShapeParameter(0.0d);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assertions.assertEquals(0.0d, component.getShapeParameter(), 0.001);

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assertions.assertEquals(component.getName(), "Test Name");

        handler.closeElement("Material", attributes, "Some Material", warnings);
        handler.endHandler("NoseCone", attributes, null, warnings);
        Assertions.assertTrue(component.getMaterial().getName().contains("Some Material"));
    }

    /**
     * RockSim's secant ogive (shape code 7) bulges beyond the tangent ogive, which OpenRocket cannot represent, so it
     * is imported as a tangent ogive with a warning.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testSecantOgiveShapeCode() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();
        NoseConeHandler handler = new NoseConeHandler(null, new AxialStage(), warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("ShapeCode", attributes, "7", warnings);
        handler.closeElement("ShapeParameter", attributes, "300", warnings);

        Assertions.assertEquals(Transition.Shape.OGIVE, component.getShapeType());
        Assertions.assertEquals(1.0, component.getShapeParameter(), 1e-9);
        Assertions.assertEquals(1, warnings.size());
    }

    /**
     * RockSim allows Haack shape parameters above OpenRocket's maximum (1/3, the LV-Haack); clamping one must be
     * reported.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testHaackShapeParameterClamped() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();
        NoseConeHandler handler = new NoseConeHandler(null, new AxialStage(), warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("ShapeCode", attributes, "6", warnings);
        handler.closeElement("ShapeParameter", attributes, "0.6", warnings);

        Assertions.assertEquals(Transition.Shape.HAACK.maxParameter(), component.getShapeParameter(), 1e-9);
        Assertions.assertEquals(1, warnings.size());
    }

    /**
     * OpenRocket nose cones cannot have a drilled core, so a drilled solid RockSim nose cone must be reported.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testDrilledCore() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();
        NoseConeHandler handler = new NoseConeHandler(null, new AxialStage(), warnings);

        handler.closeElement("Len", attributes, "100", warnings);
        handler.closeElement("BaseDia", attributes, "40", warnings);
        handler.closeElement("ConstructionType", attributes, "0", warnings);
        handler.closeElement("CoreDia", attributes, "10", warnings);
        handler.closeElement("CoreLen", attributes, "50", warnings);
        handler.endHandler("NoseCone", attributes, null, warnings);

        Assertions.assertEquals(1, warnings.size());
    }

    /**
     * RockSim's parabola (shape code 2) is a paraboloid of revolution, r = R * sqrt(x / L), which is a 1/2 power
     * series in OpenRocket. The ShapeParameter that RockSim writes for every nose cone must not override it.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testParaboloidShapeCode() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();
        NoseConeHandler handler = new NoseConeHandler(null, new AxialStage(), warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("ShapeCode", attributes, "2", warnings);
        handler.closeElement("ShapeParameter", attributes, "0", warnings);

        Assertions.assertEquals(Transition.Shape.POWER, component.getShapeType());
        Assertions.assertEquals(0.5, component.getShapeParameter(), 1e-9);
        Assertions.assertEquals(0, warnings.size());
    }

    /**
     * RockSim places the part after a nose cone at Len + BaseExtensionLen, with the shoulder behind the base
     * extension. The extension is imported as a straight transition that carries the shoulder.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testBaseExtension() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();
        AxialStage stage = new AxialStage();
        NoseConeHandler handler = new NoseConeHandler(null, stage, warnings);
        NoseCone noseCone = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("Len", attributes, "139.7", warnings);
        handler.closeElement("BaseDia", attributes, "66", warnings);
        handler.closeElement("ShoulderLen", attributes, "40", warnings);
        handler.closeElement("ShoulderOD", attributes, "64", warnings);
        handler.closeElement("BaseExtensionLen", attributes, "50.8", warnings);
        handler.endHandler("NoseCone", attributes, null, warnings);

        Assertions.assertEquals(2, stage.getChildCount());
        Assertions.assertSame(noseCone, stage.getChild(0));
        Transition extension = (Transition) stage.getChild(1);
        Assertions.assertEquals(139.7 / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, noseCone.getLength(),
                1e-9);
        Assertions.assertEquals(0, noseCone.getAftShoulderLength(), 1e-9);
        Assertions.assertEquals(50.8 / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, extension.getLength(),
                1e-9);
        Assertions.assertEquals(noseCone.getAftRadius(), extension.getForeRadius(), 1e-9);
        Assertions.assertEquals(noseCone.getAftRadius(), extension.getAftRadius(), 1e-9);
        Assertions.assertEquals(40.0 / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                extension.getAftShoulderLength(), 1e-9);
        Assertions.assertEquals(64.0 / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS,
                extension.getAftShoulderRadius(), 1e-9);
        Assertions.assertEquals(1, warnings.size());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new NoseConeHandler(null, new AxialStage(), new WarningSet()).getComponent() instanceof NoseCone);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.BULK,
                new NoseConeHandler(null, new AxialStage(), new WarningSet()).getMaterialType());
    }
}
