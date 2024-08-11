/**
 * 
 */
package info.openrocket.swing.gui.dialogs.preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;

/**
 * @author cpearls
 *
 */
@SuppressWarnings("serial")
public class DisplayPreferencesPanel extends PreferencesPanel {
	public DisplayPreferencesPanel() {
		super(new MigLayout("fillx"));
		// // Font size of information in panel window
		this.add(new JLabel(trans.get("pref.dlg.lbl.Rocketinfofontsize")),
				"gapright para");

		this.add(
				new JComboBox<>(new PrefChoiceSelector(
						ApplicationPreferences.ROCKET_INFO_FONT_SIZE,
						// // Small
						// // Medium
						// // Large
						trans.get("pref.dlg.PrefFontSmall"), trans
								.get("pref.dlg.PrefFontMedium"), trans
								.get("pref.dlg.PrefFontLarge"))),
				"wrap para, growx, sg combos");

	}
}
