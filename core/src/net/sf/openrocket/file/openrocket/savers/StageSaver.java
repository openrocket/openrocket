package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
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

		if (stage.getStageNumber() > 0) {
			elements.addAll( separationConfig( stage.getDefaultFlightConfiguration(), false));

			Rocket rocket = stage.getRocket();
			// Note - getFlightConfigurationIDs returns at least one element.  The first element
			// is null and means "default".
			String[] configs = rocket.getFlightConfigurationIDs();
			if ( configs.length > 1 ) {

				for( String id : configs ) {
					if ( id == null ) {
						continue;
					}
					StageSeparationConfiguration config = stage.getFlightConfiguration(id);
					if ( config == null ) {
						continue;
					}
					elements.add("<separationconfiguration configid=\"" + id + "\">");
					elements.addAll( separationConfig(config, true) );
					elements.add("</separationconfiguration>");
				}
			}
		}
	}

	private List<String> separationConfig( StageSeparationConfiguration config, boolean indent ) {
		List<String> elements = new ArrayList<String>(2);
		elements.add((indent?"    ":"")+ "<separationevent>"
				+ config.getSeparationEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "")
				+ "</separationevent>");
		elements.add((indent?"    ":"")+ "<separationdelay>" + config.getSeparationDelay() + "</separationdelay>");
		return elements;

	}
}
