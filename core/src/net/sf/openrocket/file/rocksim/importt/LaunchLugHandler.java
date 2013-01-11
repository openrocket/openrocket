/*
 * LaunchLugHandler.java
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
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.xml.sax.SAXException;

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
	 * @param c the parent
	 * @param warnings  the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public LaunchLugHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
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
			if (RocksimCommonConstants.OD.equals(element)) {
				lug.setOuterRadius(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RocksimCommonConstants.ID.equals(element)) {
				lug.setInnerRadius(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RocksimCommonConstants.LEN.equals(element)) {
				lug.setLength(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RocksimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
			if (RocksimCommonConstants.RADIAL_ANGLE.equals(element)) {
				lug.setRadialDirection(Double.parseDouble(content));
			}
			if (RocksimCommonConstants.FINISH_CODE.equals(element)) {
				lug.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
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
	 * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not 
	 * public in all components.
	 * 
	 * @param position  the OpenRocket position
	 */
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		lug.setRelativePosition(position);
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
