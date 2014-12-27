package net.sf.openrocket.simulation.extension.impl;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSwingSimulationExtensionConfigurator;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

@Plugin
public class ScriptingConfigurator extends AbstractSwingSimulationExtensionConfigurator<ScriptingExtension> {
	
	protected ScriptingConfigurator() {
		super(ScriptingExtension.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(final ScriptingExtension extension, Simulation simulation, JPanel panel) {
		
		panel.add(new StyledLabel(trans.get("SimulationExtension.scripting.script.label"), Style.BOLD), "wrap");
		
		final RSyntaxTextArea text = new RSyntaxTextArea(extension.getScript(), 15, 60);
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		text.setCodeFoldingEnabled(true);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(true);
		text.setCurrentLineHighlightColor(new Color(255, 255, 230));
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
		
		RTextScrollPane scroll = new RTextScrollPane(text);
		panel.add(scroll, "grow");
		
		return panel;
	}
	
}
