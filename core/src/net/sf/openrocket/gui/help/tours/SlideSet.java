package net.sf.openrocket.gui.help.tours;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.StyleSheet;

/**
 * A set of slides that composes a tour.
 * 
 * A slide set contains a (localized, plain-text) title for the tour, a (possibly
 * multiline, HTML-formatted) description and a number of slides.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SlideSet {
	
	private String title = "";
	private String description = "";
	private final List<Slide> slides = new ArrayList<Slide>();
	private StyleSheet styleSheet = new StyleSheet();
	
	

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String name) {
		this.title = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public Slide getSlide(int index) {
		return this.slides.get(index);
	}
	
	public void addSlide(Slide slide) {
		this.slides.add(slide);
	}
	
	public int getSlideCount() {
		return this.slides.size();
	}
	
	public StyleSheet getStyleSheet() {
		return styleSheet;
	}
	
	public void setStyleSheet(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}
	
}
