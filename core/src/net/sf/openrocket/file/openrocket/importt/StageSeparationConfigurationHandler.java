package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;

import org.xml.sax.SAXException;

class StageSeparationConfigurationHandler extends AbstractElementHandler {
	private final Stage stage;
	
	private SeparationEvent event = null;
	private double delay = Double.NaN;
	
	public StageSeparationConfigurationHandler(Stage stage, DocumentLoadingContext context) {
		this.stage = stage;
	}
	
	
	public StageSeparationConfiguration getConfiguration(StageSeparationConfiguration def) {
		StageSeparationConfiguration config = def.clone();
		if (event != null) {
			config.setSeparationEvent(event);
		}
		if (!Double.isNaN(delay)) {
			config.setSeparationDelay(delay);
		}
		return config;
	}
	
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content,
			WarningSet warnings) throws SAXException {
		
		content = content.trim();
		
		if ("separationevent".equals(element)) {
			event = (SeparationEvent) DocumentConfig.findEnum(content, SeparationEvent.class);
			if (event == null) {
				warnings.add(Warning.FILE_INVALID_PARAMETER);
				return;
			}
			return;
		} else if ("separationdelay".equals(element)) {
			delay = parseDouble(content, warnings, Warning.FILE_INVALID_PARAMETER);
			return;
		}
		super.closeElement(element, attributes, content, warnings);
		
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		String configId = attributes.get("configid");
		StageSeparationConfiguration def = stage.getStageSeparationConfiguration().getDefault();
		stage.getStageSeparationConfiguration().set(configId, getConfiguration(def));
	}
	
}