/*
 * NoseConeHandlerTest.java
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimNoseConeCode;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * NoseConeHandler Tester.
 *
 */
public class NoseConeHandlerTest extends RocksimTestBase {

    /**
     * Method: constructor
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testConstructor() throws Exception {

        try {
            new NoseConeHandler(null, null, new WarningSet());
            Assert.fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        Stage stage = new Stage();
        NoseConeHandler handler = new NoseConeHandler(null, stage, new WarningSet());
        NoseCone component = (NoseCone) getField(handler, "noseCone");
        assertContains(component, stage.getChildren());
    }

    /**
     * Method: openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testOpenElement() throws Exception {
        Assert.assertEquals(PlainTextHandler.INSTANCE, new NoseConeHandler(null, new Stage(), new WarningSet()).openElement(null, null, null));
        Assert.assertNotNull(new NoseConeHandler(null, new Stage(), new WarningSet()).openElement("AttachedParts", null, null));
    }

    /**
     *
     * Method: closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
     *
     * @throws Exception  thrown if something goes awry
     */
    @Test
    public void testCloseElement() throws Exception {

        Stage stage = new Stage();
        HashMap<String, String> attributes = new HashMap<String, String>();
        WarningSet warnings = new WarningSet();

        NoseConeHandler handler = new NoseConeHandler(null, stage, warnings);
        NoseCone component = (NoseCone) getField(handler, "noseCone");

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

        handler.closeElement("BaseDia", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftRadius(), 0.001);
        handler.closeElement("BaseDia", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftRadius(), 0.001);
        handler.closeElement("BaseDia", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        
        final double aft = 100d;
        component.setAftRadius(aft);
        
        handler.closeElement("ConstructionType", attributes, "0", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(component.getAftRadius(), component.getThickness(), 0.001);
        Assert.assertEquals(component.getAftShoulderThickness(), component.getAftShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "100", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(aft, component.getThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "foo", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();
        
        handler.closeElement("ConstructionType", attributes, "1", warnings);
        component.setAftShoulderRadius(1.1d);
        handler.closeElement("WallThickness", attributes, "-1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(0d, component.getThickness(), 0.001);
        Assert.assertEquals(0d, component.getAftShoulderThickness(), 0.001);
        handler.closeElement("WallThickness", attributes, "1.1", warnings);
        handler.endHandler("Transition", attributes, null, warnings);
        Assert.assertEquals(1.1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getThickness(), 0.001);
        Assert.assertEquals(1.1d/ RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderThickness(), 0.001);
        
        handler.closeElement("ShoulderLen", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftShoulderLength(), 0.001);
        handler.closeElement("ShoulderLen", attributes, "10", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(), 0.001);
        handler.closeElement("ShoulderLen", attributes, "10.0", warnings);
        Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, component.getAftShoulderLength(), 0.001);
        handler.closeElement("ShoulderLen", attributes, "foo", warnings);
        Assert.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ShoulderOD", attributes, "-1", warnings);
        Assert.assertEquals(0d, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("ShoulderOD", attributes, "100", warnings);
        Assert.assertEquals(100d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, component.getAftShoulderRadius(), 0.001);
        handler.closeElement("ShoulderOD", attributes, "foo", warnings);
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
        handler.endHandler("NoseCone", attributes, null, warnings);
        Assert.assertTrue(component.getMaterial().getName().contains("Some Material"));
     }

    /**
     * Method: getComponent()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetComponent() throws Exception {
        Assert.assertTrue(new NoseConeHandler(null, new Stage(), new WarningSet()).getComponent() instanceof NoseCone);
    }

    /**
     * Method: getMaterialType()
     *
     * @throws Exception thrown if something goes awry
     */
    @Test
    public void testGetMaterialType() throws Exception {
        Assert.assertEquals(Material.Type.BULK, new NoseConeHandler(null, new Stage(), new WarningSet()).getMaterialType());
    }
}
