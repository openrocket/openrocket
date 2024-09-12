package info.openrocket.core.componentanalysis;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.ModID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataBranch for storing component analysis data.
 */
public class CADataBranch extends DataBranch<CADataType> {
	// Map to store values for each CADataType-RocketComponent pair
	private final Map<CADataType, Map<RocketComponent, ArrayList<Double>>> componentValues = new HashMap<>();
	// Maps to store min and max values for each CADataType-RocketComponent pair
	private final Map<CADataType, Map<RocketComponent, Double>> componentMinValues = new HashMap<>();
	private final Map<CADataType, Map<RocketComponent, Double>> componentMaxValues = new HashMap<>();

	public CADataBranch(String name, CADataType... types) {
		super(name);
		for (CADataType type : types) {
			addType(type);
		}
	}

	@Override
	public void addType(CADataType type) {
		super.addType(type);
		if (!(type instanceof CADomainDataType)) {
			componentValues.put(type, new HashMap<>());
			componentMinValues.put(type, new HashMap<>());
			componentMaxValues.put(type, new HashMap<>());
		}
	}

	@Override
	public void addPoint() {
		mutable.check();

		for (Map.Entry<CADataType, ArrayList<Double>> entry : values.entrySet()) {
			entry.getValue().add(Double.NaN);
		}

		for (Map<RocketComponent, ArrayList<Double>> componentMap : componentValues.values()) {
			for (ArrayList<Double> list : componentMap.values()) {
				list.add(Double.NaN);
			}
		}

		modID = new ModID();
	}

	public void setValue(CADataType type, RocketComponent component, double value) {
		mutable.check();

		if (type instanceof CADomainDataType) {
			throw new IllegalArgumentException("Use setDomainValue for CADomainDataType");
		}

		// Ensure the type exists
		if (!componentValues.containsKey(type)) {
			addType(type);
		}

		Map<RocketComponent, ArrayList<Double>> typeMap = componentValues.get(type);
		ArrayList<Double> list = typeMap.computeIfAbsent(component, k -> {
			ArrayList<Double> newList = new ArrayList<>();
			int n = getLength();
			for (int i = 0; i < n; i++) {
				newList.add(Double.NaN);
			}
			return newList;
		});

		if (list.size() > 0) {
			list.set(list.size() - 1, value);
		}

		// Update min and max values
		updateMinMaxValues(type, component, value);

		modID = new ModID();
	}

	public void setDomainValue(CADomainDataType domainType, double value) {
		mutable.check();

		// Use the existing DataBranch functionality for domain values
		super.setValue(domainType, value);

		modID = new ModID();
	}

	private void updateMinMaxValues(CADataType type, RocketComponent component, double value) {
		Map<RocketComponent, Double> minMap = componentMinValues.get(type);
		Map<RocketComponent, Double> maxMap = componentMaxValues.get(type);

		double min = minMap.getOrDefault(component, Double.NaN);
		double max = maxMap.getOrDefault(component, Double.NaN);

		if (Double.isNaN(min) || (value < min)) {
			minMap.put(component, value);
		}
		if (Double.isNaN(max) || (value > max)) {
			maxMap.put(component, value);
		}
	}

	public List<Double> get(CADataType type, RocketComponent component) {
		if (type instanceof CADomainDataType) {
			return super.get(type);
		}

		Map<RocketComponent, ArrayList<Double>> typeMap = componentValues.get(type);
		if (typeMap == null) return null;

		ArrayList<Double> list = typeMap.get(component);
		if (list == null) return null;

		return list.clone();
	}

	public Double getByIndex(CADataType type, RocketComponent component, int index) {
		if (index < 0 || index >= getLength()) {
			throw new IllegalArgumentException("Index out of bounds");
		}

		if (type instanceof CADomainDataType) {
			return super.getByIndex(type, index);
		}

		Map<RocketComponent, ArrayList<Double>> typeMap = componentValues.get(type);
		if (typeMap == null) return null;

		ArrayList<Double> list = typeMap.get(component);
		if (list == null) return null;

		return list.get(index);
	}

	public double getLast(CADataType type, RocketComponent component) {
		if (type instanceof CADomainDataType) {
			return super.getLast(type);
		}

		Map<RocketComponent, ArrayList<Double>> typeMap = componentValues.get(type);
		if (typeMap == null) return Double.NaN;

		ArrayList<Double> list = typeMap.get(component);
		if (list == null || list.isEmpty()) return Double.NaN;

		return list.get(list.size() - 1);
	}

	public double getMinimum(CADataType type, RocketComponent component) {
		if (type instanceof CADomainDataType) {
			return super.getMinimum(type);
		}

		Map<RocketComponent, Double> minMap = componentMinValues.get(type);
		if (minMap == null) return Double.NaN;

		Double min = minMap.get(component);
		return (min != null) ? min : Double.NaN;
	}

	public double getMaximum(CADataType type, RocketComponent component) {
		if (type instanceof CADomainDataType) {
			return super.getMaximum(type);
		}

		Map<RocketComponent, Double> maxMap = componentMaxValues.get(type);
		if (maxMap == null) return Double.NaN;

		Double max = maxMap.get(component);
		return (max != null) ? max : Double.NaN;
	}
}
