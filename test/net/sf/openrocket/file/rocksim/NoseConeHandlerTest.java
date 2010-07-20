/*
 * NoseConeHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;

import java.util.HashMap;

/**
 * NoseConeHandler Tester.
 *
 */
public class NoseConeHandlerTest extends RocksimTestBase {

    /**
     * The class under test.
     */
    public static final Class classUT = NoseConeHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = NoseConeHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(NoseConeHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public NoseConeHandlerTest(String name) {
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
            new NoseConeHandler(null, new WarningSet());
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        Stage stage = new Stage();
        NoseConeHandler handler = new NoseConeHandler(stage, new WarningSet());
        NoseCone component = (NoseCone) getField(handler, "noseCone");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new NoseConeHandler(new Stage(), new WarningSet()).openElement(null, null, null));
        assertNotNull(new NoseConeHandler(new Stage(), new WarningSet()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testCloseElement() throws Exception {

        Stage stage = new Stage();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        NoseConeHandler handler = new NoseConeHandler(stage, warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

        handler.closeElement("ShapeCode", attributes, "0", warnings);
        assertEquals(Transition.Shape.CONICAL, component.getType());
        handler.closeElement("ShapeCode", attributes, "1", warnings);
        assertEquals(Transition.Shape.OGIVE, component.getType());
        handler.closeElement("ShapeCode", attributes, "17", warnings);
        assertEquals(RocksimNoseConeCode.PARABOLIC.asOpenRocket(), component.getType());  //test of default
        handler.closeElement("ShapeCode", attributes, "foo", warnings);
        assertNotNull(component.getType());
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

        handler.closeElement("BaseDia", attributes, "-1", warnings);
        assertEquals(0d, component.getAftRadius());
        handler.closeElement("BaseDia", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius());
        handler.closeElement("BaseDia", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        
        final double aft = 100d;
        component.setAftRadius(aft);
        
        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(component.getAftRadius(), component.getThickness());
        assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness());
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(aft, component.getThickness());
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(1, warnings.size());
        warnings.clear();
        
        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(0d, component.getThickness());
        assertEquals(0d, component.getAftShoulderThickness());
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(1.1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness());
        assertEquals(1.1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderThickness());
        
        handler.closeElement("ShoulderLen", attributes, "-1", warnings);
        assertEquals(0d, component.getAftShoulderLength());
        handler.closeElement("ShoulderLen", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength());
        handler.closeElement("ShoulderLen", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength());
        handler.closeElement("ShoulderLen", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShoulderOD", attributes, "-1", warnings);
        assertEquals(0d, component.getAftShoulderRadius());
        handler.closeElement("ShoulderOD", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftShoulderRadius());
        handler.closeElement("ShoulderOD", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        component.setType(Transition.Shape.HAACK);
        handler.closeElement("ShapeParameter", attributes, "-1", warnings);
        assertEquals(0d, component.getShapeParameter());
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        assertEquals(Transition.Shape.HAACK.maxParameter(), component.getShapeParameter());
        handler.closeElement("ShapeParameter", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        assertEquals("Could not convert ShapeParameter value of foo.  It is expected to be a number.", 
                     warnings.iterator().next().toString());

        warnings.clear();

        component.setType(Transition.Shape.CONICAL);
        component.setShapeParameter(0d);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        assertEquals(0d, component.getShapeParameter());

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        assertEquals("Test Name", component.getName());
        
        handler.closeElement("Material", attributes, "Some Material", warnings);
        handler.endHandler("NoseCone", attributes, null, warnings);
        assertTrue(component.getMaterial().getName().contains("Some Material"));
     }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new NoseConeHandler(new Stage(), new WarningSet()).getComponent() instanceof NoseCone);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new NoseConeHandler(new Stage(), new WarningSet()).getMaterialType());
    }
}
