/*
 * TransitionHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;

import java.util.HashMap;

/**
 * TransitionHandler Tester.
 */
public class TransitionHandlerTest extends BaseRocksimTest {

    /**
     * The class under test.
     */
    public static final Class classUT = TransitionHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = TransitionHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(TransitionHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public TransitionHandlerTest(String name) {
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
            new TransitionHandler(null);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        Stage stage = new Stage();
        TransitionHandler handler = new TransitionHandler(stage);
        Transition component = (Transition) getField(handler, "transition");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testOpenElement() throws Exception {
        assertEquals(PlainTextHandler.INSTANCE, new TransitionHandler(new Stage()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    public void testCloseElement() throws Exception {

        Stage stage = new Stage();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        TransitionHandler handler = new TransitionHandler(stage);
        Transition component = (Transition) getField(handler, "transition");

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

        handler.closeElement("FrontDia", attributes, "-1", warnings);
        assertEquals(0d, component.getForeRadius());
        handler.closeElement("FrontDia", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getForeRadius());
        handler.closeElement("FrontDia", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearDia", attributes, "-1", warnings);
        assertEquals(0d, component.getAftRadius());
        handler.closeElement("RearDia", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius());
        handler.closeElement("RearDia", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        final double aft = 100d;
        component.setAftRadius(aft);
        
        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(component.getAftRadius(), component.getThickness());
        assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness());
        assertEquals(component.getForeShoulderThickness(), component.getForeShoulderThickness());
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(aft, component.getThickness());
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(1, warnings.size());
        warnings.clear();
        
        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(0d, component.getThickness());
        assertEquals(0d, component.getAftShoulderThickness());
        assertEquals(0d, component.getForeShoulderThickness());
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        assertEquals(1.1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness());
        assertEquals(1.1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderThickness());
        assertEquals(1.1d/RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderThickness());
        

        handler.closeElement("FrontShoulderLen", attributes, "-1", warnings);
        assertEquals(0d, component.getForeShoulderLength());
        handler.closeElement("FrontShoulderLen", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderLength());
        handler.closeElement("FrontShoulderLen", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderLength());
        handler.closeElement("FrontShoulderLen", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderLen", attributes, "-1", warnings);
        assertEquals(0d, component.getAftShoulderLength());
        handler.closeElement("RearShoulderLen", attributes, "10", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength());
        handler.closeElement("RearShoulderLen", attributes, "10.0", warnings);
        assertEquals(10d / RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength());
        handler.closeElement("RearShoulderLen", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FrontShoulderDia", attributes, "-1", warnings);
        assertEquals(0d, component.getForeShoulderRadius());
        handler.closeElement("FrontShoulderDia", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getForeShoulderRadius());
        handler.closeElement("FrontShoulderDia", attributes, "foo", warnings);
        assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderDia", attributes, "-1", warnings);
        assertEquals(0d, component.getAftShoulderRadius());
        handler.closeElement("RearShoulderDia", attributes, "100", warnings);
        assertEquals(100d / RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftShoulderRadius());
        handler.closeElement("RearShoulderDia", attributes, "foo", warnings);
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
        handler.endHandler("Transition", attributes, null, warnings);
        assertTrue(component.getMaterial().getName().contains("Some Material"));
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetComponent() throws Exception {
        assertTrue(new TransitionHandler(new Stage()).getComponent() instanceof Transition);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    public void testGetMaterialType() throws Exception {
        assertEquals(Material.Type.BULK, new TransitionHandler(new Stage()).getMaterialType());
    }


}
