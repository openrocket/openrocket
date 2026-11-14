package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.swing.gui.figure3d.animation.FlightPoseProvider;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A simulation's flight data prepared for the replay, independent of any UI: a trajectory per
 * simulation branch mapped to the stages it carries, the separately flying bodies, the flight
 * events, each stage's motor burns, and which stages fly together and in what phase over time.
 *
 * <p>OpenRocket copies a branch's pre-separation history from its parent, so each branch
 * describes its stages' whole flight and a stage can simply follow its branch throughout.
 */
public final class FlightReplayData {
	private final Map<AxialStage, PoseProvider> providersByStage;
	private final List<AxialStage> stages;
	private final Map<Integer, List<FlightEvent>> eventsByBranchStage;
	private final PoseProvider primaryProvider;
	private final double startTime;
	private final double endTime;
	private final List<FlightEvent> allEvents;
	private final Map<UUID, Double> groundHitByDeploymentId;
	private final Map<AxialStage, List<BurnInterval>> burnIntervalsByStage;
	private final List<BurnInterval> burnIntervals;
	private final List<FlightBody> flightBodies;

	public FlightReplayData(FlightData data, Rocket rocket) {
		if (data == null) {
			throw new IllegalArgumentException("flight data is null");
		}
		if (rocket == null) {
			throw new IllegalArgumentException("rocket is null");
		}
		if (data.getBranchCount() == 0) {
			throw new IllegalArgumentException("flight data has no branches");
		}

		List<BranchProvider> branchProviders = createBranchProviders(data);
		this.primaryProvider = branchProviders.get(0).provider();
		List<AxialStage> sortedStages = new ArrayList<>(rocket.getStageList());
		sortedStages.sort(Comparator.comparingInt(AxialStage::getStageNumber));
		this.stages = List.copyOf(sortedStages);
		this.providersByStage = Map.copyOf(mapProvidersToStages(stages, branchProviders));
		this.flightBodies = List.copyOf(collectFlightBodies(stages, providersByStage, branchProviders));
		this.eventsByBranchStage = Collections.unmodifiableMap(collectEventsByBranchStage(branchProviders));
		this.startTime = branchProviders.stream()
				.mapToDouble(branch -> branch.provider().getStartTime())
				.min()
				.orElse(primaryProvider.getStartTime());
		this.endTime = branchProviders.stream()
				.mapToDouble(branch -> branch.provider().getEndTime())
				.max()
				.orElse(primaryProvider.getEndTime());
		this.allEvents = List.copyOf(collectEvents(data));
		this.groundHitByDeploymentId = Map.copyOf(collectDeploymentGroundHits(data));
		this.burnIntervalsByStage = Collections.unmodifiableMap(collectBurnIntervalsByStage(rocket, allEvents, endTime));
		this.burnIntervals = List.copyOf(mergeIntervals(
				burnIntervalsByStage.values().stream().flatMap(List::stream).toList()));
	}

	/** Each stage's trajectory: the branch of the nearest stage at or above it that has one. */
	public Map<AxialStage, PoseProvider> getProvidersByStage() {
		return providersByStage;
	}

	/** The separately flying bodies, primary (branch 0) first. A single-stage rocket has one. */
	public List<FlightBody> getFlightBodies() {
		return flightBodies;
	}

