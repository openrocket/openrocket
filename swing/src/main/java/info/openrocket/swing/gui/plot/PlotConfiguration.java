package info.openrocket.swing.gui.plot;

import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Pair;

import java.util.Collections;
import java.util.List;

public class PlotConfiguration<T extends DataType, B extends DataBranch<T>> implements Cloneable {
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

	private static final double INCLUDE_ZERO_DISTANCE = 0.3; // 30% of total range


	/** The data types and units to be plotted. */
	protected ArrayList<T> plotDataTypes = new ArrayList<>();
	protected ArrayList<Unit> plotDataUnits = new ArrayList<>();

	/** The corresponding Axis on which they will be plotted, or null to auto-select. */
	protected ArrayList<Integer> plotDataAxes = new ArrayList<>();

	/** The domain (x) axis. */
	protected T domainAxisType = null;
	protected Unit domainAxisUnit = null;


	/** All available axes. */
	protected final int axesCount;
	protected List<Axis> allAxes = new ArrayList<>();

	private String name;

	public PlotConfiguration(String name, T domainType) {
		this.name = name;

		// Two axes
		allAxes.add(new Axis());
		allAxes.add(new Axis());
		axesCount = 2;

		setDomainAxisType(domainType);
	}

	//// PlotDataTypes

	public void addPlotDataType(T type) {
		plotDataTypes.add(type);
		plotDataUnits.add(type.getUnitGroup().getDefaultUnit());
		plotDataAxes.add(-1);
	}

	public void addPlotDataType(T type, int axis) {
		if (axis >= axesCount) {
			throw new IllegalArgumentException("Axis index too large");
		}
		plotDataTypes.add(type);
		plotDataUnits.add(type.getUnitGroup().getDefaultUnit());
		plotDataAxes.add(axis);
	}


	public void setPlotDataType(int index, T type) {
		T origType = plotDataTypes.get(index);
		plotDataTypes.set(index, type);

		if (origType.getUnitGroup() != type.getUnitGroup()) {
			plotDataUnits.set(index, type.getUnitGroup().getDefaultUnit());
		}
	}

