package net.sf.openrocket.gui.help.tours;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import net.sf.openrocket.gui.components.ImageDisplayComponent;

/**
 * Component that displays a single slide, with the image on top and
 * text below it.  The portions are resizeable.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SlideShowComponent extends JSplitPane {
	
	private final ImageDisplayComponent imageDisplay;
	private final JEditorPane textPane;
	
	
	public SlideShowComponent() {
		super(VERTICAL_SPLIT);
		
		imageDisplay = new ImageDisplayComponent();
		imageDisplay.setPreferredSize(new Dimension(600, 350));
		this.setLeftComponent(imageDisplay);
		
		textPane = new JEditorPane("text/html", "");
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(600, 100));
		
		JScrollPane scrollPanel = new JScrollPane(textPane);
		this.setRightComponent(scrollPanel);
		
		this.setResizeWeight(0.7);
	}
	
	

	public void setSlide(Slide slide) {
		this.imageDisplay.setImage(slide.getImage());
		this.textPane.setText(slide.getText());
	}
	
	
	/**
	 * Replace the current HTML style sheet with a new style sheet.
	 */
	public void setStyleSheet(StyleSheet newStyleSheet) {
		HTMLDocument doc = (HTMLDocument) textPane.getDocument();
		StyleSheet base = doc.getStyleSheet();
		StyleSheet[] linked = base.getStyleSheets();
		if (linked != null) {
			for (StyleSheet ss : linked) {
				base.removeStyleSheet(ss);
			}
		}
		
		base.addStyleSheet(newStyleSheet);
	}
	
	
	public void addHyperlinkListener(HyperlinkListener listener) {
		textPane.addHyperlinkListener(listener);
	}
	
}
