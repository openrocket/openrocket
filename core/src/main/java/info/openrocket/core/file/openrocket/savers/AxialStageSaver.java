package info.openrocket.core.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;

public class AxialStageSaver extends ComponentAssemblySaver {

	private static final AxialStageSaver instance = new AxialStageSaver();

	public static ArrayList<String> getElements(info.openrocket.core.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<>();

		if (c.isAfter()) {
			// yes, this test is redundant. I'm merely paranoid, and attempting to
			// future-proof it
			if (c.getClass().equals(AxialStage.class)) {
				// kept as simply 'stage' for backward compatibility
				list.add("<stage>");
				instance.addParams(c, list);
				list.add("</stage>");
			}
		} else {
			if (c instanceof ParallelStage) {
				list.add("<parallelstage>");
				instance.addParams(c, list);
				list.add("</parallelstage>");
			}
		}

		return list;
	}

	@Override
	protected void addParams(RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		AxialStage stage = (AxialStage) c;

		if (stage.getStageNumber() > 0) {
			// NOTE: Default config must be BEFORE overridden config for proper backward
			// compatibility later on
			elements.addAll(addSeparationConfigParams(stage.getSeparationConfigurations().getDefault(), false));

			// Note - getFlightConfigurationIDs returns at least one element. The first
			// element
			for (FlightConfigurationId fcid : stage.getSeparationConfigurations().getIds()) {
				if (fcid == null) {
					continue;
				}
				StageSeparationConfiguration curSepCfg = stage.getSeparationConfigurations().get(fcid);

				// if( stage.getSeparationConfigurations().isDefault( curSepCfg )){
				// continue;
				// }

				elements.add("<separationconfiguration configid=\"" + fcid.key + "\">");
				elements.addAll(addSeparationConfigParams(curSepCfg, true));
				elements.add("</separationconfiguration>");
			}
		}
	}

	private List<String> addSeparationConfigParams(StageSeparationConfiguration config, boolean indent) {
		List<String> elements = new ArrayList<>(2);
		elements.add((indent ? "    " : "") + "<separationevent>"
				+ config.getSeparationEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "")
				+ "</separationevent>");
		elements.add((indent ? "    " : "") + "<separationaltitude>" + config.getSeparationAltitude() + "</separationaltitude>");
		elements.add((indent ? "    " : "") + "<separationdelay>" + config.getSeparationDelay() + "</separationdelay>");
		return elements;

	}
}
