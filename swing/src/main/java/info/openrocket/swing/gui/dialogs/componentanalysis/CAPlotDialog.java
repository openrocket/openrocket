package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.plot.PlotDialog;

import java.awt.Window;
import java.util.List;

public class CAPlotDialog extends PlotDialog<CADataType, CADataBranch, CAPlotConfiguration, CAPlot> {
	private CAPlotDialog(Window parent, String name, CAPlot plot, CAPlotConfiguration config, List<CADataBranch> allBranches, boolean initialShowPoints) {
		super(parent, name, plot, config, allBranches, initialShowPoints);
	}

	public static CAPlotDialog create(Window parent, String name, CAPlotConfiguration config, List<CADataBranch> allBranches) {
		final boolean initialShowPoints = Application.getPreferences().getBoolean(ApplicationPreferences.PLOT_SHOW_POINTS, false);
		final CAPlot plot = new CAPlot(name, allBranches.get(0), config, allBranches, initialShowPoints);

		return new CAPlotDialog(parent, name, plot, config, allBranches, initialShowPoints);
	}
}
