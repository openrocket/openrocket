/*
 * LaunchLugHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.junit.Assert;

import java.util.HashMap;

/**
 * LaunchLugHandler Tester.
 *
 */
public class LaunchLugHandlerTest extends RocksimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new LaunchLugHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        LaunchLugHandler handler = new LaunchLugHandler(null, tube, new WarningSet());
        LaunchLug component = (LaunchLug) getField(handler, "lug");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new LaunchLugHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
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
        LaunchLugHandler handler = new LaunchLugHandler(null, tube, new WarningSet());
        LaunchLug component = (LaunchLug) getField(handler, "lug");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "0", warnings);
        Assert.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getOuterRadius(), 0.001);
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

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        Assert.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        Assert.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());
    }
    
    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        LaunchLugHandler handler = new LaunchLugHandler(null, tube, new WarningSet());
        LaunchLug component = (LaunchLug) getField(handler, "lug");
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
        Assert.assertTrue(new LaunchLugHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof LaunchLug);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.BULK, new LaunchLugHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }


}
