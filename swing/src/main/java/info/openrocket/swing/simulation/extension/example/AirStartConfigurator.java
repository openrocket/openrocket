package info.openrocket.swing.simulation.extension.example;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.extension.example.AirStart;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.swing.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import info.openrocket.core.unit.UnitGroup;

@Plugin
public class AirStartConfigurator extends AbstractSwingSimulationExtensionConfigurator<AirStart> {
	
	public AirStartConfigurator() {
		super(AirStart.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(AirStart extension, Simulation simulation, JPanel panel) {
		panel.add(new JLabel("Launch altitude:"));
		
		DoubleModel m = new DoubleModel(extension, "LaunchAltitude", UnitGroup.UNITS_DISTANCE, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 65lp!");
		
		UnitSelector unit = new UnitSelector(m);
		panel.add(unit, "w 25");
		
		BasicSlider slider = new BasicSlider(m.getSliderModel(0, 1000));
		panel.add(slider, "w 75lp, wrap");
		
		
		panel.add(new JLabel("Launch velocity:"));
		
		m = new DoubleModel(extension, "LaunchVelocity", UnitGroup.UNITS_VELOCITY, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 65lp!");
		
		unit = new UnitSelector(m);
		panel.add(unit, "w 25");
		
		slider = new BasicSlider(m.getSliderModel(0, 150));
		panel.add(slider, "w 75lp, wrap");
		
		return panel;
	}
	
}
