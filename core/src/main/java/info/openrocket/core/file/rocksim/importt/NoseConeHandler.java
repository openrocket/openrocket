/*
 * NoseConeHandler.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimFinishCode;
import info.openrocket.core.file.rocksim.RockSimNoseConeCode;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;

import info.openrocket.core.util.MathUtil;
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
	 * The RockSim shape code of the nose cone.
	 */
	private RockSimNoseConeCode shapeCode = null;

	/**
	 * The wall thickness. Used for hollow nose cones.
	 */
	private double thickness = 0.0d;

	/**
	 * The length of the straight section between the nose cone and its shoulder.
	 */
	private double baseExtensionLength = 0.0d;

	/**
	 * The diameter and length of the core drilled into a solid nose cone.
	 */
	private double coreDiameter = 0.0d;
	private double coreLength = 0.0d;

	/**
	 * The parent component of the nose cone, or null if the nose cone could not be added to it.
	 */
	private RocketComponent parent = null;

	/**
	 * Constructor.
	 *
	 * @param c        the parent component to the nosecone
	 * @param warnings the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public NoseConeHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings)
			throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a nose cone may not be null.");
		}
		if (isCompatible(c, NoseCone.class, warnings)) {
			c.addChild(noseCone);
			noseCone.setAftRadiusAutomatic(false);
			parent = c;
		}
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		// Nose cones in Rocksim may have attached parts - namely Mass Objects - as
		// children.
		if (RockSimCommonConstants.ATTACHED_PARTS.equals(element)) {
			return new AttachedPartsHandler(context, noseCone);
		}
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		super.closeElement(element, attributes, content, warnings);

		try {
			if (RockSimCommonConstants.SHAPE_CODE.equals(element)) {
				shapeCode = RockSimNoseConeCode.fromCode(Integer.parseInt(content));
				noseCone.setShapeType(shapeCode.asOpenRocket());
				if (!Double.isNaN(shapeCode.getOpenRocketShapeParameter())) {
					noseCone.setShapeParameter(shapeCode.getOpenRocketShapeParameter());
				}
				if (shapeCode == RockSimNoseConeCode.SECANT_OGIVE) {
					warnings.add("The secant ogive nose cone was imported as a tangent ogive.");
				}
			}
			if (RockSimCommonConstants.LEN.equals(element)) {
				noseCone.setLength(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.BASE_DIA.equals(element)) {
				noseCone.setAftRadius(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RockSimCommonConstants.WALL_THICKNESS.equals(element)) {
				thickness = Math.max(0,
						Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.SHOULDER_OD.equals(element)) {
				noseCone.setAftShoulderRadius(Math.max(0, Double.parseDouble(
						content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS));
			}
			if (RockSimCommonConstants.SHOULDER_LEN.equals(element)) {
				noseCone.setAftShoulderLength(Math.max(0, Double.parseDouble(
						content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.CORE_DIA.equals(element)) {
				coreDiameter = Math.max(0, Double.parseDouble(content));
			}
			if (RockSimCommonConstants.CORE_LEN.equals(element)) {
				coreLength = Math.max(0, Double.parseDouble(content));
			}
			if (RockSimCommonConstants.BASE_EXTENSION_LEN.equals(element)) {
				baseExtensionLength = Math.max(0, Double.parseDouble(content)
						/ RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.SHAPE_PARAMETER.equals(element)) {
				// The Rocksim ShapeParameter only applies to certain shapes, although it is
				// included
				// in the design file for all nose cones. Applying it when it should not be
				// causes oddities so
				// a check is made for the allowable shapes.
				boolean impliedByShapeCode = shapeCode != null && !Double.isNaN(shapeCode.getOpenRocketShapeParameter())
						&& shapeCode.asOpenRocket().equals(noseCone.getShapeType());
				if (!impliedByShapeCode && (Transition.Shape.POWER.equals(noseCone.getShapeType()) ||
						Transition.Shape.HAACK.equals(noseCone.getShapeType()) ||
						Transition.Shape.PARABOLIC.equals(noseCone.getShapeType()))) {
					double shapeParameter = Double.parseDouble(content);
					noseCone.setShapeParameter(shapeParameter);
					if (!MathUtil.equals(noseCone.getShapeParameter(), shapeParameter)) {
						warnings.add(String.format("The shape parameter %s of nose cone '%s' is outside OpenRocket's range "
								+ "and was changed to %s.", content, noseCone.getName(), noseCone.getShapeParameter()));
					}
				}
			}
			if (RockSimCommonConstants.CONSTRUCTION_TYPE.equals(element)) {
				int typeCode = Integer.parseInt(content);
				if (typeCode == 0) {
					// SOLID
					noseCone.setFilled(true);
				} else if (typeCode == 1) {
					// HOLLOW
					noseCone.setFilled(false);
				}
			}
			if (RockSimCommonConstants.FINISH_CODE.equals(element)) {
				noseCone.setFinish(RockSimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket());
			}
			if (RockSimCommonConstants.MATERIAL.equals(element)) {
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
		} else {
			noseCone.setThickness(thickness);
			noseCone.setAftShoulderThickness(thickness);
		}

		if (noseCone.isFilled() && coreDiameter > 0 && coreLength > 0) {
			warnings.add("The drilled core of nose cone '" + noseCone.getName()
					+ "' is not supported; its mass and CG are those of a solid nose cone.");
		}

		if (baseExtensionLength > 0 && parent != null) {
			addBaseExtension(warnings);
		}
	}

	/**
	 * OpenRocket nose cones have no straight base extension, so it is imported as a straight transition behind the
	 * nose cone, which takes over the nose cone's shoulder.
	 *
	 * @param warnings the warning set
	 */
	private void addBaseExtension(WarningSet warnings) {
		Transition extension = new Transition();
		extension.setName(noseCone.getName() + " base extension");
		extension.setShapeType(Transition.Shape.CONICAL);
		extension.setLength(baseExtensionLength);
		extension.setForeRadiusAutomatic(false);
		extension.setAftRadiusAutomatic(false);
		extension.setForeRadius(noseCone.getAftRadius());
		extension.setAftRadius(noseCone.getAftRadius());
		extension.setFilled(noseCone.isFilled());
		extension.setThickness(noseCone.getThickness());
		extension.setMaterial(noseCone.getMaterial());
		extension.setFinish(noseCone.getFinish());

		extension.setAftShoulderRadius(noseCone.getAftShoulderRadius());
		extension.setAftShoulderLength(noseCone.getAftShoulderLength());
		extension.setAftShoulderThickness(noseCone.getAftShoulderThickness());
		extension.setAftShoulderCapped(noseCone.isAftShoulderCapped());
		noseCone.setAftShoulderLength(0);

		// A known nose cone mass already includes its base extension
		if (noseCone.isMassOverridden()) {
			extension.setMassOverridden(true);
			extension.setOverrideMass(0);
		}

		parent.addChild(extension, parent.getChildPosition(noseCone) + 1);
		warnings.add("The base extension of nose cone '" + noseCone.getName()
				+ "' was imported as a separate transition, '" + extension.getName() + "'.");
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
	@Override
	public Material.Type getMaterialType() {
		return Material.Type.BULK;
	}

}
