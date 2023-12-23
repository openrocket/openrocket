/*
 * PageFitPrintStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * A strategy for drawing multiple rocket components onto as few pages as possible.
 *
 * @author Jason Blood <dyster2000@gmail.com>
 */
public class PageFitPrintStrategy {

    /** The margin. */
    public final static int MARGIN = (int)(PrintUnit.POINTS_PER_INCH * 0.3f);

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PageFitPrintStrategy.class);

    /**
     * The iText document.
     */
    protected Document document;

    /**
     * The direct iText writer.
     */
    protected PdfWriter writer;

    /**
     * The stages selected.
     */
    protected Set<Integer> stages;

	protected ArrayList<PrintableComponent> componentToPrint;

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     */
    public PageFitPrintStrategy(Document doc, PdfWriter theWriter) {
        document = doc;
        writer = theWriter;
    	componentToPrint = new ArrayList<PrintableComponent>();
    }

    /**
     * Add a component we want to print.
     *
     * @param component The component to add for printing
     */
    public void addComponent(PrintableComponent component) {
		componentToPrint.add(component);
    }

    /**
     * Recurse through the given rocket component.
     */
    public void writeToDocument () {
        fitPrintComponents();
    }

    /**
     * Iterate through the components to print fitting them onto pages as best possible.
     */
    private void fitPrintComponents() {
    	final Dimension pageSize = getPageSize();
        double wPage = pageSize.getWidth();
        double hPage = pageSize.getHeight();
        int marginX = MARGIN;
        int marginY = MARGIN;
        PdfContentByte cb = writer.getDirectContent();

        Collections.sort(componentToPrint);

    	while (componentToPrint.size() > 0) {
    		int pageY = marginY;
    		Boolean anyAddedToRow;

            Graphics2D g2 = createGraphics((float) wPage, (float) hPage, cb);

    		do {
	    		// Fill the row
	    		int rowX = marginX;
	    		int rowY = pageY;
	        	ListIterator<PrintableComponent> entry = componentToPrint.listIterator();
	        	anyAddedToRow = false;

	        	while (entry.hasNext()) {
		        	PrintableComponent component = entry.next();
		        	java.awt.Dimension dim = component.getSize();
		        	if ((rowX + dim.width + marginX <= wPage) && (rowY + dim.height + marginY <= hPage)) {
		        		component.setPrintOffset(rowX, rowY);
                        // Separate each component horizontally by a space equal to the margin
		        		rowX += dim.width + marginX;
		        		if (rowY + dim.height + marginY > pageY) {
		        			pageY = rowY + dim.height + marginY;
                        }
                        entry.remove();
		        		component.print(g2);
		        		anyAddedToRow = true;
		        	}
		        }
                // Separate each component vertically by a space equal to the margin
                pageY += marginY;
    		} while (anyAddedToRow);

        	g2.dispose();
        	document.newPage();
    	}
    }

    /**
     * Create a graphics context.
     *
     * @param theWPage the width of the physical page
     * @param theHPage the height of the physical
     *
     * @param theCb the pdf content byte instance
     *
     * @return a Graphics2D instance
     */
    Graphics2D createGraphics(final float theWPage, final float theHPage, final PdfContentByte theCb) {
        return theCb.createGraphics(theWPage, theHPage);
    }

    /**
     * Get the dimensions of the paper page.
     *
     * @return an internal Dimension
     */
    protected Dimension getPageSize () {
        return new Dimension(document.getPageSize().getWidth(),
                             document.getPageSize().getHeight());
    }
}
