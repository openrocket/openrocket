/*
 * InnerBodyTubeHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;

import java.util.HashMap;

/**
 * InnerBodyTubeHandler Tester.
 *
 */
public class InnerBodyTubeHandlerTest extends RocksimTestBase {

    /**
     * The class under test.
     */
    public static final Class classUT = InnerBodyTubeHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = InnerBodyTubeHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(InnerBodyTubeHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public InnerBodyTubeHandlerTest(String name) {
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
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    public void testConstructor() throws Exception {

        try {
            new InnerBodyTubeHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        InnerBodyTubeHandler handler = new InnerBodyTubeHandler(tube);
        InnerTube component = (InnerTube) getField(handler, "bodyTube");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new InnerBodyTubeHandler(new BodyTube()).openElement(null, null, null));
        assertNotNull(new InnerBodyTubeHandler(new BodyTube()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testCloseElement() throws Exception {
        BodyTube tube = new BodyTube();
        InnerBodyTubeHandler handler = new InnerBodyTubeHandler(tube);
        InnerTube component = (InnerTube) getField(handler, "bodyTube");
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        handler.closeElement("OD", attributes, "-1", warnings);
        assertEquals(0d, component.getInnerRadius());
        handler.closeElement("OD", attributes, "0", warnings);
        assertEquals(0d, component.getInnerRadius());
        handler.closeElement("OD", attributes, "75", warnings);
        assertEquals(75d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getInnerRadius());
        handler.closeElement("OD", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "-1", warnings);
        assertEquals(0d, component.getInnerRadius());
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

        handler.closeElement("IsMotorMount", attributes, "1", warnings);
        assertTrue(component.isMotorMount());
        handler.closeElement("IsMotorMount", attributes, "0", warnings);
        assertFalse(component.isMotorMount());
        handler.closeElement("IsMotorMount", attributes, "foo", warnings);
        assertFalse(component.isMotorMount());

        handler.closeElement("EngineOverhang", attributes, "-1", warnings);
        assertEquals(-1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang());
        handler.closeElement("EngineOverhang", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang());
        handler.closeElement("EngineOverhang", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getMotorOverhang());
        handler.closeElement("EngineOverhang", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());
    }
    
    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        InnerBodyTubeHandler handler = new InnerBodyTubeHandler(tube);
        InnerTube component = (InnerTube) getField(handler, "bodyTube");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new InnerBodyTubeHandler(new BodyTube()).getComponent() instanceof InnerTube);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new InnerBodyTubeHandler(new BodyTube()).getMaterialType());
    }




}
