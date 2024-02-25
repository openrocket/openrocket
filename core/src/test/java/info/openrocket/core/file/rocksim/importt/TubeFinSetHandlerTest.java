package info.openrocket.core.file.rocksim.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import org.junit.jupiter.api.Assertions;

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
    @org.junit.jupiter.api.Test
    public void testAsOpenRocket() throws Exception {

        WarningSet warnings = new WarningSet();
        TubeFinSetHandler handler = new TubeFinSetHandler(null, new BodyTube(), warnings);

        HashMap<String, String> attributes = new HashMap<>();

        handler.closeElement("Name", attributes, "The name", warnings);
        handler.closeElement("TubeCount", attributes, "4", warnings);
        handler.closeElement("RadialAngle", attributes, ".123", warnings);

        TubeFinSet fins = handler.getComponent();
        Assertions.assertNotNull(fins);
        Assertions.assertEquals(0, warnings.size());

        Assertions.assertEquals(fins.getName(), "The name");
        Assertions.assertEquals(4, fins.getFinCount());

        Assertions.assertEquals(.123d, fins.getBaseRotation(), 0d);

        handler.closeElement("OD", attributes, "-1", warnings);
        Assertions.assertEquals(0d, fins.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "0", warnings);
        Assertions.assertEquals(0d, fins.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "75", warnings);
        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, fins.getOuterRadius(), 0.001);
        handler.closeElement("OD", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("ID", attributes, "-1", warnings);
        Assertions.assertEquals(0d, fins.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "0", warnings);
        Assertions.assertEquals(0d, fins.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "75", warnings);
        Assertions.assertEquals(75d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS, fins.getInnerRadius(), 0.001);
        handler.closeElement("ID", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("Len", attributes, "-1", warnings);
        Assertions.assertEquals(0d, fins.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, fins.getLength(), 0.001);
        handler.closeElement("Len", attributes, "10.0", warnings);
        Assertions.assertEquals(10d / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH, fins.getLength(), 0.001);
        handler.closeElement("Len", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

        handler.closeElement("FinishCode", attributes, "-1", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, fins.getFinish());
        handler.closeElement("FinishCode", attributes, "100", warnings);
        Assertions.assertEquals(ExternalComponent.Finish.NORMAL, fins.getFinish());
        handler.closeElement("FinishCode", attributes, "foo", warnings);
        Assertions.assertEquals(1, warnings.size());
        warnings.clear();

    }

}
