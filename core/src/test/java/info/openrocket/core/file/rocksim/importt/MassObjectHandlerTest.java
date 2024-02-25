/*
 * MassObjectHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.MassComponent;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

/**
 * MassObjectHandler Tester.
 *
 */
public class MassObjectHandlerTest extends RockSimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testConstructor() throws Exception {

        try {
            new MassObjectHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        BodyTube tube = new BodyTube();
        MassObjectHandler handler = new MassObjectHandler(null, tube, new WarningSet());
        MassComponent mass = (MassComponent) getField(handler, "mass");
        MassComponent current = (MassComponent) getField(handler, "current");
        Assertions.assertEquals(mass, current);
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
                new MassObjectHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes,
     * String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testCloseElement() throws Exception {
        BodyTube tube = new BodyTube();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        MassObjectHandler handler = new MassObjectHandler(null, tube, new WarningSet());
        MassComponent component = (MassComponent) getField(handler, "mass");

        handler.closeElement("Len", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assertions.assertEquals(0.01, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assertions.assertEquals(0.01, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("KnownMass", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getComponentMass(), 0.001);
        handler.closeElement("KnownMass", attributes, "100", warnings);
        Assertions.assertEquals(100d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS, component.getComponentMass(),
                0.001);
        handler.closeElement("KnownMass", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new MassObjectHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof MassComponent);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.LINE,
                new MassObjectHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }
}
