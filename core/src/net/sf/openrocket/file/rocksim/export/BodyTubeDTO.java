package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.TubeFinSet;

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
 * Models the XML element for a Rocksim body tube.
 */
@XmlRootElement(name = RocksimCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
public class BodyTubeDTO extends BasePartDTO implements AttachableParts {

    @XmlElement(name = RocksimCommonConstants.OD)
    private double od = 0d;
    @XmlElement(name = RocksimCommonConstants.ID)
    private double id = 0d;
    @XmlElement(name = RocksimCommonConstants.IS_MOTOR_MOUNT)
    private int isMotorMount = 0;
    @XmlElement(name = RocksimCommonConstants.MOTOR_DIA)
    private double motorDia = 0d;
    @XmlElement(name = RocksimCommonConstants.ENGINE_OVERHANG)
    private double engineOverhang = 0d;
    @XmlElement(name = RocksimCommonConstants.IS_INSIDE_TUBE)
    private int isInsideTube = 0;
    @XmlElementWrapper(name = RocksimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RocksimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.BODY_TUBE, type = InnerBodyTubeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.RING, type = CenteringRingDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.LAUNCH_LUG, type = LaunchLugDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.FIN_SET, type = FinSetDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.CUSTOM_FIN_SET, type = CustomFinSetDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.TUBE_FIN_SET, type = TubeFinSetDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.STREAMER, type = StreamerDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.PARACHUTE, type = ParachuteDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.MASS_OBJECT, type = MassObjectDTO.class)})
    List<BasePartDTO> attachedParts = new ArrayList();

    /**
     * Constructor.
     */
    public BodyTubeDTO() {
    }

    /**
     * Copy constructor.
     *
     * @param theORInnerTube  an OR inner tube; used by subclasses
     */
    protected BodyTubeDTO(InnerTube theORInnerTube) {
        super(theORInnerTube);
    }

    /**
     * Copy constructor.
     *
     * @param theORBodyTube an OR body tube
     */
    protected BodyTubeDTO(BodyTube theORBodyTube) {
        super(theORBodyTube);

        setEngineOverhang(theORBodyTube.getMotorOverhang() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setID(theORBodyTube.getInnerRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOD(theORBodyTube.getOuterRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorDia((theORBodyTube.getMotorMountDiameter() / 2) * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorMount(theORBodyTube.isMotorMount());

        List<RocketComponent> children = theORBodyTube.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
            if (rocketComponents instanceof InnerTube) {
                final InnerTube innerTube = (InnerTube) rocketComponents;
                final InnerBodyTubeDTO innerBodyTubeDTO = new InnerBodyTubeDTO(innerTube, this);
                //Only add the inner tube if it is NOT a cluster.
                if (innerTube.getClusterCount() == 1) {
                    attachedParts.add(innerBodyTubeDTO);
                }
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
            } else if (rocketComponents instanceof LaunchLug) {
                attachedParts.add(new LaunchLugDTO((LaunchLug) rocketComponents));
            } else if (rocketComponents instanceof Streamer) {
                attachedParts.add(new StreamerDTO((Streamer) rocketComponents));
            } else if (rocketComponents instanceof Parachute) {
                attachedParts.add(new ParachuteDTO((Parachute) rocketComponents));
            } else if (rocketComponents instanceof MassObject) {
                attachedParts.add(new MassObjectDTO((MassObject) rocketComponents));
            } else if (rocketComponents instanceof FreeformFinSet) {
                attachedParts.add(new CustomFinSetDTO((FreeformFinSet) rocketComponents));
            } else if (rocketComponents instanceof FinSet) {
                attachedParts.add(new FinSetDTO((FinSet) rocketComponents));
            } else if (rocketComponents instanceof TubeFinSet) {
                attachedParts.add(new TubeFinSetDTO((TubeFinSet) rocketComponents));
            }
        }
    }

    public double getOD() {
        return od;
    }

    public void setOD(double theOd) {
        od = theOd;
    }

    public double getID() {
        return id;
    }

    public void setID(double theId) {
        id = theId;
    }

    public int getMotorMount() {
        return isMotorMount;
    }

    public void setMotorMount(boolean motorMount) {
        if (motorMount) {
            isMotorMount = 1;
        } else {
            isMotorMount = 0;
        }

    }

    public void setMotorMount(int theMotorMount) {
        isMotorMount = theMotorMount;
    }

    public double getMotorDia() {
        return motorDia;
    }

    public void setMotorDia(double theMotorDia) {
        motorDia = theMotorDia;
    }

    public double getEngineOverhang() {
        return engineOverhang;
    }

    public void setEngineOverhang(double theEngineOverhang) {
        engineOverhang = theEngineOverhang;
    }

    public int getInsideTube() {
        return isInsideTube;
    }

    public void setInsideTube(boolean inside) {
        if (inside) {
            isInsideTube = 1;
        } else {
            isInsideTube = 0;
        }
    }

    public void setInsideTube(int theInsideTube) {
        isInsideTube = theInsideTube;
    }

    public List<BasePartDTO> getAttachedParts() {
        return attachedParts;
    }

    @Override
    public void addAttachedPart(BasePartDTO thePart) {
        attachedParts.add(thePart);
    }

    @Override
    public void removeAttachedPart(BasePartDTO part) {
        attachedParts.remove(part);
    }
}