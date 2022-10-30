package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.file.rocksim.RockSimNoseConeCode;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassObject;
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
import java.util.ArrayList;
import java.util.List;

/**
 * A common ancestor class for nose cones and transitions.  This class is responsible for adapting an OpenRocket
 * Transition to a RockSim Transition.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractTransitionDTO extends BasePartDTO implements AttachableParts {

    @XmlElement(name = RockSimCommonConstants.SHAPE_CODE)
    private int shapeCode = 1;
    @XmlElement(name = RockSimCommonConstants.CONSTRUCTION_TYPE)
    private int constructionType = 1;
    @XmlElement(name = RockSimCommonConstants.WALL_THICKNESS)
    private double wallThickness = 0d;
    @XmlElement(name = RockSimCommonConstants.SHAPE_PARAMETER)
    private double shapeParameter = 0d;

    @XmlElementWrapper(name = RockSimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.BODY_TUBE, type = InnerBodyTubeDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.FIN_SET, type = FinSetDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.CUSTOM_FIN_SET, type = CustomFinSetDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.RING, type = CenteringRingDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.STREAMER, type = StreamerDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.PARACHUTE, type = ParachuteDTO.class),
            @XmlElementRef(name = RockSimCommonConstants.MASS_OBJECT, type = MassObjectDTO.class)})
    List<BasePartDTO> attachedParts = new ArrayList<BasePartDTO>();

    /**
     * Default constructor.
     */
    protected AbstractTransitionDTO() {
    }

    /**
     * Conversion constructor.
     *
     * @param nc  the OpenRocket component to convert
     */
    protected AbstractTransitionDTO(Transition nc) {
        super(nc);
        setConstructionType(nc.isFilled() ? 0 : 1);
        setShapeCode(RockSimNoseConeCode.toCode(nc.getType()));

        if (Transition.Shape.POWER.equals(nc.getType()) ||
                Transition.Shape.HAACK.equals(nc.getType()) ||
                Transition.Shape.PARABOLIC.equals(nc.getType())) {
            setShapeParameter(nc.getShapeParameter());
        }

        setWallThickness(nc.getThickness() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        List<RocketComponent> children = nc.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
            if (rocketComponents instanceof InnerTube) {
                addAttachedPart(new InnerBodyTubeDTO((InnerTube) rocketComponents, this));
            } else if (rocketComponents instanceof BodyTube) {
                addAttachedPart(new BodyTubeDTO((BodyTube) rocketComponents));
            } else if (rocketComponents instanceof Transition) {
                addAttachedPart(new TransitionDTO((Transition) rocketComponents));
            } else if (rocketComponents instanceof EngineBlock) {
                addAttachedPart(new EngineBlockDTO((EngineBlock) rocketComponents));
            } else if (rocketComponents instanceof TubeCoupler) {
                addAttachedPart(new TubeCouplerDTO((TubeCoupler) rocketComponents, this));
            } else if (rocketComponents instanceof CenteringRing) {
                addAttachedPart(new CenteringRingDTO((CenteringRing) rocketComponents));
            } else if (rocketComponents instanceof Bulkhead) {
                addAttachedPart(new BulkheadDTO((Bulkhead) rocketComponents));
            } else if (rocketComponents instanceof Parachute) {
                addAttachedPart(new ParachuteDTO((Parachute) rocketComponents));
            } else if (rocketComponents instanceof MassObject) {
                addAttachedPart(new MassObjectDTO((MassObject) rocketComponents));
            } else if (rocketComponents instanceof FreeformFinSet) {
                addAttachedPart(new CustomFinSetDTO((FreeformFinSet) rocketComponents));
            } else if (rocketComponents instanceof FinSet) {
                addAttachedPart(new FinSetDTO((FinSet) rocketComponents));
            }
        }
    }

    public int getShapeCode() {
        return shapeCode;
    }

    public void setShapeCode(int theShapeCode) {
        shapeCode = theShapeCode;
    }

    public int getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(int theConstructionType) {
        constructionType = theConstructionType;
    }

    public double getWallThickness() {
        return wallThickness;
    }

    public void setWallThickness(double theWallThickness) {
        wallThickness = theWallThickness;
    }

    public double getShapeParameter() {
        return shapeParameter;
    }

    public void setShapeParameter(double theShapeParameter) {
        shapeParameter = theShapeParameter;
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
