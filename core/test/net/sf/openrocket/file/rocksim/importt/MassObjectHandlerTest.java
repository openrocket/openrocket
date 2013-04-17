/*
 * MassObjectHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import org.junit.Assert;

import java.util.HashMap;

/**
 * MassObjectHandler Tester.
 *
 */
public class MassObjectHandlerTest extends RocksimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new MassObjectHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        MassObjectHandler handler = new MassObjectHandler(null, tube, new WarningSet());
        MassComponent mass = (MassComponent) getField(handler, "mass");
        MassComponent current = (MassComponent) getField(handler, "current");
        Assert.assertEquals(mass, current);
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new MassObjectHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
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
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        MassObjectHandler handler = new MassObjectHandler(null, tube, new WarningSet());
        MassComponent component = (MassComponent) getField(handler, "mass");

        handler.closeElement("Len", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assert.assertEquals(0.01
                , component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assert.assertEquals(0.01
                , component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("KnownMass", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getComponentMass(), 0.001);
        handler.closeElement("KnownMass", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS, component.getComponentMass(), 0.001);
        handler.closeElement("KnownMass", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

    }
    
    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        MassObjectHandler handler = new MassObjectHandler(null, tube, new WarningSet());
        MassComponent component = (MassComponent) getField(handler, "mass");
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
        Assert.assertTrue(new MassObjectHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof MassComponent);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.LINE, new MassObjectHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }
}
