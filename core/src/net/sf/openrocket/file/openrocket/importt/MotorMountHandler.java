package net.sf.openrocket.file.openrocket.importt;

import java.util.HashMap;
import java.util.Locale;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;

import org.xml.sax.SAXException;

class MotorMountHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final MotorMount mount;
	private MotorHandler motorHandler;
	
	public MotorMountHandler(MotorMount mount, DocumentLoadingContext context) {
		this.mount = mount;
		this.context = context;
		mount.setMotorMount(true);
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		
		if (element.equals("motor")) {
			motorHandler = new MotorHandler(context);
			return motorHandler;
		}
		
		if (element.equals("ignitionevent") ||
				element.equals("ignitiondelay") ||
				element.equals("overhang")) {
			return PlainTextHandler.INSTANCE;
		}
		
		warnings.add(Warning.fromString("Unknown element '" + element + "' encountered, ignoring."));
		return null;
	}
	
	
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		
		if (element.equals("motor")) {
			String id = attributes.get("configid");
			if (id == null || id.equals("")) {
				warnings.add(Warning.fromString("Illegal motor specification, ignoring."));
				return;
			}
			
			Motor motor = motorHandler.getMotor(warnings);
			mount.setMotor(id, motor);
			mount.setMotorDelay(id, motorHandler.getDelay(warnings));
			MotorConfiguration motorConfig = mount.getFlightConfiguration(id);
			motorConfig.setIgnitionEvent( motorHandler.getIgnitionEvent());
			motorConfig.setIgnitionDelay( motorHandler.getIgnitionDelay());
			return;
		}
		
		if (element.equals("ignitionevent")) {
			MotorConfiguration.IgnitionEvent event = null;
			for (MotorConfiguration.IgnitionEvent e : MotorConfiguration.IgnitionEvent.values()) {
				if (e.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "").equals(content)) {
					event = e;
					break;
				}
			}
			if (event == null) {
				warnings.add(Warning.fromString("Unknown ignition event type '" + content + "', ignoring."));
				return;
			}
			mount.setDefaultIgnitionEvent(event);
			return;
		}
		
		if (element.equals("ignitiondelay")) {
			double d;
			try {
				d = Double.parseDouble(content);
			} catch (NumberFormatException nfe) {
				warnings.add(Warning.fromString("Illegal ignition delay specified, ignoring."));
				return;
			}
			mount.setDefaultIgnitionDelay(d);
			return;
		}
		
		if (element.equals("overhang")) {
			double d;
			try {
				d = Double.parseDouble(content);
			} catch (NumberFormatException nfe) {
				warnings.add(Warning.fromString("Illegal overhang specified, ignoring."));
				return;
			}
			mount.setMotorOverhang(d);
			return;
		}
		
		super.closeElement(element, attributes, content, warnings);
	}
}