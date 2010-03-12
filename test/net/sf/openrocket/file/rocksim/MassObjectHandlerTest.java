/*
 * MassObjectHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.aerodynamics.WarningSet;

import java.util.HashMap;

/**
 * MassObjectHandler Tester.
 *
 */
public class MassObjectHandlerTest extends BaseRocksimTest {

    /**
     * The class under test.
     */
    public static final Class classUT = MassObjectHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = MassObjectHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(MassObjectHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public MassObjectHandlerTest(String name) {
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
            new MassObjectHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        BodyTube tube = new BodyTube();
        MassObjectHandler handler = new MassObjectHandler(tube);
        MassComponent component = (MassComponent) getField(handler, "mass");
        assertContains(component, tube.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new MassObjectHandler(new BodyTube()).openElement(null, null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testCloseElement() throws Exception {
        BodyTube tube = new BodyTube();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        MassObjectHandler handler = new MassObjectHandler(tube);
        MassComponent component = (MassComponent) getField(handler, "mass");

        handler.closeElement("Len", attributes, "-1", warnings);
        assertEquals(0d, component.getLength());
        handler.closeElement("Len", attributes, "10", warnings);
        assertEquals(10d / (MassObjectHandler.MASS_LEN_FUDGE_FACTOR * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH)
                , component.getLength());
        handler.closeElement("Len", attributes, "10.0", warnings);
        assertEquals(10d / (MassObjectHandler.MASS_LEN_FUDGE_FACTOR * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH)
                , component.getLength());
        handler.closeElement("Len", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("KnownMass", attributes, "-1", warnings);
        assertEquals(0d, component.getComponentMass());
        handler.closeElement("KnownMass", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_MASS, component.getComponentMass());
        handler.closeElement("KnownMass", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

    }
    
    /**
     * Method: setRelativePosition(RocketComponent.Position position)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testSetRelativePosition() throws Exception {
        BodyTube tube = new BodyTube();
        MassObjectHandler handler = new MassObjectHandler(tube);
        MassComponent component = (MassComponent) getField(handler, "mass");
        handler.setRelativePosition(RocketComponent.Position.ABSOLUTE);
        assertEquals(RocketComponent.Position.ABSOLUTE, component.getRelativePosition());
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new MassObjectHandler(new BodyTube()).getComponent() instanceof MassComponent);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new MassObjectHandler(new BodyTube()).getMaterialType());
    }


}
