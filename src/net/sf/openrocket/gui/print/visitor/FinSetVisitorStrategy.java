/*
 * FinSetVisitorStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.ITextHelper;
import net.sf.openrocket.gui.print.PrintableFinSet;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Set;

/**
 * A visitor strategy for drawing fin templates.
 */
public class FinSetVisitorStrategy extends BaseVisitorStrategy {

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     */
    public FinSetVisitorStrategy (Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit) {
        super(doc, theWriter, theStagesToVisit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final TrapezoidFinSet visitable) {
        doVisit(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final EllipticalFinSet visitable) {
        doVisit(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final FreeformFinSet visitable) {
        doVisit(visitable);
    }

    /**
     * The core behavior of this visitor.
     *
     * @param visitable the object to extract info about; a graphical image of the fin shape is drawn to the document
     */
    private void doVisit (final FinSet visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            try {
                PrintableFinSet pfs = new PrintableFinSet(visitable);

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
            }
            catch (DocumentException e) {
                e.printStackTrace();
            }
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
        document.newPage();
    }
}

