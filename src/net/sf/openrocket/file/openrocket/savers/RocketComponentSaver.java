package net.sf.openrocket.file.openrocket.savers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.LineStyle;


public class RocketComponentSaver {

	protected RocketComponentSaver() {
		// Prevent instantiation from outside the package
	}

	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		elements.add("<name>" + RocketSaver.escapeXML(c.getName()) + "</name>");


		// Save color and line style if significant
		if (!(c instanceof Rocket || c instanceof ComponentAssembly)) {
			Color color = c.getColor();
			if (color != null) {
				elements.add("<color red=\"" + color.getRed() + "\" green=\"" + color.getGreen() 
						+ "\" blue=\"" + color.getBlue() + "\"/>");
			}

			LineStyle style = c.getLineStyle();
			if (style != null) {
				// Type names currently equivalent to the enum names except for case.
				elements.add("<linestyle>" + style.name().toLowerCase() + "</linestyle>");
			}
		}


		// Save position unless "AFTER"
		if (c.getRelativePosition() != RocketComponent.Position.AFTER) {
			// The type names are currently equivalent to the enum names except for case.
			String type = c.getRelativePosition().name().toLowerCase();
			elements.add("<position type=\"" + type + "\">" + c.getPositionValue() + "</position>");
		}


		// Overrides
		boolean overridden = false;
		if (c.isMassOverridden()) {
			elements.add("<overridemass>" + c.getOverrideMass() + "</overridemass>");
			overridden = true;
		}
		if (c.isCGOverridden()) {
			elements.add("<overridecg>" + c.getOverrideCGX() + "</overridecg>");
			overridden = true;
		}
		if (overridden) {
			elements.add("<overridesubcomponents>" + c.getOverrideSubcomponents()
					+ "</overridesubcomponents>");
		}


		// Comment
		if (c.getComment().length() > 0) {
			elements.add("<comment>" + RocketSaver.escapeXML(c.getComment()) + "</comment>");
		}

	}




	protected final String materialParam(Material mat) {
		return materialParam("material", mat);
	}


	protected final String materialParam(String tag, Material mat) {
		String str = "<" + tag;

		switch (mat.getType()) {
		case LINE:
			str += " type=\"line\"";
			break;
		case SURFACE:
			str += " type=\"surface\"";
			break;
		case BULK:
			str += " type=\"bulk\"";
			break;
		default:
			throw new BugException("Unknown material type: " + mat.getType());
		}

		return str + " density=\"" + mat.getDensity() + "\">" + RocketSaver.escapeXML(mat.getName()) + "</"+tag+">";
	}


	protected final List<String> motorMountParams(MotorMount mount) {
		if (!mount.isMotorMount())
			return Collections.emptyList();

		String[] motorConfigIDs = ((RocketComponent) mount).getRocket().getMotorConfigurationIDs();
		List<String> elements = new ArrayList<String>();

		elements.add("<motormount>");

		for (String id : motorConfigIDs) {
			Motor motor = mount.getMotor(id);

			// Nothing is stored if no motor loaded
			if (motor == null)
				continue;

			elements.add("  <motor configid=\"" + id + "\">");
			if (motor.getMotorType() != Motor.Type.UNKNOWN) {
				elements.add("    <type>" + motor.getMotorType().name().toLowerCase() + "</type>");
			}
			elements.add("    <manufacturer>" + RocketSaver.escapeXML(motor.getManufacturer().getSimpleName()) + "</manufacturer>");
			elements.add("    <designation>" + RocketSaver.escapeXML(motor.getDesignation()) + "</designation>");
			elements.add("    <diameter>" + motor.getDiameter() + "</diameter>");
			elements.add("    <length>" + motor.getLength() + "</length>");
			
			// Motor delay
			if (mount.getMotorDelay(id) == Motor.PLUGGED) {
				elements.add("    <delay>none</delay>");
			} else {
				elements.add("    <delay>" + mount.getMotorDelay(id) + "</delay>");
			}

			elements.add("  </motor>");
		}

		elements.add("  <ignitionevent>"
				+ mount.getIgnitionEvent().name().toLowerCase().replace("_", "")
				+ "</ignitionevent>");

		elements.add("  <ignitiondelay>" + mount.getIgnitionDelay() + "</ignitiondelay>");
		elements.add("  <overhang>" + mount.getMotorOverhang() + "</overhang>");
		
		elements.add("</motormount>");

		return elements;
	}

}
