package net.sf.openrocket.gui.plot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.FlightDataBranch.Type;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;


public class PlotConfiguration implements Cloneable {
	
	public static final PlotConfiguration[] DEFAULT_CONFIGURATIONS;
	static {
		ArrayList<PlotConfiguration> configs = new ArrayList<PlotConfiguration>();
		PlotConfiguration config;
		
		config = new PlotConfiguration("Vertical motion vs. time");
		config.addPlotDataType(FlightDataBranch.TYPE_ALTITUDE, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_VELOCITY_Z);
		config.addPlotDataType(FlightDataBranch.TYPE_ACCELERATION_Z);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);
		
		config = new PlotConfiguration("Total motion vs. time");
		config.addPlotDataType(FlightDataBranch.TYPE_ALTITUDE, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_VELOCITY_TOTAL);
		config.addPlotDataType(FlightDataBranch.TYPE_ACCELERATION_TOTAL);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);
		
		config = new PlotConfiguration("Flight side profile", FlightDataBranch.TYPE_POSITION_X);
		config.addPlotDataType(FlightDataBranch.TYPE_ALTITUDE);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);

		config = new PlotConfiguration("Stability vs. time");
		config.addPlotDataType(FlightDataBranch.TYPE_STABILITY, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_CP_LOCATION, 1);
		config.addPlotDataType(FlightDataBranch.TYPE_CG_LOCATION, 1);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);
		
		config = new PlotConfiguration("Drag coefficients vs. Mach number", 
				FlightDataBranch.TYPE_MACH_NUMBER);
		config.addPlotDataType(FlightDataBranch.TYPE_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_FRICTION_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_BASE_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_PRESSURE_DRAG_COEFF, 0);
		configs.add(config);

		config = new PlotConfiguration("Roll characteristics");
		config.addPlotDataType(FlightDataBranch.TYPE_ROLL_RATE, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_ROLL_MOMENT_COEFF, 1);
		config.addPlotDataType(FlightDataBranch.TYPE_ROLL_FORCING_COEFF, 1);
		config.addPlotDataType(FlightDataBranch.TYPE_ROLL_DAMPING_COEFF, 1);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.LAUNCHROD, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);

		config = new PlotConfiguration("Angle of attack and orientation vs. time");
		config.addPlotDataType(FlightDataBranch.TYPE_AOA, 0);
		config.addPlotDataType(FlightDataBranch.TYPE_ORIENTATION_PHI);
		config.addPlotDataType(FlightDataBranch.TYPE_ORIENTATION_THETA);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);

		config = new PlotConfiguration("Simulation time step and computation time");
		config.addPlotDataType(FlightDataBranch.TYPE_TIME_STEP);
		config.addPlotDataType(FlightDataBranch.TYPE_COMPUTATION_TIME);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		configs.add(config);

		DEFAULT_CONFIGURATIONS = configs.toArray(new PlotConfiguration[0]);
	}
	
	
	
	/** Bonus given for the first type being on the first axis */
	private static final double BONUS_FIRST_TYPE_ON_FIRST_AXIS = 1.0;

	/**
	 * Bonus given if the first axis includes zero (to prefer first axis having zero over 
	 * the others) 
	 */
	private static final double BONUS_FIRST_AXIS_HAS_ZERO = 2.0;
	
	/** Bonus given for a common zero point on left and right axes. */
	private static final double BONUS_COMMON_ZERO = 40.0;
	
	/** Bonus given for only using a single axis. */
	private static final double BONUS_ONLY_ONE_AXIS = 50.0;
	
	
	private static final double INCLUDE_ZERO_DISTANCE = 0.3;  // 30% of total range
	
	

	/** The data types to be plotted. */
	private ArrayList<FlightDataBranch.Type> plotDataTypes = new ArrayList<FlightDataBranch.Type>();
	
	private ArrayList<Unit> plotDataUnits = new ArrayList<Unit>();
	
	/** The corresponding Axis on which they will be plotted, or null to auto-select. */
	private ArrayList<Integer> plotDataAxes = new ArrayList<Integer>();

	private EnumSet<FlightEvent.Type> events = EnumSet.noneOf(FlightEvent.Type.class);
	
	/** The domain (x) axis. */
	private FlightDataBranch.Type domainAxisType = null;
	private Unit domainAxisUnit = null;
	
	
	/** All available axes. */
	private final int axesCount;
	private ArrayList<Axis> allAxes = new ArrayList<Axis>();
	
	
	
	private String name = null;
	
	
	
	public PlotConfiguration() {
		this(null, FlightDataBranch.TYPE_TIME);
	}
	
	public PlotConfiguration(String name) {
		this(name, FlightDataBranch.TYPE_TIME);
	}
	
	public PlotConfiguration(String name, FlightDataBranch.Type domainType) {
		this.name = name;
		// Two axes
		allAxes.add(new Axis());
		allAxes.add(new Axis());
		axesCount = 2;
		
		setDomainAxisType(domainType);
	}
	
	
	
	
	
	public FlightDataBranch.Type getDomainAxisType() {
		return domainAxisType;
	}
	
	public void setDomainAxisType(FlightDataBranch.Type type) {
		boolean setUnit;
		
		if (domainAxisType != null  &&  domainAxisType.getUnitGroup() == type.getUnitGroup())
			setUnit = false;
		else
			setUnit = true;
		
		domainAxisType = type;
		if (setUnit)
			domainAxisUnit = domainAxisType.getUnitGroup().getDefaultUnit();
	}
	
	public Unit getDomainAxisUnit() {
		return domainAxisUnit;
	}
	
	public void setDomainAxisUnit(Unit u) {
		if (!domainAxisType.getUnitGroup().contains(u)) {
			throw new IllegalArgumentException("Setting unit "+u+" to type "+domainAxisType);
		}
		domainAxisUnit = u;
	}
	
	
	
	public void addPlotDataType(FlightDataBranch.Type type) {
		plotDataTypes.add(type);
		plotDataUnits.add(type.getUnitGroup().getDefaultUnit());
		plotDataAxes.add(-1);
	}

	public void addPlotDataType(FlightDataBranch.Type type, int axis) {
		if (axis >= axesCount) {
			throw new IllegalArgumentException("Axis index too large");
		}
		plotDataTypes.add(type);
		plotDataUnits.add(type.getUnitGroup().getDefaultUnit());
		plotDataAxes.add(axis);
	}


	
	
	public void setPlotDataType(int index, FlightDataBranch.Type type) {
		FlightDataBranch.Type origType = plotDataTypes.get(index);
		plotDataTypes.set(index, type);
		
		if (origType.getUnitGroup() != type.getUnitGroup()) {
			plotDataUnits.set(index, type.getUnitGroup().getDefaultUnit());
		}
	}
	
	public void setPlotDataUnit(int index, Unit unit) {
		if (!plotDataTypes.get(index).getUnitGroup().contains(unit)) {
			throw new IllegalArgumentException("Attempting to set unit "+unit+" to group "
					+ plotDataTypes.get(index).getUnitGroup());
		}
		plotDataUnits.set(index, unit);
	}
	
	public void setPlotDataAxis(int index, int axis) {
		if (axis >= axesCount) {
			throw new IllegalArgumentException("Axis index too large");
		}
		plotDataAxes.set(index, axis);
	}
	
	
	public void setPlotDataType(int index, FlightDataBranch.Type type, Unit unit, int axis) {
		if (axis >= axesCount) {
			throw new IllegalArgumentException("Axis index too large");
		}
		plotDataTypes.set(index, type);
		plotDataUnits.set(index, unit);
		plotDataAxes.set(index, axis);
	}
	
	public void removePlotDataType(int index) {
		plotDataTypes.remove(index);
		plotDataUnits.remove(index);
		plotDataAxes.remove(index);
	}
	
	
	
	public FlightDataBranch.Type getType (int index) {
		return plotDataTypes.get(index);
	}
	public Unit getUnit(int index) {
		return plotDataUnits.get(index);
	}
	public int getAxis(int index) {
		return plotDataAxes.get(index);
	}
	
	public int getTypeCount() {
		return plotDataTypes.size();
	}
	
	
	/// Events
	
	public Set<FlightEvent.Type> getActiveEvents() {
		return (Set<FlightEvent.Type>) events.clone();
	}
	
	public void setEvent(FlightEvent.Type type, boolean active) {
		if (active) {
			events.add(type);
		} else {
			events.remove(type);
		}
	}
	
	public boolean isEventActive(FlightEvent.Type type) {
		return events.contains(type);
	}
	
	
	
	
	
	public List<Axis> getAllAxes() {
		List<Axis> list = new ArrayList<Axis>();
		list.addAll(allAxes);
		return list;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of this PlotConfiguration.
	 */
	@Override
	public String toString() {
		return name;
	}
	
	
	
	/**
	 * Find the best combination of the auto-selectable axes.
	 * 
	 * @return	a new PlotConfiguration with the best fitting auto-selected axes and
	 * 			axes ranges selected.
	 */
	public PlotConfiguration fillAutoAxes(FlightDataBranch data) {
		PlotConfiguration config = recursiveFillAutoAxes(data).getU();
		System.out.println("BEST FOUND, fitting");
		config.fitAxes(data);
		return config;
	}
	
	
	
	
	/**
	 * Recursively search for the best combination of the auto-selectable axes.
	 * This is a brute-force search method.
	 * 
	 * @return	a new PlotConfiguration with the best fitting auto-selected axes and
	 * 			axes ranges selected, and the goodness value
	 */
	private Pair<PlotConfiguration, Double> recursiveFillAutoAxes(FlightDataBranch data) {
		
		// Create copy to fill in
		PlotConfiguration copy = this.clone();
		
		int autoindex; 
		for (autoindex=0; autoindex < plotDataAxes.size(); autoindex++) {
			if (plotDataAxes.get(autoindex) < 0)
				break;
		}
		
		
		if (autoindex >= plotDataAxes.size()) {
			// All axes have been assigned, just return since we are already the best
			return new Pair<PlotConfiguration, Double>(copy, copy.getGoodnessValue(data));
		}
		
		
		// Set the auto-selected index one at a time and choose the best one
		PlotConfiguration best = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		for (int i=0; i < axesCount; i++) {
			copy.plotDataAxes.set(autoindex, i);
			Pair<PlotConfiguration, Double> result = copy.recursiveFillAutoAxes(data);
			if (result.getV() > bestValue) {
				best = result.getU();
				bestValue = result.getV();
			}
		}
		
		return new Pair<PlotConfiguration, Double>(best, bestValue);
	}
	
	
	
	
	
	/**
	 * Fit the axes to hold the provided data.  All of the plotDataAxis elements must
	 * be non-negative.
	 * <p>
	 * NOTE: This method assumes that only two axes are used.
	 */
	protected void fitAxes(FlightDataBranch data) {
		
		// Reset axes
		for (Axis a: allAxes) {
			a.reset();
		}
		
		// Add full range to the axes
		int length = plotDataTypes.size();
		for (int i=0; i<length; i++) {
			FlightDataBranch.Type type = plotDataTypes.get(i);
			Unit unit = plotDataUnits.get(i);
			int index = plotDataAxes.get(i);
			if (index < 0) {
				throw new IllegalStateException("fitAxes called with auto-selected axis");
			}
			Axis axis = allAxes.get(index);
			
			double min = unit.toUnit(data.getMinimum(type));
			double max = unit.toUnit(data.getMaximum(type));

			axis.addBound(min);
			axis.addBound(max);
		}
		
		// Ensure non-zero (or NaN) range, add a few percent range, include zero if it is close
		for (Axis a: allAxes) {
			if (MathUtil.equals(a.getMinValue(), a.getMaxValue())) {
				a.addBound(a.getMinValue()-1);
				a.addBound(a.getMaxValue()+1);
			}
			
			double addition = a.getRangeLength() * 0.03;
			a.addBound(a.getMinValue() - addition);
			a.addBound(a.getMaxValue() + addition);
			
			double dist;
			dist = Math.min(Math.abs(a.getMinValue()), Math.abs(a.getMaxValue()));
			if (dist <= a.getRangeLength() * INCLUDE_ZERO_DISTANCE) {
				a.addBound(0);
			}
		}

		
		// Check whether to use a common zero
		Axis left = allAxes.get(0);
		Axis right = allAxes.get(1);
		
		if (left.getMinValue() > 0 || left.getMaxValue() < 0 ||
				right.getMinValue() > 0 || right.getMaxValue() < 0 ||
				Double.isNaN(left.getMinValue()) || Double.isNaN(right.getMinValue()))
			return;
		
		
		
		//// Compute common zero
		// TODO: MEDIUM: This algorithm may require tweaking

		double min1 = left.getMinValue();
		double max1 = left.getMaxValue();
		double min2 = right.getMinValue();
		double max2 = right.getMaxValue();
		
		// Calculate and round scaling factor
		double scale = Math.max(left.getRangeLength(), right.getRangeLength()) /
						Math.min(left.getRangeLength(), right.getRangeLength());
		
		System.out.println("Scale: "+scale);
		
		scale = roundScale(scale);
		if (right.getRangeLength() > left.getRangeLength()) {
			scale = 1/scale;
		}
		System.out.println("Rounded scale: " + scale);

		// Scale right axis, enlarge axes if necessary and scale back
		min2 *= scale;
		max2 *= scale;
		min1 = Math.min(min1, min2);
		min2 = min1;
		max1 = Math.max(max1, max2);
		max2 = max1;
		min2 /= scale;
		max2 /= scale;
		
		
		
		// Scale to unit length
//		double scale1 = left.getRangeLength();
//		double scale2 = right.getRangeLength();
//		
//		double min1 = left.getMinValue() / scale1;
//		double max1 = left.getMaxValue() / scale1;
//		double min2 = right.getMinValue() / scale2;
//		double max2 = right.getMaxValue() / scale2;
//		
//		// Combine unit ranges
//		min1 = MathUtil.min(min1, min2);
//		min2 = min1;
//		max1 = MathUtil.max(max1, max2);
//		max2 = max1;
//		
//		// Scale up
//		min1 *= scale1;
//		max1 *= scale1;
//		min2 *= scale2;
//		max2 *= scale2;
//		
//		// Compute common scale
//		double range1 = max1-min1;
//		double range2 = max2-min2;
//		
//		double scale = MathUtil.max(range1, range2) / MathUtil.min(range1, range2);
//		double roundScale = roundScale(scale);
//
//		if (range2 < range1) {
//			if (roundScale < scale) {
//				min2 = min1 / roundScale;
//				max2 = max1 / roundScale;
//			} else {
//				min1 = min2 * roundScale;
//				max1 = max2 * roundScale;
//			}
//		} else {
//			if (roundScale > scale) {
//				min2 = min1 * roundScale;
//				max2 = max1 * roundScale;
//			} else {
//				min1 = min2 / roundScale;
//				max1 = max2 / roundScale;
//			}
//		}
		
		// Apply scale
		left.addBound(min1);
		left.addBound(max1);
		right.addBound(min2);
		right.addBound(max2);
		
	}
	
	

	private double roundScale(double scale) {
		double mul = 1;
		while (scale >= 10) {
			scale /= 10;
			mul *= 10;
		}
		while (scale < 1) {
			scale *= 10;
			mul /= 10;
		}
		
		// 1 2 4 5 10
		
		if (scale > 7.5) {
			scale = 10;
		} else if (scale > 4.5) {
			scale = 5;
		} else if (scale > 3) {
			scale = 4;
		} else if (scale > 1.5) {
			scale = 2;
		} else {
			scale = 1;
		}
		return scale*mul;
	}
	
	
	
	private double roundScaleUp(double scale) {
		double mul = 1;
		while (scale >= 10) {
			scale /= 10;
			mul *= 10;
		}
		while (scale < 1) {
			scale *= 10;
			mul /= 10;
		}
		
		if (scale > 5) {
			scale = 10;
		} else if (scale > 4) {
			scale = 5;
		} else if (scale > 2) {
			scale = 4;
		} else if (scale > 1) {
			scale = 2;
		} else {
			scale = 1;
		}
		return scale*mul;
	}
	

	private double roundScaleDown(double scale) {
		double mul = 1;
		while (scale >= 10) {
			scale /= 10;
			mul *= 10;
		}
		while (scale < 1) {
			scale *= 10;
			mul /= 10;
		}
		
		if (scale > 5) {
			scale = 5;
		} else if (scale > 4) {
			scale = 4;
		} else if (scale > 2) {
			scale = 2;
		} else {
			scale = 1;
		}
		return scale*mul;
	}
	
	
	
	/**
	 * Fits the axis ranges to the data and returns the "goodness value" of this 
	 * selection of axes.  All plotDataAxis elements must be non-null.
	 * <p>
	 * NOTE: This method assumes that all data can fit into the axes ranges and
	 * that only two axes are used.
	 *
	 * @return	a "goodness value", the larger the better.
	 */
	protected double getGoodnessValue(FlightDataBranch data) {
		double goodness = 0;
		int length = plotDataTypes.size();
		
		// Fit the axes ranges to the data
		fitAxes(data);
		
		/*
		 * Calculate goodness of ranges.  100 points is given if the values fill the
		 * entire range, 0 if they fill none of it.
		 */
		for (int i = 0; i < length; i++) {
			FlightDataBranch.Type type = plotDataTypes.get(i);
			Unit unit = plotDataUnits.get(i);
			int index = plotDataAxes.get(i);
			if (index < 0) {
				throw new IllegalStateException("getGoodnessValue called with auto-selected axis");
			}
			Axis axis = allAxes.get(index);
			
			double min = unit.toUnit(data.getMinimum(type));
			double max = unit.toUnit(data.getMaximum(type));
			if (Double.isNaN(min) || Double.isNaN(max))
				continue;
			if (MathUtil.equals(min, max))
				continue;
			
			double d = (max-min) / axis.getRangeLength();
			d = Math.sqrt(d);  // Prioritize small ranges
			goodness += d * 100.0;
		}
		
		
		/*
		 * Add extra points for specific things.
		 */

		// A little for the first type being on the first axis
		if (plotDataAxes.get(0) == 0)
			goodness += BONUS_FIRST_TYPE_ON_FIRST_AXIS;
		
		// A little bonus if the first axis contains zero
		Axis left = allAxes.get(0);
		if (left.getMinValue() <= 0 && left.getMaxValue() >= 0)
			goodness += BONUS_FIRST_AXIS_HAS_ZERO;
		
		// A boost if a common zero was used in the ranging
		Axis right = allAxes.get(1);
		if (left.getMinValue() <= 0 && 	left.getMaxValue() >= 0 &&
				right.getMinValue() <= 0 && right.getMaxValue() >= 0)
			goodness += BONUS_COMMON_ZERO;
		
		// A boost if only one axis is used
		if (Double.isNaN(left.getMinValue()) || Double.isNaN(right.getMinValue()))
			goodness += BONUS_ONLY_ONE_AXIS;
		
		return goodness;
	}
	
	
	
	/**
	 * Reset the units of this configuration to the default units. Returns this
	 * PlotConfiguration.
	 * 
	 * @return   this PlotConfiguration.
	 */
	public PlotConfiguration resetUnits() {
		for (int i=0; i < plotDataTypes.size(); i++) {
			plotDataUnits.set(i, plotDataTypes.get(i).getUnitGroup().getDefaultUnit());
		}
		return this;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public PlotConfiguration clone() {
		try {
			
			PlotConfiguration copy = (PlotConfiguration) super.clone();
			
			// Shallow-clone all immutable lists
			copy.plotDataTypes = (ArrayList<Type>) this.plotDataTypes.clone();
			copy.plotDataAxes = (ArrayList<Integer>) this.plotDataAxes.clone();
			copy.plotDataUnits = (ArrayList<Unit>) this.plotDataUnits.clone();
			copy.events = this.events.clone();
			
			// Deep-clone all Axis since they are mutable
			copy.allAxes = new ArrayList<Axis>();
			for (Axis a: this.allAxes) {
				copy.allAxes.add(a.clone());
			}
			
			return copy;
			
			
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("BUG! Could not clone().");
		}
	}
	
}
