package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;

public class StageSaver extends ComponentAssemblySaver {
	
	private static final StageSaver instance = new StageSaver();
	
	public static ArrayList<String> getElements(net.sf.openrocket.rocketcomponent.RocketComponent c) {
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("<stage>");
		instance.addParams(c, list);
		list.add("</stage>");
		
		return list;
	}
	
	@Override
	protected void addParams(RocketComponent c, List<String> elements) {
		super.addParams(c, elements);
		Stage stage = (Stage) c;
		
		if (stage.getOutside()) {
			elements.addAll(this.addStageReplicationParams(stage));
		}
		
		if (stage.getStageNumber() > 0) {
			// NOTE:  Default config must be BEFORE overridden config for proper backward compatibility later on
			elements.addAll(separationConfig(stage.getStageSeparationConfiguration().getDefault(), false));
			
			Rocket rocket = stage.getRocket();
			// Note - getFlightConfigurationIDs returns at least one element.  The first element
			// is null and means "default".
			String[] configs = rocket.getFlightConfigurationIDs();
			if (configs.length > 1) {
				
				for (String id : configs) {
					if (id == null) {
						continue;
					}
					if (stage.getStageSeparationConfiguration().isDefault(id)) {
						continue;
					}
					StageSeparationConfiguration config = stage.getStageSeparationConfiguration().get(id);
					elements.add("<separationconfiguration configid=\"" + id + "\">");
					elements.addAll(separationConfig(config, true));
					elements.add("</separationconfiguration>");
					
				}
			}
		}
	}
	
	private Collection<? extends String> addStageReplicationParams(final Stage currentStage) {
		List<String> elementsToReturn = new ArrayList<String>();
		
		if (null != currentStage) {
			
			boolean outsideFlag = currentStage.getOutside();
			elementsToReturn.add("<outside=\"" + outsideFlag + "\">");
			int instanceCount = currentStage.getInstanceCount();
			elementsToReturn.add("<instanceCount=\"" + instanceCount + "\">");
			double radialOffset = currentStage.getRadialPosition();
			elementsToReturn.add("<radialOffset=\"" + radialOffset + "\">");
			double angularOffset = currentStage.getAngularPosition();
			elementsToReturn.add("<angleOffset=\"" + angularOffset + "\">");
			
			// Save position unless "AFTER"
			if (currentStage.getRelativePosition() != RocketComponent.Position.AFTER) {
				// The type names are currently equivalent to the enum names except for case.
				String type = currentStage.getRelativePositionMethod().name().toLowerCase(Locale.ENGLISH);
				double axialOffset = currentStage.getAxialPosition();
				elementsToReturn.add("<position type=\"" + type + "\">" + axialOffset + "</position>");
				int relativeTo = currentStage.getRelativeToStage();
				elementsToReturn.add("<relativeTo=\"" + relativeTo + "\">");
			}
			
			// do not save
			double angularSeparation = Double.NaN; // doesn't need to be saved b/c it's derived from instanceCount 
		}
		
		return elementsToReturn;
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