/*
 * RingHandler.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import org.xml.sax.SAXException;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TubeCoupler;

/**
 * A SAX handler for centering rings, tube couplers, and bulkheads.
 */
class RingHandler extends PositionDependentHandler<CenteringRing> {

	/**
	 * The OpenRocket Ring.
	 */
	private final CenteringRing ring = new CenteringRing();

	/**
	 * The parent component.
	 */
	private final RocketComponent parent;

	/**
	 * The parsed Rocksim UsageCode.
	 */
	private int usageCode = 0;

	/**
	 * Constructor.
	 *
	 * @param theParent the parent component
	 * @param warnings  the warning set
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public RingHandler(DocumentLoadingContext context, RocketComponent theParent, WarningSet warnings)
			throws IllegalArgumentException {
		super(context);
		if (theParent == null) {
			throw new IllegalArgumentException("The parent of a ring may not be null.");
		}
		parent = theParent;
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
				ring.setOuterRadius(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
			}
			if (RockSimCommonConstants.ID.equals(element)) {
				ring.setInnerRadius(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
			}
			if (RockSimCommonConstants.LEN.equals(element)) {
				ring.setLength(Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.MATERIAL.equals(element)) {
				setMaterialName(content);
			}
			if (RockSimCommonConstants.USAGE_CODE.equals(element)) {
				usageCode = Integer.parseInt(content);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}

	/**
	 * Get the ring component this handler is working upon.
	 *
	 * @return a component
	 */
	@Override
	public CenteringRing getComponent() {
		return ring;
	}

	/**
	 * This method adds the CenteringRing as a child of the parent rocket component.
	 *
	 * @param warnings the warning set
	 */
	public void asCenteringRing(WarningSet warnings) {

		if (isCompatible(parent, CenteringRing.class, warnings)) {
			parent.addChild(ring);
		}
	}

	/**
	 * Convert the parsed Rocksim data values in this object to an instance of
	 * OpenRocket's Bulkhead.
	 * <p/>
	 * Side Effect Warning: This method adds the resulting Bulkhead as a child of
	 * the parent rocket component!
	 *
	 * @param warnings the warning set
	 */
	public void asBulkhead(WarningSet warnings) {

		Bulkhead result = new Bulkhead();

		copyValues(result);

		if (isCompatible(parent, Bulkhead.class, warnings)) {
			parent.addChild(result);
		}
	}

	/**
	 * Convert the parsed Rocksim data values in this object to an instance of
	 * OpenRocket's TubeCoupler.
	 * <p/>
	 * Side Effect Warning: This method adds the resulting TubeCoupler as a child of
	 * the parent rocket component!
	 *
	 * @param warnings the warning set
	 */
	public void asTubeCoupler(WarningSet warnings) {

		TubeCoupler result = new TubeCoupler();

		copyValues(result);

		if (isCompatible(parent, TubeCoupler.class, warnings)) {
			parent.addChild(result);
		}
	}

	/**
	 * Convert the parsed Rocksim data values in this object to an instance of
	 * OpenRocket's Engine Block.
	 * <p/>
	 * Side Effect Warning: This method adds the resulting EngineBlock as a child of
	 * the parent rocket component!
	 *
	 * @param warnings the warning set
	 */
	public void asEngineBlock(WarningSet warnings) {

		EngineBlock result = new EngineBlock();

		copyValues(result);

		if (isCompatible(parent, EngineBlock.class, warnings)) {
			parent.addChild(result);
		}
	}

	/**
	 * Copy values from the base ring to the specific component.
	 *
	 * @param result the target to which ring values will be copied
	 */
	private void copyValues(RingComponent result) {
		result.setOuterRadius(ring.getOuterRadius());
		result.setInnerRadius(ring.getInnerRadius());
		result.setLength(ring.getLength());
		result.setName(ring.getName());
		setOverride(result, ring.isOverrideSubcomponentsEnabled(), ring.getOverrideMass(), ring.getOverrideCGX());
		result.setAxialMethod(ring.getAxialMethod());
		result.setAxialOffset(ring.getAxialOffset());
		result.setMaterial(ring.getMaterial());
		result.setThickness(result.getThickness());
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.endHandler(element, attributes, content, warnings);

		// The <Ring> XML element in Rocksim design file is used for many types of
		// components, unfortunately.
		// Additional sub-elements are used to indicate the type of the rocket
		// component. When parsing using SAX
		// this poses a problem because we can't "look ahead" to see what type is being
		// represented at the start
		// of parsing - something that would be nice to do so that we can instantiate
		// the correct OR component
		// at the start, then just call setters for the appropriate data.

		// To overcome that, a CenteringRing is instantiated at the start of parsing,
		// it's mutators are called,
		// and then at the end (this method) converts the CenteringRing to a more
		// appropriate type. CenteringRing
		// is generic enough to support the representation of all similar types without
		// loss of data.

		// UsageCode
		// 0 == Centering Ring
		// 1 == Bulkhead
		// 2 == Engine Block
		// 3 == Sleeve
		// 4 == Tube Coupler

		if (usageCode == 1) {
			// Bulkhead
			asBulkhead(warnings);
		} else if (usageCode == 2) {
			asEngineBlock(warnings);
		} else if (usageCode == 4) {
			// TubeCoupler
			asTubeCoupler(warnings);
		} else {
			// Default
			asCenteringRing(warnings);
		}
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