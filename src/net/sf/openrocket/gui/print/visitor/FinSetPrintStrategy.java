/*
 * FinSetPrintStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintableFinSet;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

/**
 * A strategy for drawing fin templates.
 */
public class FinSetPrintStrategy {

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
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStages        The stages to be printed by this strategy
     */
    public FinSetPrintStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStages) {
        document = doc;
        writer = theWriter;
        stages = theStages;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param root the root component; all children will be printed recursively
     */
    public void writeToDocument (final RocketComponent root) {
        List<RocketComponent> rc = root.getChildren();
        goDeep(rc);
    }


    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be printed recursively
     */
    protected void goDeep (final List<RocketComponent> theRc) {
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof FinSet) {
                printFinSet((FinSet) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                goDeep(rocketComponent.getChildren());
            }
        }
    }

    /**
     * The core behavior of this strategy.
     *
     * @param finSet the object to extract info about; a graphical image of the fin shape is drawn to the document
     */
    private void printFinSet(final FinSet finSet) {
        if (shouldPrintStage(finSet.getStageNumber())) {
            try {
                PrintableFinSet pfs = new PrintableFinSet(finSet);

                java.awt.Dimension finSize = pfs.getSize();
                final Dimension pageSize = getPageSize();
                if (fitsOnOnePage(pageSize, finSize.getWidth(), finSize.getHeight())) {
                    printOnOnePage(pfs);
                }
                else {
                    BufferedImage image = (BufferedImage) pfs.createImage();
                    ITextHelper.renderImageAcrossPages(new Rectangle(pageSize.getWidth(), pageSize.getHeight()),
                            document, writer, image);
                }
                document.newPage();
            }
            catch (DocumentException e) {
                log.error("Could not render fin.", e);
            }
        }
    }

    /**
     * Determine if the strategy's set of stage numbers (to print) contains the specified stage.
     *
     * @param stageNumber a stage number
     *
     * @return true if the strategy contains the stage number provided
     */
    public boolean shouldPrintStage(int stageNumber) {
        if (stages == null || stages.isEmpty()) {
            return false;
        }

        for (final Integer stage : stages) {
            if (stage == stageNumber) {
                return true;
            }
        }

        return false;
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
    private boolean fitsOnOnePage (Dimension pageSize, double wImage, double hImage) {
        double wPage = pageSize.getWidth();
        double hPage = pageSize.getHeight();

        int wRatio = (int) Math.ceil(wImage / wPage);
        int hRatio = (int) Math.ceil(hImage / hPage);

        return wRatio <= 1.0d && hRatio <= 1.0d;
    }

    /**
     * Print the fin set.
     *
     * @param thePfs the printable fin set
     */
    private void printOnOnePage (final PrintableFinSet thePfs) {
        Dimension d = getPageSize();
        PdfContentByte cb = writer.getDirectContent();
        Graphics2D g2 = cb.createGraphics(d.width, d.height);
        thePfs.print(g2);
        g2.dispose();
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
