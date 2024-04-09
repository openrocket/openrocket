/*
 * FinSetPrintStrategy.java
 */
package info.openrocket.swing.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import info.openrocket.swing.gui.print.AbstractPrintable;
import info.openrocket.swing.gui.print.PrintableFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;

import java.util.List;
import java.util.Set;

/**
 * A strategy for drawing fin templates.
 */
public class FinSetPrintStrategy extends AbstractPrintStrategy<Void> {

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStages        The stages to be printed by this strategy
     */
    public FinSetPrintStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStages, PageFitPrintStrategy pageFit) {
        super(doc, pageFit, theWriter, theStages);
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be printed recursively
     */
    @Override
    protected Void goDeep (final List<RocketComponent> theRc) {
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof FinSet) {
                render((FinSet) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                goDeep(rocketComponent.getChildren());
            }
        }
        return null;
    }

    /**
     * The core behavior of this strategy.
     *
     * @param finSet the object to extract info about; a graphical image of the fin shape is drawn to the document
     */
    private void render(final FinSet finSet) {
        if (shouldPrintStage(finSet.getStageNumber())) {
            try {
                AbstractPrintable<FinSet> pfs = new PrintableFinSet(finSet);
                render(pfs);
            }
            catch (DocumentException e) {
                log.error("Could not render fin.", e);
            }
        }
    }

}