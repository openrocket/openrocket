package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.FinMarkingGuide;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import java.awt.*;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A strategy for drawing a fin marking guide. As currently implemented, each body tube with a finset will have
 * a marking guide.  If a tube has multiple fin sets, they are combined onto one marking guide.  Launch lugs are supported
 * as well.
 */
public class FinMarkingGuideStrategy {

    /**
     * The logger.
     */
	private static final Logger log = LoggerFactory.getLogger(FinMarkingGuideStrategy.class);

    /**
     * The iText document.
     */
    protected Document document;

    /**
     * The direct iText writer.
     */
    protected PdfWriter writer;

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     */
    public FinMarkingGuideStrategy(Document doc, PdfWriter theWriter) {
        document = doc;
        writer = theWriter;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param root the root component; all children will be visited recursively
     */
    public void writeToDocument(final Rocket root) {
        render(root);
    }


    /**
     * The core behavior of this strategy.
     *
     * @param rocket the rocket to render all
     */
    private void render(final Rocket rocket) {
        try {
            FinMarkingGuide pfs = new FinMarkingGuide(rocket);

            java.awt.Dimension size = pfs.getSize();
            final Dimension pageSize = getPageSize();
            if (fitsOnOnePage(pageSize, size.getWidth(), size.getHeight())) {
                printOnOnePage(pfs);
            } else {
                BufferedImage image = (BufferedImage) pfs.createImage();
                ITextHelper.renderImageAcrossPages(new Rectangle(pageSize.getWidth(), pageSize.getHeight()),
                        document, writer, image);
            }
        } catch (DocumentException e) {
            log.error("Could not render the fin marking guide.", e);
        }
    }

    /**
     * Determine if the image will fit on the given page.
     *
     * @param pageSize the page size
     * @param wImage   the width of the thing to be printed
     * @param hImage   the height of the thing to be printed
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
     * Print the transition.
     *
     * @param theMarkingGuide the fin marking guide
     */
    private void printOnOnePage(final FinMarkingGuide theMarkingGuide) {
        Dimension d = getPageSize();
        PdfContentByte cb = writer.getDirectContent();
        Graphics2D g2 = cb.createGraphics(d.width, d.height);
        theMarkingGuide.print(g2);
        g2.dispose();
        document.newPage();
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
