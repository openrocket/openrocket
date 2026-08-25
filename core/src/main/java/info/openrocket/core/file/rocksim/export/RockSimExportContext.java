package info.openrocket.core.file.rocksim.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;

/**
 * Holds per-export state that links OpenRocket motor mounts to the serial
 * numbers assigned to their physical RockSim components.
 * <p>
 * RockSim represents each member of a cluster or component assembly as a
 * separate component.  Consequently, one OpenRocket motor mount can map to
 * several RockSim serial numbers.
 */
final class RockSimExportContext {

	private final Map<UUID, UUID> sourceComponentIds = new HashMap<>();
	private final Map<UUID, List<Integer>> motorMountSerialNumbers = new HashMap<>();
	private final Map<UUID, List<Integer>> recoveryDeviceSerialNumbers = new HashMap<>();

	/**
	 * Record a physical motor mount emitted by the component exporter.
	 *
	 * @param mount        the component used to create the exported mount
	 * @param serialNumber the RockSim component serial number
	 */
	void registerMotorMount(MotorMount mount, int serialNumber) {
		RocketComponent component = (RocketComponent) mount;
		UUID sourceId = resolveSourceComponentId(component.getID());
		motorMountSerialNumbers.computeIfAbsent(sourceId, ignored -> new ArrayList<>()).add(serialNumber);
	}

	/**
	 * Return every physical RockSim mount created for an OpenRocket mount.
	 *
	 * @param mount the original OpenRocket motor mount
	 * @return an immutable, serial-number-ordered list
	 */
	List<Integer> getMotorMountSerialNumbers(MotorMount mount) {
		RocketComponent component = (RocketComponent) mount;
		List<Integer> serialNumbers = motorMountSerialNumbers.get(component.getID());
		if (serialNumbers == null) {
			return Collections.emptyList();
		}
		return List.copyOf(serialNumbers);
	}

	/**
	 * Record a physical recovery device emitted by the component exporter.
	 *
	 * @param component    the recovery device used to create the DTO
	 * @param serialNumber the RockSim component serial number
	 */
	void registerRecoveryDevice(RocketComponent component, int serialNumber) {
		UUID sourceId = resolveSourceComponentId(component.getID());
		recoveryDeviceSerialNumbers.computeIfAbsent(sourceId, ignored -> new ArrayList<>()).add(serialNumber);
	}

	/**
	 * Return every physical RockSim component created for a recovery device.
	 *
	 * @param component the original OpenRocket recovery device
	 * @return an immutable, serial-number-ordered list
	 */
	List<Integer> getRecoveryDeviceSerialNumbers(RocketComponent component) {
		List<Integer> serialNumbers = recoveryDeviceSerialNumbers.get(component.getID());
		if (serialNumbers == null) {
			return Collections.emptyList();
		}
		return List.copyOf(serialNumbers);
	}

	/**
	 * Associate a temporary split component tree with the source tree from which
	 * it was copied.  The exporter uses this for clustered mounts, pods, and
	 * parallel stages, all of which RockSim expands into physical components.
	 *
	 * @param source the source component tree
	 * @param copy   the temporary split copy
	 */
	void registerSplitComponentTree(RocketComponent source, RocketComponent copy) {
		sourceComponentIds.put(copy.getID(), resolveSourceComponentId(source.getID()));

		int childCount = Math.min(source.getChildCount(), copy.getChildCount());
		for (int i = 0; i < childCount; i++) {
			registerSplitComponentTree(source.getChild(i), copy.getChild(i));
		}
	}

	private UUID resolveSourceComponentId(UUID componentId) {
		UUID sourceId = sourceComponentIds.get(componentId);
		return sourceId == null ? componentId : sourceId;
	}
}
