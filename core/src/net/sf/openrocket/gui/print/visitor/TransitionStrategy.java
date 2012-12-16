package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.AbstractPrintable;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableNoseCone;
import net.sf.openrocket.gui.print.PrintableTransition;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

/**
 * A strategy for drawing transition/shroud/nose cone templates.
 */
public class TransitionStrategy {

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
    public TransitionStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit, PageFitPrintStrategy pageFit) {
        document = doc;
        writer = theWriter;
        stages = theStagesToVisit;
        pageFitPrint = pageFit;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param root      the root component; all children will be visited recursively
     * @param noseCones nose cones are a special form of a transition; if true, then print nose cones
     *
     * @return true if a transition/nosecone was rendered
     */
    public boolean writeToDocument(final RocketComponent root, boolean noseCones) {
        List<RocketComponent> rc = root.getChildren();
        return goDeep(rc, noseCones);
    }


    /**
     * Recurse through the given rocket component.
     *
     * @param theRc     an array of rocket components; all children will be visited recursively
     * @param noseCones nose cones are a special form of a transition; if true, then print nose cones
     *
     * @return true if a transition/nosecone was rendered
     */
    protected boolean goDeep(final List<RocketComponent> theRc, boolean noseCones) {
        boolean result = false;
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof NoseCone) {
                if (noseCones) {
                    result |= render((Transition) rocketComponent);
                }
            }
            else if (rocketComponent instanceof Transition && !noseCones) {
                result |= render((Transition) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                result |= goDeep(rocketComponent.getChildren(), noseCones);
            }
        }
        return result;
    }

    /**
     * The core behavior of this visitor.
     *
     * @param component the object to extract info about; a graphical image of the transition shape is drawn to the
     *                  document
     *
     * @return true, always
     */
    private boolean render(final Transition component) {
        try {
            AbstractPrintable<?> pfs;
            if (component instanceof NoseCone) {
                pfs = new PrintableNoseCone((NoseCone) component);
            }
            else {
                pfs = new PrintableTransition(component);
            }

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
            log.error("Could not render the transition.", e);
        }
        return true;
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
