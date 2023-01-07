package net.sf.openrocket.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.MarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog that opens when you start up OpenRocket. It will display the release notes of the current version.
 */
public class WelcomeDialog extends JDialog {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(WelcomeDialog.class);

    /**
     * @param releaseNotes the release notes to display for the current version
     */
    public WelcomeDialog(String releaseNotes) {
        super(null, trans.get("welcome.dlg.title"), ModalityType.APPLICATION_MODAL);

        JPanel panel = new JPanel(new MigLayout("insets n n 8px n, fill"));

        //	OpenRocket logo on the left
        panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-128.png", "OpenRocket")),
                "spany, top, gapright 20px, cell 0 0");

        //	Thank you for downloading!
        panel.add(new StyledLabel(trans.get("welcome.dlg.lbl.thankYou") + " " + BuildProperties.getVersion() + "!",
                2, StyledLabel.Style.BOLD), "spanx, wrap");

        // See the release notes below for what's new
        panel.add(new StyledLabel(trans.get("welcome.dlg.lbl.seeReleaseNotes"),
                1, StyledLabel.Style.PLAIN), "skip 1, spanx, wrap para");

        // Release notes
        final JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setMargin(new Insets(10, 10, 10, 10));
        textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);

        String sb = "<html>" +
                MarkdownUtil.toHtml(releaseNotes) + "<br><br>" +
                "</html>";
        textPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        log.warn("Exception hyperlink: " + ex.getMessage());
                    }
                }
            }
        });

        textPane.setText(sb);
        textPane.setCaretPosition(0);	// Scroll to the top

        panel.add(new JScrollPane(textPane), "skip 1, left, spanx, grow, push, gapbottom 6px, wrap");

        // Don't show this dialog again
        JCheckBox dontShowAgain = new JCheckBox(trans.get("welcome.dlg.checkbox.dontShowAgain"));
        dontShowAgain.setSelected(Application.getPreferences().getIgnoreWelcome(BuildProperties.getVersion()));     // Normally this should never be true, but just in case
        panel.add(dontShowAgain, "skip 1");
        dontShowAgain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Application.getPreferences().setIgnoreWelcome(BuildProperties.getVersion(), dontShowAgain.isSelected());
            }
        });

        // Close button
        JButton closeBtn = new SelectColorButton(trans.get("button.close"));
        closeBtn.setToolTipText(trans.get("welcome.dlg.btn.close.ttip"));
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WelcomeDialog.this.dispose();
            }
        });
        panel.add(closeBtn, "pushx, right, wrap");

        panel.setPreferredSize(new Dimension(800, 600));
        this.add(panel);

        this.pack();
        this.setLocationRelativeTo(null);
        GUIUtil.setDisposableDialogOptions(this, closeBtn);
    }
}
