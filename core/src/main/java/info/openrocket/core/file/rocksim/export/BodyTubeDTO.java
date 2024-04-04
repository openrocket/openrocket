package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.TubeFinSet;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the XML element for a RockSim body tube.
 */
@XmlRootElement(name = RockSimCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
public class BodyTubeDTO extends BasePartDTO implements AttachableParts {

    @XmlElement(name = RockSimCommonConstants.OD)
    private double od = 0d;
    @XmlElement(name = RockSimCommonConstants.ID)
    private double id = 0d;
    @XmlElement(name = RockSimCommonConstants.IS_MOTOR_MOUNT)
    private int isMotorMount = 0;
    @XmlElement(name = RockSimCommonConstants.MOTOR_DIA)
    private double motorDia = 0d;
    @XmlElement(name = RockSimCommonConstants.ENGINE_OVERHANG)
    private double engineOverhang = 0d;
    @XmlElement(name = RockSimCommonConstants.IS_INSIDE_TUBE)
    private int isInsideTube = 0;
    @XmlElementWrapper(name = RockSimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = InnerBodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.TRANSITION, type = TransitionDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.RING, type = CenteringRingDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.LAUNCH_LUG, type = LaunchLugDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.FIN_SET, type = FinSetDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.CUSTOM_FIN_SET, type = CustomFinSetDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.TUBE_FIN_SET, type = TubeFinSetDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.STREAMER, type = StreamerDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.PARACHUTE, type = ParachuteDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.MASS_OBJECT, type = MassObjectDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.EXTERNAL_POD, type = PodSetDTO.class) })
    List<BasePartDTO> attachedParts = new ArrayList<BasePartDTO>();

    /**
     * Constructor.
     */
    public BodyTubeDTO() {
    }

    /**
     * Copy constructor.
     *
     * @param theORInnerTube an OR inner tube; used by subclasses
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

        setEngineOverhang(theORBodyTube.getMotorOverhang() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setID(theORBodyTube.getInnerRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOD(theORBodyTube.getOuterRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorDia((theORBodyTube.getMotorMountDiameter() / 2) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorMount(theORBodyTube.isMotorMount());

        List<RocketComponent> children = theORBodyTube.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponent = children.get(i);
            if (rocketComponent instanceof InnerTube) {
                final InnerTube innerTube = (InnerTube) rocketComponent;
                final InnerBodyTubeDTO innerBodyTubeDTO = new InnerBodyTubeDTO(innerTube, this);
                //Only add the inner tube if it is NOT a cluster.
                if (innerTube.getInstanceCount() == 1) {
                    addAttachedPart(innerBodyTubeDTO);
                }
            } else if (rocketComponent instanceof BodyTube) {
                addAttachedPart(new BodyTubeDTO((BodyTube) rocketComponent));
            } else if (rocketComponent instanceof Transition) {
                addAttachedPart(new TransitionDTO((Transition) rocketComponent));
            } else if (rocketComponent instanceof EngineBlock) {
                addAttachedPart(new EngineBlockDTO((EngineBlock) rocketComponent));
            } else if (rocketComponent instanceof TubeCoupler) {
                addAttachedPart(new TubeCouplerDTO((TubeCoupler) rocketComponent, this));
            } else if (rocketComponent instanceof CenteringRing) {
                addAttachedPart(new CenteringRingDTO((CenteringRing) rocketComponent));
            } else if (rocketComponent instanceof Bulkhead) {
                addAttachedPart(new BulkheadDTO((Bulkhead) rocketComponent));
            } else if (rocketComponent instanceof LaunchLug) {
                addAttachedPart(new LaunchLugDTO((LaunchLug) rocketComponent));
            } else if (rocketComponent instanceof Streamer) {
                addAttachedPart(new StreamerDTO((Streamer) rocketComponent));
            } else if (rocketComponent instanceof Parachute) {
                addAttachedPart(new ParachuteDTO((Parachute) rocketComponent));
            } else if (rocketComponent instanceof MassObject) {
                addAttachedPart(new MassObjectDTO((MassObject) rocketComponent));
            } else if (rocketComponent instanceof FreeformFinSet) {
                addAttachedPart(new CustomFinSetDTO((FreeformFinSet) rocketComponent));
            } else if (rocketComponent instanceof FinSet) {
                addAttachedPart(new FinSetDTO((FinSet) rocketComponent));
            } else if (rocketComponent instanceof TubeFinSet) {
                addAttachedPart(new TubeFinSetDTO((TubeFinSet) rocketComponent));
            } else if (rocketComponent instanceof PodSet) {
                for (PodSetDTO podSetDTO : PodSetDTO.generatePodSetDTOs((PodSet) rocketComponent)) {
                    addAttachedPart(podSetDTO);
                }
            } else if (rocketComponent instanceof ParallelStage) {
                for (ParallelStageDTO parallelStageDTO : ParallelStageDTO.generateParallelStageDTOs((ParallelStage) rocketComponent)) {
                    addAttachedPart(parallelStageDTO);
                }
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
    public void addAttachedPart(BasePartDTO part) {
        if (!attachedParts.contains(part)) {
            attachedParts.add(part);
        }
    }

    @Override
    public void removeAttachedPart(BasePartDTO part) {
        attachedParts.remove(part);
    }
}