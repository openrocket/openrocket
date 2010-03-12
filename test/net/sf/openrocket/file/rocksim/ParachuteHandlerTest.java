/*
 * ParachuteHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * ParachuteHandler Tester.
 */
public class ParachuteHandlerTest extends BaseRocksimTest {

    /**
     * The class under test.
     */
    public static final Class classUT = ParachuteHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = ParachuteHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(ParachuteHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public ParachuteHandlerTest(String name) {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Teardown the fixture.
     */
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new ParachuteHandler(new BodyTube()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(tube);
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        assertEquals(0.94d, component.getCD());
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        assertEquals(-0.94d, component.getCD());
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Dia", attributes, "-1", warnings);
        assertEquals(-1d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter());
        handler.closeElement("Dia", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getDiameter());
        handler.closeElement("Dia", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineCount", attributes, "-1", warnings);
        assertEquals(0, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "10", warnings);
        assertEquals(10, component.getLineCount());
        handler.closeElement("ShroudLineCount", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShroudLineLen", attributes, "-1", warnings);
        assertEquals(0d, component.getLineLength());
        handler.closeElement("ShroudLineLen", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLineLength());
        handler.closeElement("ShroudLineLen", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

    }

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    public void testConstructor() throws Exception {

        try {
            new ParachuteHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(tube);
        Parachute component = (Parachute) getField(handler, "chute");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(tube);
        Parachute component = (Parachute) getField(handler, "chute");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new ParachuteHandler(new BodyTube()).getComponent() instanceof Parachute);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.SURFACE, new ParachuteHandler(new BodyTube()).getMaterialType());
    }

    /**
     * Method: endHandler()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testEndHandler() throws Exception {
        BodyTube tube = new BodyTube();
        ParachuteHandler handler = new ParachuteHandler(tube);
        Parachute component = (Parachute) getField(handler, "chute");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "1", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
        assertEquals(component.getPositionValue(), -10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Parachute", attributes, null, warnings);
        assertEquals(RocketComponent.Position.BOTTOM, component.getRelativePosition());
        assertEquals(component.getPositionValue(), 10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
    }


}
