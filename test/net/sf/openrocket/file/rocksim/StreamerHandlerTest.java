/*
 * StreamerHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;

import java.util.HashMap;

/**
 * StreamerHandler Tester.
 */
public class StreamerHandlerTest extends RocksimTestBase {

    /**
     * The class under test.
     */
    public static final Class classUT = StreamerHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = StreamerHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(StreamerHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public StreamerHandlerTest(String name) {
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
        assertEquals(PlainTextHandler.INSTANCE, new StreamerHandler(new BodyTube()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testCloseElement() throws Exception {

        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(tube);
        Streamer component = (Streamer) getField(handler, "streamer");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Width", attributes, "0", warnings);
        assertEquals(0d/ RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth());
        handler.closeElement("Width", attributes, "10", warnings);
        assertEquals(10d/ RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripWidth());
        handler.closeElement("Width", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        assertEquals(0d, component.getStripLength());
        handler.closeElement("Len", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength());
        handler.closeElement("Len", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getStripLength());
        handler.closeElement("Len", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());

        handler.closeElement("DragCoefficient", attributes, "0.94", warnings);
        assertEquals(0.94d, component.getCD());
        handler.closeElement("DragCoefficient", attributes, "-0.94", warnings);
        assertEquals(-0.94d, component.getCD());
        handler.closeElement("DragCoefficient", attributes, "foo", warnings);
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
            new StreamerHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(tube);
        Streamer component = (Streamer) getField(handler, "streamer");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(tube);
        Streamer component = (Streamer) getField(handler, "streamer");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new StreamerHandler(new BodyTube()).getComponent() instanceof Streamer);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.SURFACE, new StreamerHandler(new BodyTube()).getMaterialType());
    }

    /**
     * Method: endHandler()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testEndHandler() throws Exception {
        BodyTube tube = new BodyTube();
        StreamerHandler handler = new StreamerHandler(tube);
        Streamer component = (Streamer) getField(handler, "streamer");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "1", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
        assertEquals(component.getPositionValue(), -10d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

        handler.closeElement("Xb", attributes, "-10", warnings);
        handler.closeElement("LocationMode", attributes, "2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        assertEquals(RocketComponent.Position.BOTTOM, component.getRelativePosition());
        assertEquals(component.getPositionValue(), 10d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

        handler.closeElement("Thickness", attributes, "0.02", warnings);
        assertEquals(0.01848, handler.computeDensity(RocksimDensityType.ROCKSIM_BULK, 924d));

        //Test Density Type 0 (Bulk)
        handler.closeElement("Density", attributes, "924.0", warnings);
        handler.closeElement("DensityType", attributes, "0", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        assertEquals(0.01848d, component.getMaterial().getDensity());

        //Test Density Type 1 (Surface)
        handler.closeElement("Density", attributes, "0.006685", warnings);
        handler.closeElement("DensityType", attributes, "1", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);
        assertTrue(Math.abs(0.06685d - component.getMaterial().getDensity()) < 0.00001);

        //Test Density Type 2 (Line)
        handler.closeElement("Density", attributes, "0.223225", warnings);
        handler.closeElement("DensityType", attributes, "2", warnings);
        handler.closeElement("Len", attributes, "3810.", warnings);
        handler.closeElement("Width", attributes, "203.2", warnings);
        handler.endHandler("Streamer", attributes, null, warnings);

        assertEquals(1.728190092, component.getMass());

    }

}
