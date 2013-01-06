/*
 * StreamerHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimDensityType;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * StreamerHandler Tester.
 */
public class StreamerHandlerTest extends RocksimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new StreamerHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Width", attributes, "0", warnings);
        Assert.assertEquals(0d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth(), 0.001);
        handler.closeElement("Width", attributes, "10", warnings);
        Assert.assertEquals(10d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth(), 0.001);
        handler.closeElement("Width", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getStripLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        Assert.assertEquals(0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        Assert.assertEquals(-0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testConstructor() throws Exception {

        try {
            new StreamerHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        Assert.assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetComponent() throws Exception {
        Assert.assertTrue(new StreamerHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof Streamer);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.SURFACE, new StreamerHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

    /**
     * Method: endHandler()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testEndHandler() throws Exception {
        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "1", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assert.assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
        Assert.assertEquals(component.getPositionValue(), -10d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, 0.001);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assert.assertEquals(RocketComponent.Position.BOTTOM, component.getRelativePosition());
        Assert.assertEquals(component.getPositionValue(), 10d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, 0.001);

        handler.closeElement("Thickness", attributes, "0.02", warnings);
        Assert.assertEquals(0.01848, handler.computeDensity(RocksimDensityType.ROCKSIM_BULK, 924d), 0.001);

        //Test Density Type 0 (Bulk)
        handler.closeElement("Density", attributes, "924.0", warnings);
        handler.closeElement("DensityType", attributes, "0", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assert.assertEquals(0.01848d, component.getMaterial().getDensity(), 0.001);

        //Test Density Type 1 (Surface)
        handler.closeElement("Density", attributes, "0.006685", warnings);
        handler.closeElement("DensityType", attributes, "1", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assert.assertTrue(Math.abs(0.06685d - component.getMaterial().getDensity()) < 0.00001);

        //Test Density Type 2 (Line)
        handler.closeElement("Density", attributes, "0.223225", warnings);
        handler.closeElement("DensityType", attributes, "2", warnings);
        handler.closeElement("Len", attributes, "3810.", warnings);
        handler.closeElement("Width", attributes, "203.2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);

        Assert.assertEquals(1.728190092, component.getMass(), 0.001);

    }

}
