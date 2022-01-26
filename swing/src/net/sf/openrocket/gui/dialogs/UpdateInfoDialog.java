package net.sf.openrocket.gui.dialogs;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.ReleaseInfo;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.MarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog that pops up when a new update for OpenRocket is found
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class UpdateInfoDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(UpdateInfoDialog.class);
	private static final Translator trans = Application.getTranslator();
	private final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();

	public UpdateInfoDialog(UpdateInfo info) {
		//// OpenRocket update available
		super(null, trans.get("update.dlg.updateAvailable.title"), ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("insets n n 8px n, fill"));

		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-about.png", "OpenRocket")),
				"split, span, top");

		// Release information box
		final JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);

		ReleaseInfo release = info.getLatestRelease();
		StringBuilder sb = new StringBuilder();

		// 		OpenRocket version available!
		sb.append("<html>");
		sb.append(String.format("<h1>%s</h1>", String.format(trans.get("update.dlg.updateAvailable.txtPane.title"), release.getReleaseName())));

		//		Your version
		sb.append(String.format("<i>%s</i> <br><br>", String.format(trans.get("update.dlg.updateAvailable.txtPane.yourVersion"), BuildProperties.getVersion())));

		// 		Changelog
		sb.append(String.format("<h2>%s</h2>", trans.get("update.dlg.updateAvailable.txtPane.changelog")));
		String releaseNotes = release.getReleaseNotes();
		releaseNotes = releaseNotes.replaceAll("^\"|\"$", "");	// Remove leading and trailing quotations
		sb.append(MarkdownUtil.toHtml(releaseNotes)).append("<br><br>");

		//		GitHub link
		String releaseURL = release.getReleaseURL();
		releaseURL = releaseURL.replaceAll("^\"|\"$", "");	// Remove leading and trailing quotations
		sb.append(String.format("<a href='%s'>%s</a>", releaseURL, trans.get("update.dlg.updateAvailable.txtPane.readMore")));
		sb.append("</html>");
		textPane.addHyperlinkListener(new HyperlinkListener() {
				  @Override
				  public void hyperlinkUpdate(HyperlinkEvent e) {
					  if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
						  Desktop desktop = Desktop.getDesktop();
						  try {
							  desktop.browse(e.getURL().toURI());
						  } catch (Exception ex) {
							  log.debug("Exception hyperlink: " + ex.getMessage());
						  }
					  }
				  }
			  });

		textPane.setText(sb.toString());

		panel.add(new JScrollPane(textPane), "left, grow, span, push, gapleft 40px, gapbottom 6px, wrap");

		//// Check for software updates at startup
		JCheckBox checkAtStartup = new JCheckBox(trans.get("pref.dlg.checkbox.Checkupdates"));
		//// Check for software updates every time you start up OpenRocket
		checkAtStartup.setToolTipText(trans.get("pref.dlg.checkbox.Checkupdates.ttip"));
		checkAtStartup.setSelected(preferences.getCheckUpdates());
		checkAtStartup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setCheckUpdates(checkAtStartup.isSelected());
			}
		});
		panel.add(checkAtStartup);
		
		// Cancel button
		JButton button = new SelectColorButton(trans.get("button.cancel"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UpdateInfoDialog.this.dispose();
			}
		});
		panel.add(button, "right, gapright para");

		panel.setPreferredSize(new Dimension(900, 600));

		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		GUIUtil.setDisposableDialogOptions(this, button);
	}
	
}
