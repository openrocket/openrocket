/*
 * TransitionHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimNoseConeCode;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Transition;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

/**
 * TransitionHandler Tester.
 */
public class TransitionHandlerTest extends RockSimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testConstructor() throws Exception {

        try {
            new TransitionHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        AxialStage stage = new AxialStage();
        TransitionHandler handler = new TransitionHandler(null, stage, new WarningSet());
        Transition component = (Transition) getField(handler, "transition");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes,
     * WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testOpenElement() throws Exception {
        Assertions.assertEquals(PlainTextHandler.INSTANCE,
                new TransitionHandler(null, new AxialStage(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes,
     * String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testCloseElement() throws Exception {

        AxialStage stage = new AxialStage();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        TransitionHandler handler = new TransitionHandler(null, stage, new WarningSet());
        Transition component = (Transition) getField(handler, "transition");

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
        Assertions.assertEquals(0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FrontDia", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getForeRadius(), 0.001);
        handler.closeElement("FrontDia", attributes, "100", warnings);
        Assertions.assertEquals(100d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getForeRadius(),
                0.001);
        handler.closeElement("FrontDia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearDia", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getAftRadius(), 0.001);
        handler.closeElement("RearDia", attributes, "100", warnings);
        Assertions.assertEquals(100d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius(),
                0.001);
        handler.closeElement("RearDia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        final double aft = 100d;
        component.setAftRadius(aft);

        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(component.getAftRadius(), component.getThickness(), 0.001);
        Assertions.assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness(), 0.001);
        Assertions.assertEquals(component.getForeShoulderThickness(), component.getForeShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(aft, component.getThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(0d, component.getThickness(), 0.001);
        Assertions.assertEquals(0d, component.getAftShoulderThickness(), 0.001);
        Assertions.assertEquals(0d, component.getForeShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertEquals(1.1d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness(),
                0.001);
        Assertions.assertEquals(1.1d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                component.getAftShoulderThickness(), 0.001);
        Assertions.assertEquals(1.1d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                component.getForeShoulderThickness(), 0.001);

        handler.closeElement("FrontShoulderLen", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "10.0", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderLen", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getAftShoulderLength(), 0.001);
        handler.closeElement("RearShoulderLen", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(),
                0.001);
        handler.closeElement("RearShoulderLen", attributes, "10.0", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(),
                0.001);
        handler.closeElement("RearShoulderLen", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FrontShoulderDia", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getForeShoulderRadius(), 0.001);
        handler.closeElement("FrontShoulderDia", attributes, "100", warnings);
        Assertions.assertEquals(100d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS,
                component.getForeShoulderRadius(), 0.001);
        handler.closeElement("FrontShoulderDia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderDia", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("RearShoulderDia", attributes, "100", warnings);
        Assertions.assertEquals(100d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS,
                component.getAftShoulderRadius(), 0.001);
        handler.closeElement("RearShoulderDia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        component.setShapeType(Transition.Shape.HAACK);
        handler.closeElement("ShapeParameter", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getShapeParameter(), 0.001);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assertions.assertEquals(Transition.Shape.HAACK.maxParameter(), component.getShapeParameter(), 0.001);
        handler.closeElement("ShapeParameter", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        Assertions.assertEquals(warnings.iterator().next().toString(), "Could not convert ShapeParameter value of foo.  It is expected to be a number.");

        warnings.clear();

        component.setShapeType(Transition.Shape.CONICAL);
        component.setShapeParameter(0d);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assertions.assertEquals(0d, component.getShapeParameter(), 0.001);

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
        handler.endHandler("Transition", attributes, null, warnings);
        Assertions.assertTrue(component.getMaterial().getName().contains("Some Material"));
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new TransitionHandler(null, new AxialStage(), new WarningSet()).getComponent() instanceof Transition);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.BULK,
                new TransitionHandler(null, new AxialStage(), new WarningSet()).getMaterialType());
    }

}
