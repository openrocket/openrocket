package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.AbstractPrintable;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableCenteringRing;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A strategy for printing a centering ring to iText.
 */
public class CenteringRingStrategy {

    /**
     * The logger.
     */
   private static final Logger log = LoggerFactory.getLogger(CenteringRingStrategy.class);

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
     * Find the inner tubes that are physically supported by the given centering ring.  Note that this only looks for
     * motor mount tubes that are siblings to the centering ring.
     *
     * @param rc the centering ring, for which all motor mount tubes that run through it are located.
     *
     * @return the list of tubes found
     */
    private List<InnerTube> findMotorMount(CenteringRing rc) {
        RocketComponent parent = rc.getParent();
        List<RocketComponent> siblings = parent.getChildren();

        List<InnerTube> mounts = new ArrayList<InnerTube>();
        for (RocketComponent rocketComponents : siblings) {
            if (rocketComponents != rc) {
                if (rocketComponents instanceof InnerTube) {
                    InnerTube it = (InnerTube) rocketComponents;
                    if (overlaps(rc, it)) {
                        mounts.add(it);
                    }
                }
            }
        }

        return mounts;
    }

    /**
     * Determine if the centering ring physically overlaps with the inner tube.
     *
     * @param one the centering ring
     * @param two the inner body tube
     *
     * @return true if the two physically intersect, from which we infer that the centering ring supports the tube
     */
    private boolean overlaps(CenteringRing one, InnerTube two) {
        final double crTopPosition = one.asPositionValue(RocketComponent.Position.ABSOLUTE, one.getParent());
        final double mmTopPosition = two.asPositionValue(RocketComponent.Position.ABSOLUTE, two.getParent());
        final double crBottomPosition = one.getLength() + crTopPosition;
        final double mmBottomPosition = two.getLength() + mmTopPosition;

        if (crTopPosition >= mmTopPosition && crTopPosition <= mmBottomPosition) {
            return true;
        }
        if (crBottomPosition >= mmTopPosition && crBottomPosition <= mmBottomPosition) {
            return true;
        }
        return false;
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
            pfs = PrintableCenteringRing.create(component, findMotorMount(component));

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
    public static class Dimension {
        /**
         * Width, in points.
         */
        public float width;
        /**
         * Height, in points.
         */
        public float height;
        /**
         * Breadth, in points.
         */
        public float breadth = 0f;

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
         * Constructor.
         *
         * @param w width
         * @param h height
         * @param b breadth; optionally used to represent radius
         */
        public Dimension(float w, float h, float b) {
            width = w;
            height = h;
            breadth = b;
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

        /**
         * Get the breadth.
         *
         * @return the breadth
         */
        public float getBreadth() {
            return breadth;
        }
    }
}
