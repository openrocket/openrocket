/*
 * BodyTubeHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimFinishCode;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.xml.sax.SAXException;

/**
 * A SAX handler for Rocksim Body Tubes.
 */
class BodyTubeHandler extends BaseHandler<BodyTube> {
	/**
	 * The OpenRocket BodyTube.
	 */
	private final BodyTube bodyTube;
	
	/**
	 * Constructor.
	 *
	 * @param c parent component
	 * @param warnings  the warning set
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public BodyTubeHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a body tube may not be null.");
		}
		bodyTube = new BodyTube();
		if (isCompatible(c, BodyTube.class, warnings)) {
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
			if (RocksimCommonConstants.FINISH_CODE.equals(element)) {
				bodyTube.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
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
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	/**
	 * Get the component this handler is working upon.
	 * 
	 * @return a component
	 */
	@Override
	public BodyTube getComponent() {
		return bodyTube;
	}
	
	/**
	 * Get the required type of material for this component.
	 *
	 * @return BULK
	 */
	public Material.Type getMaterialType() {
		return Material.Type.BULK;
	}
}
