package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.MassObject;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Class that models a Rocksim MassObject.
 */
@XmlRootElement(name = RockSimCommonConstants.MASS_OBJECT)
@XmlAccessorType(XmlAccessType.FIELD)
public class MassObjectDTO extends BasePartDTO {

    @XmlElement(name = RockSimCommonConstants.TYPE_CODE)
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
        setCalcMass(0d);
        setCalcCG(0d);
        setKnownCG(getXb());
        setUseKnownCG(1);
    }
}
