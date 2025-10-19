package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.plot.Plot;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.List;

public class CAPlot extends Plot<CADataType, CADataBranch, CAPlotConfiguration> {
	public CAPlot(String name, CADataBranch mainBranch, CAPlotConfiguration config,
					 List<CADataBranch> allBranches, boolean initialShowPoints) {
		super(name, mainBranch, config, allBranches, initialShowPoints);
	}

	@Override
	protected List<XYSeries> createSeriesForType(int dataIndex, int startIndex, CADataType type, Unit unit,
												 CADataBranch branch, int branchIdx, String branchName, String baseName) {
		// Get the component info
		List<RocketComponent> components = filledConfig.getComponents(dataIndex);
		List<String> componentNames = filledConfig.getComponentNames(dataIndex);

		// Create the series for each component
		List<XYSeries> allSeries = new ArrayList<>();
		for (int i = 0; i < components.size(); i++) {
			XYSeries series = createSingleSeries(startIndex*1000 + i, type, unit, branch, branchIdx, branchName, dataIndex, baseName,
					components.get(i), componentNames.get(i));
			allSeries.add(series);
		}

		return allSeries;
	}

	private XYSeries createSingleSeries(int key, CADataType type, Unit unit,
										CADataBranch branch, int branchIdx, String branchName, int dataIndex, String baseName,
										RocketComponent component, String componentName) {
		// Default implementation for regular DataBranch
		MetadataXYSeries series = new MetadataXYSeries(key, false, true, branchIdx, dataIndex, unit.getUnit(),
				branchName, baseName);

		// Create a new description that includes the component name
		String newBaseName = baseName;
		if (!componentName.isEmpty()) {
			newBaseName += " (" + componentName + ")";
		}
		series.setBaseName(newBaseName);
		series.updateDescription();

		List<Double> plotx = branch.get(filledConfig.getDomainAxisType());
		List<Double> ploty = branch.get(type, component);

		int pointCount = plotx.size();
		for (int j = 0; j < pointCount; j++) {
			double x = filledConfig.getDomainAxisUnit().toUnit(plotx.get(j));
			double y = unit.toUnit(ploty.get(j));
			series.add(x, y);
		}

		return series;
	}
}
