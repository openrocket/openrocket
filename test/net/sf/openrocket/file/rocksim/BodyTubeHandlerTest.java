/*
 * BodyTubeHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.Stage;

import java.util.HashMap;

/**
 * BodyTubeHandler Tester.
 *
 */
public class BodyTubeHandlerTest extends RocksimTestBase {

    /**
     * The class under test.
     */
    public static final Class classUT = BodyTubeHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = BodyTubeHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(BodyTubeHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public BodyTubeHandlerTest(String name) {
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
            new BodyTubeHandler(null, new WarningSet());
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        Stage stage = new Stage();
        BodyTubeHandler handler = new BodyTubeHandler(stage, new WarningSet());
        BodyTube component = (BodyTube) getField(handler, "bodyTube");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new BodyTubeHandler(new Stage(), new WarningSet()).openElement(null, null, null));
        assertNotNull(new BodyTubeHandler(new Stage(), new WarningSet()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testCloseElement() throws Exception {
        Stage stage = new Stage();
        BodyTubeHandler handler = new BodyTubeHandler(stage, new WarningSet());
        BodyTube component = (BodyTube) getField(handler, "bodyTube");
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

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());
    }
    
    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new BodyTubeHandler(new Stage(), new WarningSet()).getComponent() instanceof BodyTube);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new BodyTubeHandler(new Stage(), new WarningSet()).getMaterialType());
    }

}
