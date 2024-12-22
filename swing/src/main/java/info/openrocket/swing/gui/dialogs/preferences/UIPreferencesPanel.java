package info.openrocket.swing.gui.dialogs.preferences;

import info.openrocket.core.util.Named;
import info.openrocket.core.util.Utils;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class UIPreferencesPanel extends PreferencesPanel {
	private final UITheme.Theme currentTheme;
	private final int currentFontSize;
	private final double currentUIScale;

	public UIPreferencesPanel(PreferencesDialog parent) {
		super(parent, new MigLayout("fillx, ins 30lp n n n"));

		this.currentTheme = GUIUtil.getUITheme();
		this.currentFontSize = preferences.getUIFontSize();
		this.currentUIScale = preferences.getUIScale();

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
		this.add(themesCombo, "wrap, growx");

		// UI Scale selector
		JLabel lblUIScale = new JLabel(trans.get("generalprefs.lbl.UIScale"));
		lblUIScale.setToolTipText(trans.get("generalprefs.lbl.UIScale.ttip"));
		this.add(lblUIScale, "gapright para");
		final DoubleModel uiScaleModel = new DoubleModel(preferences, "UIScale", 0.5, 2.0);
		final JSpinner uiScaleSpinner = new JSpinner(uiScaleModel.getSpinnerModel());
		uiScaleSpinner.setEditor(new SpinnerEditor(uiScaleSpinner));
		uiScaleSpinner.setToolTipText(trans.get("generalprefs.lbl.UIScale.ttip"));
		this.add(uiScaleSpinner, "growx, wrap");

		// Font size selector
		JLabel lblFontSize = new JLabel(trans.get("generalprefs.lbl.FontSize"));
		lblFontSize.setToolTipText(trans.get("generalprefs.lbl.FontSize.ttip"));
		this.add(lblFontSize, "gapright para");
		final IntegerModel fontSizeModel = new IntegerModel(preferences, "UIFontSize", 5, 25);
		final JSpinner fontSizeSpinner = new JSpinner(fontSizeModel.getSpinnerModel());
		fontSizeSpinner.setEditor(new SpinnerEditor(fontSizeSpinner));
		fontSizeSpinner.setToolTipText(trans.get("generalprefs.lbl.FontSize.ttip"));
		this.add(fontSizeSpinner, "growx, wrap");

		// Restart warning label
		final JLabel lblRestartOR = new JLabel();
		lblRestartOR.setForeground(GUIUtil.getUITheme().getDarkErrorColor());
		this.add(lblRestartOR, "spanx, wrap, growx");

		// Add change listeners
		fontSizeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateRestartLabel(lblRestartOR);
			}
		});

		uiScaleSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateRestartLabel(lblRestartOR);
			}
		});

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
				updateRestartLabel(lblRestartOR);
			}
		});
	}

	private void updateRestartLabel(JLabel label) {
		boolean needsRestart = false;

		// Check if any UI settings have changed
		needsRestart |= preferences.getUIFontSize() != currentFontSize;
		needsRestart |= preferences.getUIScale() != currentUIScale;
		needsRestart |= !GUIUtil.getUITheme().equals(currentTheme);

		if (needsRestart) {
			label.setText(trans.get("generalprefs.lbl.themeRestartOR"));
		} else {
			label.setText("");
		}
	}
}