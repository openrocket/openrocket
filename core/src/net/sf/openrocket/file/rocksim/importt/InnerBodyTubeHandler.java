/*
 * InnerBodyTubeHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.xml.sax.SAXException;

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
		if (RocksimCommonConstants.ATTACHED_PARTS.equals(element)) {
			return new AttachedPartsHandler(context, bodyTube);
		}
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		
		try {
			if (RocksimCommonConstants.OD.equals(element)) {
				bodyTube.setOuterRadius(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
			}
			if (RocksimCommonConstants.ID.equals(element)) {
				final double r = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS;
				bodyTube.setInnerRadius(r);
			}
			if (RocksimCommonConstants.LEN.equals(element)) {
				bodyTube.setLength(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RocksimCommonConstants.IS_MOTOR_MOUNT.equals(element)) {
				bodyTube.setMotorMount("1".equals(content));
			}
			if (RocksimCommonConstants.ENGINE_OVERHANG.equals(element)) {
				bodyTube.setMotorOverhang(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RocksimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
			if (RocksimCommonConstants.RADIAL_ANGLE.equals(element)) {
				bodyTube.setRadialDirection(Double.parseDouble(content));
			}
			if (RocksimCommonConstants.RADIAL_LOC.equals(element)) {
				bodyTube.setRadialPosition(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
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
	 * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not 
	 * public in all components.
	 * 
	 * @param position  the OpenRocket position
	 */
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		bodyTube.setRelativePosition(position);
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
