/*
 * PartsListVisitorStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.rocketcomponent.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A visitor strategy for creating documentation about a parts list.
 */
public class PartsListVisitorStrategy {

    /**
     * Accumulator for parts data.
     */
    private Map<PartsAccumulator, PartsAccumulator> crap = new HashMap<PartsAccumulator, PartsAccumulator>();

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
     * Construct a strategy for visiting a parts hierarchy for the purposes of collecting details on those parts.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     */
    public PartsListVisitorStrategy (Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit) {
        document = doc;
        writer = theWriter;
        stages = theStagesToVisit;
    }


    /**
     * Print the parts detail.
     *
     * @param root the root component
     */
    public void doVisit (final RocketComponent root) {
        goDeep(root.getChildren());
    }

    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be visited recursively
     */
    protected void goDeep (final List<RocketComponent> theRc) {
        for (RocketComponent rocketComponent : theRc) {
            doIt(rocketComponent);
        }
    }

    /**
     * {@inheritDoc}
     */
    private void doIt (final RocketComponent component) {
        if (component instanceof InnerTube) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof LaunchLug) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
        }

        else if (component instanceof NoseCone) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof Transition) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof RadiusRingComponent) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof RingComponent) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof BodyTube) {
            final PartsAccumulator key = new PartsAccumulator(component);
            PartsAccumulator pa = crap.get(key);
            if (pa == null) {
                pa = key;
                crap.put(pa, pa);
            }
            pa.increment();
            List<RocketComponent> rc = component.getChildren();
            goDeep(rc);
        }
        else if (component instanceof TrapezoidFinSet) {
        }
        else if (component instanceof EllipticalFinSet) {
        }
        else if (component instanceof FreeformFinSet) {
        }
    }


    /**
     * {@inheritDoc}
     */
    public void close () {
        for (PartsAccumulator partsAccumulator : crap.keySet()) {
            System.err.println(partsAccumulator.component.getComponentName() + " " + partsAccumulator.quantity);
        }
    }

}

class PartsAccumulator {

    int quantity = 0;

    RocketComponent component;

    PartsAccumulator (RocketComponent theComponent) {
        component = theComponent;
    }

    void increment () {
        quantity++;
    }

    int quantity () {
        return quantity;
    }

    @Override
    public boolean equals (final Object o1) {
        if (this == o1) {
            return true;
        }

        RocketComponent that;
        if (o1 instanceof net.sf.openrocket.gui.print.visitor.PartsAccumulator) {
            that = ((net.sf.openrocket.gui.print.visitor.PartsAccumulator) o1).component;
        }
        else if (o1 instanceof RocketComponent) {
            that = (RocketComponent) o1;
        }
        else {
            return false;
        }

        if (this.component.getClass().equals(that.getClass())) {
            //If
            if (that.getLength() == this.component.getLength()) {
                if (that.getMass() == this.component.getMass()) {
                    return true;
                }
            }
            if (this.component instanceof Coaxial &&
                that instanceof Coaxial) {
                Coaxial cThis = (Coaxial) this.component;
                Coaxial cThat = (Coaxial) that;
                if (cThis.getInnerRadius() == cThat.getInnerRadius() &&
                    cThis.getOuterRadius() == cThat.getOuterRadius()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode () {
        return component.getComponentName().hashCode();
    }
}