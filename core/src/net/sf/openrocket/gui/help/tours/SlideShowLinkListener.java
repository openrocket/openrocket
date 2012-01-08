package net.sf.openrocket.gui.help.tours;

import java.awt.Desktop;
import java.awt.Window;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import net.sf.openrocket.startup.Application;

public class SlideShowLinkListener implements HyperlinkListener {
	
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
				Application.getExceptionHandler().handleErrorCondition("Guided tour '" + name + "' not found.");
			}
			
		}
		
	}
}
