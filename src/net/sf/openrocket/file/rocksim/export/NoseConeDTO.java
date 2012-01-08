package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 */
@XmlRootElement(name = "NoseCone")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends AbstractTransitionDTO {


    @XmlElement(name = "BaseDia")
    private double baseDia = 0d;
    @XmlElement(name = "ShoulderLen")
    private double shoulderLen = 0d;
    @XmlElement(name = "ShoulderOD")
    private double shoulderOD = 0d;

    @XmlElementWrapper(name = "AttachedParts")
    @XmlElementRefs({
            @XmlElementRef(name = "BodyTube", type = BodyTubeDTO.class),
            @XmlElementRef(name = "BodyTube", type = InnerBodyTubeDTO.class),
            @XmlElementRef(name = "FinSet", type = FinSetDTO.class),
            @XmlElementRef(name = "CustomFinSet", type = CustomFinSetDTO.class),
            @XmlElementRef(name = "Ring", type = CenteringRingDTO.class),
            @XmlElementRef(name = "Parachute", type = ParachuteDTO.class),
            @XmlElementRef(name = "MassObject", type = MassObjectDTO.class)})
    List<BasePartDTO> attachedParts = new ArrayList<BasePartDTO>();

    public NoseConeDTO() {
    }

    public NoseConeDTO(NoseCone nc) {
        super(nc);
        setBaseDia(nc.getAftRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setShoulderLen(nc.getAftShoulderLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setShoulderOD(nc.getAftShoulderRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);

        List<RocketComponent> children = nc.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
            if (rocketComponents instanceof InnerTube) {
                attachedParts.add(new InnerBodyTubeDTO((InnerTube) rocketComponents));
            } else if (rocketComponents instanceof BodyTube) {
                attachedParts.add(new BodyTubeDTO((BodyTube) rocketComponents));
            } else if (rocketComponents instanceof Transition) {
                attachedParts.add(new TransitionDTO((Transition) rocketComponents));
            } else if (rocketComponents instanceof EngineBlock) {
                attachedParts.add(new EngineBlockDTO((EngineBlock) rocketComponents));
            } else if (rocketComponents instanceof TubeCoupler) {
                attachedParts.add(new TubeCouplerDTO((TubeCoupler) rocketComponents));
            } else if (rocketComponents instanceof CenteringRing) {
                attachedParts.add(new CenteringRingDTO((CenteringRing) rocketComponents));
            } else if (rocketComponents instanceof Bulkhead) {
                attachedParts.add(new BulkheadDTO((Bulkhead) rocketComponents));
            } else if (rocketComponents instanceof Parachute) {
                attachedParts.add(new ParachuteDTO((Parachute) rocketComponents));
            } else if (rocketComponents instanceof MassObject) {
                attachedParts.add(new MassObjectDTO((MassObject) rocketComponents));
            } else if (rocketComponents instanceof FreeformFinSet) {
                attachedParts.add(new CustomFinSetDTO((FreeformFinSet) rocketComponents));
            } else if (rocketComponents instanceof FinSet) {
                attachedParts.add(new FinSetDTO((FinSet) rocketComponents));
            }
        }
    }

    public double getBaseDia() {
        return baseDia;
    }

    public void setBaseDia(double theBaseDia) {
        baseDia = theBaseDia;
    }

    public double getShoulderLen() {
        return shoulderLen;
    }

    public void setShoulderLen(double theShoulderLen) {
        shoulderLen = theShoulderLen;
    }

    public double getShoulderOD() {
        return shoulderOD;
    }

    public void setShoulderOD(double theShoulderOD) {
        shoulderOD = theShoulderOD;
    }
}
