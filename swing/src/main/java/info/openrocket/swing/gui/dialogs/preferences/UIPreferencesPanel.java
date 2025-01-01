package info.openrocket.swing.gui.dialogs.preferences;

import info.openrocket.core.unit.FixedPrecisionUnit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Named;
import info.openrocket.core.util.Utils;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.miginfocom.swing.MigLayout;

import static info.openrocket.core.util.Chars.ZWSP;

@SuppressWarnings("serial")
public class UIPreferencesPanel extends PreferencesPanel {
	private final UITheme.Theme currentTheme;
	private final double currentUIScale;
	private final int currentFontSize;
	private final String currentFontStyle;

	private final JLabel lblRestartOR;
	private final UIPreviewPanel previewPanel;

	public static final double TRACKING_SCALE_FACTOR = 100.0; // UI shows 0-100 instead of 0-1
	private static final double MIN_TRACKING_UI = -20;  // Shows as -20 in UI, means -0.2 tracking
	private static final double MAX_TRACKING_UI = 30;   // Shows as 30 in UI, means 0.3 tracking

	// Font weight options
	public enum FontStyle {
		LIGHT("Light", "Inter-Regular_Light"),
		REGULAR("Regular", "Inter-Regular"),
		MEDIUM("Medium", "Inter-Regular_Medium"),
		BOLD("Bold", "Inter-Regular_Bold");

		private final String displayName;
		private final String fontName;

		FontStyle(String displayName, String fontName) {
			this.displayName = displayName;
			this.fontName = fontName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getFontName() {
			return fontName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	public UIPreferencesPanel(PreferencesDialog parent) {
		super(parent, new MigLayout());

		this.currentTheme = GUIUtil.getUITheme();
		this.currentUIScale = preferences.getUIScale();
		this.currentFontSize = preferences.getUIFontSize();
		this.currentFontStyle = preferences.getUIFontStyle();

		// UI Theme selector
		UITheme.Theme currentTheme = GUIUtil.getUITheme();
		List<Named<UITheme.Theme>> themes = new ArrayList<>();
		for (UITheme.Theme t : UITheme.Themes.values()) {
			themes.add(new Named<>(t, t.getDisplayName()));
		}
		Collections.sort(themes);

		final JComboBox<?> themesCombo = new JComboBox<>(themes.toArray());
		for (int i = 0; i < themes.size(); i++) {
			if (Utils.equals(currentTheme, themes.get(i).get())) {
				themesCombo.setSelectedIndex(i);
			}
		}

		this.add(new JLabel(trans.get("generalprefs.lbl.UITheme")), "gapright para");
		this.add(themesCombo, "sizegroup uiSettings, wrap");

		// UI Scale selector
		UnitGroup scaleUnit = new UnitGroup();
		scaleUnit.addUnit(new FixedPrecisionUnit("" + ZWSP, 0.05));
		JLabel lblUIScale = new JLabel(trans.get("generalprefs.lbl.UIScale"));
		lblUIScale.setToolTipText(trans.get("generalprefs.lbl.UIScale.ttip"));
		this.add(lblUIScale, "gapright para");
		final DoubleModel uiScaleModel = new DoubleModel(preferences, "UIScale", scaleUnit, 0.5, 2.0);
		final JSpinner uiScaleSpinner = new JSpinner(uiScaleModel.getSpinnerModel());
		uiScaleSpinner.setEditor(new SpinnerEditor(uiScaleSpinner));
		uiScaleSpinner.setToolTipText(trans.get("generalprefs.lbl.UIScale.ttip"));
		this.add(uiScaleSpinner, "sizegroup uiSettings, wrap");

		// Font size selector
		JLabel lblFontSize = new JLabel(trans.get("generalprefs.lbl.FontSize"));
		lblFontSize.setToolTipText(trans.get("generalprefs.lbl.FontSize.ttip"));
		this.add(lblFontSize, "gapright para");
		final IntegerModel fontSizeModel = new IntegerModel(preferences, "UIFontSizeRaw", 5, 25);
		final JSpinner fontSizeSpinner = new JSpinner(fontSizeModel.getSpinnerModel());
		fontSizeSpinner.setEditor(new SpinnerEditor(fontSizeSpinner));
		fontSizeSpinner.setToolTipText(trans.get("generalprefs.lbl.FontSize.ttip"));
		this.add(fontSizeSpinner, "wrap, sizegroup uiSettings");

		// Font style selector
		JLabel lblFontStyle = new JLabel(trans.get("generalprefs.lbl.FontStyle"));
		lblFontStyle.setToolTipText(trans.get("generalprefs.lbl.FontStyle.ttip"));
		this.add(lblFontStyle, "gapright para");
		final JComboBox<FontStyle> fontStyleCombo = new JComboBox<>(FontStyle.values());

		// Set the current font style
		for (FontStyle style : FontStyle.values()) {
			if (style.getFontName().equals(currentFontStyle)) {
				fontStyleCombo.setSelectedItem(style);
				break;
			}
		}
		this.add(fontStyleCombo, "sizegroup uiSettings, wrap");

		// Letter spacing selector
		JLabel lblSpacing = new JLabel(trans.get("generalprefs.lbl.CharacterSpacing"));
		lblSpacing.setToolTipText(trans.get("generalprefs.lbl.CharacterSpacing.ttip"));
		this.add(lblSpacing, "gapright para");

		// Create a custom DoubleModel that converts between UI values and actual tracking values
		final DoubleModel letterSpacingModel = new DoubleModel(preferences, "UIFontTracking", TRACKING_SCALE_FACTOR,
				UnitGroup.UNITS_NONE, MIN_TRACKING_UI, MAX_TRACKING_UI);

		final JSpinner characterSpacingSpinner = new JSpinner(letterSpacingModel.getSpinnerModel());
		characterSpacingSpinner.setEditor(new SpinnerEditor(characterSpacingSpinner));
		this.add(characterSpacingSpinner, "sizegroup uiSettings, wrap");

		// Restart warning label
		this.lblRestartOR = new JLabel();
		this.lblRestartOR.setForeground(GUIUtil.getUITheme().getDarkErrorColor());
		this.add(lblRestartOR, "spanx, wrap, growx");


		// Add preview panel
		previewPanel = new UIPreviewPanel();
		this.add(previewPanel, "span, grow, wrap");


		// Add change listeners
		themesCombo.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				Named<UITheme.Theme> selection = (Named<UITheme.Theme>) themesCombo.getSelectedItem();
				if (selection == null) return;
				UITheme.Theme t = selection.get();
				if (t == currentTheme) {
					lblRestartOR.setText("");
					return;
				}
				preferences.setUITheme(t);
				// TODO: re-enable once you have figured out how to update custom UITheme Colors (from UITheme.java)
				//  t.applyTheme();
				//  previewPanel.updateTheme(t);
				updateRestartLabel(lblRestartOR);
			}
		});

