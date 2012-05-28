/*
 * PageFitPrintStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableComponent;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

/**
 * A strategy for drawing multiple rocket components onto as few pages as possible.
 *
 * @author Jason Blood <dyster2000@gmail.com>
 */
public class PageFitPrintStrategy {

    /**
     * The logger.
     */
    private static final LogHelper log = Application.getLogger();

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
     *
     * @param root the root component; all children will be printed recursively
     */
    public void writeToDocument (final RocketComponent root) {
        fitPrintComponents();
    }

    /**
     * Iterate through the components to print fitting them onto pages as best possible.
     */
    private void fitPrintComponents() {
    	final Dimension pageSize = getPageSize();
        double wPage = pageSize.getWidth();
        double hPage = pageSize.getHeight();
        int marginX = (int)(PrintUnit.POINTS_PER_INCH * 0.3f);
        int marginY = (int)(PrintUnit.POINTS_PER_INCH * 0.3f);
        PdfContentByte cb = writer.getDirectContent();

        Collections.sort(componentToPrint);

    	while (componentToPrint.size() > 0) {
    		int pageY = marginY;
    		Boolean anyAddedToRow;

            Graphics2D g2 = cb.createGraphics(pageSize.width, pageSize.height);

    		do {
	    		// Fill the row
	    		int rowX = marginX;
	    		int rowY = pageY;
	        	ListIterator<PrintableComponent> entry = componentToPrint.listIterator();
	        	anyAddedToRow = false;

	        	while (entry.hasNext()) {
		        	PrintableComponent component = entry.next();
		        	java.awt.Dimension dim = component.getSize();
		        	if ((rowX + dim.width + marginX < wPage) && (rowY + dim.height + marginY < hPage)) {
		        		component.setPrintOffset(rowX, rowY);
		        		rowX += dim.width + marginX;
		        		if (rowY + dim.height + marginY > pageY) {
		        			pageY = rowY + dim.height + marginY;
                        }
		        		entry.remove();
		        		component.print(g2);
		        		anyAddedToRow = true;
		        	}
		        }
                pageY += marginY;
    		} while (anyAddedToRow);

        	g2.dispose();
        	document.newPage();
    	}
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

    /**
     * Convenience class to model a dimension.
     */
    class Dimension {
        /** Width, in points. */
        public float width;
        /** Height, in points. */
        public float height;

        /**
         * Constructor.
         * @param w width
         * @param h height
         */
        public Dimension (float w, float h) {
            width = w;
            height = h;
        }

        /**
         * Get the width.
         *
         * @return  the width
         */
        public float getWidth () {
            return width;
        }

        /**
         * Get the height.
         *
         * @return the height
         */
        public float getHeight () {
            return height;
        }
    }
}
