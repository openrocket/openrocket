package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimNoseConeCode;
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
 * Transition to a Rocksim Transition.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractTransitionDTO extends BasePartDTO implements AttachableParts {

    @XmlElement(name = RocksimCommonConstants.SHAPE_CODE)
    private int shapeCode = 1;
    @XmlElement(name = RocksimCommonConstants.CONSTRUCTION_TYPE)
    private int constructionType = 1;
    @XmlElement(name = RocksimCommonConstants.WALL_THICKNESS)
    private double wallThickness = 0d;
    @XmlElement(name = RocksimCommonConstants.SHAPE_PARAMETER)
    private double shapeParameter = 0d;

    @XmlElementWrapper(name = RocksimCommonConstants.ATTACHED_PARTS)
    @XmlElementRefs({
            @XmlElementRef(name = RocksimCommonConstants.BODY_TUBE, type = BodyTubeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.BODY_TUBE, type = InnerBodyTubeDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.FIN_SET, type = FinSetDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.CUSTOM_FIN_SET, type = CustomFinSetDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.RING, type = CenteringRingDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.STREAMER, type = StreamerDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.PARACHUTE, type = ParachuteDTO.class),
            @XmlElementRef(name = RocksimCommonConstants.MASS_OBJECT, type = MassObjectDTO.class)})
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
        setShapeCode(RocksimNoseConeCode.toCode(nc.getType()));

        if (Transition.Shape.POWER.equals(nc.getType()) ||
                Transition.Shape.HAACK.equals(nc.getType()) ||
                Transition.Shape.PARABOLIC.equals(nc.getType())) {
            setShapeParameter(nc.getShapeParameter());
        }

        setWallThickness(nc.getThickness() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        List<RocketComponent> children = nc.getChildren();
        for (int i = 0; i < children.size(); i++) {
            RocketComponent rocketComponents = children.get(i);
            if (rocketComponents instanceof InnerTube) {
                attachedParts.add(new InnerBodyTubeDTO((InnerTube) rocketComponents, this));
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
        attachedParts.add(part);
    }

    @Override
    public void removeAttachedPart(BasePartDTO part) {
        attachedParts.remove(part);
    }
}
