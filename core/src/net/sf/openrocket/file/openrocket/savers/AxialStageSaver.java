package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BoosterSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;

public class AxialStageSaver extends ComponentAssemblySaver {
	
	private static final AxialStageSaver instance = new AxialStageSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		if (c.isCenterline()) {
			// yes, this test is redundant.  I'm merely paranoid, and attempting to future-proof it
			if (c.getClass().equals(AxialStage.class)) {
				// kept as simply 'stage' for backward compatability
				list.add("<stage>");
				instance.addParams(c, list);
				list.add("</stage>");
			}
		} else {
			if (c instanceof BoosterSet) {
				list.add("<boosterset>");
				instance.addParams(c, list);
				list.add("</boosterset>");
			}
		}
		
		return list;
	}
	
	@Override
	protected void addParams(RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		AxialStage stage = (AxialStage) c;
		
		if (stage.getStageNumber() > 0) {
			// NOTE:  Default config must be BEFORE overridden config for proper backward compatibility later on
			elements.addAll(separationConfig(stage.getSeparationConfigurations().getDefault(), false));
			
			Rocket rocket = stage.getRocket();
			// Note - getFlightConfigurationIDs returns at least one element.  The first element
			// is null and means "default".
			
			int configCount = rocket.getConfigurationSet().size();
			if (1 < configCount ){
				
				for (FlightConfiguration curConfig : rocket.getConfigurationSet()){
					FlightConfigurationID fcid = curConfig.getFlightConfigurationID();
					if (fcid == null) {
						continue;
					}
					if (stage.getSeparationConfigurations().isDefault(fcid)) {
						continue;
					}
					
					StageSeparationConfiguration separationConfig = stage.getSeparationConfigurations().get(fcid);
					elements.add("<separationconfiguration configid=\"" + fcid.key + "\">");
					elements.addAll(separationConfig(separationConfig, true));
					elements.add("</separationconfiguration>");
					
				}
			}
		}
	}
	
	private List<String> separationConfig(StageSeparationConfiguration config, boolean indent) {
		List<String> elements = new ArrayList<String>(2);
		elements.add((indent ? "    " : "") + "<separationevent>"
				+ config.getSeparationEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "")
				+ "</separationevent>");
		elements.add((indent ? "    " : "") + "<separationdelay>" + config.getSeparationDelay() + "</separationdelay>");
		return elements;
		
	}
}