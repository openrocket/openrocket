package info.openrocket.swing.simulation.extension.example;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.extension.example.StopSimulation;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.swing.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import info.openrocket.core.unit.UnitGroup;

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
