/*
 * RingHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

/**
 * RingHandler Tester.
 */
public class RingHandlerTest extends RockSimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes,
     * WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testOpenElement() throws Exception {
        Assertions.assertEquals(PlainTextHandler.INSTANCE,
                new RingHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes,
     * String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "0", warnings);
        Assertions.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getOuterRadius(),
                0.001);
        handler.closeElement("OD", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "0", warnings);
        Assertions.assertEquals(0d, component.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "75", warnings);
        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getInnerRadius(),
                0.001);
        handler.closeElement("ID", attributes, "foo", warnings);
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

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assertions.assertEquals(component.getName(), "Test Name");
    }

    /**
     * Test a bulkhead.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testBulkhead() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());

        @SuppressWarnings("unused")
        CenteringRing component = (CenteringRing) getField(handler, "ring");

        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "75", warnings);
        handler.closeElement("ID", attributes, "0", warnings);
        handler.closeElement("Len", attributes, "10", warnings);
        handler.closeElement("Name", attributes, "Test Name", warnings);
        handler.closeElement("KnownMass", attributes, "109.9", warnings);
        handler.closeElement("UsageCode", attributes, "1", warnings);
        handler.closeElement("UseKnownCG", attributes, "1", warnings);
        handler.endHandler("", attributes, "", warnings);

        Assertions.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent) tube.getChild(0);

        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assertions.assertEquals(0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assertions.assertEquals(child.getName(), "Test Name");
        Assertions.assertEquals(109.9 / 1000, child.getMass(), 0.001);
        Assertions.assertEquals(0, child.getAxialOffset(), 0.0);
        Assertions.assertEquals(AxialMethod.TOP, child.getAxialMethod());
        Assertions.assertTrue(child instanceof Bulkhead);

    }

    /**
     * Test a tube coupler.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testTubeCoupler() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "75", warnings);
        handler.closeElement("ID", attributes, "70", warnings);
        handler.closeElement("Len", attributes, "10", warnings);
        handler.closeElement("Name", attributes, "Test Name", warnings);
        handler.closeElement("KnownMass", attributes, "109.9", warnings);
        handler.closeElement("UsageCode", attributes, "4", warnings);
        handler.closeElement("UseKnownCG", attributes, "1", warnings);
        handler.endHandler("", attributes, "", warnings);

        Assertions.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent) tube.getChild(0);
        Assertions.assertTrue(child instanceof TubeCoupler);

        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assertions.assertEquals(70d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assertions.assertEquals(child.getName(), "Test Name");
        Assertions.assertEquals(109.9 / 1000, child.getMass(), 0.001);
        Assertions.assertEquals(0, child.getAxialOffset(), 0.0);
        Assertions.assertEquals(AxialMethod.TOP, child.getAxialMethod());
    }

    /**
     * Test a engine block.
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testEngineBlock() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "75", warnings);
        handler.closeElement("ID", attributes, "70", warnings);
        handler.closeElement("Len", attributes, "10", warnings);
        handler.closeElement("Name", attributes, "Test Name", warnings);
        handler.closeElement("KnownMass", attributes, "109.9", warnings);
        handler.closeElement("UsageCode", attributes, "2", warnings);
        handler.closeElement("KnownCG", attributes, "4", warnings);
        handler.closeElement("UseKnownCG", attributes, "1", warnings);
        handler.endHandler("", attributes, "", warnings);

        Assertions.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent) tube.getChild(0);
        Assertions.assertTrue(child instanceof EngineBlock);

        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assertions.assertEquals(70d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assertions.assertEquals(child.getName(), "Test Name");
        Assertions.assertEquals(109.9 / 1000, child.getMass(), 0.001);
        Assertions.assertEquals(0, child.getAxialOffset(), 0.0);
        Assertions.assertEquals(AxialMethod.TOP, child.getAxialMethod());
        Assertions.assertEquals(4d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getCG().x, 0.000001);

    }

    /**
     * Test a centering ring
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testRing() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "75", warnings);
        handler.closeElement("ID", attributes, "0", warnings);
        handler.closeElement("Len", attributes, "10", warnings);
        handler.closeElement("Name", attributes, "Test Name", warnings);
        handler.closeElement("KnownMass", attributes, "109.9", warnings);
        handler.closeElement("UsageCode", attributes, "0", warnings);
        handler.closeElement("UseKnownCG", attributes, "1", warnings);
        handler.endHandler("", attributes, "", warnings);

        Assertions.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent) tube.getChild(0);

        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assertions.assertEquals(0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assertions.assertEquals(child.getName(), "Test Name");
        Assertions.assertEquals(109.9 / 1000, child.getMass(), 0.001);
        Assertions.assertEquals(0, child.getAxialOffset(), 0.0);
        Assertions.assertEquals(AxialMethod.TOP, child.getAxialMethod());
        Assertions.assertTrue(child instanceof CenteringRing);
    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testConstructor() throws Exception {

        try {
            new RingHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());

        @SuppressWarnings("unused")
        CenteringRing component = (CenteringRing) getField(handler, "ring");
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new RingHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof CenteringRing);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.BULK,
                new RingHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

}
