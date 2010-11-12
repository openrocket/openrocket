/*
 * ComponentVisitorStrategy.java
 */
package net.sf.openrocket.rocketcomponent;

/**
 * This interface defines the methods used in a Rocket component visitor.  By using a strategy, we can reuse one visitor
 * definition and just instrument it with different strategies.
 */
public interface ComponentVisitorStrategy {

    /**
     * Visit a Rocket object.
     *
     * @param visitable the Rocket to visit
     */
    void visit (final Rocket visitable);

    /**
     * Visit a RocketComponent object.  This is used to catch any RocketComponent subclass not explicity defined by a
     * visit method in this strategy.
     *
     * @param visitable the RocketComponent to visit
     */
    void visit (final RocketComponent visitable);

    /**
     * Visit a Stage object.
     *
     * @param visitable the Stage to visit
     */
    void visit (final Stage visitable);

    /**
     * Visit an ExternalComponent object.
     *
     * @param visitable the ExternalComponent to visit
     */
    void visit (final ExternalComponent visitable);

    /**
     * Visit a BodyComponent object.
     *
     * @param visitable the BodyComponent to visit
     */
    void visit (final BodyComponent visitable);

    /**
     * Visit a RingComponent object.
     *
     * @param visitable the RingComponent to visit
     */
    void visit (final RingComponent visitable);

    /**
     * Visit an InnerTube object.
     *
     * @param visitable the InnerTube to visit
     */
    void visit (final InnerTube visitable);

    /**
     * Visit a LaunchLug object.
     *
     * @param visitable the LaunchLug to visit
     */
    void visit (final LaunchLug visitable);

    /**
     * Visit a Transition object.
     *
     * @param visitable the Transition to visit
     */
    void visit (final Transition visitable);

    /**
     * Visit a RadiusRingComponent object.
     *
     * @param visitable the RadiusRingComponent to visit
     */
    void visit (final RadiusRingComponent visitable);

    /**
     * Visit a MassComponent object.
     *
     * @param visitable the MassComponent to visit
     */
    void visit (final MassObject visitable);

    /**
     * Visit a NoseCone object.
     *
     * @param visitable the NoseCone to visit
     */
    void visit (final NoseCone visitable);

    /**
     * Visit a BodyTube object.
     *
     * @param visitable the BodyTube to visit
     */
    void visit (final BodyTube visitable);

    /**
     * Visit a TrapezoidFinSet object.
     *
     * @param visitable the TrapezoidFinSet to visit
     */
    void visit (final TrapezoidFinSet visitable);

    /**
     * Visit an EllipticalFinSet object.
     *
     * @param visitable the EllipticalFinSet to visit
     */
    void visit (final EllipticalFinSet visitable);

    /**
     * Visit a FreeformFinSet object.
     *
     * @param visitable the FreeformFinSet to visit
     */
    void visit (final FreeformFinSet visitable);

    /**
     * Set the visitor that is using this strategy.
     *
     * @param parent the visitor
     */
    void setParent (ComponentVisitor parent);

    /**
     * Perform any cleanup or finishing operations.
     */
    void close ();
}
