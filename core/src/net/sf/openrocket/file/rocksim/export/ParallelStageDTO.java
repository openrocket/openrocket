package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.rocketcomponent.ParallelStage;

public class ParallelStageDTO extends PodSetDTO {

	/**
	 * Default constructor.
	 */
	protected ParallelStageDTO() {
	}

	/**
	 * Copy constructor.  Fully populates this instance with values taken from the OR PodSet.
	 *
	 * @param theORParallelStage
	 */
	protected ParallelStageDTO(ParallelStage theORParallelStage, double angle) {
		super(theORParallelStage, angle);
		setDetachable(true);
		setEjected(false);
	}

	/**
	 * Generate a set of ParallelStageDTOs from the given OR ParallelStage.
	 * RockSim only allows single-instance ParallelStages, so we need to generate a set of them.
	 * @param theORParallelStage the OR ParallelStage
	 * @return the set of ParallelStageDTOs
	 */
	public static ParallelStageDTO[] generateParallelStageDTOs(ParallelStage theORParallelStage) {
		ParallelStageDTO[] set = new ParallelStageDTO[theORParallelStage.getInstanceCount()];
		int i = 0;
		for (double angle : theORParallelStage.getInstanceAngles()) {
			set[i] = new ParallelStageDTO(theORParallelStage, angle);
			i++;
		}
		return set;
	}
}
