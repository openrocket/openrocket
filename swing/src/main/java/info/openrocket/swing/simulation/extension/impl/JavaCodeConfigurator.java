package info.openrocket.swing.simulation.extension.impl;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.extension.impl.JavaCode;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.simulation.extension.AbstractSwingSimulationExtensionConfigurator;

import java.awt.Color;

@Plugin
public class JavaCodeConfigurator extends AbstractSwingSimulationExtensionConfigurator<JavaCode> {
	private JavaCode extension;
	private JTextField classNameField;
	private StyledLabel errorMsg;

	private static final Translator trans = Application.getTranslator();

	private static Color darkErrorColor;

	static {
		initColors();
	}

	public JavaCodeConfigurator() {
		super(JavaCode.class);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(JavaCodeConfigurator::updateColors);
	}

	private static void updateColors() {
		darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
	}

	@Override
	protected JComponent getConfigurationComponent(final JavaCode extension, Simulation simulation, JPanel panel) {
		this.extension = extension;
		panel.add(new JLabel(trans.get("SimulationExtension.javacode.desc")), "wrap para");
		panel.add(new JLabel(trans.get("SimulationExtension.javacode.className")), "wrap rel");
		classNameField = new JTextField(extension.getClassName());
		panel.add(classNameField, "growx, wrap");
		this.errorMsg = new StyledLabel();
		errorMsg.setFontColor(darkErrorColor);
		errorMsg.setVisible(false);
		panel.add(errorMsg, "growx, wrap");

		classNameField.getDocument().addDocumentListener(new DocumentListener() {
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
				updateErrorMsg();
			}
		});
		updateErrorMsg();

		return panel;
	}

	private void updateErrorMsg() {
		if (this.errorMsg == null) {
			return;
		}
		// Display error message if the class name is invalid
		String text = classNameField.getText().trim();
		try {
			Class.forName(text);
			errorMsg.setVisible(false);
		} catch (ClassNotFoundException e) {
			// Don't display an error message for an empty field
			if (text.length() == 0) {
				errorMsg.setVisible(false);
				return;
			}
			errorMsg.setText(trans.get("SimulationExtension.javacode.classnotfound"));
			errorMsg.setVisible(true);
		}
	}

	@Override
	protected void close() {
		if (this.extension != null && this.classNameField != null) {
			this.extension.setClassName(this.classNameField.getText().trim());

		}
		super.close();
	}
	
}
