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
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import net.sf.openrocket.unit.UnitGroup;

@Plugin
public class RollControlConfigurator extends AbstractSwingSimulationExtensionConfigurator<RollControl> {
	private JPanel panel;
	private RollControl extension;
	
	public RollControlConfigurator() {
		super(RollControl.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(RollControl extension, Simulation simulation, JPanel panel) {
		this.panel = panel;
		this.extension = extension;
		panel.add(new JLabel("Control FinSet Name:"));

		JTextField finSetName = new JTextField();
		finSetName.setText(extension.getControlFinName());

		finSetName.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {}
				
				@Override
				public void focusLost(FocusEvent e) {
					extension.setControlFinName(finSetName.getText());
				}
			});

		panel.add(finSetName, "growx, span3, wrap");
		
		addRow("Start Time", "StartTime", UnitGroup.UNITS_SHORT_TIME, 0.0, 1200.0);
		addRow("Desired Roll Rate", "SetPoint", UnitGroup.UNITS_ROLL, -10.0, 10.0);
		addRow("Max Fin Turn Rate", "FinRate", UnitGroup.UNITS_ROLL, 0, 10);
		addRow("Max Fin Angle", "MaxFinAngle", UnitGroup.UNITS_ANGLE, 0, 0.25);

		panel.add(new JLabel("PID parameters:"), "newline 0.25in, span4, wrap");
		panel.add(new JLabel("At M=0.3 KP oscillation threshold between 0.35 and 0.4.    Good KI=3"  ), "gapbefore indent, span4, wrap");
		panel.add(new JLabel("At M=0.6 KP oscillation threshold between 0.07 and 0.08    Good KI=2"  ), "gapbefore indent, span4, wrap");
		panel.add(new JLabel("At M=0.9 KP oscillation threshold between 0.013 and 0.014  Good KI=0.5"), "gapbefore indent, span4, wrap");
		
		addRow("KP (Proportional)", "KP", UnitGroup.UNITS_COEFFICIENT, 0.0, 0.02);
		addRow("KI (Integrated)", "KI", UnitGroup.UNITS_COEFFICIENT, 0.0, 1.0);
		
		return panel;
	}

	private void addRow(String prompt, String fieldName, UnitGroup units, double min, double max) {
		panel.add(new JLabel(prompt + ":"));
		
		DoubleModel m = new DoubleModel(extension, fieldName, units, min, max);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "w 65lp!");
		
		UnitSelector unit = new UnitSelector(m);
		panel.add(unit, "w 25");
		
		BasicSlider slider = new BasicSlider(m.getSliderModel(0, 1000));
		panel.add(slider, "w 75lp, wrap");

	}		
}
