package net.sf.openrocket.utils;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.file.GeneralMotorLoader;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.GUIUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MotorPlot extends JFrame {

	
	public MotorPlot(List<String> filenames, List<Motor> motors) {
		
		JTabbedPane tabs = new JTabbedPane();
		for (int i=0; i<filenames.size(); i++) {
			JPanel pane = createPlotPanel((ThrustCurveMotor) motors.get(i));
			tabs.addTab(filenames.get(i), pane);
		}
		
		this.add(tabs);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.validate();
		this.pack();
	}
	
	
	private JPanel createPlotPanel(ThrustCurveMotor motor) {
		JPanel panel = new JPanel(new MigLayout());
		

        XYSeries series = new XYSeries("", false, true);
        double[] time = motor.getTimePoints();
        double[] thrust = motor.getThrustPoints();
		
        for (int i=0; i<time.length; i++) {
        	series.add(time[i], thrust[i]);
        }
        
		// Create the chart using the factory to get all default settings
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Motor thrust curve",
            "Time / s", 
            "Thrust / N", 
            new XYSeriesCollection(series),
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        ((XYLineAndShapeRenderer)chart.getXYPlot().getRenderer()).setShapesVisible(true);
        
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
		
		panel.add(chartPanel, "grow, wrap para");
        
		
		JTextArea area = new JTextArea(5, 40);
		StringBuilder sb = new StringBuilder();
		sb.append("Designation:  ").append(motor.getDesignation()).append("        ");
		sb.append("Manufacturer: ").append(motor.getManufacturer()).append('\n');
		sb.append("Comment:\n").append(motor.getDescription());
		area.setText(sb.toString());
		panel.add(area, "grow");
		
		
		return panel;
	}
	
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.println("MotorPlot <files>");
			System.exit(1);
		}
		
		final List<String> filenames = new ArrayList<String>();
		final List<Motor> motors = new ArrayList<Motor>();
		
		GeneralMotorLoader loader = new GeneralMotorLoader();
		for (String file: args) {
			for (Motor m: loader.load(new FileInputStream(file), file)) {
				filenames.add(file);
				motors.add(m);
			}
		}
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				GUIUtil.setBestLAF();
				
				MotorPlot plot = new MotorPlot(filenames, motors);
				plot.setVisible(true);
			}
			
		});
		
	}
	
	
	
	
}
