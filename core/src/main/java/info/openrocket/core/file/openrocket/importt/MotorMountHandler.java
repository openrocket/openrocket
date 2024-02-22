package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

class MotorMountHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final MotorMount mount;
	private MotorHandler motorHandler;
	private IgnitionConfigurationHandler ignitionConfigHandler;

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

		if (element.equals("ignitionconfiguration")) {
			ignitionConfigHandler = new IgnitionConfigurationHandler(context);
			return ignitionConfigHandler;
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

		// DEBUG ONLY
		// System.err.println("closing MotorMount element: "+ element);

		if (element.equals("motor")) {
			FlightConfigurationId fcid = new FlightConfigurationId(attributes.get("configid"));
			if (!fcid.isValid()) {
				warnings.add(Warning.fromString("Illegal motor specification, ignoring."));
				return;
			}
			Motor motor = motorHandler.getMotor(warnings);
			MotorConfiguration motorConfig = new MotorConfiguration(mount, fcid, mount.getDefaultMotorConfig());
			motorConfig.setMotor(motor);
			motorConfig.setEjectionDelay(motorHandler.getDelay(warnings));

			mount.setMotorConfig(motorConfig, fcid);

			Rocket rkt = ((RocketComponent) mount).getRocket();
			rkt.createFlightConfiguration(fcid);
			rkt.getFlightConfiguration(fcid).addMotor(motorConfig);
			return;
		}

		if (element.equals("ignitionconfiguration")) {
			FlightConfigurationId fcid = new FlightConfigurationId(attributes.get("configid"));
			if (!fcid.isValid()) {
				warnings.add(Warning.fromString("Illegal motor specification, ignoring."));
				return;
			}

			MotorConfiguration inst = mount.getMotorConfig(fcid);
			inst.setIgnitionDelay(ignitionConfigHandler.ignitionDelay);
			inst.setIgnitionEvent(ignitionConfigHandler.ignitionEvent);
			return;
		}

		if (element.equals("ignitionevent")) {
			IgnitionEvent event = null;
			for (IgnitionEvent ie : IgnitionEvent.values()) {
				if (ie.equals(content)) {
					event = ie;
					break;
				}
			}
			if (event == null) {
				warnings.add(Warning.fromString("Unknown ignition event type '" + content + "', ignoring."));
				return;
			}

			mount.getDefaultMotorConfig().setIgnitionEvent(event);

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

			mount.getDefaultMotorConfig().setIgnitionDelay(d);
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