/*
 * PrintUtilities.java
 */
package net.sf.openrocket.gui.print;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.RepaintManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;

/**
 * Utilities methods and fonts used for printing.
 */
public class PrintUtilities implements Printable {
	
	/**
	 * The logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(PrintUtilities.class);
	
	public static final int NORMAL_FONT_SIZE = Font.DEFAULTSIZE - 3;
	public static final int SMALL_FONT_SIZE = NORMAL_FONT_SIZE - 3;
	
	public static final Font BOLD = new Font(ITextHelper.getBaseFont(), NORMAL_FONT_SIZE, Font.BOLD);
	public static final Font BIG_BOLD = new Font(ITextHelper.getBaseFont(), NORMAL_FONT_SIZE + 3, Font.BOLD);
	public static final Font BOLD_UNDERLINED = new Font(ITextHelper.getBaseFont(), NORMAL_FONT_SIZE,
														Font.BOLD | Font.UNDERLINE);
	public static final Font NORMAL = new Font(ITextHelper.getBaseFont(), NORMAL_FONT_SIZE);
	public static final Font SMALL = new Font(ITextHelper.getBaseFont(), SMALL_FONT_SIZE);
	

	private Component componentToBePrinted;
	
	public PrintUtilities(Component componentToBePrinted) {
		this.componentToBePrinted = componentToBePrinted;
	}
	
	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return (NO_SUCH_PAGE);
		} else {
			Graphics2D g2d = (Graphics2D) g;
			translateToJavaOrigin(g2d, pageFormat);
			disableDoubleBuffering(componentToBePrinted);
			componentToBePrinted.paint(g2d);
			enableDoubleBuffering(componentToBePrinted);
			return (PAGE_EXISTS);
		}
	}
	
	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}
	
	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
	
	
	/**
	 * Translate the page format coordinates onto the graphics object using Java's origin (top left).
	 *
	 * @param g2d        the graphics object
	 * @param pageFormat the print page format
	 */
	public static void translateToJavaOrigin(Graphics2D g2d, PageFormat pageFormat) {
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	}
	
	/**
	 * Add text as a new paragraph in a given font to the document.
	 *
	 * @param document  the document
	 * @param font      the font
	 * @param title     the title
	 */
	public static void addText(Document document, com.itextpdf.text.Font font, String title) {
		Chunk sectionHeader = new Chunk(title);
		sectionHeader.setFont(font);
		try {
			Paragraph p = new Paragraph();
			p.add(sectionHeader);
			document.add(p);
		} catch (DocumentException e) {
			log.error("Could not add paragraph.", e);
		}
	}
}
