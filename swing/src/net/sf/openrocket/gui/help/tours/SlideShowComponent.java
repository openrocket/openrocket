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
	
	@SuppressWarnings("hiding")
	private final int WIDTH = 600;
	private final int HEIGHT_IMAGE = 400;
	private final int HEIGHT_TEXT = 100;
	
	private final ImageDisplayComponent imageDisplay;
	private final JEditorPane textPane;
	
	
	public SlideShowComponent() {
		super(VERTICAL_SPLIT);
		
		imageDisplay = new ImageDisplayComponent();
		imageDisplay.setPreferredSize(new Dimension(WIDTH, HEIGHT_IMAGE));
		this.setLeftComponent(imageDisplay);
		
		textPane = new JEditorPane("text/html", "");
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(WIDTH, HEIGHT_TEXT));
		
		JScrollPane scrollPanel = new JScrollPane(textPane);
		this.setRightComponent(scrollPanel);
		
		this.setResizeWeight(((double) HEIGHT_IMAGE) / (HEIGHT_IMAGE + HEIGHT_TEXT));
	}
	
	
	
	public void setSlide(Slide slide) {
		this.imageDisplay.setImage(slide.getImage());
		this.textPane.setText(slide.getText());
		this.textPane.setCaretPosition(0);
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
