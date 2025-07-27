/*
 * LaunchLugHandler.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimFinishCode;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.RocketComponent;

/**
 * The SAX handler for Rocksim Launch Lugs.
 */
class LaunchLugHandler extends PositionDependentHandler<LaunchLug> {

	/**
	 * The OpenRocket LaunchLug instance.
	 */
	private final LaunchLug lug;

	/**
	 * Constructor.
	 *
	 * @param c        the parent
	 * @param warnings the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public LaunchLugHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings)
			throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a launch lug may not be null.");
		}
		lug = new LaunchLug();
		if (isCompatible(c, LaunchLug.class, warnings)) {
			c.addChild(lug);
		}
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);

		try {
			if (RockSimCommonConstants.OD.equals(element)) {
				lug.setOuterRadius(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RockSimCommonConstants.ID.equals(element)) {
				lug.setInnerRadius(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RockSimCommonConstants.LEN.equals(element)) {
				lug.setLength(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
			if (RockSimCommonConstants.RADIAL_ANGLE.equals(element)) {
				lug.setAngleOffset(Double.parseDouble(content));
			}
			if (RockSimCommonConstants.FINISH_CODE.equals(element)) {
				lug.setFinish(RockSimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}

	/**
	 * Get the LaunchLug component this handler is working upon.
	 * 
	 * @return a LaunchLug component
	 */
	@Override
	public LaunchLug getComponent() {
		return lug;
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
