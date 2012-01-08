package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.rocketcomponent.TubeCoupler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "Ring")
@XmlAccessorType(XmlAccessType.FIELD)
public class TubeCouplerDTO extends CenteringRingDTO {

    public TubeCouplerDTO(TubeCoupler tc) {
        super(tc);
        setUsageCode(UsageCode.TubeCoupler);
    }
}
