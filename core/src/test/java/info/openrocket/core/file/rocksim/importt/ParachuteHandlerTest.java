/*
 * ParachuteHandlerTest.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

/**
 * ParachuteHandler Tester.
 */
public class ParachuteHandlerTest extends RockSimTestBase {

    /**
     * Method: openElement(String element, HashMap<String, String> attributes,
     * WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testOpenElement() throws Exception {
        Assertions.assertEquals(PlainTextHandler.INSTANCE,
                new ParachuteHandler(null, new BodyTube(), new WarningSet()).openElement(null, null, null));
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
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assertions.assertEquals(component.getName(), "Test Name");

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        Assertions.assertEquals(0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        Assertions.assertEquals(-0.94d, component.getCD(), 0.001);
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Dia", attributes, "-1", warnings);
        Assertions.assertEquals(-1.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter(), 0.001);
        handler.closeElement("Dia", attributes, "10", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter(), 0.001);
        handler.closeElement("Dia", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineCount", attributes, "-1", warnings);
        Assertions.assertEquals(0, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "10", warnings);
        Assertions.assertEquals(10, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineLen", attributes, "-1", warnings);
        Assertions.assertEquals(0.0d, component.getLineLength(), 0.001);
        handler.closeElement("ShroudLineLen", attributes, "10", warnings);
        Assertions.assertEquals(10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLineLength(),
                0.001);
        handler.closeElement("ShroudLineLen", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testConstructor() throws Exception {

        try {
            new ParachuteHandler(null, null, new WarningSet());
            Assertions.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // success
        }

        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setAxialMethod(AxialMethod position)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testSetAxialMethod() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        handler.getComponent().setAxialMethod(AxialMethod.ABSOLUTE);
        Assertions.assertEquals(AxialMethod.ABSOLUTE, component.getAxialMethod());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetComponent() throws Exception {
        Assertions.assertTrue(
                new ParachuteHandler(null, new BodyTube(), new WarningSet()).getComponent() instanceof Parachute);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testGetMaterialType() throws Exception {
        Assertions.assertEquals(Material.Type.SURFACE,
                new ParachuteHandler(null, new BodyTube(), new WarningSet()).getMaterialType());
    }

    /**
     * Method: endHandler()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testEndHandler() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(null, tube, new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "1", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        Assertions.assertEquals(AxialMethod.ABSOLUTE, component.getAxialMethod());
        Assertions.assertEquals(component.getAxialOffset(), -10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                0.001);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        Assertions.assertEquals(AxialMethod.BOTTOM, component.getAxialMethod());
        Assertions.assertEquals(component.getAxialOffset(), 10.0d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
                0.001);
    }

    /**
     * RockSim's drag area is that of all canopies, each a regular polygon whose diameter is measured between
     * opposite corners, minus the spill hole. The imported round parachute must have the same drag area.
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testPolygonalCanopyDragArea() throws Exception {
        ParachuteHandler handler = new ParachuteHandler(null, new BodyTube(), new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Dia", attributes, "1000", warnings);
        handler.closeElement("SideCount", attributes, "6", warnings);
        handler.closeElement("SpillHoleDia", attributes, "100", warnings);
        handler.closeElement("ChuteCount", attributes, "2", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);

        double hexagonArea = 3 * Math.sqrt(3) / 2 * 0.5 * 0.5;
        double expectedArea = 2 * (hexagonArea - Math.PI * 0.05 * 0.05);
        Assertions.assertEquals(expectedArea, Math.PI * Math.pow(component.getDiameter() / 2, 2), 1e-9);
        Assertions.assertEquals(1, warnings.size());
    }

    /**
     * A round RockSim parachute (SideCount 0) keeps its diameter.
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testRoundCanopy() throws Exception {
        ParachuteHandler handler = new ParachuteHandler(null, new BodyTube(), new WarningSet());
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Dia", attributes, "1000", warnings);
        handler.closeElement("SideCount", attributes, "0", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);

        Assertions.assertEquals(1.0, component.getDiameter(), 1e-9);
        Assertions.assertEquals(0, warnings.size());
    }
}
