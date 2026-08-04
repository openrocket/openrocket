package info.openrocket.swing.gui.dialogs.preferences;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.figure3d.RocketFigure3d;
import info.openrocket.swing.gui.figure3d.scene.properties.Figure3DPreferences;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.GraphicsEditorChooser;

import com.itextpdf.text.Font;

@SuppressWarnings("serial")
public class GraphicsPreferencesPanel extends PreferencesPanel {

	public GraphicsPreferencesPanel(JDialog parent) {
		super(parent, new MigLayout("fillx"));
		
		JPanel editorPrefPanel = new JPanel(new MigLayout("fill, ins n n n")) {
			{ // Editor Options
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
				
				boolean commandLineIsSelected = preferences.isDecalEditorPreferenceSet() &&
						!preferences.isDecalEditorPreferenceSystem();
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
						GraphicsEditorChooser.chooseEditor(GraphicsPreferencesPanel.this.parentDialog)
								.ifPresent(commandLine -> {
									commandText.setText(commandLine);
									preferences.setDecalEditorPreference(false, commandLine);
								});
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
		};
		
		/* Don't show the editor preferences panel when confined in a snap on Linux.
		 * The snap confinement doesn't allow to run any edit commands, and instead
		 * we will rely on using the xdg-open command which allows the user to pick
		 * their preferred application.
		 */
		if ((SystemInfo.getPlatform() != Platform.UNIX) || !SystemInfo.isConfined()) {
			this.add(editorPrefPanel, "growx, span");
		}
		
		this.add(new JPanel(new MigLayout("fill, ins n n n", "[][grow]")) {
			{ // GL Options
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.opengl.lbl.title"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				// The effects will take place the next time you open a window.
				add(new StyledLabel(
						trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
						"span 2, wrap");
				
				BooleanModel enableGLModel =
						new BooleanModel(preferences.getBoolean(ApplicationPreferences.OPENGL_ENABLED, true));
				
				// Enable 3D Graphics
				final JCheckBox enableGL = new JCheckBox(enableGLModel);
				enableGL.setText(trans.get("pref.dlg.opengl.but.enableGL"));
				enableGL.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(ApplicationPreferences.OPENGL_ENABLED, enableGL.isSelected());
					}
				});
				add(enableGL, "span 2, wrap");

				// Level of detail
				add(new JLabel(trans.get("pref.dlg.opengl.lbl.renderQuality")), "gapright unrel");
				final JComboBox<GraphicsQualitySettings.RenderQuality> renderQualityCombo =
						new JComboBox<>(GraphicsQualitySettings.RenderQuality.values());
				renderQualityCombo.setRenderer(new DefaultListCellRenderer() {
					@Override
					public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						Object labelValue = value;
						if (value instanceof GraphicsQualitySettings.RenderQuality quality) {
							labelValue = getRenderQualityLabel(quality);
						}
						return super.getListCellRendererComponent(list, labelValue, index, isSelected, cellHasFocus);
					}
				});
				renderQualityCombo.setSelectedItem(Figure3DPreferences.getDefaultRenderQuality(preferences));
				renderQualityCombo.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						GraphicsQualitySettings.RenderQuality quality =
								(GraphicsQualitySettings.RenderQuality) renderQualityCombo.getSelectedItem();
						if (quality != null) {
							Figure3DPreferences.setDefaultRenderQuality(preferences, quality);
						}
					}
				});
				enableGLModel.addEnableComponent(renderQualityCombo);
				add(renderQualityCombo, "alignx left, wrap");
				
				// Enable multisample anti-aliasing
				final JCheckBox enableMSAA = new JCheckBox(trans.get("pref.dlg.opengl.but.enableMSAA"));
				enableMSAA.setSelected(Figure3DPreferences.isMSAAEnabled(preferences));
				enableMSAA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setMSAAEnabled(preferences, enableMSAA.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableMSAA);
				add(enableMSAA, "span 2, wrap");

				// Enable post-process anti-aliasing
				final JCheckBox enableFXAA = new JCheckBox(trans.get("pref.dlg.opengl.but.enableAA") + " (FXAA)");
				enableFXAA.setSelected(Figure3DPreferences.isAntiAliasingEnabled(preferences));
				enableFXAA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setAntiAliasingEnabled(preferences, enableFXAA.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableFXAA);
				add(enableFXAA, "span 2, wrap");

				// Enable shadows
				final JCheckBox enableShadows = new JCheckBox(trans.get("pref.dlg.opengl.but.enableShadows"));
				enableShadows.setSelected(Figure3DPreferences.isShadowsEnabled(preferences));
				enableShadows.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setShadowsEnabled(preferences, enableShadows.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableShadows);
				add(enableShadows, "span 2, wrap");

				// Enable ambient occlusion
				final JCheckBox enableAmbientOcclusion = new JCheckBox(trans.get("pref.dlg.opengl.but.enableAO"));
				enableAmbientOcclusion.setSelected(Figure3DPreferences.isAmbientOcclusionEnabled(preferences));
				enableAmbientOcclusion.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setAmbientOcclusionEnabled(preferences,
								enableAmbientOcclusion.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableAmbientOcclusion);
				add(enableAmbientOcclusion, "span 2, wrap");

				// Reduce costly effects while interacting with the 3D view
				final JCheckBox reduceEffectsDuringInteraction = new JCheckBox(
						trans.get("pref.dlg.opengl.but.reduceEffectsDuringInteraction"));
				reduceEffectsDuringInteraction.setSelected(
						Figure3DPreferences.shouldReduceEffectsDuringInteraction(preferences));
				reduceEffectsDuringInteraction.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setReduceEffectsDuringInteraction(preferences,
								reduceEffectsDuringInteraction.isSelected());
					}
				});
				enableGLModel.addEnableComponent(reduceEffectsDuringInteraction);
				add(reduceEffectsDuringInteraction, "span 2, wrap");

				// Enable surface roughness
				final JCheckBox enableRoughness = new JCheckBox(trans.get("pref.dlg.opengl.but.enableRoughness"));
				enableRoughness.setSelected(Figure3DPreferences.isRoughnessBumpEnabled(preferences));
				enableRoughness.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Figure3DPreferences.setRoughnessBumpEnabled(preferences, enableRoughness.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableRoughness);
				add(enableRoughness, "span 2, wrap");

				// 3D drag sensitivity
				add(new JLabel(trans.get("pref.dlg.opengl.lbl.dragSensitivity")), "gapright unrel");
				final JSpinner dragSensitivitySpinner = new JSpinner(new SpinnerNumberModel(
						(double) Figure3DPreferences.getDragRotationSensitivity(preferences),
						0.05d,
						5.0d,
						0.1d));
				dragSensitivitySpinner.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						Object value = dragSensitivitySpinner.getValue();
						if (value instanceof Number number) {
							float sensitivity = number.floatValue();
							Figure3DPreferences.setDragRotationSensitivity(preferences, sensitivity);
							applyDragSensitivityToOpenViews(sensitivity);
						}
					}
				});
				enableGLModel.addEnableComponent(dragSensitivitySpinner);
				add(dragSensitivitySpinner, "alignx left, wrap");
			}
		}, "growx, span");
	}

	private String getRenderQualityLabel(GraphicsQualitySettings.RenderQuality quality) {
		return switch (quality) {
			case LOW -> trans.get("LevelOfDetail.LOW_QUALITY");
			case MEDIUM -> trans.get("LevelOfDetail.NORMAL_QUALITY");
			case HIGH -> trans.get("LevelOfDetail.HIGH_QUALITY");
		};
	}

	private void applyDragSensitivityToOpenViews(float sensitivity) {
		for (Window window : Window.getWindows()) {
			if (window == null) {
				continue;
			}
			applyDragSensitivityToComponentTree(window, sensitivity);
		}
	}

	private void applyDragSensitivityToComponentTree(Component component, float sensitivity) {
		if (component instanceof RocketPanel rocketPanel) {
			RocketFigure3d figure3d = rocketPanel.getFigure3d();
			if (figure3d != null) {
				figure3d.setDragRotationSensitivity(sensitivity);
			}
		}
		if (!(component instanceof Container container)) {
			return;
		}
		for (Component child : container.getComponents()) {
			applyDragSensitivityToComponentTree(child, sensitivity);
		}
	}
}
