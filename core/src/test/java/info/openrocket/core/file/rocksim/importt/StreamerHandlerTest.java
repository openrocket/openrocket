/*
 * StreamerHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimDensityType;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

/**
 * StreamerHandler Tester.
 */
public class StreamerHandlerTest extends RockSimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes,
     * WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testOpenElement() throws Exception {
        Assertions.assertEquals(PlainTextHandler.INSTANCE,
                new StreamerHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes,
     * String content, WarningSet warnings)
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
        Assertions.assertEquals(0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth(), 0.001);
        handler.closeElement("Width", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth(),
                0.001);
        handler.closeElement("Width", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getStripLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength(),
                0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength(),
                0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assertions.assertEquals(component.getName(), "Test Name");

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        Assertions.assertEquals(0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        Assertions.assertEquals(-0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
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
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(AxialMethod position)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(null, tube, new WarningSet());
        Streamer component = (Streamer) getField(handler, "streamer");
        handler.getComponent().setAxialMethod(AxialMethod.ABSOLUTE);
        Assertions.assertEquals(AxialMethod.ABSOLUTE, component.getAxialMethod());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new StreamerHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof Streamer);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.SURFACE,
                new StreamerHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
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
        Assertions.assertEquals(AxialMethod.ABSOLUTE, component.getAxialMethod());
        Assertions.assertEquals(component.getAxialOffset(), -10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                0.001);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assertions.assertEquals(AxialMethod.BOTTOM, component.getAxialMethod());
        Assertions.assertEquals(component.getAxialOffset(), 10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                0.001);

        handler.closeElement("Thickness", attributes, "0.02", warnings);
        Assertions.assertEquals(0.01848, handler.computeDensity(RockSimDensityType.ROCKSIM_BULK, 924d), 0.001);

        // Test Density Type 0 (Bulk)
        handler.closeElement("Density", attributes, "924.0", warnings);
        handler.closeElement("DensityType", attributes, "0", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assertions.assertEquals(0.01848d, component.getMaterial().getDensity(), 0.001);

        // Test Density Type 1 (Surface)
        handler.closeElement("Density", attributes, "0.006685", warnings);
        handler.closeElement("DensityType", attributes, "1", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        Assertions.assertTrue(Math.abs(0.06685d - component.getMaterial().getDensity()) < 0.00001);

        // Test Density Type 2 (Line)
        handler.closeElement("Density", attributes, "0.223225", warnings);
        handler.closeElement("DensityType", attributes, "2", warnings);
        handler.closeElement("Len", attributes, "3810.", warnings);
        handler.closeElement("Width", attributes, "203.2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);

        Assertions.assertEquals(1.728190092, component.getMass(), 0.001);

    }

}
