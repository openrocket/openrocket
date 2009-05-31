package net.sf.openrocket.gui.components;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;

/**
 * A label of a URL that is clickable.  Clicking the URL will launch the URL in
 * the default browser if the Desktop class is supported.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class URLLabel extends JLabel {
	
	private final String url;
	
	public URLLabel(String urlLabel) {
		super();
		
		this.url = urlLabel;
		

		if (Desktop.isDesktopSupported()) {
			
			setText("<html><a href=\"" + url + "\">" + url + "</a>");

			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Desktop d = Desktop.getDesktop();
					try {
						d.browse(new URI(url));
					} catch (URISyntaxException e1) {
						throw new RuntimeException("BUG: Illegal URL: " + url, e1);
					} catch (IOException e1) {
						System.err.println("Unable to launch browser:");
						e1.printStackTrace();
					}
				}
			});
			
		} else {
			setText(url);
		}
	}
}
