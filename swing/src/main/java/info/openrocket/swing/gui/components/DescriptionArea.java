package info.openrocket.swing.gui.components;

import info.openrocket.swing.gui.util.URLUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import java.net.URI;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class DescriptionArea extends JScrollPane {
	
	private final JEditorPane editorPane;

	private final float size;
	
	
	/**
	 * Construct a description area with the specified number of rows, default description font size,
	 * being opaque.
	 * 
	 * @param rows	the number of rows
	 */
	public DescriptionArea(int rows) {
		this("", rows, -1);
	}
	
	/**
	 * Construct a description area with the specified number of rows and size, being opaque.
	 * 
	 * @param rows	the number of rows.
	 * @param size	the font size difference compared to the default font size.
	 */
	public DescriptionArea(int rows, float size) {
		this("", rows, size);
	}
	
	/**
	 * Construct an opaque description area with the specified number of rows, size and text, being opaque.
	 * 
	 * @param text	the initial text.
	 * @param rows	the number of rows.
	 * @param size	the font size difference compared to the default font size.
	 */
	public DescriptionArea(String text, int rows, float size) {
		this(text, rows, size, true);
	}

	/**
	 * Construct an opaque description area with the specified number of rows, size and text, being opaque.
	 *
	 * @param text	the initial text.
	 * @param rows	the number of rows.
	 */
	public DescriptionArea(String text, int rows) {
		this(text, rows, -1);
	}

	/**
	 * Constructor with all options.
	 * 
	 * @param text		the text for the description area.
	 * @param rows		the number of rows to set
	 * @param size		the relative font size in points (positive or negative)
	 * @param opaque	if <code>false</code> the background color will be set to the background color
	 * 					of a default JPanel (simulation non-opaque)
	 */
	public DescriptionArea(String text, int rows, float size, boolean opaque) {
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.size = size;
		
		editorPane = new JEditorPane("text/html", "");
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						URI uri;
						try {
							uri = e.getURL().toURI();
						}
						catch (Exception ex) {
							throw new RuntimeException(ex);
						}
						
						// If the uri scheme indicates this is a resource in a jar file,
						// extract and write to a temporary file
						if (uri.getScheme().equals("jar")) {
							
							// get the resource
							final String uriString = uri.toString();
							final String resourceName = uriString.substring(uriString.indexOf("!") + 1);
							final BufferedInputStream is = new BufferedInputStream(getClass().getResourceAsStream(resourceName));
							
							// construct filename from resource name
							String prefix = resourceName.substring(1);
							String suffix;
							final int dotIndex = prefix.lastIndexOf(".");
							if (dotIndex > 0) {
								prefix = resourceName.substring(0, dotIndex);
								suffix = resourceName.substring(dotIndex+1);
							} else {
								// if there is no suffix, assume it's a raw text file.
								suffix = ".txt";
							}


							// create temporary file and copy resource to it
							File of;
							BufferedOutputStream os;
							try {
								of = File.createTempFile(prefix, suffix);
								os = new BufferedOutputStream(new FileOutputStream(of));
							}
							catch (Exception ex) {
								throw new RuntimeException(ex);
							}
							of.deleteOnExit();
							uri = of.toURI();

							try {
								byte buffer[] = is.readAllBytes();
								os.write(buffer);
								os.close();
							}
							catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						}
						
						try {	
							URLUtil.openWebpage(uri);
						}
						catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					}
				}
			});
		if (!opaque) {
			Color bg = new JPanel().getBackground();
			editorPane.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));
			this.setOpaque(true);
		}
		
		// Calculate correct height
		this.setText("abc");
		Dimension oneline = editorPane.getPreferredSize();
		this.setText("abc<br>def");
		Dimension twolines = editorPane.getPreferredSize();
		this.setText("");
		
		int lineheight = twolines.height - oneline.height;
		int extraheight = oneline.height - lineheight;
		
		Dimension dim = editorPane.getPreferredSize();
		dim.height = lineheight * rows + extraheight + 2;
		this.setPreferredSize(dim);
		
		this.setViewportView(editorPane);
		this.setText(text);
	}
	
	public void setText(String txt) {
		// Set the font size (we can't simply set the font to change the font size, because we're using text/html)
		Font defaultFont = editorPane.getFont();
		String fontName = defaultFont.getFontName();
		float fontSize = defaultFont.getSize2D() + size;

		editorPane.setText("<html><body style='font-family:" + fontName + ";font-size:" + fontSize + "pt;'>" + txt + "</body></html>");
		editorPane.revalidate();
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				editorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
			}
			
		});
		setForeground(editorPane.getForeground());
		editorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
	}

	/**
	 * Set the font to use for the text pane. If null, then the default font from OR is used.
	 * @param font font to use
	 */
	public void setTextFont(Font font) {
		if (editorPane == null) return;
		editorPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
		if (font != null) {
			editorPane.setFont(font);
		}
	}

	public void setBackground(Color color) {
		if (editorPane == null) return;
		editorPane.setBackground(color);
		StyledDocument styledDocument = (StyledDocument) editorPane.getDocument();
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, color);
		styledDocument.setCharacterAttributes(0, styledDocument.getLength(), attributeSet, false);
	}

	public void setForeground(Color color) {
		if (editorPane == null) return;
		editorPane.setForeground(color);
		StyledDocument styledDocument = (StyledDocument) editorPane.getDocument();
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, color);
		styledDocument.setCharacterAttributes(0, styledDocument.getLength(), attributeSet, false);
	}
	
}
