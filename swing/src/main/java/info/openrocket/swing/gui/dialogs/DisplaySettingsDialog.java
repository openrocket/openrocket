package info.openrocket.swing.gui.dialogs;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.DocumentPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.Figure3DPreferences;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.components.ColorChooserButton;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;

/**
 * Dialog for configuring design view display settings.
 * Supports setting background colors and text colors for 2D and 3D views.
 * 
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DisplaySettingsDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	private final RocketPanel rocketPanel;
	private final DocumentPreferences docPreferences;
	private final Figure3DPreferences.Values originalDocument3DPreferences;
	private final GraphicsQualitySettings.RenderQuality originalRenderQuality;
	private final boolean originalShadowsEnabled;
	private final boolean originalAmbientOcclusionEnabled;
	private final boolean originalRoughnessEnabled;
	private final boolean originalOriginAxesVisible;
	private final boolean originalLightVisualizersVisible;
	private final boolean originalCameraPointOfInterestVisible;
	private final boolean originalRotateRocketOnDrag;
	private final boolean originalCaretScaleWithView;
	
	private ColorChooserButton color2DButton;
	private ColorChooserButton color3DButton;
	private ColorChooserButton textColor2DButton;
	private ColorChooserButton textColor3DButton;
	private JComboBox<GraphicsQualitySettings.RenderQuality> renderQualityCombo;
	private JCheckBox renderShadowsCheckBox;
	private JCheckBox ambientOcclusionCheckBox;
	private JCheckBox roughnessCheckBox;
	private JCheckBox originAxesCheckBox;
	private JCheckBox lightVisualizersCheckBox;
	private JCheckBox cameraPointOfInterestCheckBox;
	private JCheckBox rotateRocketOnDragCheckBox;
	private JCheckBox scaleCaretsCheckBox;
	
	private JButton reset2DBgButton;
	private JButton reset3DBgButton;
	private JButton reset2DTextButton;
	private JButton reset3DTextButton;
	
	private final Color original2DBgColor;
	private final Color original3DBgColor;
	private final Color original2DTextColor;
	private final Color original3DTextColor;
	
	// Easter egg: track clicks when settings are already at default
	private int defaultStateClickCount = 0;
	private boolean updatingRenderingControls = false;
	
	public DisplaySettingsDialog(Window parent, RocketPanel rocketPanel) {
		super(parent, trans.get("RocketPanel.dlg.displaySettings.title"), Dialog.ModalityType.APPLICATION_MODAL);

		this.rocketPanel = rocketPanel;
		this.docPreferences = rocketPanel.getDocument().getDocumentPreferences();
		this.originalDocument3DPreferences = Figure3DPreferences.load(docPreferences, prefs);
		
		// Get current colors from document preferences (null if not explicitly set)
		original2DBgColor = docPreferences.getColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, null);
		original3DBgColor = docPreferences.getColor(DocumentPreferences.PREF_3D_BACKGROUND_COLOR, null);
		original2DTextColor = docPreferences.getColor(DocumentPreferences.PREF_2D_TEXT_COLOR, null);
		original3DTextColor = docPreferences.getColor(DocumentPreferences.PREF_3D_TEXT_COLOR, null);
		originalRenderQuality = getCurrentRenderQuality();
		originalShadowsEnabled = getCurrentShadowsEnabled();
		originalAmbientOcclusionEnabled = getCurrentAmbientOcclusionEnabled();
		originalRoughnessEnabled = getCurrentRoughnessEnabled();
		originalOriginAxesVisible = getCurrentOriginAxesVisible();
		originalLightVisualizersVisible = getCurrentLightVisualizersVisible();
		originalCameraPointOfInterestVisible = getCurrentCameraPointOfInterestVisible();
		originalRotateRocketOnDrag = getCurrentRotateRocketOnDrag();
		originalCaretScaleWithView = getCurrentCaretScaleWithView();
		
		init();
	}
	
	private void init() {
		JPanel panel = new JPanel(new MigLayout("fill, ins 15", "[grow]", "[grow][]"));
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(trans.get("RocketPanel.dlg.displaySettings.generalTab"), createGeneralSettingsPanel());
		tabbedPane.addTab(trans.get("RocketPanel.dlg.displaySettings.advancedTab"), createAdvancedSettingsPanel());
		panel.add(tabbedPane, "grow, wrap");

		// Buttons
		JButton saveAsDefaultButton = new JButton(trans.get("RocketPanel.btn.saveAsDefault"));
		saveAsDefaultButton.setToolTipText(trans.get("RocketPanel.btn.saveAsDefault.ttip"));
		saveAsDefaultButton.addActionListener(e -> {
			handleSaveAsDefault();
		});
		panel.add(saveAsDefaultButton, "split 3, right");
		
		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(e -> {
			dispose();
		});
		panel.add(okButton, "gap para");
		
		JButton cancelButton = new JButton(trans.get("button.cancel"));
		cancelButton.addActionListener(e -> {
			// Revert to original colors (null means not set, will use theme/default)
			docPreferences.putColor(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, original2DBgColor);
			docPreferences.putColor(DocumentPreferences.PREF_3D_BACKGROUND_COLOR, original3DBgColor);
			docPreferences.putColor(DocumentPreferences.PREF_2D_TEXT_COLOR, original2DTextColor);
			docPreferences.putColor(DocumentPreferences.PREF_3D_TEXT_COLOR, original3DTextColor);
			revert3DRenderingSettings();
			revertAdvancedSettings();
			update2DView();
			update3DView();
			updateTextColors();
			dispose();
		});
		panel.add(cancelButton, "gap para");
		
		add(panel);
		pack();
		setLocationRelativeTo(getParent());
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}

	private JPanel createGeneralSettingsPanel() {
		JPanel panel = new JPanel(new MigLayout("fill, ins 10", "[][grow][]"));

		// 2D View background color
		JLabel label2D = new JLabel(trans.get("RocketPanel.dlg.displaySettings.2DBackground"));
		panel.add(label2D, "gapright unrel");
		
		Color initial2DColor = getEffectiveColor(original2DBgColor,
				prefs.getDefault2DBackgroundColor(), 
				UITheme.getColor(UITheme.Keys.BACKGROUND));
		color2DButton = new ColorChooserButton(initial2DColor);
		color2DButton.addColorPropertyChangeListener(e -> {
			Color newColor = (Color) e.getNewValue();
			Color themeDefault = UITheme.getColor(UITheme.Keys.BACKGROUND);
			saveColorIfDifferent(DocumentPreferences.PREF_2D_BACKGROUND_COLOR, newColor, themeDefault);
			updateResetButtonState(reset2DBgButton, DocumentPreferences.PREF_2D_BACKGROUND_COLOR, newColor, themeDefault);
			resetDefaultStateClickCount();
			update2DView();
		});
		panel.add(color2DButton, "growx");
		setTooltip(label2D, color2DButton, "RocketPanel.dlg.displaySettings.2DBackground.ttip");
		
		reset2DBgButton = createResetButton(color2DButton, DocumentPreferences.PREF_2D_BACKGROUND_COLOR,
				prefs.getDefault2DBackgroundColor(), UITheme.getColor(UITheme.Keys.BACKGROUND),
				this::update2DView);
		panel.add(reset2DBgButton, "wrap");

		// 2D View text color
		JLabel label2DText = new JLabel(trans.get("RocketPanel.dlg.displaySettings.2DTextColor"));
		panel.add(label2DText, "gapright unrel");

		Color initial2DTextColor = getEffectiveColor(original2DTextColor,
				prefs.getDefault2DTextColor(),
				UITheme.getColor(UITheme.Keys.TEXT));
		textColor2DButton = new ColorChooserButton(initial2DTextColor);
		textColor2DButton.addColorPropertyChangeListener(e -> {
			Color newColor = (Color) e.getNewValue();
			Color themeDefault = UITheme.getColor(UITheme.Keys.TEXT);
			saveColorIfDifferent(DocumentPreferences.PREF_2D_TEXT_COLOR, newColor, themeDefault);
			updateResetButtonState(reset2DTextButton, DocumentPreferences.PREF_2D_TEXT_COLOR, newColor, themeDefault);
			resetDefaultStateClickCount();
			updateTextColors();
		});
		panel.add(textColor2DButton, "growx");
		setTooltip(label2DText, textColor2DButton, "RocketPanel.dlg.displaySettings.2DTextColor.ttip");

		reset2DTextButton = createResetButton(textColor2DButton, DocumentPreferences.PREF_2D_TEXT_COLOR,
				prefs.getDefault2DTextColor(), UITheme.getColor(UITheme.Keys.TEXT),
				this::updateTextColors);
		panel.add(reset2DTextButton, "wrap para");
		
		// 3D View background color
		JLabel label3D = new JLabel(trans.get("RocketPanel.dlg.displaySettings.3DBackground"));
		panel.add(label3D, "gapright unrel");
		
		Color initial3DColor = getEffectiveColor(original3DBgColor, 
				prefs.getDefault3DBackgroundColor(), 
				UITheme.getColor(UITheme.Keys.BACKGROUND));
		color3DButton = new ColorChooserButton(initial3DColor);
		color3DButton.addColorPropertyChangeListener(e -> {
			Color newColor = (Color) e.getNewValue();
			Color themeDefault = UITheme.getColor(UITheme.Keys.BACKGROUND);
			saveColorIfDifferent(DocumentPreferences.PREF_3D_BACKGROUND_COLOR, newColor, themeDefault);
			updateResetButtonState(reset3DBgButton, DocumentPreferences.PREF_3D_BACKGROUND_COLOR, newColor, themeDefault);
			resetDefaultStateClickCount();
			update3DView();
		});
		panel.add(color3DButton, "growx");
		setTooltip(label3D, color3DButton, "RocketPanel.dlg.displaySettings.3DBackground.ttip");
		
		reset3DBgButton = createResetButton(color3DButton, DocumentPreferences.PREF_3D_BACKGROUND_COLOR,
				prefs.getDefault3DBackgroundColor(), UITheme.getColor(UITheme.Keys.BACKGROUND),
				this::update3DView);
		panel.add(reset3DBgButton, "wrap");
		
		// 3D View text color
		JLabel label3DText = new JLabel(trans.get("RocketPanel.dlg.displaySettings.3DTextColor"));
		panel.add(label3DText, "gapright unrel");
		
		Color initial3DTextColor = getEffectiveColor(original3DTextColor, 
				prefs.getDefault3DTextColor(), 
				UITheme.getColor(UITheme.Keys.TEXT));
		textColor3DButton = new ColorChooserButton(initial3DTextColor);
		textColor3DButton.addColorPropertyChangeListener(e -> {
			Color newColor = (Color) e.getNewValue();
			Color themeDefault = UITheme.getColor(UITheme.Keys.TEXT);
			saveColorIfDifferent(DocumentPreferences.PREF_3D_TEXT_COLOR, newColor, themeDefault);
			updateResetButtonState(reset3DTextButton, DocumentPreferences.PREF_3D_TEXT_COLOR, newColor, themeDefault);
			resetDefaultStateClickCount();
			updateTextColors();
		});
		panel.add(textColor3DButton, "growx");
		setTooltip(label3DText, textColor3DButton, "RocketPanel.dlg.displaySettings.3DTextColor.ttip");
		
		reset3DTextButton = createResetButton(textColor3DButton, DocumentPreferences.PREF_3D_TEXT_COLOR,
				prefs.getDefault3DTextColor(), UITheme.getColor(UITheme.Keys.TEXT),
				this::updateTextColors);
		panel.add(reset3DTextButton, "wrap para");

		panel.add(createRenderingSettingsPanel(), "span 3, growx, wrap para");

		// Initialize reset button states (check if current colors equal factory defaults)
		updateResetButtonState(reset2DBgButton, DocumentPreferences.PREF_2D_BACKGROUND_COLOR, 
				color2DButton.getSelectedColor(), UITheme.getColor(UITheme.Keys.BACKGROUND));
		updateResetButtonState(reset3DBgButton, DocumentPreferences.PREF_3D_BACKGROUND_COLOR, 
				color3DButton.getSelectedColor(), UITheme.getColor(UITheme.Keys.BACKGROUND));
		updateResetButtonState(reset2DTextButton, DocumentPreferences.PREF_2D_TEXT_COLOR, 
				textColor2DButton.getSelectedColor(), UITheme.getColor(UITheme.Keys.TEXT));
		updateResetButtonState(reset3DTextButton, DocumentPreferences.PREF_3D_TEXT_COLOR, 
				textColor3DButton.getSelectedColor(), UITheme.getColor(UITheme.Keys.TEXT));

		return panel;
	}

	private JPanel createRenderingSettingsPanel() {
		JPanel panel = new JPanel(new MigLayout("fillx, ins 10", "[][grow]"));
		panel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				trans.get("RocketPanel.dlg.displaySettings.rendering.title")));

		// Level of detail
		renderQualityCombo = new JComboBox<>(GraphicsQualitySettings.RenderQuality.values());
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
		renderQualityCombo.setSelectedItem(originalRenderQuality);
		renderQualityCombo.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			GraphicsQualitySettings.RenderQuality quality =
					(GraphicsQualitySettings.RenderQuality) renderQualityCombo.getSelectedItem();
			if (quality == null) {
				return;
			}
			resetDefaultStateClickCount();
			applyRenderingChange(config -> config.getQuality().setQuality(quality), true);
		});
		renderQualityCombo.setToolTipText(trans.get("RocketPanel.dlg.displaySettings.rendering.quality.ttip"));

		JLabel renderQualityLabel = new JLabel(trans.get("RocketPanel.dlg.displaySettings.rendering.quality"));
		panel.add(renderQualityLabel, "gapright unrel");
		panel.add(renderQualityCombo, "growx, wrap");
		renderQualityLabel.setToolTipText(trans.get("RocketPanel.dlg.displaySettings.rendering.quality.ttip"));

		// Render shadows
		renderShadowsCheckBox = new JCheckBox(trans.get("RocketPanel.dlg.displaySettings.rendering.shadows"));
		renderShadowsCheckBox.setToolTipText(trans.get("RocketPanel.dlg.displaySettings.rendering.shadows.ttip"));
		renderShadowsCheckBox.setSelected(originalShadowsEnabled);
		renderShadowsCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyRenderingChange(config -> config.getQuality().setShadowsEnabled(renderShadowsCheckBox.isSelected()),
					false);
		});
		panel.add(renderShadowsCheckBox, "span 2, wrap");

		// Ambient occlusion
		ambientOcclusionCheckBox = new JCheckBox(
				trans.get("RocketPanel.dlg.displaySettings.rendering.ambientOcclusion"));
		ambientOcclusionCheckBox.setToolTipText(
				trans.get("RocketPanel.dlg.displaySettings.rendering.ambientOcclusion.ttip"));
		ambientOcclusionCheckBox.setSelected(originalAmbientOcclusionEnabled);
		ambientOcclusionCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyRenderingChange(
					config -> config.getQuality().setAmbientOcclusionEnabled(ambientOcclusionCheckBox.isSelected()),
					false);
		});
		panel.add(ambientOcclusionCheckBox, "span 2, wrap");

		// Surface roughness
		roughnessCheckBox = new JCheckBox(trans.get("RocketPanel.dlg.displaySettings.rendering.roughness"));
		roughnessCheckBox.setToolTipText(trans.get("RocketPanel.dlg.displaySettings.rendering.roughness.ttip"));
		roughnessCheckBox.setSelected(originalRoughnessEnabled);
		roughnessCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyRenderingChange(config -> config.getQuality().setRoughnessBumpEnabled(roughnessCheckBox.isSelected()),
					false);
		});
		panel.add(roughnessCheckBox, "span 2, wrap");

		boolean liveControlsAvailable = getScene3DOrchestrator() != null;
		setRenderingControlsEnabled(liveControlsAvailable);
		if (!liveControlsAvailable) {
			JLabel hint = new JLabel(trans.get("RocketPanel.dlg.displaySettings.rendering.liveHint"));
			panel.add(hint, "span 2, gaptop 5");
		}

		return panel;
	}

	private JPanel createAdvancedSettingsPanel() {
		JPanel panel = new JPanel(new MigLayout("fillx, ins 10", "[grow]"));

		// Show origin axes
		originAxesCheckBox = new JCheckBox(trans.get("RocketPanel.dlg.displaySettings.advanced.originAxes"));
		originAxesCheckBox.setToolTipText(trans.get("RocketPanel.dlg.displaySettings.advanced.originAxes.ttip"));
		originAxesCheckBox.setSelected(originalOriginAxesVisible);
		originAxesCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyVisualEffectsChange(
					settings -> settings.setOriginAxesVisible(originAxesCheckBox.isSelected()),
					true,
					false);
		});
		panel.add(originAxesCheckBox, "wrap");

		// Show light visualizers
		lightVisualizersCheckBox = new JCheckBox(
				trans.get("RocketPanel.dlg.displaySettings.advanced.lightVisualizers"));
		lightVisualizersCheckBox.setToolTipText(
				trans.get("RocketPanel.dlg.displaySettings.advanced.lightVisualizers.ttip"));
		lightVisualizersCheckBox.setSelected(originalLightVisualizersVisible);
		lightVisualizersCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyVisualEffectsChange(
					settings -> settings.setLightVisualizersVisible(lightVisualizersCheckBox.isSelected()),
					false,
					true);
		});
		panel.add(lightVisualizersCheckBox, "wrap");

		// Show camera point of interest
		cameraPointOfInterestCheckBox = new JCheckBox(
				trans.get("RocketPanel.dlg.displaySettings.advanced.cameraPointOfInterest"));
		cameraPointOfInterestCheckBox.setToolTipText(
				trans.get("RocketPanel.dlg.displaySettings.advanced.cameraPointOfInterest.ttip"));
		cameraPointOfInterestCheckBox.setSelected(originalCameraPointOfInterestVisible);
		cameraPointOfInterestCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyVisualEffectsChange(
					settings -> settings.setCameraPointOfInterestVisible(cameraPointOfInterestCheckBox.isSelected()),
					false,
					false);
		});
		panel.add(cameraPointOfInterestCheckBox, "wrap");

		// Rotate rocket when dragging
		rotateRocketOnDragCheckBox = new JCheckBox(
				trans.get("RocketPanel.dlg.displaySettings.advanced.rotateRocketOnDrag"));
		rotateRocketOnDragCheckBox.setToolTipText(
				trans.get("RocketPanel.dlg.displaySettings.advanced.rotateRocketOnDrag.ttip"));
		rotateRocketOnDragCheckBox.setSelected(originalRotateRocketOnDrag);
		rotateRocketOnDragCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyVisualEffectsChange(
					settings -> settings.setRotateRocketOnDrag(rotateRocketOnDragCheckBox.isSelected()),
					false,
					false);
		});
		panel.add(rotateRocketOnDragCheckBox, "wrap");

		// Scale carets with view
		scaleCaretsCheckBox = new JCheckBox(
				trans.get("RocketPanel.dlg.displaySettings.advanced.scaleCarets"));
		scaleCaretsCheckBox.setToolTipText(
				trans.get("RocketPanel.dlg.displaySettings.advanced.scaleCarets.ttip"));
		scaleCaretsCheckBox.setSelected(originalCaretScaleWithView);
		scaleCaretsCheckBox.addActionListener(e -> {
			if (updatingRenderingControls) {
				return;
			}
			resetDefaultStateClickCount();
			applyVisualEffectsChange(
					settings -> settings.setCaretScaleWithView(scaleCaretsCheckBox.isSelected()),
					false,
					false);
		});
		panel.add(scaleCaretsCheckBox, "wrap");

		boolean liveControlsAvailable = getScene3DOrchestrator() != null;
		setAdvancedControlsEnabled(liveControlsAvailable);
		if (!liveControlsAvailable) {
			JLabel hint = new JLabel(trans.get("RocketPanel.dlg.displaySettings.rendering.liveHint"));
			panel.add(hint, "gaptop 5");
		}

		return panel;
	}

	private void setRenderingControlsEnabled(boolean enabled) {
		renderQualityCombo.setEnabled(enabled);
		renderShadowsCheckBox.setEnabled(enabled);
		ambientOcclusionCheckBox.setEnabled(enabled);
		roughnessCheckBox.setEnabled(enabled);
	}

	private void setAdvancedControlsEnabled(boolean enabled) {
		originAxesCheckBox.setEnabled(enabled);
		lightVisualizersCheckBox.setEnabled(enabled);
		cameraPointOfInterestCheckBox.setEnabled(enabled);
		rotateRocketOnDragCheckBox.setEnabled(enabled);
		scaleCaretsCheckBox.setEnabled(enabled);
	}

	private void applyRenderingChange(java.util.function.Consumer<RenderingConfiguration> change,
			boolean rebuildScene) {
		Scene3DOrchestrator orchestrator = getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			RenderingConfiguration config = orchestrator.getRenderingConfiguration();
			change.accept(config);
			config.notifyListeners();
			if (rebuildScene) {
				orchestrator.rebuildRocketScene(false);
			}
		});
		saveCurrent3DSettingsToDocument();
		rocketPanel.getFigure3d().updateFigure();
	}

	private void applyVisualEffectsChange(java.util.function.Consumer<info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings> change,
			boolean rebuildOriginAxes, boolean updateLightVisualizers) {
		Scene3DOrchestrator orchestrator = getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			RenderingConfiguration config = orchestrator.getRenderingConfiguration();
			change.accept(config.getVisualEffects());
			if (rebuildOriginAxes && orchestrator.getScene() instanceof info.openrocket.swing.gui.figure3d.scene.core.Scene scene) {
				info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder.rebuildOriginAxes(
						scene, config, true, true);
			}
			if (updateLightVisualizers) {
				orchestrator.getScene().getLightController().setVisualizersVisible(
						config.getVisualEffects().areLightVisualizersVisible());
			}
			config.notifyListeners();
		});
		saveCurrent3DSettingsToDocument();
		rocketPanel.getFigure3d().updateFigure();
	}

	private void revert3DRenderingSettings() {
		updatingRenderingControls = true;
		try {
			renderQualityCombo.setSelectedItem(originalRenderQuality);
			renderShadowsCheckBox.setSelected(originalShadowsEnabled);
			ambientOcclusionCheckBox.setSelected(originalAmbientOcclusionEnabled);
			roughnessCheckBox.setSelected(originalRoughnessEnabled);
		} finally {
			updatingRenderingControls = false;
		}
		applyRenderingChange(config -> {
			config.getQuality().setQuality(originalRenderQuality);
			config.getQuality().setShadowsEnabled(originalShadowsEnabled);
			config.getQuality().setAmbientOcclusionEnabled(originalAmbientOcclusionEnabled);
			config.getQuality().setRoughnessBumpEnabled(originalRoughnessEnabled);
		}, true);
		restoreDocument3DPreferences();
	}

	private void revertAdvancedSettings() {
		updatingRenderingControls = true;
		try {
			originAxesCheckBox.setSelected(originalOriginAxesVisible);
			lightVisualizersCheckBox.setSelected(originalLightVisualizersVisible);
			cameraPointOfInterestCheckBox.setSelected(originalCameraPointOfInterestVisible);
			rotateRocketOnDragCheckBox.setSelected(originalRotateRocketOnDrag);
			scaleCaretsCheckBox.setSelected(originalCaretScaleWithView);
		} finally {
			updatingRenderingControls = false;
		}
		applyVisualEffectsChange(settings -> {
			settings.setOriginAxesVisible(originalOriginAxesVisible);
			settings.setLightVisualizersVisible(originalLightVisualizersVisible);
			settings.setCameraPointOfInterestVisible(originalCameraPointOfInterestVisible);
			settings.setRotateRocketOnDrag(originalRotateRocketOnDrag);
			settings.setCaretScaleWithView(originalCaretScaleWithView);
		}, true, true);
		restoreDocument3DPreferences();
	}

	private Scene3DOrchestrator getScene3DOrchestrator() {
		return rocketPanel.getFigure3d().getSceneController();
	}

	private RenderingConfiguration getCurrentRenderingConfiguration() {
		Scene3DOrchestrator orchestrator = getScene3DOrchestrator();
		return orchestrator != null ? orchestrator.getRenderingConfiguration() : null;
	}

	private GraphicsQualitySettings.RenderQuality getCurrentRenderQuality() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getQuality().getQuality();
		}
		return Figure3DPreferences.getRenderQuality(docPreferences, prefs);
	}

	private boolean getCurrentShadowsEnabled() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getQuality().isShadowsEnabled();
		}
		return Figure3DPreferences.getShadowsEnabled(docPreferences, prefs);
	}

	private boolean getCurrentAmbientOcclusionEnabled() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getQuality().isAmbientOcclusionEnabled();
		}
		return Figure3DPreferences.getAmbientOcclusionEnabled(docPreferences, prefs);
	}

	private boolean getCurrentRoughnessEnabled() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getQuality().isRoughnessBumpEnabled();
		}
		return Figure3DPreferences.getRoughnessBumpEnabled(docPreferences, prefs);
	}

	private boolean getCurrentOriginAxesVisible() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getVisualEffects().isOriginAxesVisible();
		}
		return Figure3DPreferences.getOriginAxesVisible(docPreferences, prefs);
	}

	private boolean getCurrentLightVisualizersVisible() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getVisualEffects().areLightVisualizersVisible();
		}
		return Figure3DPreferences.getLightVisualizersVisible(docPreferences, prefs);
	}

	private boolean getCurrentCameraPointOfInterestVisible() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getVisualEffects().isCameraPointOfInterestVisible();
		}
		return Figure3DPreferences.getCameraPointOfInterestVisible(docPreferences, prefs);
	}

	private boolean getCurrentRotateRocketOnDrag() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getVisualEffects().isRotateRocketOnDrag();
		}
		return Figure3DPreferences.getRotateRocketOnDrag(docPreferences, prefs);
	}

	private boolean getCurrentCaretScaleWithView() {
		RenderingConfiguration config = getCurrentRenderingConfiguration();
		if (config != null) {
			return config.getVisualEffects().isCaretScaleWithView();
		}
		return Figure3DPreferences.getCaretScaleWithView(docPreferences, prefs);
	}

	private void saveCurrent3DSettingsToDocument() {
		Figure3DPreferences.saveToDocument(docPreferences, prefs, new Figure3DPreferences.Values(
				getSelectedRenderQuality(),
				Figure3DPreferences.isAntiAliasingEnabled(prefs),
				renderShadowsCheckBox.isSelected(),
				ambientOcclusionCheckBox.isSelected(),
				roughnessCheckBox.isSelected(),
				originAxesCheckBox.isSelected(),
				lightVisualizersCheckBox.isSelected(),
				cameraPointOfInterestCheckBox.isSelected(),
				rotateRocketOnDragCheckBox.isSelected(),
				Figure3DPreferences.getDragRotationSensitivity(prefs),
				scaleCaretsCheckBox.isSelected()));
	}

	private void restoreDocument3DPreferences() {
		Figure3DPreferences.saveToDocument(docPreferences, prefs, originalDocument3DPreferences);
	}

	private String getRenderQualityLabel(GraphicsQualitySettings.RenderQuality quality) {
		return switch (quality) {
			case LOW -> trans.get("LevelOfDetail.LOW_QUALITY");
			case MEDIUM -> trans.get("LevelOfDetail.NORMAL_QUALITY");
			case HIGH -> trans.get("LevelOfDetail.HIGH_QUALITY");
		};
	}

	private GraphicsQualitySettings.RenderQuality getSelectedRenderQuality() {
		Object selected = renderQualityCombo.getSelectedItem();
		if (selected instanceof GraphicsQualitySettings.RenderQuality quality) {
			return quality;
		}
		return Figure3DPreferences.getDefaultRenderQuality(prefs);
	}

	private void setTooltip(JLabel label, JComponent component, String key) {
		String tooltip = trans.get(key);
		label.setToolTipText(tooltip);
		component.setToolTipText(tooltip);
	}
	
	/**
	 * Get the effective color: document preference -> SwingPreferences default -> theme default
	 */
	private Color getEffectiveColor(Color docColor, Color defaultColor, Color themeDefault) {
		if (docColor != null) {
			return docColor;
		}
		if (defaultColor != null) {
			return defaultColor;
		}
		return themeDefault;
	}
	
	/**
	 * Save color to document preferences only if different from theme default.
	 */
	private void saveColorIfDifferent(String prefKey, Color newColor, Color themeDefault) {
		if (newColor != null && !newColor.equals(themeDefault)) {
			docPreferences.putColor(prefKey, newColor);
		} else {
			docPreferences.putColor(prefKey, null);
		}
	}
	
	/**
	 * Create a reset button for a color chooser.
	 * Resets to factory default (theme default), which means setting document preference to null.
	 */
	private JButton createResetButton(ColorChooserButton colorButton, String prefKey, 
			Color defaultColor, Color themeDefault, Runnable updateAction) {
		JButton resetButton = new JButton(Icons.RESET);
		resetButton.setToolTipText(trans.get("RocketPanel.btn.reset.ttip"));
		resetButton.addActionListener(e -> {
			// Reset to factory default (UITheme color) by setting document preference to null
			// Factory default is the theme default, not the application default
			colorButton.setSelectedColor(themeDefault);
			docPreferences.putColor(prefKey, null);
			// Update button state - should be disabled after reset (since we're at factory default)
			updateResetButtonState(resetButton, prefKey, themeDefault, themeDefault);
			// Force update the view with factory default (bypass SwingPreferences default)
			forceUpdateView(prefKey, themeDefault, updateAction);
		});
		
		// Set initial button state based on whether current color equals factory default
		Color currentColor = colorButton.getSelectedColor();
		updateResetButtonState(resetButton, prefKey, currentColor, themeDefault);
		
		return resetButton;
	}
	
	/**
	 * Update the reset button's enabled state.
	 * Button is disabled when the current color equals the factory default (UITheme color).
	 * Button is enabled when there's a custom color (document preference or application default).
	 */
	private void updateResetButtonState(JButton resetButton, String prefKey, Color currentColor, Color themeDefault) {
		// Button is disabled if current color equals factory default (UITheme color)
		// This includes cases where document pref is null but we're using factory default
		boolean isAtFactoryDefault = currentColor != null && currentColor.equals(themeDefault);
		resetButton.setEnabled(!isAtFactoryDefault);
	}
	
	/**
	 * Handle the "Save as Default" button click with confirmation and easter egg.
	 */
	private void handleSaveAsDefault() {
		// Check if settings are already at defaults (easter egg)
		if (areSettingsAtDefault()) {
			defaultStateClickCount++;
			String message;
			String title = trans.get("RocketPanel.dlg.saveAsDefault.alreadyDefault.title");
			
			if (defaultStateClickCount >= 3) {
				message = trans.get("RocketPanel.dlg.saveAsDefault.alreadyDefault.funny");
			} else {
				message = trans.get("RocketPanel.dlg.saveAsDefault.alreadyDefault.message");
			}
			
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// Show confirmation dialog
		int result = JOptionPane.showConfirmDialog(this,
				trans.get("RocketPanel.dlg.saveAsDefault.confirm.message"),
				trans.get("RocketPanel.dlg.saveAsDefault.confirm.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		
		if (result == JOptionPane.YES_OPTION) {
			saveAsDefaults();
			JOptionPane.showMessageDialog(this,
					trans.get("RocketPanel.dlg.saveAsDefault.success.message"),
					trans.get("RocketPanel.dlg.saveAsDefault.success.title"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Check if all current settings are already at their default values.
	 */
	private boolean areSettingsAtDefault() {
		Figure3DPreferences.Values defaults = Figure3DPreferences.load(prefs);
		Color themeBg = UITheme.getColor(UITheme.Keys.BACKGROUND);
		Color themeText = UITheme.getColor(UITheme.Keys.TEXT);
		
		Color current2DBg = color2DButton.getSelectedColor();
		Color current3DBg = color3DButton.getSelectedColor();
		Color current2DText = textColor2DButton.getSelectedColor();
		Color current3DText = textColor3DButton.getSelectedColor();
		
		Color default2DBg = getEffectiveColor(null, prefs.getDefault2DBackgroundColor(), themeBg);
		Color default3DBg = getEffectiveColor(null, prefs.getDefault3DBackgroundColor(), themeBg);
		Color default2DText = getEffectiveColor(null, prefs.getDefault2DTextColor(), themeText);
		Color default3DText = getEffectiveColor(null, prefs.getDefault3DTextColor(), themeText);
		return current2DBg.equals(default2DBg) &&
			   current3DBg.equals(default3DBg) &&
			   current2DText.equals(default2DText) &&
			   current3DText.equals(default3DText) &&
			   getSelectedRenderQuality() == defaults.renderQuality() &&
			   renderShadowsCheckBox.isSelected() == defaults.shadowsEnabled() &&
			   ambientOcclusionCheckBox.isSelected() == defaults.ambientOcclusionEnabled() &&
			   roughnessCheckBox.isSelected() == defaults.roughnessBumpEnabled() &&
			   originAxesCheckBox.isSelected() == defaults.originAxesVisible() &&
			   lightVisualizersCheckBox.isSelected() == defaults.lightVisualizersVisible() &&
			   cameraPointOfInterestCheckBox.isSelected() == defaults.cameraPointOfInterestVisible() &&
			   rotateRocketOnDragCheckBox.isSelected() == defaults.rotateRocketOnDrag() &&
			   scaleCaretsCheckBox.isSelected() == defaults.caretScaleWithView();
	}
	
	/**
	 * Reset the easter egg click count when settings change.
	 */
	private void resetDefaultStateClickCount() {
		defaultStateClickCount = 0;
	}
	
	/**
	 * Save current colors as defaults to SwingPreferences.
	 */
	private void saveAsDefaults() {
		// Get current effective colors from buttons (which show the effective color)
		Color current2DBg = color2DButton.getSelectedColor();
		Color current3DBg = color3DButton.getSelectedColor();
		Color current2DText = textColor2DButton.getSelectedColor();
		Color current3DText = textColor3DButton.getSelectedColor();
		
		// Only save if different from theme defaults
		saveDefaultIfDifferent(current2DBg, 
				UITheme.getColor(UITheme.Keys.BACKGROUND), prefs::setDefault2DBackgroundColor);
		saveDefaultIfDifferent(current3DBg, 
				UITheme.getColor(UITheme.Keys.BACKGROUND), prefs::setDefault3DBackgroundColor);
		saveDefaultIfDifferent(current2DText, 
				UITheme.getColor(UITheme.Keys.TEXT), prefs::setDefault2DTextColor);
		saveDefaultIfDifferent(current3DText, 
				UITheme.getColor(UITheme.Keys.TEXT), prefs::setDefault3DTextColor);
		Figure3DPreferences.save(prefs, new Figure3DPreferences.Values(
				getSelectedRenderQuality(),
				Figure3DPreferences.isAntiAliasingEnabled(prefs),
				renderShadowsCheckBox.isSelected(),
				ambientOcclusionCheckBox.isSelected(),
				roughnessCheckBox.isSelected(),
				originAxesCheckBox.isSelected(),
				lightVisualizersCheckBox.isSelected(),
				cameraPointOfInterestCheckBox.isSelected(),
				rotateRocketOnDragCheckBox.isSelected(),
				Figure3DPreferences.getDragRotationSensitivity(prefs),
				scaleCaretsCheckBox.isSelected()));
	}
	
	/**
	 * Helper to save default color only if different from theme default.
	 */
	private void saveDefaultIfDifferent(Color currentColor, Color themeDefault, 
			java.util.function.Consumer<Color> setter) {
		if (currentColor != null && !currentColor.equals(themeDefault)) {
			setter.accept(currentColor);
		} else {
			setter.accept(null);
		}
	}
	
	private void update2DView() {
		rocketPanel.updateBackgroundColors();
		rocketPanel.updateTextColors();
		rocketPanel.getFigure().repaint();
	}
	
	private void update3DView() {
		rocketPanel.updateBackgroundColors();
		rocketPanel.updateTextColors();
		rocketPanel.getFigure3d().repaint();
	}
	
	private void updateTextColors() {
		rocketPanel.updateTextColors();
		rocketPanel.getFigure().repaint();
		rocketPanel.getFigure3d().repaint();
	}
	
	/**
	 * Force update the view with factory default (bypassing SwingPreferences default).
	 * Used when reset button is pressed to ensure we use theme default, not application default.
	 */
	private void forceUpdateView(String prefKey, Color themeDefault, Runnable updateAction) {
		// Force set to null (factory default) instead of using updateBackgroundColors()
		// which would check SwingPreferences default
		if (prefKey.equals(DocumentPreferences.PREF_2D_BACKGROUND_COLOR)) {
			rocketPanel.getFigure().setCustomBackgroundColor(null); // null = factory default
			rocketPanel.updateTextColors();
			rocketPanel.getFigure().repaint();
		} else if (prefKey.equals(DocumentPreferences.PREF_3D_BACKGROUND_COLOR)) {
			rocketPanel.getFigure3d().setCustomBackgroundColor(null); // null = factory default
			rocketPanel.updateTextColors();
			rocketPanel.getFigure3d().repaint();
		} else if (prefKey.equals(DocumentPreferences.PREF_2D_TEXT_COLOR) ||
				prefKey.equals(DocumentPreferences.PREF_3D_TEXT_COLOR)) {
			// Force factory default by directly setting null for the reset view
			// Get current effective colors for the other view to preserve it
			DocumentPreferences docPrefs = rocketPanel.getDocument().getDocumentPreferences();
			SwingPreferences swingPrefs = (SwingPreferences) Application.getPreferences();

			Color textColor2D;
			Color textColor3D;

			if (prefKey.equals(DocumentPreferences.PREF_2D_TEXT_COLOR)) {
				// Resetting 2D text color - get current 3D color to preserve it
				Color doc3DTextColor = docPrefs.getColor(DocumentPreferences.PREF_3D_TEXT_COLOR, null);
				Color default3DTextColor = swingPrefs.getDefault3DTextColor();
				textColor3D = doc3DTextColor != null ? doc3DTextColor :
						(default3DTextColor != null ? default3DTextColor : null);
				textColor2D = null; // Force factory default for 2D
			} else {
				// Resetting 3D text color - get current 2D color to preserve it
				Color doc2DTextColor = docPrefs.getColor(DocumentPreferences.PREF_2D_TEXT_COLOR, null);
				Color default2DTextColor = swingPrefs.getDefault2DTextColor();
				textColor2D = doc2DTextColor != null ? doc2DTextColor :
						(default2DTextColor != null ? default2DTextColor : null);
				textColor3D = null; // Force factory default for 3D
			}

			// Preserve the current view state before setting colors
			boolean currentIs3DView = rocketPanel.getExtraText().is3DView();

			// Set the colors directly (bypassing updateTextColors priority logic)
			rocketPanel.getExtraText().setCustomTextColors(textColor2D, textColor3D);
			// Restore view state (updateTextColors would do this, but we skip it to avoid overwriting colors)
			rocketPanel.getExtraText().set3DView(currentIs3DView);
			rocketPanel.getFigure().repaint();
			rocketPanel.getFigure3d().repaint();
		} else {
			// Fallback to normal update
			updateAction.run();
		}
	}
}
