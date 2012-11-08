package net.sf.openrocket.gui.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MathUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

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

		final SimulationPlot myPlot = new SimulationPlot(simulation, config, initialShowPoints );
		
		// Create the dialog
		JPanel panel = new JPanel(new MigLayout("fill"));
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

		//// Zoom out button
		JButton button = new JButton(Icons.ZOOM_OUT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( (e.getModifiers() & InputEvent.ALT_MASK)  == InputEvent.ALT_MASK ) {
					chartPanel.actionPerformed( new ActionEvent( chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_OUT_DOMAIN_COMMAND));
				} else {
					chartPanel.actionPerformed( new ActionEvent( chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_OUT_BOTH_COMMAND));
				}
			}
		});
		panel.add(button, "gapleft rel");

		//// Zoom in button
		button = new JButton(Icons.ZOOM_IN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( (e.getModifiers() & InputEvent.ALT_MASK)  == InputEvent.ALT_MASK ) {
					chartPanel.actionPerformed( new ActionEvent( chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_IN_DOMAIN_COMMAND));
				} else {
					chartPanel.actionPerformed( new ActionEvent( chartPanel, ActionEvent.ACTION_FIRST, ChartPanel.ZOOM_IN_BOTH_COMMAND));

				}
			}
		});
		panel.add(button, "gapleft rel");

		//// Add series selection box
		//// FIXME
		List<String> stages = new ArrayList<String>();
		stages.add("All");
		for ( int i=0; i< simulation.getSimulatedData().getBranchCount(); i++ ) {
			stages.add( simulation.getSimulatedData().getBranch(i).getBranchName() + " (" + i + ")");
		}
		final JComboBox<String> stageSelection = new JComboBox<String>(stages.toArray(new String[0]));
		stageSelection.addItemListener( new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int selectedStage = stageSelection.getSelectedIndex() -1;
				myPlot.setShowBranch(selectedStage);
			}
			
		});
		if ( stages.size() > 1 ) {
			panel.add(stageSelection, "gapleft rel");
			panel.add(new JPanel(), "growx");
		}
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
	public static void showPlot(Window parent, Simulation simulation, PlotConfiguration config) {
		new SimulationPlotDialog(parent, simulation, config).setVisible(true);
	}

}
