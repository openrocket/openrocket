package net.sf.openrocket.utils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MotorPlot extends JDialog {
	
	private int selected = -1;
	private static final Translator trans = Application.getTranslator();
	
	public MotorPlot(List<String> filenames, List<ThrustCurveMotor> motors) {
		//// Motor plot
		super((JFrame) null, trans.get("MotorPlot.title.Motorplot"), true);
		
		JTabbedPane tabs = new JTabbedPane();
		for (int i = 0; i < filenames.size(); i++) {
			JPanel pane = createPlotPanel((ThrustCurveMotor) motors.get(i));
			
			//// Select button
			JButton button = new JButton(trans.get("MotorPlot.but.Select"));
			final int number = i;
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selected = number;
					MotorPlot.this.setVisible(false);
				}
			});
			pane.add(button, "wrap", 0);
			
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
		
		for (int i = 0; i < time.length; i++) {
			series.add(time[i], thrust[i]);
		}
		
		// Create the chart using the factory to get all default settings
		JFreeChart chart = ChartFactory.createXYLineChart(
				//// Motor thrust curve
				trans.get("MotorPlot.Chart.Motorthrustcurve"),
				//// Time / s
				trans.get("MotorPlot.Chart.Time"),
				//// Thrust / N
				trans.get("MotorPlot.Chart.Thrust"),
				new XYSeriesCollection(series),
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);
		
		((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).setShapesVisible(true);
		
		ChartPanel chartPanel = new ChartPanel(chart,
				false, // properties
				true, // save
				false, // print
				true, // zoom
				true); // tooltips
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setEnforceFileExtensions(true);
		chartPanel.setInitialDelay(500);
		
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		
		panel.add(chartPanel, "grow, wrap para");
		

		JTextArea area = new JTextArea(5, 40);
		StringBuilder sb = new StringBuilder();
		//// Designation:  
		sb.append("MotorPlot.txt.Designation" + "  ").append(motor.getDesignation()).append("        ");
		//// Manufacturer:
		sb.append("MotorPlot.txt.Manufacturer" + " ").append(motor.getManufacturer()).append("        ");
		//// Type:
		sb.append("MotorPlot.txt.Type" + " ").append(motor.getMotorType()).append('\n');
		//// Delays:
		sb.append("MotorPlot.txt.Delays" +" ").append(Arrays.toString(motor.getStandardDelays())).append('\n');
		//// Comment:\n
		sb.append("MotorPlot.txt.Comment" + " ").append(motor.getDescription());
		area.setText(sb.toString());
		panel.add(area, "grow, wrap");
		

		return panel;
	}
	
	

	public int getSelected() {
		return selected;
	}
	
	
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.println("MotorPlot <files>");
			System.exit(1);
		}
		
		final List<String> filenames = new ArrayList<String>();
		final List<ThrustCurveMotor> motors = new ArrayList<ThrustCurveMotor>();
		
		GeneralMotorLoader loader = new GeneralMotorLoader();
		for (String file : args) {
			for (Motor m : loader.load(new FileInputStream(file), file)) {
				filenames.add(file);
				motors.add((ThrustCurveMotor) m);
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
