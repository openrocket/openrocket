package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

/**
 * Dialog that shows a plot of a simulation results based on user options.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationPlotDialog extends PlotDialog<FlightDataType, FlightDataBranch, SimulationPlotConfiguration,
		SimulationPlot> {
	private static final Translator trans = Application.getTranslator();

	private final Simulation simulation;

	private static Color darkErrorColor;
	private JCheckBox checkErrors;

	static {
		initColors();
	}

	private SimulationPlotDialog(Window parent, Simulation simulation, SimulationPlotConfiguration config,
								 SimulationPlot plot, boolean initialShowPoints) {
		super(parent, simulation.getName(), plot, config, simulation.getSimulatedData().getBranches(), initialShowPoints);
		this.simulation = simulation;
		this.checkErrors.setVisible(this.simulation.hasErrors());
	}

	/**
	 * Static method that shows a plot with the specified parameters.
	 *
	 * @param parent		the parent window, which will be blocked.
	 * @param simulation	the simulation to plot.
	 * @param config		the configuration of the plot.
	 */
	public static SimulationPlotDialog getPlot(Window parent, Simulation simulation, SimulationPlotConfiguration config) {
		final boolean initialShowPoints = Application.getPreferences().getBoolean(ApplicationPreferences.PLOT_SHOW_POINTS, false);
		final SimulationPlot plot = SimulationPlot.create(simulation, config, initialShowPoints);

		return new SimulationPlotDialog(parent, simulation, config, plot, initialShowPoints);
	}

	@Override
	protected void addXAxisWarningMessage(JPanel panel, SimulationPlotConfiguration config) {
		// Add warning if X axis type is not time
		if (config.getDomainAxisType() != FlightDataType.TYPE_TIME) {
			JLabel msg = new StyledLabel(trans.get("PlotDialog.lbl.timeSeriesWarning"), -2);
			msg.setForeground(darkErrorColor);
			panel.add(msg, "wrap");
		}
	}

	@Override
	protected void showErrorsCheckbox(JPanel panel, SimulationPlot plot) {
		//// Show errors if any
		//// Always enable 'show errors' initially; make user turn it off for themselves
		this.checkErrors = new JCheckBox(trans.get("PlotDialog.CheckBox.ShowErrors"));
		this.checkErrors.setSelected(true);
		this.checkErrors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plot.setShowErrors(checkErrors.isSelected());
			}
		});
		panel.add(this.checkErrors, "split, left");
	}

	@Override
	protected void addSeriesSelectionBox(JPanel panel, SimulationPlot plot, List<FlightDataBranch> allBranches) {
		ArrayList<String> stages = new ArrayList<>();
		stages.add(trans.get("PlotDialog.StageDropDown.allStages"));
		stages.addAll(Util.generateSeriesLabels(allBranches));

		final JComboBox<String> stageSelection = new JComboBox<>(stages.toArray(new String[0]));
		stageSelection.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (simulation == null) {
					return;
				}
				int selectedStage = stageSelection.getSelectedIndex() - 1;
				checkErrors.setEnabled(selectedStage == -1 ? simulation.hasErrors() : simulation.hasErrors(selectedStage));
				plot.setShowBranch(selectedStage);
			}

		});
		if (stages.size() > 2) {
			// Only show the combo box if there are at least 3 entries (ie, "All", "Main", and one other one)
			panel.add(stageSelection, "gapleft rel");
		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationPlotDialog::updateColors);
	}

	public static void updateColors() {
		darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
	}
}
