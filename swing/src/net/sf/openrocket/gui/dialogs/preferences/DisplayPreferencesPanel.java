/**
 * 
 */
package net.sf.openrocket.gui.dialogs.preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.startup.Preferences;

/**
 * @author cpearls
 *
 */
public class DisplayPreferencesPanel extends PreferencesPanel {
	public DisplayPreferencesPanel() {
		super(new MigLayout("fillx"));
		// // Font size of information in panel window
		this.add(new JLabel(trans.get("pref.dlg.lbl.Rocketinfofontsize")),
				"gapright para");

		this.add(
				new JComboBox<Object>(new PrefChoiceSelector(
						Preferences.ROCKET_INFO_FONT_SIZE,
						// // Small
						// // Medium
						// // Large
						trans.get("pref.dlg.PrefFontSmall"), trans
								.get("pref.dlg.PrefFontMedium"), trans
								.get("pref.dlg.PrefFontLarge"))),
				"wrap para, growx, sg combos");

	}
}