	public void setPlotDataUnit(int index, Unit unit) {
		if (!plotDataTypes.get(index).getUnitGroup().contains(unit)) {
			throw new IllegalArgumentException("Attempting to set unit " + unit + " to group "
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

	public void setPlotDataType(int index, T type, Unit unit, int axis) {
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



	public T getType(int index) {
		return plotDataTypes.get(index);
	}

	public Unit getUnit(int index) {
		return plotDataUnits.get(index);
	}

	public int getAxis(int index) {
		return plotDataAxes.get(index);
	}

	/**
	 * Returns the number of data types in this configuration.
	 * @return the number of data types in this configuration.
	 */
	public int getDataCount() {
		return plotDataTypes.size();
	}


	//// Axis

	public T getDomainAxisType() {
		return domainAxisType;
	}

	public void setDomainAxisType(T type) {
		boolean setUnit;

		setUnit = domainAxisType == null || domainAxisType.getUnitGroup() != type.getUnitGroup();

		domainAxisType = type;
		if (setUnit) {
			domainAxisUnit = domainAxisType.getUnitGroup().getDefaultUnit();
		}
	}

	public Unit getDomainAxisUnit() {
		return domainAxisUnit;
	}

	public void setDomainAxisUnit(Unit u) {
		if (!domainAxisType.getUnitGroup().contains(u)) {
			throw new IllegalArgumentException("Setting unit " + u + " to type " + domainAxisType);
		}
		domainAxisUnit = u;
	}

	public List<Axis> getAllAxes() {
		List<Axis> list = new ArrayList<>();
		list.addAll(allAxes);
		return list;
	}

	/**
	 * Find the best combination of the auto-selectable axes.
	 *
	 * @return	a new PlotConfiguration with the best fitting auto-selected axes and
	 * 			axes ranges selected.
	 */
	@SuppressWarnings("unchecked")
	public <C extends PlotConfiguration<T, B>> C fillAutoAxes(B data) {
		C config = (C) recursiveFillAutoAxes(data).getU();
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
	private Pair<PlotConfiguration<T, B>, Double> recursiveFillAutoAxes(B data) {
		// Create copy to fill in
		PlotConfiguration<T, B> copy = this.cloneConfiguration();

		int autoindex;
		for (autoindex = 0; autoindex < plotDataAxes.size(); autoindex++) {
			if (plotDataAxes.get(autoindex) < 0)
				break;
		}


		if (autoindex >= plotDataAxes.size()) {
			// All axes have been assigned, just return since we are already the best
			return new Pair<>(copy, copy.getGoodnessValue(data));
		}


		// Set the auto-selected index one at a time and choose the best one
		PlotConfiguration<T, B> best = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < axesCount; i++) {
			copy.plotDataAxes.set(autoindex, i);
			Pair<PlotConfiguration<T, B>, Double> result = copy.recursiveFillAutoAxes(data);
			if (result.getV() > bestValue) {
				best = result.getU();
				bestValue = result.getV();
			}
		}

		return new Pair<>(best, bestValue);
	}


	protected void fitAxes(B data) {
		this.fitAxes(Collections.singletonList(data));
	}

	/**
	 * Fit the axes to hold the provided data.  All of the plotDataAxis elements must
	 * be non-negative.
	 * <p>
	 * NOTE: This method assumes that only two axes are used.
	 */
	public void fitAxes(List<B> data) {
		// Reset axes
		for (Axis a : allAxes) {
			a.reset();
		}

		// Add full range to the axes
		calculateAxisBounds(data);

		// Ensure non-zero (or NaN) range, add a few percent range, include zero if it is close
		for (Axis a : allAxes) {
			if (MathUtil.equals(a.getMinValue(), a.getMaxValue())) {
				a.addBound(a.getMinValue() - 1);
				a.addBound(a.getMaxValue() + 1);
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
				Double.isNaN(left.getMinValue()) || Double.isNaN(right.getMinValue())) {
			return;
		}



		//// Compute common zero
		double min1 = left.getMinValue();
		double max1 = left.getMaxValue();
		double min2 = right.getMinValue();
		double max2 = right.getMaxValue();

		// Get the zero locations, scaled down to a value from 0 to 1 (0 = bottom of y axis, 1 = top)
		double zeroLoc1 = -min1 / left.getRangeLength();
		double zeroLoc2 = -min2 / right.getRangeLength();

		// Scale the right axis
		if (zeroLoc1 > zeroLoc2) {
			min2 = -max2 * (zeroLoc1 / (1 - zeroLoc1));
		} else {
			max2 = min2 * (-1 / zeroLoc1 + 1);
		}

		// Apply scale
		left.addBound(min1);
		left.addBound(max1);
		right.addBound(min2);
		right.addBound(max2);
	}

	protected void calculateAxisBounds(List<B> data) {
		int length = plotDataTypes.size();
		for (int i = 0; i < length; i++) {
			T type = plotDataTypes.get(i);
			Unit unit = plotDataUnits.get(i);
			int index = plotDataAxes.get(i);
			if (index < 0) {
				throw new IllegalStateException("fitAxes called with auto-selected axis");
			}
			Axis axis = allAxes.get(index);

			double min = unit.toUnit(data.get(0).getMinimum(type));
			double max = unit.toUnit(data.get(0).getMaximum(type));

			for (int j = 1; j < data.size(); j++) {
				// Ignore empty data
				if (data.get(j).getLength() == 0) {
					continue;
				}
				min = Math.min(min, unit.toUnit(data.get(j).getMinimum(type)));
				max = Math.max(max, unit.toUnit(data.get(j).getMaximum(type)));
			}

			axis.addBound(min);
			axis.addBound(max);
		}
	}

	//// Helper methods
	/**
	 * Fits the axis ranges to the data and returns the "goodness value" of this
	 * selection of axes.  All plotDataAxis elements must be non-null.
	 * <p>
	 * NOTE: This method assumes that all data can fit into the axes ranges and
	 * that only two axes are used.
	 *
	 * @return	a "goodness value", the larger the better.
	 */
	protected double getGoodnessValue(B data) {
		double goodness = 0;
		int length = plotDataTypes.size();

		// Fit the axes ranges to the data
		fitAxes(data);

		/*
		 * Calculate goodness of ranges.  100 points is given if the values fill the
		 * entire range, 0 if they fill none of it.
		 */
		for (int i = 0; i < length; i++) {
			T type = plotDataTypes.get(i);
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

			double d = (max - min) / axis.getRangeLength();
			d = MathUtil.safeSqrt(d); // Prioritize small ranges
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
		if (left.getMinValue() <= 0 && left.getMaxValue() >= 0 &&
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
	@SuppressWarnings("unchecked")
	public <C extends PlotConfiguration<T, B>> C resetUnits() {
		for (int i = 0; i < plotDataTypes.size(); i++) {
			plotDataUnits.set(i, plotDataTypes.get(i).getUnitGroup().getDefaultUnit());
		}
		return (C) this;
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
		return scale * mul;
	}



	@SuppressWarnings("unused")
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
		return scale * mul;
	}


	@SuppressWarnings("unused")
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
		return scale * mul;
	}


	//// Other
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

	@Override
	public PlotConfiguration<T, B> clone() {
		return cloneConfiguration();
	}

	@SuppressWarnings("unchecked")
	public <C extends PlotConfiguration<T, B>> C cloneConfiguration() {
		try {
			C copy = (C) super.clone();

			// Shallow-clone all immutable lists
			copy.plotDataTypes = this.plotDataTypes.clone();
			copy.plotDataAxes = this.plotDataAxes.clone();
			copy.plotDataUnits = this.plotDataUnits.clone();

			// Deep-clone all Axis since they are mutable
			copy.allAxes = new ArrayList<>();
			for (Axis a : this.allAxes) {
				copy.allAxes.add(a.clone());
			}

			return copy;

		} catch (CloneNotSupportedException e) {
			throw new BugException("BUG! Could not clone().");
		}
	}
}
