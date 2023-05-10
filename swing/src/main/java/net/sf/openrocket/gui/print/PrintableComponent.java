/*
 * PrintableComponent.java
 */
package net.sf.openrocket.gui.print;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * Common interface for components we want to print. Used by PageFitPrintStrategy
 *
 * @author Jason Blood <dyster2000@gmail.com>
 */
public class PrintableComponent extends JPanel implements Printable, Comparable<PrintableComponent> {

    /**
     * The printing offsets.
     */
    private int offsetX = 0;
    private int offsetY = 0;

    /**
     * Constructor.
     */
    public PrintableComponent() {
    	super(false);
    }

    /**
     * From the java.awt.print.Printable interface.
     * <p/>
     * Prints the page at the specified index into the specified {@link java.awt.Graphics} context in the specified
     * format. A <code>PrinterJob</code> calls the <code>Printable</code> interface to request that a page be rendered
     * into the context specified by <code>graphics</code>.  The format of the page to be drawn is specified by
     * <code>pageFormat</code>.  The zero based index of the requested page is specified by <code>pageIndex</code>. If
     * the requested page does not exist then this method returns NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned. The
     * <code>Graphics</code> class or subclass implements the {@link java.awt.print.PrinterGraphics} interface to
     * provide additional information.  If the <code>Printable</code> object aborts the print job then it throws a
     * {@link java.awt.print.PrinterException}.
     * <p/>
     * Note: This is not currently used in OpenRocket.  It's only here for reference.
     *
     * @param graphics   the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex  the zero based index of the page to be drawn
     *
     * @return PAGE_EXISTS if the page is rendered successfully or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
     *         non-existent page.
     *
     * @throws java.awt.print.PrinterException
     *          thrown when the print job is terminated.
     */
    @Override
    public int print (final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
            throws PrinterException {

        Graphics2D g2d = (Graphics2D) graphics;
        PrintUtilities.translateToJavaOrigin(g2d, pageFormat);
        PrintUtilities.disableDoubleBuffering(this);
        paint(g2d);
        PrintUtilities.enableDoubleBuffering(this);
        return Printable.PAGE_EXISTS;
    }

	/**
	 * Set the offset this component will be printed to the page.
	 * @param x	X offset to print at.
	 * @param y	Y offset to print at.
	 */
	public void setPrintOffset(int x, int y) {
        offsetX = x;
        offsetY = y;
	}

	/**
	 * Get the X offset this component will be printed to the page.
	 * @return X offset to print at.
	 */
	public int getOffsetX() {
		return offsetX;
	}

	/**
	 * Get the Y offset this component will be printed to the page.
	 * @return Y offset to print at.
	 */
	public int getOffsetY() {
		return offsetY;
	}

    /**
     * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * Bin packing theory says that trying to fit the biggest items first may have a better outcome. So this is sorted
     * in size descending order, with width taking precedence over height.
     *
     * @param other the object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it from being compared to this object.
     */
    @Override
    public int compareTo(final PrintableComponent other) {
        int widthDiff = other.getWidth() - getWidth();
        if (widthDiff > 0) {
            return 1;
        }
        else if (widthDiff < 0) {
            return -1;
        }
        return other.getHeight() - getHeight();
    }
}
