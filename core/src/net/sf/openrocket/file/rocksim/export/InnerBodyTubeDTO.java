package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.util.Coordinate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 */
@XmlRootElement(name = RocksimCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
public class InnerBodyTubeDTO extends BodyTubeDTO implements AttachedParts {

    public InnerBodyTubeDTO() {
        super.setInsideTube(true);
    }

    public InnerBodyTubeDTO(InnerTube bt, AttachedParts parent) {
        super(bt);
        setEngineOverhang(bt.getMotorOverhang() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setID(bt.getInnerRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOD(bt.getOuterRadius() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorDia((bt.getMotorMountDiameter() / 2) * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setMotorMount(bt.isMotorMount());
        setInsideTube(true);
        setRadialAngle(bt.getRadialDirection());
        setRadialLoc(bt.getRadialPosition() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

            List<RocketComponent> children = bt.getChildren();
            for (int i = 0; i < children.size(); i++) {
                RocketComponent rocketComponents = children.get(i);
                if (rocketComponents instanceof InnerTube) {
                    final InnerTube innerTube = (InnerTube) rocketComponents;
                    if (innerTube.getClusterCount() == 1) {
                        attachedParts.add(new InnerBodyTubeDTO(innerTube, this));
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
                } else if (rocketComponents instanceof Streamer) {
                    attachedParts.add(new StreamerDTO((Streamer) rocketComponents));
                } else if (rocketComponents instanceof Parachute) {
                    attachedParts.add(new ParachuteDTO((Parachute) rocketComponents));
                } else if (rocketComponents instanceof MassObject) {
                    attachedParts.add(new MassObjectDTO((MassObject) rocketComponents));
                }
            }

        //Do the cluster.  For now this splits the cluster into separate tubes, which is how Rocksim represents it.
        //The import (from Rocksim to OR) could be augmented to be more intelligent and try to determine if the
        //co-located tubes are a cluster.
        if (bt.getClusterConfiguration().getClusterCount() > 1) {
            handleCluster(bt, parent);
            parent.removeAttachedPart(this);
        }
    }

    private void handleCluster(InnerTube it, AttachedParts p) {

        Coordinate[] coords = {Coordinate.NUL};
        coords = it.shiftCoordinates(coords);
        for (int x = 0; x < coords.length; x++) {
            InnerTube copy = (InnerTube) it.copy();
            copy.setClusterConfiguration(ClusterConfiguration.SINGLE);
            copy.setClusterRotation(0.0);
            copy.setClusterScale(1.0);
            copy.setRadialShift(coords[x].y, coords[x].z);
            copy.setName(copy.getName() + " #" + (x + 1));
            p.addAttachedPart(copy(copy, p));
        }
    }

    private InnerBodyTubeDTO copy(InnerTube it, AttachedParts p) {
        return new InnerBodyTubeDTO(it, p);
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
