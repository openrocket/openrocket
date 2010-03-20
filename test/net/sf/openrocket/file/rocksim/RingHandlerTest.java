/*
 * RingHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import java.util.HashMap;

/**
 * RingHandler Tester.
 */
public class RingHandlerTest extends RocksimTestBase {

    /**
     * The class under test.
     */
    public static final Class classUT = RingHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = RingHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(RingHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public RingHandlerTest(String name) {
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
        assertEquals(PlainTextHandler.INSTANCE, new RingHandler(new BodyTube()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(tube);
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "0", warnings);
        assertEquals(0d, component.getOuterRadius());
        handler.closeElement("OD", attributes, "75", warnings);
        assertEquals(75d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getOuterRadius());
        handler.closeElement("OD", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "0", warnings);
        assertEquals(0d, component.getInnerRadius());
        handler.closeElement("ID", attributes, "75", warnings);
        assertEquals(75d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getInnerRadius());
        handler.closeElement("ID", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        assertEquals(0d, component.getLength());
        handler.closeElement("Len", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength());
        handler.closeElement("Len", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength());
        handler.closeElement("Len", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());
    }


    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    public void testConstructor() throws Exception {

        try {
            new RingHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(tube);
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        RingHandler handler = new RingHandler(tube);
        CenteringRing component = (CenteringRing) getField(handler, "ring");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new RingHandler(new BodyTube()).getComponent() instanceof CenteringRing);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new RingHandler(new BodyTube()).getMaterialType());
    }


}
