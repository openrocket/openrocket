package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class ParallelStageDTO extends PodSetDTO {

	/**
	 * Default constructor.
	 */
	protected ParallelStageDTO() {
	}

	/**
	 * Copy constructor.  Fully populates this instance with values taken from the OR PodSet.
	 * This constructor should not be called directly.  Instead, use {@link #generateParallelStageDTOs}.
	 *
	 * @param theORParallelStage the single-instance OR ParallelStage
	 */
	protected ParallelStageDTO(ParallelStage theORParallelStage) {
		this(theORParallelStage, null);
	}

	/**
	 * Copy constructor used while exporting a complete document.
	 *
	 * @param theORParallelStage the single-instance OR ParallelStage
	 * @param context            per-export motor-mount mapping state
	 */
	protected ParallelStageDTO(ParallelStage theORParallelStage, RockSimExportContext context) {
		super(theORParallelStage, context);
		setDetachable(1);
		setEjected(0);
	}

	/**
	 * Generate a set of ParallelStageDTOs from the given OR ParallelStage.
	 * RockSim only allows single-instance ParallelStages, so we need to generate a set of them.
	 * @param theORParallelStage the OR ParallelStage
	 * @return the set of ParallelStageDTOs
	 */
	public static ParallelStageDTO[] generateParallelStageDTOs(ParallelStage theORParallelStage) {
		return generateParallelStageDTOs(theORParallelStage, null);
	}

	/**
	 * Generate physical RockSim parallel stages while retaining links to their
	 * source components for motor-mount references.
	 *
	 * @param theORParallelStage the OR ParallelStage
	 * @param context            per-export motor-mount mapping state
	 * @return the set of ParallelStageDTOs
	 */
	public static ParallelStageDTO[] generateParallelStageDTOs(ParallelStage theORParallelStage,
			RockSimExportContext context) {
		ParallelStageDTO[] set = new ParallelStageDTO[theORParallelStage.getInstanceCount()];
		int i = 0;
		for (RocketComponent stageInstance : theORParallelStage.splitInstances(false)) {
			if (context != null) {
				context.registerSplitComponentTree(theORParallelStage, stageInstance);
			}
			set[i] = new ParallelStageDTO((ParallelStage) stageInstance, context);
			i++;
		}
		return set;
	}
}
