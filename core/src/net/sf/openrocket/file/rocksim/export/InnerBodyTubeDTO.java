package net.sf.openrocket.file.rocksim.export;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
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
import net.sf.openrocket.util.Coordinate;

/**
 * This class models the XML element for a Rocksim inside tube.
 */
@XmlRootElement(name = RocksimCommonConstants.BODY_TUBE)
@XmlAccessorType(XmlAccessType.FIELD)
public class InnerBodyTubeDTO extends BodyTubeDTO implements AttachableParts {
	
	/**
	 * Constructor.
	 */
	public InnerBodyTubeDTO() {
		super.setInsideTube(true);
	}
	
	/**
	 * Full copy constructor.
	 *
	 * @param bt     the corresponding OR inner body tube
	 * @param parent the attached parts (subcomponents in Rocksim speak) of the InnerTube's parent.  This instance
	 *               is a member of those attached parts, as well as all sibling components.  This is passed in the
	 *               event that the inner tube is a cluster.  In that situation this instance will be removed and
	 *               individual instances for each cluster member will be added.
	 */
	public InnerBodyTubeDTO(InnerTube bt, AttachableParts parent) {
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
				//Only if the inner tube is NOT a cluster, then create the corresponding Rocksim DTO and add it
				//to the list of attached parts.  If it is a cluster, then it is handled specially outside of this
				//loop.
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
	
	
	/**
	 * Handle the inner tube as a cluster.  This amounts to splitting it up so that each motor mount in the cluster
	 * is created individually to support Rocksim's view of clusters.
	 *
	 * @param it  the clustered tube
	 * @param p   the collection (parent's attached parts really) to which all cluster tubes will be added
	 */
	private void handleCluster(InnerTube it, AttachableParts p) {
		
		Coordinate[] coords = { Coordinate.NUL };
		coords = it.shiftCoordinates(coords);
		for (int x = 0; x < coords.length; x++) {
			InnerTube partialClone = InnerTube.makeIndividualClusterComponent(coords[x], it.getName() + " #" + (x + 1), it);
			p.addAttachedPart(new InnerBodyTubeDTO(partialClone, p));
		}
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
