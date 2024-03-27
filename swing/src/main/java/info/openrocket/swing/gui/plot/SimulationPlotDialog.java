package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

/**
 * Dialog that shows a plot of a simulation results based on user options.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationPlotDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();

	private static Color darkWarningColor;

	static {
		initColors();
	}

	private SimulationPlotDialog(Window parent, Simulation simulation, PlotConfiguration config) {
		//// Flight data plot
		super(parent, simulation.getName());
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		final boolean initialShowPoints = Application.getPreferences().getBoolean(Preferences.PLOT_SHOW_POINTS, false);
		
		final SimulationPlot myPlot = new SimulationPlot(simulation, config, initialShowPoints);
		
		// Create the dialog
		JPanel panel = new JPanel(new MigLayout("fill","[]","[grow][]"));
		this.add(panel);
		
		final ChartPanel chartPanel = new SimulationChart(myPlot.getJFreeChart());
		final JFreeChart jChart = myPlot.getJFreeChart();
		panel.add(chartPanel, "grow, wrap 20lp");

		// Ensures normal aspect-ratio of chart elements when resizing the panel
		chartPanel.setMinimumDrawWidth(0);
		chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
		chartPanel.setMinimumDrawHeight(0);
		chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
		
		//// Description text
		JLabel label = new StyledLabel(trans.get("PlotDialog.lbl.Chart"), -2);
		panel.add(label, "wrap");

		// Add warning if X axis type is not time
		if (config.getDomainAxisType() != FlightDataType.TYPE_TIME) {
			JLabel msg = new StyledLabel(trans.get("PlotDialog.lbl.timeSeriesWarning"), -2);
			msg.setForeground(darkWarningColor);
			panel.add(msg, "wrap");
		}
		
		//// Show data points
		final JCheckBox check = new JCheckBox(trans.get("PlotDialog.CheckBox.Showdatapoints"));
		check.setSelected(initialShowPoints);
		check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean show = check.isSelected();
				Application.getPreferences().putBoolean(Preferences.PLOT_SHOW_POINTS, show);
				myPlot.setShowPoints(show);
			}
		});
		panel.add(check, "split, left");

		//// Add series selection box
		ArrayList<String> stages = new ArrayList<String>();
		stages.add("All");
		stages.addAll(Util.generateSeriesLabels(simulation));

		final JComboBox<String> stageSelection = new JComboBox<>(stages.toArray(new String[0]));
		stageSelection.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int selectedStage = stageSelection.getSelectedIndex() - 1;
				myPlot.setShowBranch(selectedStage);
			}

		});
		if (stages.size() > 2) {
			// Only show the combo box if there are at least 3 entries (ie, "All", "Main", and one other one)
			panel.add(stageSelection, "gapleft rel");
		}
		
		//// Zoom in button
		JButton button = new SelectColorButton(Icons.ZOOM_IN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK) {
					chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_IN_DOMAIN_COMMAND));
				} else {
					chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_IN_BOTH_COMMAND));
					
				}
			}
		});
		panel.add(button, "gapleft rel");
		
		//// Reset Zoom button.
		button = new SelectColorButton(Icons.ZOOM_RESET);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_RESET_BOTH_COMMAND));
			}
		});
		panel.add(button, "gapleft rel");
		
		
		//// Zoom out button
		button = new SelectColorButton(Icons.ZOOM_OUT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ((e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK) {
					chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_OUT_DOMAIN_COMMAND));
				} else {
					chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_OUT_BOTH_COMMAND));
				}
			}
		});
		panel.add(button, "gapleft rel");

		//// Print chart button
		button = new SelectColorButton(trans.get("PlotDialog.btn.exportImage"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPNGExport(chartPanel,jChart);
			}
		});
		panel.add(button, "gapleft rel");
		
		//// Close button
		button = new SelectColorButton(trans.get("dlg.but.close"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationPlotDialog.this.dispose();
			}
		});
		panel.add(button, "gapbefore push, right");
		this.setLocationByPlatform(true);
		this.pack();
		
		GUIUtil.setDisposableDialogOptions(this, button);
		GUIUtil.rememberWindowSize(this);
		this.setLocationByPlatform(true);
		GUIUtil.rememberWindowPosition(this);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationPlotDialog::updateColors);
	}

	private static void updateColors() {
		darkWarningColor = GUIUtil.getUITheme().getDarkWarningColor();
	}

	private boolean doPNGExport(ChartPanel chartPanel, JFreeChart chart){
		JFileChooser chooser = new SaveFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(FileHelper.PNG_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		//// Ensures No Problems When Choosing File
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;

		File file = chooser.getSelectedFile();
		if (file == null)
			return false;

		file = FileHelper.forceExtension(file, "png");
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}

		//// Uses JFreeChart Built In PNG Export Method
		try{
			ChartUtils.saveChartAsPNG(file, chart, chartPanel.getWidth(), chartPanel.getHeight());
		} catch(Exception e){
			return false;
		}

		return true;
	}


	/**
	 * Static method that shows a plot with the specified parameters.
	 * 
	 * @param parent		the parent window, which will be blocked.
	 * @param simulation	the simulation to plot.
	 * @param config		the configuration of the plot.
	 */
	public static SimulationPlotDialog getPlot(Window parent, Simulation simulation, PlotConfiguration config) {
		return new SimulationPlotDialog(parent, simulation, config);
	}
	
}