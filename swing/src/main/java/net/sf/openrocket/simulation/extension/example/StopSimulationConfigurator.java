package net.sf.openrocket.simulation.extension.example;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import net.sf.openrocket.unit.UnitGroup;

@Plugin
public class StopSimulationConfigurator extends AbstractSwingSimulationExtensionConfigurator<StopSimulation> {
	private JPanel panel;
	private StopSimulation extension;
	
	public StopSimulationConfigurator() {
		super(StopSimulation.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(StopSimulation extension, Simulation simulation, JPanel panel) {
		this.panel = panel;
		this.extension = extension;
		
		addRow("Report Rate", "ReportRate", 0, 1000);
		addRow("Stop Step", "StopStep", 0, 50000);
		addRow("Stop Time", "StopTime", 0, 1000);
		
		return panel;
	}

	private void addRow(String prompt, String fieldName, int min, int max) {
		panel.add(new JLabel(prompt + ":"));
		
		IntegerModel m = new IntegerModel(extension, fieldName, min, max);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 65lp!");
		
		BasicSlider slider = new BasicSlider(m.getSliderModel());
		panel.add(slider, "w 75lp, wrap");
	}		
}