	/** The first branch's trajectory: the sustainer's, which every stage rides until it separates. */
	public PoseProvider getPrimaryProvider() {
		return primaryProvider;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	/** Every branch's events once each, in flight order. */
	public List<FlightEvent> getAllEvents() {
		return allEvents;
	}

	/** When the stage that deployed the given recovery device lands, or the fallback if it never does. */
	public double getGroundHitTime(FlightEvent deployment, double fallback) {
		if (deployment == null) {
			return fallback;
		}
		return groundHitByDeploymentId.getOrDefault(deployment.getID(), fallback);
	}

	/** Each stage's motor burns, from ignition to burnout, overlapping burns merged. */
	public Map<AxialStage, List<BurnInterval>> getBurnIntervalsByStage() {
		return burnIntervalsByStage;
	}

	public List<BurnInterval> getBurnIntervals() {
		return burnIntervals;
	}

	/** Times at which a stage separates, ascending and without duplicates. */
	public List<Double> getSeparationTimes() {
		return allEvents.stream()
				.filter(event -> event.getType() == FlightEvent.Type.STAGE_SEPARATION)
				.map(FlightEvent::getTime)
				.distinct()
				.sorted()
				.toList();
	}

	/** The stages attached to the given stage at the given time, including itself. */
	public List<AxialStage> getAttachedStages(AxialStage stage, double time) {
		for (List<AxialStage> group : connectedStageGroups(time)) {
			if (group.contains(stage)) {
				return group;
			}
		}
		return List.of(stage);
	}

	/** Returns the current flight phase of each connected group of stages. */
	public List<StageStatus> getStageStatuses(double time) {
		List<StageStatus> statuses = new ArrayList<>();
		for (List<AxialStage> group : connectedStageGroups(time)) {
			statuses.add(new StageStatus(group, phaseFor(group, time)));
		}
		return List.copyOf(statuses);
	}

	private static List<BranchProvider> createBranchProviders(FlightData data) {
		List<BranchProvider> providers = new ArrayList<>(data.getBranchCount());
		for (FlightDataBranch branch : data.getBranches()) {
			providers.add(new BranchProvider(data.getStageNr(branch), FlightPoseProvider.fromFlightDataBranch(branch),
					List.copyOf(branch.getEvents())));
		}
		providers.sort(Comparator.comparingInt(BranchProvider::stageNumber));
		return providers;
	}

	private static Map<AxialStage, PoseProvider> mapProvidersToStages(List<AxialStage> stages,
			List<BranchProvider> branchProviders) {
		Map<AxialStage, PoseProvider> result = new LinkedHashMap<>();
		for (AxialStage stage : stages) {
			result.put(stage, findProviderForStage(stage.getStageNumber(), branchProviders));
		}
		return result;
	}

	private static List<FlightBody> collectFlightBodies(List<AxialStage> stages,
			Map<AxialStage, PoseProvider> providersByStage, List<BranchProvider> branchProviders) {
		List<FlightBody> bodies = new ArrayList<>();
		for (BranchProvider branch : branchProviders) {
			List<AxialStage> members = stages.stream()
					.filter(stage -> providersByStage.get(stage) == branch.provider())
					.toList();
			if (!members.isEmpty()) {
				bodies.add(new FlightBody(branch.stageNumber(), members, branch.provider()));
			}
		}
		return bodies;
	}

	private static Map<Integer, List<FlightEvent>> collectEventsByBranchStage(
			List<BranchProvider> branchProviders) {
		Map<Integer, List<FlightEvent>> result = new LinkedHashMap<>();
		for (BranchProvider branch : branchProviders) {
			result.put(branch.stageNumber(), branch.events());
		}
		return result;
	}

	private static PoseProvider findProviderForStage(int stageNumber, List<BranchProvider> branchProviders) {
		BranchProvider selected = branchProviders.get(0);
		for (BranchProvider branch : branchProviders) {
			if (branch.stageNumber() <= stageNumber && branch.stageNumber() >= selected.stageNumber()) {
				selected = branch;
			}
		}
		return selected.provider();
	}

	private static List<FlightEvent> collectEvents(FlightData data) {
		Map<UUID, FlightEvent> byId = new LinkedHashMap<>();
		for (FlightDataBranch branch : data.getBranches()) {
			for (FlightEvent event : branch.getEvents()) {
				byId.putIfAbsent(event.getID(), event);
			}
		}

		List<FlightEvent> events = new ArrayList<>(byId.values());
		events.sort(FlightEvent::compareTo);
		return events;
	}

	private static Map<UUID, Double> collectDeploymentGroundHits(FlightData data) {
		Map<UUID, Double> result = new LinkedHashMap<>();
		for (FlightDataBranch branch : data.getBranches()) {
			List<FlightEvent> events = new ArrayList<>(branch.getEvents());
			events.sort(FlightEvent::compareTo);
			for (FlightEvent event : events) {
				if (event.getType() != FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
					continue;
				}
				for (FlightEvent later : events) {
					if (later.getType() == FlightEvent.Type.GROUND_HIT && later.getTime() >= event.getTime()) {
						result.putIfAbsent(event.getID(), later.getTime());
						break;
					}
				}
			}
		}
		return result;
	}

	private static Map<AxialStage, List<BurnInterval>> collectBurnIntervalsByStage(Rocket rocket,
			List<FlightEvent> events, double replayEndTime) {
		List<AxialStage> stages = new ArrayList<>(rocket.getStageList());
		stages.sort(Comparator.comparingInt(AxialStage::getStageNumber));

		Map<AxialStage, BurnState> statesByStage = new HashMap<>();
		for (AxialStage stage : stages) {
			statesByStage.put(stage, new BurnState());
		}

		for (FlightEvent event : events) {
			if (event.getType() != FlightEvent.Type.IGNITION && event.getType() != FlightEvent.Type.BURNOUT) {
				continue;
			}
			AxialStage stage = stageFor(event.getSource());
			if (stage == null) {
				continue;
			}
			BurnState state = statesByStage.computeIfAbsent(stage, ignored -> new BurnState());
			if (event.getType() == FlightEvent.Type.IGNITION) {
				state.ignite(event.getTime());
			} else {
				state.burnout(event.getTime());
			}
		}

		Map<AxialStage, List<BurnInterval>> result = new LinkedHashMap<>();
		for (AxialStage stage : stages) {
			BurnState state = statesByStage.get(stage);
			if (state == null) {
				result.put(stage, List.of());
				continue;
			}
			state.closeOpenBurn(replayEndTime);
			result.put(stage, List.copyOf(mergeIntervals(state.intervals)));
		}
		return result;
	}

	private static AxialStage stageFor(RocketComponent source) {
		if (source == null) {
			return null;
		}
		if (source instanceof AxialStage stage) {
			return stage;
		}
		try {
			return source.getStage();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	private List<List<AxialStage>> connectedStageGroups(double time) {
		if (stages.isEmpty()) {
			return List.of();
		}
		java.util.Set<Integer> separatedBoundaries = new java.util.HashSet<>();
		for (FlightEvent event : allEvents) {
			if (event.getType() != FlightEvent.Type.STAGE_SEPARATION || event.getTime() > time) {
				continue;
			}
			AxialStage separatedStage = stageFor(event.getSource());
			if (separatedStage != null) {
				separatedBoundaries.add(separatedStage.getStageNumber());
			}
		}

		List<List<AxialStage>> groups = new ArrayList<>();
		List<AxialStage> current = new ArrayList<>();
		for (AxialStage stage : stages) {
			if (!current.isEmpty() && separatedBoundaries.contains(stage.getStageNumber())) {
				groups.add(List.copyOf(current));
				current.clear();
			}
			current.add(stage);
		}
		if (!current.isEmpty()) {
			groups.add(List.copyOf(current));
		}
		return groups;
	}

	private FlightPhase phaseFor(List<AxialStage> group, double time) {
		List<FlightEvent> branchEvents = eventsForStage(group.get(0).getStageNumber());
		if (eventOccurred(branchEvents, FlightEvent.Type.GROUND_HIT, time)) {
			return FlightPhase.LANDED;
		}
		if (eventOccurred(branchEvents, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, time)) {
			return FlightPhase.RECOVERY;
		}
		if (eventOccurred(branchEvents, FlightEvent.Type.TUMBLE, time)) {
			return FlightPhase.TUMBLING;
		}
		for (AxialStage stage : group) {
			for (BurnInterval interval : burnIntervalsByStage.getOrDefault(stage, List.of())) {
				if (interval.contains(time)) {
					return FlightPhase.UNDER_THRUST;
				}
			}
		}
		if (!eventOccurred(allEvents, FlightEvent.Type.LIFTOFF, time)) {
			return FlightPhase.ON_PAD;
		}
		return FlightPhase.COASTING;
	}

	private List<FlightEvent> eventsForStage(int stageNumber) {
		List<FlightEvent> selected = eventsByBranchStage.values().iterator().next();
		int selectedStage = Integer.MIN_VALUE;
		for (Map.Entry<Integer, List<FlightEvent>> entry : eventsByBranchStage.entrySet()) {
			if (entry.getKey() <= stageNumber && entry.getKey() >= selectedStage) {
				selected = entry.getValue();
				selectedStage = entry.getKey();
			}
		}
		return selected;
	}

	private static boolean eventOccurred(List<FlightEvent> events, FlightEvent.Type type, double time) {
		for (FlightEvent event : events) {
			if (event.getType() == type && event.getTime() <= time) {
				return true;
			}
		}
		return false;
	}

	private static List<BurnInterval> mergeIntervals(List<BurnInterval> intervals) {
		if (intervals.isEmpty()) {
			return List.of();
		}
		List<BurnInterval> sorted = new ArrayList<>(intervals);
		sorted.sort(Comparator.comparingDouble(BurnInterval::start));
		List<BurnInterval> merged = new ArrayList<>();
		for (BurnInterval interval : sorted) {
			if (interval.end() < interval.start()) {
				continue;
			}
			if (merged.isEmpty()) {
				merged.add(interval);
				continue;
			}
			BurnInterval last = merged.get(merged.size() - 1);
			if (interval.start() <= last.end()) {
				merged.set(merged.size() - 1,
						new BurnInterval(last.start(), Math.max(last.end(), interval.end())));
			} else {
				merged.add(interval);
			}
		}
		return merged;
	}

	public record BurnInterval(double start, double end) {
		public boolean contains(double time) {
			return time >= start && time <= end;
		}
	}

	public enum FlightPhase {
		ON_PAD,
		UNDER_THRUST,
		COASTING,
		RECOVERY,
		TUMBLING,
		LANDED
	}

	public record StageStatus(List<AxialStage> stages, FlightPhase phase) {
		public StageStatus {
			stages = List.copyOf(stages);
		}
	}

	private static final class BurnState {
		private final List<BurnInterval> intervals = new ArrayList<>();
		private int activeBurns;
		private double activeStart;

		private void ignite(double time) {
			if (activeBurns == 0) {
				activeStart = time;
			}
			activeBurns++;
		}

		private void burnout(double time) {
			if (activeBurns <= 0) {
				return;
			}
			activeBurns--;
			if (activeBurns == 0) {
				intervals.add(new BurnInterval(activeStart, time));
			}
		}

		private void closeOpenBurn(double replayEndTime) {
			if (activeBurns > 0) {
				intervals.add(new BurnInterval(activeStart, replayEndTime));
				activeBurns = 0;
			}
		}
	}

	/**
	 * A part of the rocket that flies on its own simulation branch after separation: the stages
	 * sharing that branch's trajectory. Before separation every body rides the whole stack.
	 */
	public record FlightBody(int branchIndex, List<AxialStage> stages, PoseProvider provider) {
		public FlightBody {
			stages = List.copyOf(stages);
		}
	}

	private record BranchProvider(int stageNumber, PoseProvider provider, List<FlightEvent> events) {
	}
}
