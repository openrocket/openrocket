package net.sf.openrocket.gui.plot;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;

import org.jfree.chart.ChartPanel;

/**
 * Dialog that shows a plot of a simulation results based on user options.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationPlotDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
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
		panel.add(chartPanel, "grow, wrap 20lp");
		
		//// Description text
		JLabel label = new StyledLabel(trans.get("PlotDialog.lbl.Chart"), -2);
		panel.add(label, "wrap");
		
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
		
		//// Zoom in button
		JButton button = new JButton(Icons.ZOOM_IN);
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
		button = new JButton(Icons.ZOOM_RESET);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chartPanel.actionPerformed(new ActionEvent(chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_RESET_BOTH_COMMAND));
			}
		});
		panel.add(button, "gapleft rel");
		
		
		//// Zoom out button
		button = new JButton(Icons.ZOOM_OUT);
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
		
		//// Add series selection box
		ArrayList<String> stages = new ArrayList<String>();
		stages.add("All");
		stages.addAll(Util.generateSeriesLabels(simulation));
		
		final JComboBox stageSelection = new JComboBox(stages.toArray(new String[0]));
		stageSelection.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				int selectedStage = stageSelection.getSelectedIndex() - 1;
				myPlot.setShowBranch(selectedStage);
			}
			
		});
		if (stages.size() > 2) {
			// Only show the combo box if there are at least 3 entries (ie, "All", "Main", and one other one
			panel.add(stageSelection, "gapleft rel");
		}
		
		//// Spacer for layout to push close button to the right.
		panel.add(new JPanel(), "growx");
		
		//// Close button
		button = new JButton(trans.get("dlg.but.close"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationPlotDialog.this.dispose();
			}
		});
		panel.add(button, "right");
		
		this.setLocationByPlatform(true);
		this.pack();
		
		GUIUtil.setDisposableDialogOptions(this, button);
		GUIUtil.rememberWindowSize(this);
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
