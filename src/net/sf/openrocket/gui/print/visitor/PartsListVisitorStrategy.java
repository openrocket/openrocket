/*
 * PartsListVisitorStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Coaxial;
import net.sf.openrocket.rocketcomponent.ComponentVisitor;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A visitor strategy for creating documentation about a parts list.
 */
public class PartsListVisitorStrategy extends BaseVisitorStrategy {

    /**
     * Accumulator for parts data.
     */
    private Map<PartsAccumulator, PartsAccumulator> crap = new HashMap<PartsAccumulator, PartsAccumulator>();
    
    /**
     * Construct a strategy for visiting a parts hierarchy for the purposes of collecting details on those parts.
     *
     * @param doc              The iText document
     * @param theWriter        The direct iText writer
     * @param theStagesToVisit The stages to be visited by this strategy
     */    
    public PartsListVisitorStrategy (Document doc, PdfWriter theWriter, Set<Integer> theStagesToVisit) {
        super(doc, theWriter, theStagesToVisit);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final RingComponent visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final InnerTube visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final LaunchLug visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final Transition visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final RadiusRingComponent visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final NoseCone visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final BodyTube visitable) {
        final PartsAccumulator key = new PartsAccumulator(visitable);
            PartsAccumulator pa = crap.get(key);
        if (pa == null) {
            pa = key;
            crap.put(pa, pa);
        }
        pa.increment();
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final TrapezoidFinSet visitable) {
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final EllipticalFinSet visitable) {
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void visit (final FreeformFinSet visitable) {
    }

    /**
     * {@inheritDoc}
     */
    @Override 
    public void setParent (final ComponentVisitor theParent) {
        parent = theParent;
    }

    /**
     * {@inheritDoc}
     */
    @Override 
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

    void increment() {
        quantity++;
    }
    
    int quantity() {
        return quantity;
    }
    
    public boolean equals (final Object o1) {
        if (this == o1) {
            return true;
        }
        
        RocketComponent that;
        if (o1 instanceof net.sf.openrocket.gui.print.visitor.PartsAccumulator) {
            that = ((net.sf.openrocket.gui.print.visitor.PartsAccumulator)o1).component;
        }
        else if (o1 instanceof RocketComponent) {
            that = (RocketComponent)o1;
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
                Coaxial cThis = (Coaxial)this.component;
                Coaxial cThat = (Coaxial)that;
                if (cThis.getInnerRadius() == cThat.getInnerRadius() &&
                        cThis.getOuterRadius() == cThat.getOuterRadius()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    
    public int hashCode() {
        return component.getComponentName().hashCode();
    }
}