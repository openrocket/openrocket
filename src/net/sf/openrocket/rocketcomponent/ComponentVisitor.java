/*
 * ComponentVisitor.java
 */
package net.sf.openrocket.rocketcomponent;

/**
 * This class implements a Visitor pattern to visit any/all components of a Rocket.
 */
public class ComponentVisitor implements Visitor<ComponentVisitor, RocketComponent> {

    /**
     * The delegate.
     */
    private ComponentVisitorStrategy strategy;

    /**
     * Constructor.
     *
     * @param aStrategy the object to delegate the visiting to
     */
    public ComponentVisitor (ComponentVisitorStrategy aStrategy) {
        strategy = aStrategy;
        strategy.setParent(this);
    }

    /**
     * Visit a Rocket object.
     *
     * @param visitable the Rocket to visit
     */
    public void visit (final Rocket visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a RocketComponent object.  This is used to catch any RocketComponent subclass not explicity defined by a
     * visit method in this strategy.
     *
     * @param visitable the RocketComponent to visit
     */
    public void visit (final RocketComponent visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a Stage object.
     *
     * @param visitable the Stage to visit
     */
    public void visit (final Stage visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit an ExternalComponent object.
     *
     * @param visitable the ExternalComponent to visit
     */
    public void visit (final ExternalComponent visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a BodyComponent object.
     *
     * @param visitable the BodyComponent to visit
     */
    public void visit (final BodyComponent visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a RingComponent object.
     *
     * @param visitable the RingComponent to visit
     */
    public void visit (final RingComponent visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit an InnerTube object.
     *
     * @param visitable the InnerTube to visit
     */
    public void visit (final InnerTube visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a LaunchLug object.
     *
     * @param visitable the LaunchLug to visit
     */
    public void visit (final LaunchLug visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a Transition object.
     *
     * @param visitable the Transition to visit
     */
    public void visit (final Transition visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a RadiusRingComponent object.
     *
     * @param visitable the RadiusRingComponent to visit
     */
    public void visit (final RadiusRingComponent visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a MassObject object.
     *
     * @param visitable the MassObject to visit
     */
    public void visit (final MassObject visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a NoseCone object.
     *
     * @param visitable the NoseCone to visit
     */
    public void visit (final NoseCone visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a BodyTube object.
     *
     * @param visitable the BodyTube to visit
     */
    public void visit (final BodyTube visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a Rocket object.
     *
     * @param visitable the Rocket to visit
     */
    public void visit (final TrapezoidFinSet visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a Rocket object.
     *
     * @param visitable the Rocket to visit
     */
    public void visit (final EllipticalFinSet visitable) {
        strategy.visit(visitable);
    }

    /**
     * Visit a FreeformFinSet object.
     *
     * @param visitable the FreeformFinSet to visit
     */
    public void visit (final FreeformFinSet visitable) {
        strategy.visit(visitable);
    }

    /**
     * Perform any cleanup or finishing operations.
     */
    public void close () {
        strategy.close();
    }

}
