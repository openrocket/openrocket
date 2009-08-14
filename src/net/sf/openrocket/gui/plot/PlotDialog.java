package net.sf.openrocket.gui.plot;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotDialog extends JDialog {
	
	private PlotDialog(Window parent, Simulation simulation, PlotConfiguration config) {
		super(parent, "Flight data plot");
		this.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		
		// Fill the auto-selections
		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		PlotConfiguration filled = config.fillAutoAxes(branch);
		List<Axis> axes = filled.getAllAxes();


		// Create the data series for both axes
		XYSeriesCollection[] data = new XYSeriesCollection[2];
		data[0] = new XYSeriesCollection();
		data[1] = new XYSeriesCollection();
		
		
		// Get the domain axis type
		final FlightDataBranch.Type domainType = filled.getDomainAxisType();
		final Unit domainUnit = filled.getDomainAxisUnit();
		if (domainType == null) {
			throw new IllegalArgumentException("Domain axis type not specified.");
		}
		List<Double> x = branch.get(domainType);
		
		
		// Get plot length (ignore trailing NaN's)
		int typeCount = filled.getTypeCount();
		int dataLength = 0;
		for (int i=0; i<typeCount; i++) {
			FlightDataBranch.Type type = filled.getType(i);
			List<Double> y = branch.get(type);
			
			for (int j = dataLength; j < y.size(); j++) {
				if (!Double.isNaN(y.get(j)) && !Double.isInfinite(y.get(j)))
					dataLength = j;
			}
		}
		dataLength = Math.min(dataLength, x.size());
		
		
		// Create the XYSeries objects from the flight data and store into the collections
		String[] axisLabel = new String[2];
		for (int i = 0; i < typeCount; i++) {
			// Get info
			FlightDataBranch.Type type = filled.getType(i);
			Unit unit = filled.getUnit(i);
			int axis = filled.getAxis(i);
			String name = getLabel(type, unit);
			
			// Store data in provided units
			List<Double> y = branch.get(type);
			XYSeries series = new XYSeries(name, false, true);
			for (int j=0; j < dataLength; j++) {
				series.add(domainUnit.toUnit(x.get(j)), unit.toUnit(y.get(j)));
			}
			data[axis].addSeries(series);

			// Update axis label
			if (axisLabel[axis] == null)
				axisLabel[axis] = type.getName();
			else
				axisLabel[axis] += "; " + type.getName();
		}
		
		
		// Create the chart using the factory to get all default settings
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Simulated flight",
            null, 
            null, 
            null,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
		
        chart.addSubtitle(new TextTitle(config.getName()));
        
		// Add the data and formatting to the plot
		XYPlot plot = chart.getXYPlot();
		int axisno = 0;
		for (int i=0; i<2; i++) {
			// Check whether axis has any data
			if (data[i].getSeriesCount() > 0) {
				// Create and set axis
				double min = axes.get(i).getMinValue();
				double max = axes.get(i).getMaxValue();
				NumberAxis axis = new PresetNumberAxis(min, max);
				axis.setLabel(axisLabel[i]);
//				axis.setRange(axes.get(i).getMinValue(), axes.get(i).getMaxValue());
				plot.setRangeAxis(axisno, axis);
				
				// Add data and map to the axis
				plot.setDataset(axisno, data[i]);
				plot.setRenderer(axisno, new StandardXYItemRenderer());
				plot.mapDatasetToRangeAxis(axisno, axisno);
				axisno++;
			}
		}
		
		plot.getDomainAxis().setLabel(getLabel(domainType,domainUnit));
		plot.addDomainMarker(new ValueMarker(0));
		plot.addRangeMarker(new ValueMarker(0));
		
		
		// Create the dialog
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		this.add(panel);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true,  // save
				false, // print
				true,  // zoom
				true); // tooltips
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setEnforceFileExtensions(true);
		chartPanel.setInitialDelay(500);
		
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		
		panel.add(chartPanel, "grow, wrap 20lp");

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlotDialog.this.dispose();
			}
		});
		panel.add(button, "right");

		this.setLocationByPlatform(true);
		this.pack();
		GUIUtil.installEscapeCloseOperation(this);
		GUIUtil.setDefaultButton(button);
	}
	
	
	private String getLabel(FlightDataBranch.Type type, Unit unit) {
		String name = type.getName();
		if (unit != null  &&  !UnitGroup.UNITS_NONE.contains(unit)  &&
				!UnitGroup.UNITS_COEFFICIENT.contains(unit) && unit.getUnit().length() > 0)
			name += " ("+unit.getUnit() + ")";
		return name;
	}
	

	
	private class PresetNumberAxis extends NumberAxis {
		private final double min;
		private final double max;
		
		public PresetNumberAxis(double min, double max) {
			this.min = min;
			this.max = max;
			autoAdjustRange();
		}
		
		@Override
		protected void autoAdjustRange() {
			this.setRange(min, max);
		}
	}
	
	
	/**
	 * Static method that shows a plot with the specified parameters.
	 * 
	 * @param parent		the parent window, which will be blocked.
	 * @param simulation	the simulation to plot.
	 * @param config		the configuration of the plot.
	 */
	public static void showPlot(Window parent, Simulation simulation, PlotConfiguration config) {
		new PlotDialog(parent, simulation, config).setVisible(true);
	}
	
}
