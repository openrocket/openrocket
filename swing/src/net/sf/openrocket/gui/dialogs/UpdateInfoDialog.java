package net.sf.openrocket.gui.dialogs;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.AssetHandler;
import net.sf.openrocket.communication.AssetHandler.UpdatePlatform;
import net.sf.openrocket.communication.ReleaseInfo;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.gui.components.StyledLabel;
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

		//	OpenRocket logo on the left
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-128.png", "OpenRocket")),
				"spany, top, gapright 20px, cell 0 0");

		//	OpenRocket version available!
		panel.add(new StyledLabel(trans.get("update.dlg.updateAvailable.lbl.title"), 8, StyledLabel.Style.BOLD), "spanx, wrap");

		// Your version
		ReleaseInfo release = info.getLatestRelease();
		panel.add(new StyledLabel(String.format(trans.get("update.dlg.updateAvailable.lbl.yourVersion"),
				release.getReleaseName(), BuildProperties.getVersion()), -1, StyledLabel.Style.PLAIN), "skip 1, spanx, wrap para");

		// Release notes
		panel.add(new StyledLabel(trans.get("update.dlg.updateAvailable.lbl.releaseNotes"), 1, StyledLabel.Style.BOLD), "spanx, wrap");

		// Release information box
		final JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		textPane.setMargin(new Insets(10, 10, 40, 10));
		textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);

		StringBuilder sb = new StringBuilder();
		sb.append("<html>");

		// 		Release notes
		String releaseNotes = release.getReleaseNotes();
		sb.append(MarkdownUtil.toHtml(releaseNotes)).append("<br><br>");

		//		GitHub link
		String releaseURL = release.getReleaseURL();
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
							  log.warn("Exception hyperlink: " + ex.getMessage());
						  }
					  }
				  }
			  });

		textPane.setText(sb.toString());
		textPane.setCaretPosition(0);	// Scroll to the top

		panel.add(new JScrollPane(textPane), "skip 1, left, spanx, grow, push, gapbottom 6px, wrap");

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
		panel.add(checkAtStartup, "skip 1, spanx, wrap");

		// Lower row buttons
		//// Remind me later button
		JButton btnLater = new SelectColorButton(trans.get("update.dlg.btn.remindMeLater"));
		btnLater.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UpdateInfoDialog.this.dispose();
			}
		});
		panel.add(btnLater, "skip 1, split 2");

		//// Skip this version button
		JButton btnSkip = new SelectColorButton(trans.get("update.dlg.checkbox.skipThisVersion"));
		btnSkip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> ignoreVersions = new ArrayList<>(preferences.getIgnoreUpdateVersions());
				String ignore = release.getReleaseName();
				if (!ignoreVersions.contains(ignore)) {
					ignoreVersions.add(ignore);
					preferences.setIgnoreUpdateVersions(ignoreVersions);
				}
				UpdateInfoDialog.this.dispose();
			}
		});
		panel.add(btnSkip);

		//// Install operating system combo box
		List<String> assetURLs = release.getAssetURLs();
		Map<UpdatePlatform, String> mappedAssets = AssetHandler.mapURLToPlatform(assetURLs);
		JComboBox<Object> comboBox;
		if (mappedAssets == null || mappedAssets.size() == 0) {
			comboBox = new JComboBox<>(new String[]{
					String.format("- %s -", trans.get("update.dlg.updateAvailable.combo.noDownloads"))});
		}
		else {
			comboBox = new JComboBox<>(mappedAssets.keySet().toArray(new UpdatePlatform[0]));
			comboBox.setRenderer(new CustomComboBoxRenderer());
			UpdatePlatform platform = AssetHandler.getUpdatePlatform();
			comboBox.setSelectedItem(platform);
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					((SwingPreferences) Application.getPreferences()).setUpdatePlatform((UpdatePlatform) comboBox.getSelectedItem());
				}
			});
		}
		panel.add(comboBox, "pushx, right");

		//// Install update button
		JButton btnInstall = new SelectColorButton(trans.get("update.dlg.updateAvailable.but.install"));
		btnInstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mappedAssets == null) return;
				String url = AssetHandler.getInstallerURLForPlatform((UpdatePlatform) comboBox.getSelectedItem(),
						release.getReleaseName());
				if (url == null) return;
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI(url));
				} catch (Exception ex) {
					log.warn("Exception install link: " + ex.getMessage());
				}
			}
		});
		if (mappedAssets == null || mappedAssets.size() == 0) {
			btnInstall.setEnabled(false);
		}
		panel.add(btnInstall, "wrap");

		panel.setPreferredSize(new Dimension(850, 700));

		this.add(panel);
		
		this.pack();
		this.setLocationRelativeTo(null);
		GUIUtil.setDisposableDialogOptions(this, btnLater);
	}

	/**
	 * ComboBox renderer to display an UpdatePlatform by the platform name
	 */
	private static class CustomComboBoxRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof UpdatePlatform) {
				setText(AssetHandler.getPlatformName((UpdatePlatform)value));
			}
			return this;
		}
	}
}
