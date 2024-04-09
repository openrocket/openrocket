package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversion DTO for a TubeCoupler. TubeCoupler's are represented as Rings in
 * Rocksim.
 */
@XmlRootElement(name = RockSimCommonConstants.RING)
@XmlAccessorType(XmlAccessType.FIELD)
public class TubeCouplerDTO extends CenteringRingDTO {

    /**
     * Constructor.
     *
     * @param tc an OR TubeCoupler
     */
    public TubeCouplerDTO(TubeCoupler tc) {
        super(tc);
        setUsageCode(UsageCode.TubeCoupler);
    }

    /**
     * Full copy constructor.
     *
     * @param tc     the corresponding OR tube coupler
     * @param parent the attached parts (subcomponents in RockSim speak) of the
     *               TubeCoupler's parent. This instance
     *               is a member of those attached parts, as well as all sibling
     *               components. This is passed in the
     *               event that the tube coupler is a cluster. In that situation
     *               this instance will be removed and
     *               individual instances for each cluster member will be added.
     */
    public TubeCouplerDTO(TubeCoupler tc, AttachableParts parent) {
        super(tc);
        setUsageCode(UsageCode.TubeCoupler);

        // Add this component first, then the children
        parent.addAttachedPart(this);

        for (RocketComponent component : tc.getChildren()) {
            component.setAxialMethod(AxialMethod.ABSOLUTE);
            if (component instanceof InnerTube) {
                parent.addAttachedPart(new InnerBodyTubeDTO((InnerTube) component, parent));
            } else if (component instanceof EngineBlock) {
                parent.addAttachedPart(new EngineBlockDTO((EngineBlock) component));
            } else if (component instanceof TubeCoupler) {
                new TubeCouplerDTO((TubeCoupler) component, parent);
            } else if (component instanceof CenteringRing) {
                parent.addAttachedPart(new CenteringRingDTO((CenteringRing) component));
            } else if (component instanceof Bulkhead) {
                parent.addAttachedPart(new BulkheadDTO((Bulkhead) component));
            } else if (component instanceof Parachute) {
                parent.addAttachedPart(new ParachuteDTO((Parachute) component));
            } else if (component instanceof Streamer) {
                parent.addAttachedPart(new StreamerDTO((Streamer) component));
            } else if (component instanceof MassObject) {
                parent.addAttachedPart(new MassObjectDTO((MassObject) component));
            }
        }
    }
}
