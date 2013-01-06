package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.gui.print.AbstractPrintable;
import net.sf.openrocket.gui.print.PrintableCenteringRing;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.ArrayList;

import java.util.List;
import java.util.Set;

/**
 * A strategy for printing a centering ring to iText.
 */
public class CenteringRingStrategy extends AbstractPrintStrategy<Void> {

    /**
     * Constructor.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     */
    public CenteringRingStrategy(Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit, PageFitPrintStrategy pageFit) {
        super(doc, pageFit, theWriter, theStagesToVisit);
        document = doc;
        writer = theWriter;
        stages = theStagesToVisit;
        pageFitPrint = pageFit;
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be visited recursively
     */
    protected Void goDeep(final List<RocketComponent> theRc) {
        for (RocketComponent rocketComponent : theRc) {
            if (rocketComponent instanceof CenteringRing) {
                render((CenteringRing) rocketComponent);
            }
            else if (rocketComponent.getChildCount() > 0) {
                goDeep(rocketComponent.getChildren());
            }
        }
        return null;
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
     * The core behavior of this strategy.
     *
     * @param component the object to extract info about; a graphical image of the centering ring shape is drawn to the
     *                  document
     */
    private void render(final CenteringRing component) {
        try {
            AbstractPrintable pfs;
            pfs = PrintableCenteringRing.create(component, findMotorMount(component));

            render(pfs);
        }
        catch (DocumentException e) {
            log.error("Could not render the centering ring.", e);
        }
    }

}
