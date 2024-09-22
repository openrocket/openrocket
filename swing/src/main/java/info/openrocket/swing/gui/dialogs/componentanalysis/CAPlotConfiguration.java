package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.plot.Axis;
import info.openrocket.swing.gui.plot.PlotConfiguration;

import java.util.ArrayList;
import java.util.List;


public class CAPlotConfiguration extends PlotConfiguration<CADataType, CADataBranch> {
	private static final Translator trans = Application.getTranslator();

	private List<List<RocketComponent>> plotDataComponents = new ArrayList<>();

	public static final CAPlotConfiguration[] DEFAULT_CONFIGURATIONS;
	static {
		List<CAPlotConfiguration> configs = new ArrayList<>();
		CAPlotConfiguration config;

		//// Total CD vs Mach
		config = new CAPlotConfiguration(trans.get("CAPlotConfiguration.TotalCD"),
				CADomainDataType.MACH);
		config.addPlotDataType(CADataType.TOTAL_CD, 0);
		configs.add(config);

		DEFAULT_CONFIGURATIONS = configs.toArray(new CAPlotConfiguration[0]);
	}

	public CAPlotConfiguration(String name, CADomainDataType domainType) {
		super(name, domainType);
	}

	public CAPlotConfiguration(String name) {
		super(name, CADomainDataType.MACH);
	}

	@Override
	public void addPlotDataType(CADataType type, int axis) {
		super.addPlotDataType(type, axis);
		plotDataComponents.add(null);
	}

	@Override
	public void addPlotDataType(CADataType type) {
		super.addPlotDataType(type);
		plotDataComponents.add(null);
	}

	public List<RocketComponent> getComponents(int index) {
		return plotDataComponents.get(index);
	}

	public void setPlotDataComponents(int index, List<RocketComponent> components) {
		plotDataComponents.set(index, components);
	}

	public List<String> getComponentNames(int dataIndex) {
		List<RocketComponent> components = getComponents(dataIndex);
		List<String> names = new ArrayList<>(components.size());
		for (RocketComponent c : components) {
			names.add(c != null ? c.getName() : "");
		}
		return names;
	}

	@Override
	protected void calculateAxisBounds(List<CADataBranch> data) {
		int length = plotDataTypes.size();
		for (int i = 0; i < length; i++) {
			CADataType type = plotDataTypes.get(i);
			Unit unit = plotDataUnits.get(i);
			int index = plotDataAxes.get(i);
			if (index < 0) {
				throw new IllegalStateException("fitAxes called with auto-selected axis");
			}
			Axis axis = allAxes.get(index);

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (RocketComponent c : getComponents(i)) {
				min = Math.min(min, unit.toUnit(data.get(0).getMinimum(type, c)));
				max = Math.max(max, unit.toUnit(data.get(0).getMaximum(type, c)));
			}

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

	@SuppressWarnings("unchecked")
	public CAPlotConfiguration cloneConfiguration() {
		CAPlotConfiguration clone = super.cloneConfiguration();
		clone.plotDataComponents = new ArrayList<>(plotDataComponents.size());
		for (List<RocketComponent> components : plotDataComponents) {
			clone.plotDataComponents.add(components != null ? new ArrayList<>(components) : null);
		}
		return clone;
	}
}
