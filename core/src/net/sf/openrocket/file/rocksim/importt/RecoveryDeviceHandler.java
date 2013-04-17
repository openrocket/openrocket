/*
 * RecoveryDeviceHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimDensityType;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.xml.sax.SAXException;

/**
 * A handler specific to streamers and parachutes.  This is done because Rocksim allows any type of material to be
 * used as a recovery device, which causes oddities with respect to densities.  Density computation is overridden
 * here to try to correctly compute a material's density in OpenRocket units.
 *
 * @param <C>  either a Streamer or Parachute
 */
public abstract class RecoveryDeviceHandler<C extends RecoveryDevice> extends PositionDependentHandler<C> {
	
	/**
	 * The thickness.  Not used by every component, and some component handlers may parse it for their own purposes.
	 */
	private double thickness = 0d;
	/**
	 * The Rocksim calculated mass.  Used only when not overridden and when Rocksim says density == 0 (Rocksim bug).
	 */
	private Double calcMass = 0d;
	
	public RecoveryDeviceHandler(DocumentLoadingContext context) {
		super(context);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		super.closeElement(element, attributes, content, warnings);
		
		try {
			if (RocksimCommonConstants.THICKNESS.equals(element)) {
				thickness = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.CALC_MASS.equals(element)) {
				calcMass = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	
	/**
	 * Compute the density.  Rocksim does strange things with densities.  For some streamer material it's in cubic,
	 * rather than square, units.  In those cases it needs to be converted to an appropriate SURFACE material density.
	 *
	 * @param type       the rocksim density
	 * @param rawDensity the density as specified in the Rocksim design file
	 * @return a value in OpenRocket SURFACE density units
	 */
	protected double computeDensity(RocksimDensityType type, double rawDensity) {
		
		double result;
		
		if (rawDensity > 0d) {
			//ROCKSIM_SURFACE is a square area density; compute normally
			//ROCKSIM_LINE is a single length dimension (kg/m) but Rocksim ignores thickness for this type and treats
			//it like a SURFACE.
			if (RocksimDensityType.ROCKSIM_SURFACE.equals(type) || RocksimDensityType.ROCKSIM_LINE.equals(type)) {
				result = rawDensity / RocksimDensityType.ROCKSIM_SURFACE.asOpenRocket();
			}
			//ROCKSIM_BULK is a cubic area density; multiple by thickness to make per square area; the result, when
			//multiplied by the area will then equal Rocksim's computed mass.
			else {
				result = (rawDensity / type.asOpenRocket()) * thickness;
			}
		}
		else {
			result = calcMass / getComponent().getArea();
			//A Rocksim bug on streamers/parachutes results in a 0 density at times.  When that is detected, try
			//to compute an approximate density from Rocksim's computed mass.
			if (RocksimDensityType.ROCKSIM_BULK.equals(type)) {
				//ROCKSIM_BULK is a cubic area density; multiple by thickness to make per square area
				result *= thickness;
			}
		}
		return result;
	}
	
	/**
	 * Set the relative position onto the component.  This cannot be done directly because setRelativePosition is not
	 * public in all components.
	 *
	 * @param position the OpenRocket position
	 */
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		getComponent().setRelativePosition(position);
	}
	
	/**
	 * Get the required type of material for this component.  This is the OpenRocket type, which does NOT always
	 * correspond to Rocksim.  Some streamer material is defined as BULK in the Rocksim file.  In those cases
	 * it is adjusted in this handler.
	 *
	 * @return SURFACE
	 */
	@Override
	public Material.Type getMaterialType() {
		return Material.Type.SURFACE;
	}
	
}
