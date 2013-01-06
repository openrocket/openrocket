/*
 * RingHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * RingHandler Tester.
 */
public class RingHandlerTest extends RocksimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new RingHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "0", warnings);
        Assert.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "0", warnings);
        Assert.assertEquals(0d, component.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "75", warnings);
        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());
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
        
        Assert.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent)tube.getChild(0);

        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assert.assertEquals(0d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assert.assertEquals("Test Name", child.getName());
        Assert.assertEquals(109.9/1000, child.getMass(), 0.001);
        Assert.assertEquals(0, child.getPositionValue(), 0.0);
        Assert.assertEquals(RocketComponent.Position.TOP, child.getRelativePosition());
        Assert.assertTrue(child instanceof Bulkhead);

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

        Assert.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent)tube.getChild(0);
        Assert.assertTrue(child instanceof TubeCoupler);

        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assert.assertEquals(70d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assert.assertEquals("Test Name", child.getName());
        Assert.assertEquals(109.9/1000, child.getMass(), 0.001);
        Assert.assertEquals(0, child.getPositionValue(), 0.0);
        Assert.assertEquals(RocketComponent.Position.TOP, child.getRelativePosition());
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

        Assert.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent)tube.getChild(0);
        Assert.assertTrue(child instanceof EngineBlock);

        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assert.assertEquals(70d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assert.assertEquals("Test Name", child.getName());
        Assert.assertEquals(109.9/1000, child.getMass(), 0.001);
        Assert.assertEquals(0, child.getPositionValue(), 0.0);
        Assert.assertEquals(RocketComponent.Position.TOP, child.getRelativePosition());
        Assert.assertEquals(4d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getCG().x, 0.000001);
        
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

        Assert.assertEquals(1, tube.getChildren().size());
        RingComponent child = (RingComponent)tube.getChild(0);

        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getOuterRadius(), 0.001);
        Assert.assertEquals(0d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, child.getInnerRadius(), 0.001);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, child.getLength(), 0.001);
        Assert.assertEquals("Test Name", child.getName());
        Assert.assertEquals(109.9/1000, child.getMass(), 0.001);
        Assert.assertEquals(0, child.getPositionValue(), 0.0);
        Assert.assertEquals(RocketComponent.Position.TOP, child.getRelativePosition());
        Assert.assertTrue(child instanceof CenteringRing);
    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new RingHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        CenteringRing component = (CenteringRing) getField(handler, "ring");
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(null, tube, new WarningSet());
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        Assert.assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    
    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetComponent() throws Exception {
        Assert.assertTrue(new RingHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof CenteringRing);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.BULK, new RingHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }


}
