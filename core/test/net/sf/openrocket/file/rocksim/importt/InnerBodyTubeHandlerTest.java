/*
 * InnerBodyTubeHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.InnerTube;

import org.junit.Assert;

import java.util.HashMap;

/**
 * InnerBodyTubeHandler Tester.
 *
 */
public class InnerBodyTubeHandlerTest extends RocksimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new InnerBodyTubeHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        InnerBodyTubeHandler handler = new InnerBodyTubeHandler(null, tube, new WarningSet());
        InnerTube component = (InnerTube) getField(handler, "bodyTube");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new InnerBodyTubeHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
        Assert.assertNotNull(new InnerBodyTubeHandler(null, new BodyTube(), new WarningSet()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    @org.junit.Test
    public void testCloseElement() throws Exception {
        BodyTube tube = new BodyTube();
        InnerBodyTubeHandler handler = new InnerBodyTubeHandler(null, tube, new WarningSet());
        InnerTube component = (InnerTube) getField(handler, "bodyTube");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getInnerRadius(), 0.001);
        handler.closeElement("OD", attributes, "0", warnings);
        Assert.assertEquals(0d, component.getInnerRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getInnerRadius(), 0.001);
        handler.closeElement("OD", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getInnerRadius(), 0.001);
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

        handler.closeElement("IsMotorMount", attributes, "1", warnings);
        Assert.assertTrue(component.isMotorMount());
        handler.closeElement("IsMotorMount", attributes, "0", warnings);
        Assert.assertFalse(component.isMotorMount());
        handler.closeElement("IsMotorMount", attributes, "foo", warnings);
        Assert.assertFalse(component.isMotorMount());

        handler.closeElement("EngineOverhang", attributes, "-1", warnings);
        Assert.assertEquals(-1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang(), 0.001);
        handler.closeElement("EngineOverhang", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang(), 0.001);
        handler.closeElement("EngineOverhang", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang(), 0.001);
        handler.closeElement("EngineOverhang", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetComponent() throws Exception {
        Assert.assertTrue(new InnerBodyTubeHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof InnerTube);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.BULK, new InnerBodyTubeHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

}
