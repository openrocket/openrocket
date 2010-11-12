/*
 * BaseVisitorStrategy.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ComponentVisitor;
import net.sf.openrocket.rocketcomponent.ComponentVisitorStrategy;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;

import java.util.HashSet;
import java.util.Set;

/**
 * This abstract class contains boilerplate functionality to support visiting the components of a rocket. It is a
 * visitor strategy, not a visitor per se.
 */
public abstract class BaseVisitorStrategy implements ComponentVisitorStrategy {

    /**
     * The owning visitor.
     */
    protected ComponentVisitor parent;

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
     * State variable to track the level of hierarchy.
     */
    protected int level = 0;

    /**
     * Default no-arg constructor.
     */
    public BaseVisitorStrategy () {
    }

    /**
     * Constructor.
     *
     * @param doc the iText document
     */
    public BaseVisitorStrategy (Document doc) {
        this(doc, null);
    }

    /**
     * Constructor.
     *
     * @param doc       the iText document
     * @param theWriter an iText byte writer
     */
    public BaseVisitorStrategy (Document doc, PdfWriter theWriter) {
        this(doc, theWriter, new HashSet<Integer>());
    }

    /**
     * Constructor.
     *
     * @param doc       the iText document
     * @param theWriter an iText byte writer
     * @param theStages a set of stage numbers
     */
    public BaseVisitorStrategy (Document doc, PdfWriter theWriter, Set<Integer> theStages) {
        document = doc;
        writer = theWriter;
        stages = theStages;
    }

    /**
     * Determine if the visitor strategy's set of stage numbers (to print) contains the specified stage.
     *
     * @param stageNumber a stage number
     *
     * @return true if the visitor strategy contains the stage number provided
     */
    public boolean shouldVisitStage (int stageNumber) {
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
     * Recurse through the given rocket component.
     *
     * @param root the root component; all children will be visited recursively
     */
    protected void goDeep (final RocketComponent root) {
        RocketComponent[] rc = root.getChildren();
        goDeep(rc);
    }


    /**
     * Recurse through the given rocket component.
     *
     * @param theRc an array of rocket components; all children will be visited recursively
     */
    protected void goDeep (final RocketComponent[] theRc) {
        level++;
        for (RocketComponent rocketComponent : theRc) {
            rocketComponent.accept(parent);
        }
        level--;
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
     * {@inheritDoc}
     */
    @Override
    public void visit (final Rocket visitable) {
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RocketComponent visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final Stage visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final ExternalComponent visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final BodyComponent visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RingComponent visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final InnerTube visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final LaunchLug visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final Transition visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RadiusRingComponent visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final MassObject visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final NoseCone visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final BodyTube visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final TrapezoidFinSet visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final EllipticalFinSet visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final FreeformFinSet visitable) {
        if (shouldVisitStage(visitable.getStageNumber())) {
            goDeep(visitable);
        }
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
    }
}

class Dimension {
    public float width;
    public float height;

    public Dimension (float w, float h) {
        width = w;
        height = h;
    }

    public float getWidth () {
        return width;
    }

    public float getHeight () {
        return height;
    }
}