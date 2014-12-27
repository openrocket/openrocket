package net.sf.openrocket.simulation.extension.impl;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;

@Plugin
public class ScriptingConfigurator extends AbstractSwingSimulationExtensionConfigurator<ScriptingExtension> {
	
	protected ScriptingConfigurator() {
		super(ScriptingExtension.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(final ScriptingExtension extension, Simulation simulation, JPanel panel) {
		
		panel.add(new StyledLabel(trans.get("SimulationExtension.scripting.script.label"), Style.BOLD), "wrap");
		
		final JTextArea text = new JTextArea(extension.getScript(), 20, 60);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(true);
		GUIUtil.setTabToFocusing(text);
		text.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent event) {
			}
			
			@Override
			public void focusLost(FocusEvent event) {
				String str = text.getText();
				if (!extension.getScript().equals(str)) {
					extension.setScript(str);
				}
			}
		});
		
		panel.add(new JScrollPane(text), "grow");
		
		return panel;
	}
	
}
