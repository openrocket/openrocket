package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.rocketcomponent.MassObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that models a Rocksim MassObject.
 */
@XmlRootElement(name = RockSimCommonConstants.MASS_OBJECT)
@XmlAccessorType(XmlAccessType.FIELD)
public class MassObjectDTO extends BasePartDTO{

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
     * @param mo  OR MassObject
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
