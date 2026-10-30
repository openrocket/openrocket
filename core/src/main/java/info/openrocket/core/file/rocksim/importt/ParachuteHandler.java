/*
 * ParachuteHandler.java
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.HashMap;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.MathUtil;

import org.xml.sax.SAXException;

/**
 * A SAX handler for Rocksim's Parachute XML type.
 */
class ParachuteHandler extends RecoveryDeviceHandler<Parachute> {
	/**
	 * The OpenRocket Parachute instance
	 */
	private final Parachute chute;
	/**
	 * The shroud line density.
	 */
	private double shroudLineDensity = 0.0d;
	/**
	 * RockSim's canopy diameter. For a polygonal canopy, this is the distance between opposite corners.
	 */
	private double rockSimDiameter = 0.0d;
	/**
	 * The number of canopy sides, or 0 for a round canopy. RockSim defaults to 6 sides.
	 */
	private int sideCount = 6;
	/**
	 * The number of identical parachutes.
	 */
	private int chuteCount = 1;
	/**
	 * The spill hole diameter.
	 */
	private double spillHoleDiameter = 0.0d;

	/**
	 * Constructor.
	 *
	 * @param c        the parent component
	 * @param warnings the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public ParachuteHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings)
			throws IllegalArgumentException {
		super(context);
		if (c == null) {
			throw new IllegalArgumentException("The parent of a parachute may not be null.");
		}
		chute = new Parachute();
		if (isCompatible(c, Parachute.class, warnings)) {
			c.addChild(chute);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		try {
			if (RockSimCommonConstants.DIAMETER.equals(element)) {
				rockSimDiameter = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
				chute.setDiameter(rockSimDiameter);
				/* Rocksim doesn't have a packed parachute radius, so we approximate it. */
				double packed;
				RocketComponent parent = chute.getParent();
				if (parent instanceof BodyTube) {
					packed = ((BodyTube) parent).getOuterRadius() * 0.9;
				} else if (parent instanceof InnerTube) {
					packed = ((InnerTube) parent).getInnerRadius() * 0.9;
				} else {
					packed = chute.getDiameter() * 0.025;
				}
				chute.setRadius(packed);
				chute.setLength(packed);
			}
			if (RockSimCommonConstants.SHROUD_LINE_COUNT.equals(element)) {
				chute.setLineCount(Math.max(0, Integer.parseInt(content)));
			}
			if (RockSimCommonConstants.SHROUD_LINE_LEN.equals(element)) {
				chute.setLineLength(
						Math.max(0, Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RockSimCommonConstants.SPILL_HOLE_DIA.equals(element)) {
				spillHoleDiameter = Math.max(0, Double.parseDouble(content)
						/ RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RockSimCommonConstants.SIDE_COUNT.equals(element)) {
				sideCount = Integer.parseInt(content.trim());
			}
			if (RockSimCommonConstants.CHUTE_COUNT.equals(element)) {
				chuteCount = Math.max(1, Integer.parseInt(content.trim()));
			}
			if (RockSimCommonConstants.SHROUD_LINE_MASS_PER_MM.equals(element)) {
				shroudLineDensity = Double.parseDouble(content)
						/ RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY;
			}
			if (RockSimCommonConstants.SHROUD_LINE_MATERIAL.equals(element)) {
				chute.setLineMaterial(createCustomMaterial(Material.Type.LINE, content, shroudLineDensity));
			}
			if (RockSimCommonConstants.DRAG_COEFFICIENT.equals(element)) {
				chute.setCD(Double.parseDouble(content));
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
		setEquivalentDiameter(warnings);
	}

	/**
	 * OpenRocket parachutes are round, single canopies without a spill hole. RockSim's drag area is that of all
	 * canopies, each a regular polygon (or a circle) minus its spill hole, so the parachute is imported with the
	 * round diameter that has the same drag area.
	 *
	 * @param warnings the warning set
	 */
	private void setEquivalentDiameter(WarningSet warnings) {
		if (rockSimDiameter <= 0) {
			return;
		}
		double canopyArea;
		if (sideCount < 3) {
			canopyArea = Math.PI * MathUtil.pow2(rockSimDiameter / 2);
		} else {
			canopyArea = 0.5 * sideCount * MathUtil.pow2(rockSimDiameter / 2) * Math.sin(2 * Math.PI / sideCount);
		}
		double area = chuteCount * (canopyArea - Math.PI * MathUtil.pow2(spillHoleDiameter / 2));
		if (area <= 0) {
			return;
		}

		double diameter = 2 * Math.sqrt(area / Math.PI);
		if (MathUtil.equals(diameter, rockSimDiameter)) {
			return;
		}
		chute.setDiameter(diameter);
		warnings.add(String.format("Parachute '%s' was imported as a single round parachute with the same drag area "
				+ "(diameter %.1f mm instead of %.1f mm).", chute.getName(),
				diameter * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
				rockSimDiameter * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
	}

	/**
	 * Get the component this handler is working upon.
	 *
	 * @return a component
	 */
	@Override
	public Parachute getComponent() {
		return chute;
	}

}
