package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.startup.Preferences;

import com.itextpdf.text.Font;

public class GraphicsPreferencesPanel extends PreferencesPanel {

	public GraphicsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("fillx"));
		
		this.add(new JPanel(new MigLayout("fill, ins n n n")) {
			{ //Editor Options		
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.lbl.DecalEditor"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				ButtonGroup execGroup = new ButtonGroup();
				
				JRadioButton showPrompt = new JRadioButton(trans.get("EditDecalDialog.lbl.prompt"));
				showPrompt.setSelected(!preferences.isDecalEditorPreferenceSet());
				showPrompt.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (((JRadioButton) e.getItem()).isSelected()) {
							preferences.clearDecalEditorPreference();
						}
					}
				});
				add(showPrompt, "wrap");
				execGroup.add(showPrompt);
				
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
					
					JRadioButton systemRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.system"));
					systemRadio.setSelected(preferences.isDecalEditorPreferenceSystem());
					systemRadio.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (((JRadioButton) e.getItem()).isSelected()) {
								preferences.setDecalEditorPreference(true, null);
							}
						}
					});
					add(systemRadio, "wrap");
					execGroup.add(systemRadio);
					
				}
				
				boolean commandLineIsSelected = preferences.isDecalEditorPreferenceSet() && !preferences.isDecalEditorPreferenceSystem();
				final JRadioButton commandRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.cmdline"));
				commandRadio.setSelected(commandLineIsSelected);
				add(commandRadio, "wrap");
				execGroup.add(commandRadio);
				
				final JTextField commandText = new JTextField();
				commandText.setEnabled(commandLineIsSelected);
				commandText.setText(commandLineIsSelected ? preferences.getDecalEditorCommandLine() : "");
				commandText.getDocument().addDocumentListener(new DocumentListener() {
					
					@Override
					public void insertUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void removeUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void changedUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
				});
				add(commandText, "growx, wrap");
				
				final JButton chooser = new JButton(trans.get("EditDecalDialog.btn.chooser"));
				chooser.setEnabled(commandLineIsSelected);
				chooser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						int action = fc.showOpenDialog(SwingUtilities.windowForComponent(GraphicsPreferencesPanel.this.parentDialog));
						if (action == JFileChooser.APPROVE_OPTION) {
							String commandLine = fc.getSelectedFile().getAbsolutePath();
							commandText.setText(commandLine);
							preferences.setDecalEditorPreference(false, commandLine);
						}
						
					}
					
				});
				add(chooser, "wrap");
				
				
				commandRadio.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						boolean enabled = commandRadio.isSelected();
						commandText.setEnabled(enabled);
						chooser.setEnabled(enabled);
					}
					
				});
			}
		}, "growx, span");
		
		this.add(new JPanel(new MigLayout("fill, ins n n n")) {
			{/////GL Options
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.opengl.lbl.title"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				//// The effects will take place the next time you open a window.
				add(new StyledLabel(
						trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
						"spanx, wrap");
				
				BooleanModel enableGLModel = new BooleanModel(preferences.getBoolean(Preferences.OPENGL_ENABLED, true));
				final JCheckBox enableGL = new JCheckBox(enableGLModel);
				enableGL.setText(trans.get("pref.dlg.opengl.but.enableGL"));
				enableGL.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_ENABLED, enableGL.isSelected());
					}
				});
				add(enableGL, "wrap");
				
				final JCheckBox enableAA = new JCheckBox(trans.get("pref.dlg.opengl.but.enableAA"));
				enableAA.setSelected(preferences.getBoolean(Preferences.OPENGL_ENABLE_AA, true));
				enableAA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_ENABLE_AA, enableAA.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableAA);
				add(enableAA, "wrap");
				
				final JCheckBox useFBO = new JCheckBox(trans.get("pref.dlg.opengl.lbl.useFBO"));
				useFBO.setSelected(preferences.getBoolean(Preferences.OPENGL_USE_FBO, false));
				useFBO.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_USE_FBO, useFBO.isSelected());
					}
				});
				enableGLModel.addEnableComponent(useFBO);
				add(useFBO, "wrap");
			}
		}, "growx, span");
	}

}
