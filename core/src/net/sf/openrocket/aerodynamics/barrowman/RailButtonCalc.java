package net.sf.openrocket.aerodynamics.barrowman;

import java.util.List;
import java.lang.Math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class RailButtonCalc extends RocketComponentCalc {
	private final static Logger log = LoggerFactory.getLogger(RailButtonCalc.class);	

	// values transcribed from Gowen and Perkins, "Drag of Circular Cylinders for a Wide Range
	// of Reynolds Numbers and Mach Numbers", NACA Technical Note 2960, Figure 7
	private static final List<Double> cdDomain = List.of(0.0, 0.2,  0.3,  0.4, 0.5, 0.6, 0.7, 1.0, 1.6, 2.0,  2.8, 100.0);
	private static final List<Double> cdRange =  List.of(1.2, 1.22, 1.25, 1.3, 1.4, 1.5, 1.6, 2.1, 1.5, 1.45, 1.33,  1.33);

	private final RailButton button;

	public RailButtonCalc(RocketComponent component) {
		super(component);

		// need to stash the button
		button = (RailButton) component;
	}

	@Override
	public double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings) {
		// very small relative surface area, and slick
		return 0.0;
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
			AerodynamicForces forces, WarningSet warnings) {
		// Nothing to be done
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
									  double stagnationCD, double baseCD, WarningSet warnings) {

		// grab relevant button params
		final int instanceCount = button.getInstanceCount();
		final Coordinate[] instanceOffsets = button.getInstanceOffsets();

		// compute button reference area
		final double buttonHt = button.getTotalHeight();
		final double outerArea = buttonHt * button.getOuterDiameter();
		final double notchArea = (button.getOuterDiameter() - button.getInnerDiameter()) * button.getInnerHeight();
		final double refArea = outerArea - notchArea;

		// accumulate Cd contribution from each rail button
		double CDmul = 0.0;
		for (int i = 0; i < button.getInstanceCount(); i++) {
			
			// compute boundary layer height at button location.  I can't find a good reference for the
			// formula, e.g. https://aerospaceengineeringblog.com/boundary-layers/ simply says it's the
			// "scientific consensus".
			double x = (button.toAbsolute(instanceOffsets[i]))[0].x;   // location of button
			double rex = calculateReynoldsNumber(x, conditions);       // Reynolds number of button location
			double del = 0.37 * x / Math.pow(rex, 0.2);                // Boundary layer thickness

			// compute mean airspeed over button
			// this assumes airspeed changes linearly through boundary layer
			// and that all parts of the railbutton contribute equally to Cd,
			// neither of which is true but both are plenty close enough for our purposes

			double mach;
			if (buttonHt > del) {
				// Case 1:  button extends beyond boundary layer
				// Mean velocity is 1/2 rocket velocity up to limit of boundary layer,
				// full velocity after that
				mach = (buttonHt - 0.5*del) * conditions.getMach()/buttonHt;
			} else {
				// Case 2:  button is entirely within boundary layer
				mach = MathUtil.map(buttonHt/2.0, 0, del, 0, conditions.getMach());
			}

			// look up Cd as function of speed.  It's pretty constant as a function of Reynolds
			// number when slow, so we can just use a function of Mach number
			double cd = MathUtil.interpolate(cdDomain, cdRange, mach);

			// Since later drag force calculations don't consider boundary layer, compute "effective Cd"
			// based on rocket velocity
			cd = cd * MathUtil.pow2(mach)/MathUtil.pow2(conditions.getMach());
			
			// add to CDmul
			CDmul += cd;
		}
		
		return CDmul * stagnationCD * refArea / conditions.getRefArea();
	}
}
