/*
 * TransitionHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimNoseConeCode;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import org.junit.Assert;

import java.util.HashMap;

/**
 * TransitionHandler Tester.
 */
public class TransitionHandlerTest extends RocksimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testConstructor() throws Exception {

        try {
            new TransitionHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        Stage stage = new Stage();
        TransitionHandler handler = new TransitionHandler(null, stage, new WarningSet());
        Transition component = (Transition) getField(handler, "transition");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new TransitionHandler(null, new Stage(), new WarningSet()).openElement(null, null, null));
    }

    /**
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testCloseElement() throws Exception {

        Stage stage = new Stage();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        TransitionHandler handler = new TransitionHandler(null, stage, new WarningSet());
        Transition component = (Transition) getField(handler, "transition");

        handler.closeElement("ShapeCode", attributes, "0", warnings);
        Assert.assertEquals(Transition.Shape.CONICAL, component.getType());
        handler.closeElement("ShapeCode", attributes, "1", warnings);
        Assert.assertEquals(Transition.Shape.OGIVE, component.getType());
        handler.closeElement("ShapeCode", attributes, "17", warnings);
        Assert.assertEquals(RocksimNoseConeCode.PARABOLIC.asOpenRocket(), component.getType());  //test of default
        handler.closeElement("ShapeCode", attributes, "foo", warnings);
        Assert.assertNotNull(component.getType());
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FrontDia", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getForeRadius(), 0.001);
        handler.closeElement("FrontDia", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getForeRadius(), 0.001);
        handler.closeElement("FrontDia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearDia", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftRadius(), 0.001);
        handler.closeElement("RearDia", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius(), 0.001);
        handler.closeElement("RearDia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        final double aft = 100d;
        component.setAftRadius(aft);
        
        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(component.getAftRadius(), component.getThickness(), 0.001);
        Assert.assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness(), 0.001);
        Assert.assertEquals(component.getForeShoulderThickness(), component.getForeShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(aft, component.getThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();
        
        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        component.setForeShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(0d, component.getThickness(), 0.001);
        Assert.assertEquals(0d, component.getAftShoulderThickness(), 0.001);
        Assert.assertEquals(0d, component.getForeShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(1.1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness(), 0.001);
        Assert.assertEquals(1.1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderThickness(), 0.001);
        Assert.assertEquals(1.1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderThickness(), 0.001);
        

        handler.closeElement("FrontShoulderLen", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getForeShoulderLength(), 0.001);
        handler.closeElement("FrontShoulderLen", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderLen", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftShoulderLength(), 0.001);
        handler.closeElement("RearShoulderLen", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(), 0.001);
        handler.closeElement("RearShoulderLen", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(), 0.001);
        handler.closeElement("RearShoulderLen", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FrontShoulderDia", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getForeShoulderRadius(), 0.001);
        handler.closeElement("FrontShoulderDia", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getForeShoulderRadius(), 0.001);
        handler.closeElement("FrontShoulderDia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("RearShoulderDia", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("RearShoulderDia", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("RearShoulderDia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        component.setType(Transition.Shape.HAACK);
        handler.closeElement("ShapeParameter", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getShapeParameter(), 0.001);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assert.assertEquals(Transition.Shape.HAACK.maxParameter(), component.getShapeParameter(), 0.001);
        handler.closeElement("ShapeParameter", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        Assert.assertEquals("Could not convert ShapeParameter value of foo.  It is expected to be a number.", 
                     warnings.iterator().next().toString());

        warnings.clear();

        component.setType(Transition.Shape.CONICAL);
        component.setShapeParameter(0d);
        handler.closeElement("ShapeParameter", attributes, "100", warnings);
        Assert.assertEquals(0d, component.getShapeParameter(), 0.001);

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        Assert.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        Assert.assertEquals(ExternalComponent.Finish.NORMAL, component.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Name", attributes, "Test Name", warnings);
        Assert.assertEquals("Test Name", component.getName());
        
        handler.closeElement("Material", attributes, "Some Material", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertTrue(component.getMaterial().getName().contains("Some Material"));
    }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetComponent() throws Exception {
        Assert.assertTrue(new TransitionHandler(null, new Stage(), new WarningSet()).getComponent() instanceof Transition);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.BULK, new TransitionHandler(null, new Stage(), new WarningSet()).getMaterialType());
    }


}
