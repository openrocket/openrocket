package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.AbstractPrintable;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableCenteringRing;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

/**
 * A strategy for printing a centering ring to iText.
 */
public class CenteringRingStrategy {

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

    /**
     * Strategy for fitting multiple components onto a page.
     */
    protected PageFitPrintStrategy pageFitPrint;

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     */
    public CenteringRingStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit, PageFitPrintStrategy pageFit) {
        document = doc;
        writer = theWriter;
        stages = theStagesToVisit;
        pageFitPrint = pageFit;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param root the root component; all children will be visited recursively
     */
    public void writeToDocument(final RocketComponent root) {
        List<RocketComponent> rc = root.getChildren();
        goDeep(rc);
    }


    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be visited recursively
     */
    protected void goDeep(final List<RocketComponent> theRc) {
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof CenteringRing) {
                render((CenteringRing) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                goDeep(rocketComponent.getChildren());
            }
        }
    }

    /**
     * The core behavior of this visitor.
     *
     * @param component the object to extract info about; a graphical image of the centering ring shape is drawn to the
     *                  document
     */
    private void render(final CenteringRing component) {
        try {
            AbstractPrintable pfs;
            pfs = new PrintableCenteringRing(component);

            java.awt.Dimension size = pfs.getSize();
            final Dimension pageSize = getPageSize();
            if (fitsOnOnePage(pageSize, size.getWidth(), size.getHeight())) {
                pageFitPrint.addComponent(pfs);
            }
            else {
                int off = (int) (PrintUnit.POINTS_PER_INCH * 0.3f);
                pfs.setPrintOffset(off, off);
                BufferedImage image = (BufferedImage) pfs.createImage();
                ITextHelper.renderImageAcrossPages(new Rectangle(pageSize.getWidth(), pageSize.getHeight()),
                        document, writer, image);
                document.newPage();
            }
        }
        catch (DocumentException e) {
            log.error("Could not render the centering ring.", e);
        }
    }

    /**
     * Determine if the image will fit on the given page.
     *
     * @param pageSize the page size
     * @param wImage   the width of the thing to be printed
     * @param hImage   the height of the thing to be printed
     *
     * @return true if the thing to be printed will fit on a single page
     */
    private boolean fitsOnOnePage(Dimension pageSize, double wImage, double hImage) {
        double wPage = pageSize.getWidth();
        double hPage = pageSize.getHeight();

        int wRatio = (int) Math.ceil(wImage / wPage);
        int hRatio = (int) Math.ceil(hImage / hPage);

        return wRatio <= 1.0d && hRatio <= 1.0d;
    }

    /**
     * Get the dimensions of the paper page.
     *
     * @return an internal Dimension
     */
    protected Dimension getPageSize() {
        return new Dimension(document.getPageSize().getWidth(),
                document.getPageSize().getHeight());
    }

    /**
     * Convenience class to model a dimension.
     */
    class Dimension {
        /**
         * Width, in points.
         */
        public float width;
        /**
         * Height, in points.
         */
        public float height;

        /**
         * Constructor.
         *
         * @param w width
         * @param h height
         */
        public Dimension(float w, float h) {
            width = w;
            height = h;
        }

        /**
         * Get the width.
         *
         * @return the width
         */
        public float getWidth() {
            return width;
        }

        /**
         * Get the height.
         *
         * @return the height
         */
        public float getHeight() {
            return height;
        }
    }
}
