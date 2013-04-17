package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

public class AboutDialog extends JDialog {
	
	public static final String OPENROCKET_URL = "http://openrocket.sourceforge.net/";
	private static final Translator trans = Application.getTranslator();
	
	private static final String CREDITS = "<html><center>" +
			"<font size=\"+1\"><b>OpenRocket has been developed by:</b></font><br><br>" +
			"Sampo Niskanen (main developer)<br>" +
			"Doug Pedrick (RockSim file format, printing)<br>" +
			"Kevin Ruland (Android version)<br>" +
			"Bill Kuker (3D visualization)<br>" +
			"Boris du Reau (internationalization, translation lead)<br>" +
			"Richard Graham (geodetic computations)<br>" +
			"Jason Blood (finset import)<br><br>" +
			"<b>Translations by:</b><br><br>" +
			"Tripoli France (French)<br>" +
			"Stefan Lobas / ERIG e.V. (German)<br>" +
			"Tripoli Spain (Spanish)<br>" +
			"Sky Dart Team (Russian)<br>" +
			"Mauro Biasutti (Italian)<br><br>" +
			"Vladimir Beran  (Czech)<br><br>" +
			"Polish Rocketry Society / \u0141ukasz & Alex kazanski  (Polish)<br><br>" +
			"<b>OpenRocket utilizes the following libraries:</b><br><br>" +
			"MiG Layout (http://www.miglayout.com/)<br>" +
			"JFreeChart (http://www.jfree.org/jfreechart/)<br>" +
			"iText (http://www.itextpdf.com/)<br>" +
			"exp4j (http://projects.congrace.de/exp4j/index.html)<br>" +
			"JOGL (http://jogamp.org/jogl/www/)";
	
	
	public AboutDialog(JFrame parent) {
		super(parent, true);
		
		final String version = BuildProperties.getVersion();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		
		
		// OpenRocket logo
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-about.png", "OpenRocket")), "top");
		
		
		// OpenRocket version info + copyright
		sub = new JPanel(new MigLayout("fill"));
		
		sub.add(new StyledLabel("OpenRocket", 20), "ax 50%, growy, wrap para");
		sub.add(new StyledLabel(trans.get("lbl.version").trim() + " " + version, 3), "ax 50%, growy, wrap rel");
		sub.add(new StyledLabel("Copyright " + Chars.COPY + " 2007-2013 Sampo Niskanen and others"), "ax 50%, growy, wrap para");
		
		sub.add(new URLLabel(OPENROCKET_URL), "ax 50%, growy, wrap para");
		panel.add(sub, "grow");
		
		
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
		panel.add(info, "newline, width 10px, height 150lp, grow, spanx, wrap para");
		
		//		JTextArea area = new JTextArea(CREATORS);
		//		area.setEditable(false);
		//		area.setLineWrap(true);
		//		area.setWrapStyleWord(true);
		//		panel.add(new JScrollPane(area), "width 10px, height 100lp, grow, spanx, wrap para");
		
		
		//Close button
		JButton close = new JButton(trans.get("button.close"));
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
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
}
