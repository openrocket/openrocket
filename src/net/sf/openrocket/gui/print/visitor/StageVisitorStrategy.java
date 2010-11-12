/*
 * StageVisitor.java
 */
package net.sf.openrocket.gui.print.visitor;

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

import java.util.ArrayList;
import java.util.List;

/**
 * This visitor strategy accumulates Stage references in a Rocket hierarchy.
 */
public class StageVisitorStrategy implements ComponentVisitorStrategy {

    /**
     * The collection of stages, accumulated during a visitation.
     */
    private List<Double> stageComponents = new ArrayList<Double>();

    private Double mass = 0d;

    /**
     * The owning visitor.
     */
    protected ComponentVisitor parent;

    /**
     * Constructor.
     */
    public StageVisitorStrategy () {
    }

    /**
     * Override the method that determines if the visiting should be going deep.
     *
     * @param stageNumber a stage number
     *
     * @return true, always
     */
    public boolean shouldVisitStage (int stageNumber) {
        return true;
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
    public void visit (final Rocket visitable) {
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final Stage visitable) {

        if (mass > 0d) {
            stageComponents.add(mass);
        }
        mass = 0d;
        RocketComponent[] rc = visitable.getChildren();
        goDeep(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RocketComponent visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
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
        for (RocketComponent rocketComponent : theRc) {
            rocketComponent.accept(parent);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final ExternalComponent visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final BodyComponent visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RingComponent visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final InnerTube visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final LaunchLug visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final Transition visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final RadiusRingComponent visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final MassObject visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final NoseCone visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final BodyTube visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final TrapezoidFinSet visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final EllipticalFinSet visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final FreeformFinSet visitable) {
        mass += visitable.getMass();
        goDeep(visitable);
    }

    /**
     * Get the list of stages, sort from Stage 1 .. Stage N.
     *
     * @return a sorted list of stages
     */
    public List<Double> getStages () {
        return stageComponents;
    }

    /**
     * Close by setting the last stage.
     */
    public void close () {
        if (mass > 0d) {
            stageComponents.add(mass);
        }
    }

}
