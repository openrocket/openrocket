/*
 * TransitionHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimFinishCode;
import net.sf.openrocket.file.rocksim.RocksimNoseConeCode;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;

import org.xml.sax.SAXException;

/**
 * The SAX handler for Transition components.
 */
class TransitionHandler extends BaseHandler<Transition> {
	/**
	 * The OpenRocket Transition.
	 */
	private final Transition transition = new Transition();
	
	/**
	 * The wall thickness.  Used for hollow nose cones.
	 */
	private double thickness = 0d;
	
	/**
	 * Constructor.
	 *
	 * @param c the parent component
	 * @param warnings  the warning set
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public TransitionHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent of a transition may not be null.");
		}
		if (isCompatible(c, Transition.class, warnings)) {
			c.addChild(transition);
		}
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RocksimCommonConstants.ATTACHED_PARTS.equals(element)) {
			return new AttachedPartsHandler(context, transition);
		}
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		
		try {
			if ("ShapeCode".equals(element)) {
				transition.setType(RocksimNoseConeCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
			if ("Len".equals(element)) {
				transition.setLength(Math.max(0, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if ("FrontDia".equals(element)) {
				transition.setForeRadius(Math.max(0, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if ("RearDia".equals(element)) {
				transition.setAftRadius(Math.max(0, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if ("WallThickness".equals(element)) {
				thickness = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if ("FrontShoulderDia".equals(element)) {
				transition.setForeShoulderRadius(Math.max(0d, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if ("RearShoulderDia".equals(element)) {
				transition.setAftShoulderRadius(Math.max(0d, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if ("FrontShoulderLen".equals(element)) {
				transition.setForeShoulderLength(Math.max(0d, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if ("RearShoulderLen".equals(element)) {
				transition.setAftShoulderLength(Math.max(0d, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if ("ShapeParameter".equals(element)) {
				if (Transition.Shape.POWER.equals(transition.getType()) ||
						Transition.Shape.HAACK.equals(transition.getType()) ||
						Transition.Shape.PARABOLIC.equals(transition.getType())) {
					transition.setShapeParameter(Double.parseDouble(content));
				}
			}
			if ("ConstructionType".equals(element)) {
				int typeCode = Integer.parseInt(content);
				if (typeCode == 0) {
					//SOLID
					transition.setFilled(true);
				}
				else if (typeCode == 1) {
					//HOLLOW
					transition.setFilled(false);
				}
			}
			if ("FinishCode".equals(element)) {
				transition.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
			if ("Material".equals(element)) {
				setMaterialName(content);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.endHandler(element, attributes, content, warnings);
		
		if (transition.isFilled()) {
			transition.setAftShoulderThickness(transition.getAftShoulderRadius());
			transition.setForeShoulderThickness(transition.getForeShoulderRadius());
		}
		else {
			transition.setThickness(thickness);
			transition.setAftShoulderThickness(thickness);
			transition.setForeShoulderThickness(thickness);
		}
	}
	
	
	@Override
	public Transition getComponent() {
		return transition;
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
