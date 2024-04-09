/*
 * LaunchLugHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.LaunchLug;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

/**
 * LaunchLugHandler Tester.
 *
 */
public class LaunchLugHandlerTest extends RockSimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testConstructor() throws Exception {

        try {
            new LaunchLugHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        BodyTube tube = new BodyTube();
        LaunchLugHandler handler = new LaunchLugHandler(null, tube, new WarningSet());
        LaunchLug component = (LaunchLug) getField(handler, "lug");
        assertContains(component, tube.getChildren());
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
                new LaunchLugHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
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
        LaunchLugHandler handler = new LaunchLugHandler(null, tube, new WarningSet());
        LaunchLug component = (LaunchLug) getField(handler, "lug");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "0", warnings);
        Assertions.assertEquals(0d, component.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getOuterRadius(),
                0.001);
        handler.closeElement("OD", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "-1", warnings);
        Assertions.assertEquals(0d, component.getInnerRadius(), 0.001);
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

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assertions.assertEquals(component.getName(), "Test Name");
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new LaunchLugHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof LaunchLug);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.BULK,
                new LaunchLugHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

}
