package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.MassObject;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Class that models a Rocksim MassObject.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.MASS_OBJECT)
public class MassObjectDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.TYPE_CODE)
    private final int typeCode = 0;

    /**
     * Default constructor.
     */
    public MassObjectDTO() {
    }

    /**
     * Typed copy constructor.
     *
     * @param mo OR MassObject
     */
    public MassObjectDTO(MassObject mo) {
        super(mo);
        setRadialAngle(mo.getRadialDirection());
        setRadialLoc(mo.getRadialPosition() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setCalcMass(0.0d);
        setCalcCG(0.0d);
        setKnownCG(getXb());
        setUseKnownCG(1);
    }
}
