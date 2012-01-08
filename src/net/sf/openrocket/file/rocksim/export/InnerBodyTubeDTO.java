package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 */
@XmlRootElement(name = "BodyTube")
@XmlAccessorType(XmlAccessType.FIELD)
public class InnerBodyTubeDTO extends BodyTubeDTO {

    public InnerBodyTubeDTO() {
        super.setInsideTube(true);
    }

    public InnerBodyTubeDTO(InnerTube bt) {
        super(bt);
        setEngineOverhang(bt.getMotorOverhang() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setId(bt.getInnerRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(bt.getOuterRadius() * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorDia((bt.getMotorMountDiameter() / 2) * RocksimHandler.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorMount(bt.isMotorMount());

        List<RocketComponent> children = bt.getChildren();
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
            } else if (rocketComponents instanceof Streamer) {
                attachedParts.add(new StreamerDTO((Streamer) rocketComponents));
            } else if (rocketComponents instanceof Parachute) {
                attachedParts.add(new ParachuteDTO((Parachute) rocketComponents));
            } else if (rocketComponents instanceof MassObject) {
                attachedParts.add(new MassObjectDTO((MassObject) rocketComponents));
            }
        }
        setInsideTube(true);
    }
}
