/*
 * InnerBodyTubeHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RockSimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A SAX handler for Rocksim inside tubes.
 */
class InnerBodyTubeHandler extends PositionDependentHandler<InnerTube> {
	
	/**
	 * The OpenRocket InnerTube instance.
	 */
	private final InnerTube bodyTube;
	
	/**
	 * Constructor.
	 *
	 * @param c the parent component
	 * @param warnings  the warning set
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public InnerBodyTubeHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of an inner tube may not be null.");
		}
		bodyTube = new InnerTube();
		if (isCompatible(c, InnerTube.class, warnings)) {
			c.addChild(bodyTube);
		}
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RockSimCommonConstants.ATTACHED_PARTS.equals(element)) {
			return new AttachedPartsHandler(context, bodyTube);
		}
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		
		try {
			if (RockSimCommonConstants.OD.equals(element)) {
				bodyTube.setOuterRadius(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
			}
			if (RockSimCommonConstants.ID.equals(element)) {
				final double r = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS;
				bodyTube.setInnerRadius(r);
			}
			if (RockSimCommonConstants.LEN.equals(element)) {
				bodyTube.setLength(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.IS_MOTOR_MOUNT.equals(element)) {
				bodyTube.setMotorMount("1".equals(content));
			}
			if (RockSimCommonConstants.ENGINE_OVERHANG.equals(element)) {
				bodyTube.setMotorOverhang(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
			if (RockSimCommonConstants.RADIAL_ANGLE.equals(element)) {
				bodyTube.setRadialDirection(Double.parseDouble(content));
			}
			if (RockSimCommonConstants.RADIAL_LOC.equals(element)) {
				bodyTube.setRadialPosition(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	/**
	 * Get the InnerTube component this handler is working upon.
	 * 
	 * @return an InnerTube component
	 */
	@Override
	public InnerTube getComponent() {
		return bodyTube;
	}
	
	/**
	 * Get the required type of material for this component.
	 *
	 * @return BULK
	 */
	@Override
	public Material.Type getMaterialType() {
		return Material.Type.BULK;
	}
	
}
