/*
 * ParachuteHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;

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
	 * Constructor.
	 *
	 * @param c the parent component
	 * @param warnings  the warning set
	 * 
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public ParachuteHandler(DocumentLoadingContext context, RocketComponent c, WarningSet warnings) throws IllegalArgumentException {
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
			if (RocksimCommonConstants.DIAMETER.equals(element)) {
				chute.setDiameter(Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
				/* Rocksim doesn't have a packed parachute radius, so we approximate it. */
				double packed;
				RocketComponent parent = chute.getParent();
				if (parent instanceof BodyTube) {
					packed = ((BodyTube) parent).getOuterRadius() * 0.9;
				}
				else if (parent instanceof InnerTube) {
					packed = ((InnerTube) parent).getInnerRadius() * 0.9;
				}
				else {
					packed = chute.getDiameter() * 0.025;
				}
				chute.setRadius(packed);
			}
			if (RocksimCommonConstants.SHROUD_LINE_COUNT.equals(element)) {
				chute.setLineCount(Math.max(0, Integer.parseInt(content)));
			}
			if (RocksimCommonConstants.SHROUD_LINE_LEN.equals(element)) {
				chute.setLineLength(Math.max(0, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH));
			}
			if (RocksimCommonConstants.SPILL_HOLE_DIA.equals(element)) {
				//Not supported in OpenRocket
				double spillHoleRadius = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS;
				if (spillHoleRadius > 0) {
					warnings.add("Parachute spill holes are not supported. Ignoring.");
				}
			}
			if (RocksimCommonConstants.SHROUD_LINE_MASS_PER_MM.equals(element)) {
				shroudLineDensity = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LINE_DENSITY;
			}
			if (RocksimCommonConstants.SHROUD_LINE_MATERIAL.equals(element)) {
				chute.setLineMaterial(createCustomMaterial(Material.Type.LINE, content, shroudLineDensity));
			}
			if (RocksimCommonConstants.DRAG_COEFFICIENT.equals(element)) {
				chute.setCD(Double.parseDouble(content));
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
	public Parachute getComponent() {
		return chute;
	}
	
}
