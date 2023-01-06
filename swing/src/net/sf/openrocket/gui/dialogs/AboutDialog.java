package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	
	public final String OPENROCKET_URL = "http://openrocket.info/";

	private final Translator trans = Application.getTranslator();
	
	private final String CREDITS = "<html><center>" +
		"<font size=\"+1\"><b>OpenRocket has been developed by:</b></font><br>" +
		"<br>" +
		"Sampo Niskanen (main developer)<br>" +
		"Doug Pedrick (RockSim file format, printing)<br>" +
		"Kevin Ruland (Android version)<br>" +
		"Bill Kuker (3D visualization)<br>" +
		"Boris du Reau (internationalization, translation lead)<br>" +
		"Richard Graham (geodetic computations)<br>" +
		"Jason Blood (finset import)<br>" +
		"Daniel Williams (pod support, maintainer)<br>" +
		"Joe Pfeiffer (maintainer)<br>" +
		"Billy Olsen (maintainer)<br>" +
		"Sibo Van Gool (maintainer)<br>" +
		"Justin Hanney (maintainer)<br>" +
		"Neil Weinstock (tester, icons, forum support)<br>" +
		"H. Craig Miller (tester)<br><br>" +
		"<b>Translations by:</b><br><br>" +
		"Tripoli France (French)<br>" +
		"Stefan Lobas / ERIG e.V. (German)<br>" +
		"Tripoli Spain (Spanish)<br>" +
		"Sky Dart Team / Ruslan V. Uss (Russian)<br>" +
		"Mauro Biasutti (Italian)<br>" +
		"Vladimir Beran (Czech)<br>" +
		"Polish Rocketry Society / \u0141ukasz & Alex Kazanski (Polish)<br>" +
		"Sibo Van Gool (Dutch)<br>" +
		"Mohamed Amin Elkebsi (Arabic)<br>" +
		"<br>" +
		"See all contributors at <br>" + href("https://github.com/openrocket/openrocket/graphs/contributors", false, false) + "<br>" +
		"<br>" +
		"<b>OpenRocket utilizes the following libraries:</b><br>" +
		"<br>" +
		"MiG Layout" + href("http://www.miglayout.com", true, true) + "<br>" +
		"JFreeChart" + href("http://www.jfree.org/jfreechart", true, true) + "<br>" +
		"iText" + href("http://www.itextpdf.com", true, true) + "<br>" +
		"exp4j" + href("http://projects.congrace.de/exp4j/index.html", true, true) + "<br>" +
		"JOGL" + href("http://jogamp.org/jogl/www", true, true) + "<br>" +
		"Guava" + href("https://github.com/google/guava", true, true) + "<br>" +
		"Opencsv" + href("http://opencsv.sourceforge.net", true, true) + "<br>" +
		"Simple Logging Facade for Java" + href("http://www.slf4j.org", true, true) + "<br>" +
		"Java library for parsing and rendering CommonMark" + href("https://github.com/commonmark/commonmark-java", true, true) + "<br>" +
		"RSyntaxTextArea" + href("http://bobbylight.github.io/RSyntaxTextArea", true, true) + "<br>" +
		"<br>" +
		"<b>OpenRocket gratefully acknowledges our use of the following databases:</b><br>" +
		"<br>" +
		"Rocket Motor Data" + href("https://www.thrustcurve.org", true, true) + "<br>" +
		"Enhanced components database for OpenRocket" + href("https://github.com/dbcook/openrocket-database", true, true) +
		"</center></html>";

	private String href(String url, boolean delimiters, boolean leadingSpace) {
		return (leadingSpace ? " " : "") + (delimiters ? "(" : "") + "<a href=\"" + url + "\">" + url + "</a>" + (delimiters ? ")" : "");
	}
	
	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = BuildProperties.getVersion();
		final String copyrightYear = BuildProperties.getCopyrightYear();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		
		
		// OpenRocket logo
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-128.png", "OpenRocket")), "top");
		
		
		// OpenRocket version info + copyright
		sub = new JPanel(new MigLayout("fill"));
		
		sub.add(new StyledLabel("OpenRocket", 20), "ax 50%, growy, wrap para");
		sub.add(new StyledLabel(trans.get("lbl.version").trim() + " " + version, 3), "ax 50%, growy, wrap rel");
		String copyright = String.format("Copyright %c 2007-%s Sampo Niskanen and others", Chars.COPY, copyrightYear);
		sub.add(new StyledLabel(copyright), "ax 50%, growy, wrap para");
		
		sub.add(new URLLabel(OPENROCKET_URL), "ax 50%, growy, wrap para");
		panel.add(sub, "grow, pushx");
		
		
		// Translation information (if present)
		String translation = trans.get("lbl.translation").trim();
		String translator = trans.get("lbl.translator").trim();
		String translatorWebsite = trans.get("lbl.translatorWebsite").trim();
		String translatorIcon = trans.get("lbl.translatorIcon").trim();
		
		if (translator.length() > 0 || translatorWebsite.length() > 0 || translatorIcon.length() > 0) {
			sub = new JPanel(new MigLayout("fill"));
			
			sub.add(new StyledLabel(translation, Style.BOLD), "ax 50%, growy, wrap para");
			
			if (translatorIcon.length() > 0) {
				sub.add(new JLabel(Icons.loadImageIcon("pix/translators/" + translatorIcon, translator)),
						"ax 50%, growy, wrap para");
			}
			if (translator.length() > 0) {
				sub.add(new JLabel(translator), "ax 50%, growy, wrap rel");
			}
			if (translatorWebsite.length() > 0) {
				sub.add(new URLLabel(translatorWebsite), "ax 50%, growy, wrap para");
			}
			
			panel.add(sub);
		}
		
		
		DescriptionArea info = new DescriptionArea(5);
		info.setText(CREDITS);
		info.setTextFont(UIManager.getFont("Label.font"));
		panel.add(info, "newline, width 10px, height 250lp, pushy, grow, spanx, wrap para");
		
		//		JTextArea area = new JTextArea(CREATORS);
		//		area.setEditable(false);
		//		area.setLineWrap(true);
		//		area.setWrapStyleWord(true);
		//		panel.add(new JScrollPane(area), "width 10px, height 100lp, grow, spanx, wrap para");
		
		
		//Close button
		JButton close = new SelectColorButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle("OpenRocket " + version);
		this.pack();
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
}
