package info.openrocket.swing.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import info.openrocket.swing.gui.print.AbstractPrintable;
import info.openrocket.swing.gui.print.PrintableNoseCone;
import info.openrocket.swing.gui.print.PrintableTransition;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;

import java.util.List;
import java.util.Set;

/**
 * A strategy for drawing transition/shroud/nose cone templates.
 */
public class TransitionStrategy extends AbstractPrintStrategy <Boolean> {

    /**
     * Print nose cones?
     */
    private boolean printNoseCones = false;

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     * @param pageFit          the page fit strategy
     * @param noseCones        nose cones are a special form of a transition; if true, then print nose cones
     */
    public TransitionStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit, PageFitPrintStrategy pageFit, boolean noseCones) {
        super(doc, pageFit, theWriter, theStagesToVisit);
        document = doc;
        writer = theWriter;
        stages = theStagesToVisit;
        pageFitPrint = pageFit;
        printNoseCones = noseCones;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param theRc     an array of rocket components; all children will be visited recursively
     *
     * @return true if a transition/nosecone was rendered
     */
    protected Boolean goDeep(final List<RocketComponent> theRc) {
        boolean result = false;
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof NoseCone) {
                if (printNoseCones) {
                    result |= render((Transition) rocketComponent);
                }
            }
            else if (rocketComponent instanceof Transition && !printNoseCones) {
                result |= render((Transition) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                result |= goDeep(rocketComponent.getChildren());
            }
        }
        return result;
    }

    /**
     * The core behavior of this strategy.
     *
     * @param component the object to extract info about; a graphical image of the transition shape is drawn to the document
     *
     * @return true, always
     */
    private boolean render(final Transition component) {
        try {
            AbstractPrintable pfs;
            if (component instanceof NoseCone) {
                pfs = new PrintableNoseCone((NoseCone) component);
            }
            else {
                pfs = new PrintableTransition(component);
            }

            render(pfs);
        }
        catch (DocumentException e) {
            log.error("Could not render the transition.", e);
        }
        return true;
    }

}
