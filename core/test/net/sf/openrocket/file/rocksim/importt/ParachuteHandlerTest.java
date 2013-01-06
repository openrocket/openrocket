/*
 * ParachuteHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.junit.Assert;

import java.util.HashMap;

/**
 * ParachuteHandler Tester.
 */
public class ParachuteHandlerTest extends RocksimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new ParachuteHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        Assert.assertEquals(0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        Assert.assertEquals(-0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Dia", attributes, "-1", warnings);
        Assert.assertEquals(-1d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter(), 0.001);
        handler.closeElement("Dia", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter(), 0.001);
        handler.closeElement("Dia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineCount", attributes, "-1", warnings);
        Assert.assertEquals(0, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "10", warnings);
        Assert.assertEquals(10, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineLen", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getLineLength(), 0.001);
        handler.closeElement("ShroudLineLen", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLineLength(), 0.001);
        handler.closeElement("ShroudLineLen", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new ParachuteHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
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
        Assert.assertTrue(new ParachuteHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof Parachute);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.SURFACE, new ParachuteHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

    /**
     * Method: endHandler()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testEndHandler() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "1", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        Assert.assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
        Assert.assertEquals(component.getPositionValue(), -10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, 0.001);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        Assert.assertEquals(RocketComponent.Position.BOTTOM, component.getRelativePosition());
        Assert.assertEquals(component.getPositionValue(), 10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, 0.001);
    }
}
