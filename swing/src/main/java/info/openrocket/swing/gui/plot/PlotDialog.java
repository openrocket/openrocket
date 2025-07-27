package info.openrocket.swing.gui.plot;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.List;

public abstract class PlotDialog<T extends DataType, B extends DataBranch<T>, C extends PlotConfiguration<T, B>,
		P extends Plot<T, B, C>> extends JDialog {
	protected static final Translator trans = Application.getTranslator();

	public PlotDialog(Window parent, String name, P plot, C config, List<B> allBranches, boolean initialShowPoints) {
		super(parent, name);
		this.setModalityType(ModalityType.DOCUMENT_MODAL);

		// Create the dialog
		JPanel panel = new JPanel(new MigLayout("fill, hidemode 3","[]","[grow][]"));
		this.add(panel);

		final ChartPanel chartPanel = new SimulationChart(plot.getJFreeChart());
		final JFreeChart jChart = plot.getJFreeChart();
		panel.add(chartPanel, "grow, wrap 20lp");

		// Ensures normal aspect-ratio of chart elements when resizing the panel
		chartPanel.setMinimumDrawWidth(0);
		chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
		chartPanel.setMinimumDrawHeight(0);
		chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

		//// Description text
		JLabel label = new StyledLabel(trans.get("PlotDialog.lbl.Chart"), -2);
		panel.add(label, "wrap");

		// Add X axis warnings
		addXAxisWarningMessage(panel, config);

		//// Show data points
		final JCheckBox checkData = new JCheckBox(trans.get("PlotDialog.CheckBox.Showdatapoints"));
		checkData.setSelected(initialShowPoints);
		checkData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean show = checkData.isSelected();
				Application.getPreferences().putBoolean(ApplicationPreferences.PLOT_SHOW_POINTS, show);
				plot.setShowPoints(show);
			}
		});
		panel.add(checkData, "split, left");

		// Show errors checkbox
		showErrorsCheckbox(panel, plot);

		// Add series selection box
		addSeriesSelectionBox(panel, plot, allBranches);

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

		//// Print chart button
		button = new JButton(trans.get("PlotDialog.btn.exportImage"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPNGExport(chartPanel,jChart);
			}
		});
		panel.add(button, "gapleft rel");

		//// Close button
		button = new JButton(trans.get("dlg.but.close"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlotDialog.this.dispose();
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

	protected void addXAxisWarningMessage(JPanel panel, C config) {
		// Default implementation does nothing
	}

	protected void showErrorsCheckbox(JPanel panel, P plot) {
		// Default implementation does nothing
	}

	protected void addSeriesSelectionBox(JPanel panel, P plot, List<B> allBranches) {
		// Default implementation does nothing
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
}
