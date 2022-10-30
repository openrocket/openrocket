package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
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
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversion DTO for a TubeCoupler.  TubeCoupler's are represented as Rings in Rocksim.
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
     * @param parent the attached parts (subcomponents in RockSim speak) of the TubeCoupler's parent.  This instance
     *               is a member of those attached parts, as well as all sibling components.  This is passed in the
     *               event that the tube coupler is a cluster.  In that situation this instance will be removed and
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
