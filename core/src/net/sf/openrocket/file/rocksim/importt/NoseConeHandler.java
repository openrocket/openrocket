/*
 * NoseConeHandler.java
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
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;

import org.xml.sax.SAXException;

/**
 * The SAX nose cone handler for Rocksim NoseCones.
 */
class NoseConeHandler extends BaseHandler<NoseCone> {
	
	/**
	 * The OpenRocket NoseCone.
	 */
	private final NoseCone noseCone = new NoseCone();
	
	/**
	 * The wall thickness.  Used for hollow nose cones.  
	 */
	private double thickness = 0d;
	
	/**
	 * Constructor.
	 *
	 * @param c the parent component to the nosecone
	 * @param warnings  the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public NoseConeHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a nose cone may not be null.");
		}
		if (isCompatible(c, NoseCone.class, warnings)) {
			c.addChild(noseCone);
			noseCone.setAftRadiusAutomatic(false);
		}
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		//Nose cones in Rocksim may have attached parts - namely Mass Objects - as children.
		if (RocksimCommonConstants.ATTACHED_PARTS.equals(element)) {
			return new AttachedPartsHandler(context, noseCone);
		}
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		
		try {
			if (RocksimCommonConstants.SHAPE_CODE.equals(element)) {
				noseCone.setType(RocksimNoseConeCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
			if (RocksimCommonConstants.LEN.equals(element)) {
				noseCone.setLength(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RocksimCommonConstants.BASE_DIA.equals(element)) {
				noseCone.setAftRadius(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RocksimCommonConstants.WALL_THICKNESS.equals(element)) {
				thickness = Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RocksimCommonConstants.SHOULDER_OD.equals(element)) {
				noseCone.setAftShoulderRadius(Math.max(0, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RocksimCommonConstants.SHOULDER_LEN.equals(element)) {
				noseCone.setAftShoulderLength(Math.max(0, Double.parseDouble(
						content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RocksimCommonConstants.SHAPE_PARAMETER.equals(element)) {
				//The Rocksim ShapeParameter only applies to certain shapes, although it is included
				//in the design file for all nose cones.  Applying it when it should not be causes oddities so 
				//a check is made for the allowable shapes.
				if (Transition.Shape.POWER.equals(noseCone.getType()) ||
						Transition.Shape.HAACK.equals(noseCone.getType()) ||
						Transition.Shape.PARABOLIC.equals(noseCone.getType())) {
					noseCone.setShapeParameter(Double.parseDouble(content));
				}
			}
			if (RocksimCommonConstants.CONSTRUCTION_TYPE.equals(element)) {
				int typeCode = Integer.parseInt(content);
				if (typeCode == 0) {
					//SOLID
					noseCone.setFilled(true);
				}
				else if (typeCode == 1) {
					//HOLLOW
					noseCone.setFilled(false);
				}
			}
			if (RocksimCommonConstants.FINISH_CODE.equals(element)) {
				noseCone.setFinish(RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
			if (RocksimCommonConstants.MATERIAL.equals(element)) {
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
		
		if (noseCone.isFilled()) {
			noseCone.setAftShoulderThickness(noseCone.getAftShoulderRadius());
		}
		else {
			noseCone.setThickness(thickness);
			noseCone.setAftShoulderThickness(thickness);
		}
	}
	
	/**
	 * Get the nose cone component this handler is working upon.
	 *
	 * @return a nose cone component
	 */
	@Override
	public NoseCone getComponent() {
		return noseCone;
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
