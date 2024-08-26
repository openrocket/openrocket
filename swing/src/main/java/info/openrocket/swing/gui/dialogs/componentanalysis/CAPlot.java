package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.plot.Plot;
import org.jfree.data.xy.XYSeries;

import java.util.Collections;
import java.util.List;

public class CAPlot extends Plot<CADataType, CADataBranch, CAPlotConfiguration> {
	public CAPlot(String name, CADataBranch mainBranch, CAPlotConfiguration config,
					 List<CADataBranch> allBranches, boolean initialShowPoints) {
		super(name, mainBranch, config, allBranches, initialShowPoints);
	}

	@Override
	protected List<XYSeries> createSeriesForType(int dataIndex, int startIndex, CADataType type, Unit unit,
												 CADataBranch branch, int branchIdx, String branchName, String baseName) {
		// Default implementation for regular DataBranch
		MetadataXYSeries series = new MetadataXYSeries(startIndex, false, true, branchIdx, unit.getUnit(),
				branchName, baseName);

		// Get the component name
		String componentName = filledConfig.getComponentName(dataIndex);

		// Create a new description that includes the component name
		String newBaseName = baseName;
		if (!componentName.isEmpty()) {
			newBaseName += " (" + componentName + ")";
		}
		series.setBaseName(newBaseName);
		series.updateDescription();

		List<Double> plotx = branch.get(filledConfig.getDomainAxisType());
		List<Double> ploty = branch.get(type, filledConfig.getComponent(dataIndex));

		int pointCount = plotx.size();
		for (int j = 0; j < pointCount; j++) {
			double x = filledConfig.getDomainAxisUnit().toUnit(plotx.get(j));
			double y = unit.toUnit(ploty.get(j));
			series.add(x, y);
		}

		return Collections.singletonList(series);
	}
}
