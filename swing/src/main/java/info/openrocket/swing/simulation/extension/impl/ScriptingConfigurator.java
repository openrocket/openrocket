package info.openrocket.swing.simulation.extension.impl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.extension.impl.ScriptingExtension;
import info.openrocket.core.simulation.extension.impl.ScriptingUtil;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.swing.simulation.extension.AbstractSwingSimulationExtensionConfigurator;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.google.inject.Inject;

@Plugin
public class ScriptingConfigurator extends AbstractSwingSimulationExtensionConfigurator<ScriptingExtension> {
	@Inject
	private ScriptingUtil util;
	
	private JComboBox languageSelector;
	private RSyntaxTextArea text;
	private JCheckBox trusted;
	
	private ScriptingExtension extension;
	private Simulation simulation;

	public ScriptingConfigurator() {
		super(ScriptingExtension.class);
	}
	
	@Override
	protected JComponent getConfigurationComponent(final ScriptingExtension extension, Simulation simulation, JPanel panel) {
		this.extension = extension;
		this.simulation = simulation;
		
		panel.add(new StyledLabel(trans.get("SimulationExtension.scripting.language.label"), Style.BOLD), "spanx, split");
		
		String[] languages = util.getLanguages().toArray(new String[0]);
		languageSelector = new JComboBox(languages);
		languageSelector.setEditable(false);
		languageSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLanguage((String) languageSelector.getSelectedItem());
			}
		});
		panel.add(languageSelector, "wrap para");
		
		text = new RSyntaxTextArea(extension.getScript(), 20, 80);
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		text.setCodeFoldingEnabled(true);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setEditable(true);
		GUIUtil.getUITheme().formatScriptTextArea(text);
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
		panel.add(scroll, "spanx, grow, wrap para");
		
		BooleanModel enabled = new BooleanModel(extension, "Enabled");
		JCheckBox check = new JCheckBox(enabled);
		check.setText(trans.get("SimulationExtension.scripting.text.enabled"));
		check.setToolTipText(trans.get("SimulationExtension.scripting.text.enabled.ttip"));
		panel.add(check, "spanx, wrap rel");
		
		trusted = new JCheckBox(trans.get("SimulationExtension.scripting.text.trusted"));
		trusted.setSelected(util.isTrustedScript(extension.getLanguage(), extension.getScript()));
		panel.add(trusted, "spanx, split");
		
		panel.add(new JPanel(), "growx");
		
		JButton button = new SelectColorButton(trans.get("SimulationExtension.scripting.text.trusted.clear"));
		button.setToolTipText(trans.get("SimulationExtension.scripting.text.trusted.clear.ttip"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				util.clearTrustedScripts();
				JOptionPane.showMessageDialog(getDialog(), trans.get("SimulationExtension.scripting.text.trusted.cleared"),
						trans.get("SimulationExtension.scripting.text.trusted.cleared.title"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		panel.add(button, "wrap rel");
		
		StyledLabel label = new StyledLabel(trans.get("SimulationExtension.scripting.text.trusted.msg"), -1, Style.ITALIC);
		panel.add(label);
		
		setLanguage(util.getLanguage(extension.getLanguage()));
		
		return panel;
	}
	
	@Override
	protected void close() {
		util.setTrustedScript(extension.getLanguage(), extension.getScript(), trusted.isSelected());
		super.close();
	}
	
	private void setLanguage(String language) {
		if (language == null) {
			language = "";
		}
		if (!language.equals(languageSelector.getSelectedItem())) {
			languageSelector.setSelectedItem(language);
		}
		extension.setLanguage(language);
		text.setSyntaxEditingStyle(findSyntaxLanguage(language));
		getDialog().setTitle(getTitle(extension, simulation));
	}
	
	private String findSyntaxLanguage(String language) {
		ScriptEngine engine = util.getEngineByName(language);
		
		if (engine != null) {
			Set<String> supported = TokenMakerFactory.getDefaultInstance().keySet();
			for (String type : engine.getFactory().getMimeTypes()) {
				if (supported.contains(type)) {
					return type;
				}
				for (String match : supported) {
					if (match.contains("/" + language.toLowerCase())) {
						return match;
					}
				}
			}
		}
		
		return SyntaxConstants.SYNTAX_STYLE_NONE;
	}
}