		uiScaleSpinner.addChangeListener(new UIPreferenceChangeListener());
		fontSizeSpinner.addChangeListener(new UIPreferenceChangeListener());
		characterSpacingSpinner.addChangeListener(new UIPreferenceChangeListener());

		fontStyleCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FontStyle selected = (FontStyle) fontStyleCombo.getSelectedItem();
				if (selected == null) return;
				preferences.setUIFontStyle(selected.getFontName());
				updatePreview();
				updateRestartLabel(lblRestartOR);
			}
		});

		updatePreview();
	}

	private void updateRestartLabel(JLabel label) {
		boolean needsRestart = false;

		// Check if any UI settings have changed
		needsRestart |= preferences.getUIScale() != currentUIScale;
		needsRestart |= preferences.getUIFontSize() != currentFontSize;
		needsRestart |= !preferences.getUIFontStyle().equals(currentFontStyle);
		needsRestart |= !GUIUtil.getUITheme().equals(currentTheme);

		if (needsRestart) {
			label.setText(trans.get("generalprefs.lbl.themeRestartOR"));
		} else {
			label.setText("");
		}
	}

	private void updatePreview() {
		previewPanel.updatePreview(
				preferences.getUIFontStyle(),
				preferences.getUIFontSize(),
				(float) preferences.getUIFontTracking()
		);
	}

	private class UIPreferenceChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			updatePreview();
			updateRestartLabel(lblRestartOR);
		}
	}
}