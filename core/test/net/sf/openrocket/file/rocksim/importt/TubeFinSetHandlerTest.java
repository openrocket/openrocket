package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import org.junit.Assert;

import java.util.HashMap;

/**
 * Test for importing a Rocksim TubeFinSet into OR.
 */
public class TubeFinSetHandlerTest {

    /**
      * Method: asOpenRocket(WarningSet warnings)
      *
      * @throws Exception thrown if something goes awry
      */
     @org.junit.Test
     public void testAsOpenRocket() throws Exception {

         WarningSet warnings = new WarningSet();
         TubeFinSetHandler handler = new TubeFinSetHandler(null, new BodyTube(), warnings);

         HashMap<String, String> attributes = new HashMap<>();

         handler.closeElement("Name", attributes, "The name", warnings);
         handler.closeElement("TubeCount", attributes, "4", warnings);
         handler.closeElement("RadialAngle", attributes, ".123", warnings);

         TubeFinSet fins = handler.getComponent();
         Assert.assertNotNull(fins);
         Assert.assertEquals(0, warnings.size());

         Assert.assertEquals("The name", fins.getName());
         Assert.assertEquals(4, fins.getFinCount());

         Assert.assertEquals(.123d, fins.getBaseRotation(), 0d);

         handler.closeElement("OD", attributes, "-1", warnings);
         Assert.assertEquals(0d, fins.getOuterRadius(), 0.001);
         handler.closeElement("OD", attributes, "0", warnings);
         Assert.assertEquals(0d, fins.getOuterRadius(), 0.001);
         handler.closeElement("OD", attributes, "75", warnings);
         Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, fins.getOuterRadius(), 0.001);
         handler.closeElement("OD", attributes, "foo", warnings);
         Assert.assertEquals(1, warnings.size());
         warnings.clear();

         handler.closeElement("ID", attributes, "-1", warnings);
         Assert.assertEquals(0d, fins.getInnerRadius(), 0.001);
         handler.closeElement("ID", attributes, "0", warnings);
         Assert.assertEquals(0d, fins.getInnerRadius(), 0.001);
         handler.closeElement("ID", attributes, "75", warnings);
         Assert.assertEquals(75d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, fins.getInnerRadius(), 0.001);
         handler.closeElement("ID", attributes, "foo", warnings);
         Assert.assertEquals(1, warnings.size());
         warnings.clear();

         handler.closeElement("Len", attributes, "-1", warnings);
         Assert.assertEquals(0d, fins.getLength(), 0.001);
         handler.closeElement("Len", attributes, "10", warnings);
         Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, fins.getLength(), 0.001);
         handler.closeElement("Len", attributes, "10.0", warnings);
         Assert.assertEquals(10d / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, fins.getLength(), 0.001);
         handler.closeElement("Len", attributes, "foo", warnings);
         Assert.assertEquals(1, warnings.size());
         warnings.clear();

         handler.closeElement("FinishCode", attributes, "-1", warnings);
         Assert.assertEquals(ExternalComponent.Finish.NORMAL, fins.getFinish());
         handler.closeElement("FinishCode", attributes, "100", warnings);
         Assert.assertEquals(ExternalComponent.Finish.NORMAL, fins.getFinish());
         handler.closeElement("FinishCode", attributes, "foo", warnings);
         Assert.assertEquals(1, warnings.size());
         warnings.clear();


     }

}
