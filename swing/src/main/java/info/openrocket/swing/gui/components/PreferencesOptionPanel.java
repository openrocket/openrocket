package info.openrocket.swing.gui.components;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A panel that adds storage options for exporting preferences.
 */
public class PreferencesOptionPanel extends JPanel {
    private static final Translator trans = Application.getTranslator();
    private static final Preferences prefs = Application.getPreferences();

    private final JCheckBox exportUserDirectories;
    private final JCheckBox exportWindowInfo;

    public PreferencesOptionPanel() {
        super(new MigLayout("fill, ins 0"));

        JPanel panel = new JPanel(new MigLayout("fill, ins 4lp"));
        panel.setBorder(BorderFactory.createTitledBorder(trans.get("PreferencesOptionPanel.title")));

        // Export user directories
        exportUserDirectories = new JCheckBox(trans.get("PreferencesOptionPanel.checkbox.userDirectories"));
        exportUserDirectories.setToolTipText(trans.get("PreferencesOptionPanel.checkbox.userDirectories.ttip"));
        exportUserDirectories.setSelected(prefs.getExportUserDirectories());
        exportUserDirectories.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                prefs.setExportUserDirectories(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        panel.add(exportUserDirectories, "wrap");

        // Export window information (position, size...)
        exportWindowInfo = new JCheckBox(trans.get("PreferencesOptionPanel.checkbox.windowInfo"));
        exportWindowInfo.setToolTipText(trans.get("PreferencesOptionPanel.checkbox.windowInfo.ttip"));
        exportWindowInfo.setSelected(prefs.getExportWindowInformation());
        exportWindowInfo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                prefs.setExportWindowInformation(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        panel.add(exportWindowInfo, "wrap 10lp");


        this.add(panel, "growx, north");
    }

    public boolean isIgnoreUserDirectories() {
        return !exportUserDirectories.isSelected();
    }

    public boolean isIgnoreWindowInformation() {
        return !exportWindowInfo.isSelected();
    }
}
