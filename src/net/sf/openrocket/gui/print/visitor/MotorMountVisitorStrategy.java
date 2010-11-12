/*
 * MotorMountVisitor.java
 */
package net.sf.openrocket.gui.print.visitor;

import com.itextpdf.text.Document;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor strategy for finding data about motor configurations.  This visitor accumulates information about the
 * motors currently 'installed' into each motor mount in the rocket.  When the visitor is complete, invoke {@link
 * #getMotors()} to obtain the list of Motor instances that correspond to the motor configuration.
 */
public class MotorMountVisitorStrategy extends BaseVisitorStrategy {

    /**
     * The motor configuration identifier.
     */
    private String mid;

    /** The accumulating list of motors. */
    private List<Motor> motors = new ArrayList<Motor>();

    /**
     * Constructor.
     *
     * @param doc           the iText document
     * @param motorConfigID the motor configuration ID
     */
    public MotorMountVisitorStrategy (Document doc,
                                      String motorConfigID) {
        super(doc);
        mid = motorConfigID;
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
    public void visit (final BodyTube visitable) {
        if (visitable.isMotorMount()) {
            doVisit(visitable);
        }
        else {
            goDeep(visitable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit (final InnerTube visitable) {
        if (visitable.isMotorMount()) {
            doVisit(visitable);
        }
    }

    /**
     * The core behavior of this visitor.
     *
     * @param visitable the object to extract info about; a graphical image of the fin shape is drawn to the document
     */
    private void doVisit (final MotorMount visitable) {
        final Motor motor = visitable.getMotor(mid);
        if (motor != null) {
            motors.add(motor);
        }
    }

    /**
     * Answer with the list of motors that have been accumulated from visiting all of the motor mount components in the
     * rocket component hierarchy.
     *
     * @return a list of motors
     */
    public List<Motor> getMotors () {
        return motors;
    }

}


