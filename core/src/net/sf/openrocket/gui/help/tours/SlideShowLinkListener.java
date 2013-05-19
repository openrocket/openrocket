package net.sf.openrocket.gui.help.tours;

import java.awt.Desktop;
import java.awt.Window;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class SlideShowLinkListener implements HyperlinkListener {
	
	private static final Logger log = LoggerFactory.getLogger(SlideShowLinkListener.class);
	private static final Translator trans = Application.getTranslator();
	
	private final Window parent;
	
	public SlideShowLinkListener(Window parent) {
		this.parent = parent;
	}
	
	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		
		if (event.getEventType() != EventType.ACTIVATED) {
			return;
		}
		
		URL url = event.getURL();
		if (url != null && (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equals("https"))) {
			
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(url.toURI());
				} catch (Exception e) {
					// Ignore
				}
			}
			
		} else {
			
			String name = event.getDescription();
			try {
				SlideSet ss = SlideSetManager.getSlideSetManager().getSlideSet(name);
				
				SlideShowDialog dialog = new SlideShowDialog(parent);
				dialog.setSlideSet(ss, 0);
				dialog.setVisible(true);
			} catch (IllegalArgumentException e) {
				log.warn("Guided tour '" + name + "' not found");
				JOptionPane.showMessageDialog(parent,
						trans.get("error.msg"), trans.get("error.title"), JOptionPane.WARNING_MESSAGE);
			}
			
		}
		
	}
}
