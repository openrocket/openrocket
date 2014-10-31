package net.sf.openrocket.file.openrocket.savers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.TextUtil;

public class RocketComponentSaver {
	private static final Translator trans = Application.getTranslator();
	
	protected RocketComponentSaver() {
		// Prevent instantiation from outside the package
	}
	
	protected void addParams(net.sf.openrocket.rocketcomponent.RocketComponent c, List<String> elements) {
		elements.add("<name>" + TextUtil.escapeXML(c.getName()) + "</name>");
		
		ComponentPreset preset = c.getPresetComponent();
		if (preset != null) {
			elements.add("<preset type=\"" + preset.getType() +
					"\" manufacturer=\"" + preset.getManufacturer().getSimpleName() +
					"\" partno=\"" + preset.getPartNo() + "\" digest=\"" + preset.getDigest() + "\"/>");
		}
		
		Appearance ap = c.getAppearance();
		if (ap != null) {
			elements.add("<appearance>");
			Color paint = ap.getPaint();
			emitColor("paint", elements, paint);
			elements.add("<shine>" + ap.getShine() + "</shine>");
			Decal decal = ap.getTexture();
			if (decal != null) {
				String name = decal.getImage().getName();
				double rotation = decal.getRotation();
				EdgeMode edgeMode = decal.getEdgeMode();
				elements.add("<decal name=\"" + TextUtil.escapeXML(name) + "\" rotation=\"" + rotation + "\" edgemode=\"" + edgeMode.name() + "\">");
				Coordinate center = decal.getCenter();
				elements.add("<center x=\"" + center.x + "\" y=\"" + center.y + "\"/>");
				Coordinate offset = decal.getOffset();
				elements.add("<offset x=\"" + offset.x + "\" y=\"" + offset.y + "\"/>");
				Coordinate scale = decal.getScale();
				elements.add("<scale x=\"" + scale.x + "\" y=\"" + scale.y + "\"/>");
				elements.add("</decal>");
			}
			elements.add("</appearance>");
		}
		
		// Save color and line style if significant
		if (!(c instanceof Rocket || c instanceof ComponentAssembly)) {
			Color color = c.getColor();
			emitColor("color", elements, color);
			
			LineStyle style = c.getLineStyle();
			if (style != null) {
				// Type names currently equivalent to the enum names except for case.
				elements.add("<linestyle>" + style.name().toLowerCase(Locale.ENGLISH) + "</linestyle>");
			}
		}
		
		
		// Save position unless "AFTER"
		if (c.getRelativePosition() != RocketComponent.Position.AFTER) {
			// The type names are currently equivalent to the enum names except for case.
			String type = c.getRelativePosition().name().toLowerCase(Locale.ENGLISH);
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
			elements.add("<comment>" + TextUtil.escapeXML(c.getComment()) + "</comment>");
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
		
		String baseName = trans.getBaseText("material", mat.getName());
		
		return str + " density=\"" + mat.getDensity() + "\">" + TextUtil.escapeXML(baseName) + "</" + tag + ">";
	}
	
	
	protected final List<String> motorMountParams(MotorMount mount) {
		if (!mount.isMotorMount())
			return Collections.emptyList();
		String[] motorConfigIDs = ((RocketComponent) mount).getRocket().getFlightConfigurationIDs();
		List<String> elements = new ArrayList<String>();
		
		elements.add("<motormount>");
		
		// NOTE:  Default config must be BEFORE overridden config for proper backward compatibility later on
		elements.add("  <ignitionevent>"
				+ mount.getIgnitionConfiguration().getDefault().getIgnitionEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "")
				+ "</ignitionevent>");
		elements.add("  <ignitiondelay>" + mount.getIgnitionConfiguration().getDefault().getIgnitionDelay() + "</ignitiondelay>");
		elements.add("  <overhang>" + mount.getMotorOverhang() + "</overhang>");
		
		for (String id : motorConfigIDs) {
			MotorConfiguration motorConfig = mount.getMotorConfiguration().get(id);
			Motor motor = motorConfig.getMotor();
			// Nothing is stored if no motor loaded
			if (motor == null)
				continue;
			
			elements.add("  <motor configid=\"" + id + "\">");
			if (motor.getMotorType() != Motor.Type.UNKNOWN) {
				elements.add("    <type>" + motor.getMotorType().name().toLowerCase(Locale.ENGLISH) + "</type>");
			}
			if (motor instanceof ThrustCurveMotor) {
				ThrustCurveMotor m = (ThrustCurveMotor) motor;
				elements.add("    <manufacturer>" + TextUtil.escapeXML(m.getManufacturer().getSimpleName()) +
						"</manufacturer>");
				elements.add("    <digest>" + m.getDigest() + "</digest>");
			}
			elements.add("    <designation>" + TextUtil.escapeXML(motor.getDesignation()) + "</designation>");
			elements.add("    <diameter>" + motor.getDiameter() + "</diameter>");
			elements.add("    <length>" + motor.getLength() + "</length>");
			
			// Motor delay
			if (motorConfig.getEjectionDelay() == Motor.PLUGGED) {
				elements.add("    <delay>none</delay>");
			} else {
				elements.add("    <delay>" + motorConfig.getEjectionDelay() + "</delay>");
			}
			
			elements.add("  </motor>");
			
			if (!mount.getIgnitionConfiguration().isDefault(id)) {
				IgnitionConfiguration ignition = mount.getIgnitionConfiguration().get(id);
				elements.add("  <ignitionconfiguration configid=\"" + id + "\">");
				elements.add("    <ignitionevent>" + ignition.getIgnitionEvent().name().toLowerCase(Locale.ENGLISH).replace("_", "") + "</ignitionevent>");
				elements.add("    <ignitiondelay>" + ignition.getIgnitionDelay() + "</ignitiondelay>");
				elements.add("  </ignitionconfiguration>");
				
			}
		}
		
		elements.add("</motormount>");
		
		return elements;
	}
	
	private final static void emitColor(String elementName, List<String> elements, Color color) {
		if (color != null) {
			elements.add("<" + elementName + " red=\"" + color.getRed() + "\" green=\"" + color.getGreen()
					+ "\" blue=\"" + color.getBlue() + "\"/>");
		}
		
	}
	
}
