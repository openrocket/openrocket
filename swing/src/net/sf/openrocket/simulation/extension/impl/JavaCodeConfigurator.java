package net.sf.openrocket.simulation.extension.impl;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;

@Plugin
public class JavaCodeConfigurator extends AbstractSwingSimulationExtensionConfigurator<JavaCode> {
	
	public JavaCodeConfigurator() {
		super(JavaCode.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(final JavaCode extension, Simulation simulation, JPanel panel) {
		panel.add(new JLabel(trans.get("SimulationExtension.javacode.desc")), "wrap para");
		panel.add(new JLabel(trans.get("SimulationExtension.javacode.className")), "wrap rel");
		final JTextField textField = new JTextField(extension.getClassName());
		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			
			public void update() {
				extension.setClassName(textField.getText());
			}
		});
		panel.add(textField, "growx");
		return panel;
	}
	
}
