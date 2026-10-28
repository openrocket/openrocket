package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;

import org.xml.sax.SAXException;

/**
 * OpenRocket has no ring tails, so a RockSim ring tail is imported as a mass component with the mass and CG that
 * RockSim calculated for it (or its mass override). Its aerodynamics are not simulated.
 */
class RingTailHandler extends PositionDependentHandler<MassComponent> {

	private final MassComponent ringTail = new MassComponent();
	private final RocketComponent parent;

	/**
	 * RockSim's calculated mass and CG of the ring tail. The CG is relative to the front of the ring tail.
	 */
	private double calcMass = 0.0d;
	private double calcCG = 0.0d;

	public RingTailHandler(DocumentLoadingContext context, RocketComponent c) throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a ring tail may not be null.");
		}
		parent = c;
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
			if (RockSimCommonConstants.LEN.equals(element)) {
				ringTail.setLength(Math.max(0, Double.parseDouble(content)
						/ RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.CALC_MASS.equals(element)) {
				calcMass = Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
			if (RockSimCommonConstants.CALC_CG.equals(element)) {
				calcCG = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		if (!isCompatible(parent, MassComponent.class, warnings)) {
			return;
		}
		if (!ringTail.isMassOverridden()) {
			ringTail.setComponentMass(calcMass);
			ringTail.setCGOverridden(true);
			ringTail.setOverrideCGX(calcCG);
		}
		parent.addChild(ringTail);
		super.endHandler(element, attributes, content, warnings);
		warnings.add("Ring tail '" + ringTail.getName() + "' was imported as a mass component; its aerodynamics "
				+ "are not simulated.");
	}

	@Override
	public MassComponent getComponent() {
		return ringTail;
	}

	@Override
	public Material.Type getMaterialType() {
		return Material.Type.BULK;
	}
}
