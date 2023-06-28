package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RailButtonTest extends BaseTestCase {
    protected final double EPSILON = MathUtil.EPSILON;

    @Test
    public void testCMSingleInstance() {
        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.025);
        RailButton button = new RailButton();
        button.setOuterDiameter(0.05);
        button.setTotalHeight(0.05);
        bodyTube.addChild(button);

        // Test normal CG
        Coordinate CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", -0.05, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.05, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.025, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.04330127, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);


        // Change dimensions
        button.setOuterDiameter(0.025);
        button.setTotalHeight(0.02);
        button.setAngleOffset(0);

        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.035, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.003930195, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.035, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.003930195, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.0175, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.03031089, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.003930195, CG.weight, EPSILON);
    }

    @Test
    public void testCMSingleInstanceOverride() {
        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.025);
        RailButton button = new RailButton();
        button.setOuterDiameter(0.05);
        button.setTotalHeight(0.05);
        button.setCGOverridden(true);
        button.setOverrideCGX(0.0123);
        bodyTube.addChild(button);

        // Test normal CG
        Coordinate CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", -0.05, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.05, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.025, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.04330127, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.014435995, CG.weight, EPSILON);


        // Change dimensions
        button.setOuterDiameter(0.025);
        button.setTotalHeight(0.02);
        button.setAngleOffset(0);
        button.setOverrideCGX(0.0321);
        button.setMassOverridden(true);
        button.setOverrideMass(0.1);

        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.035, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.1, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.035, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.1, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.0175, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.03031089, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.1, CG.weight, EPSILON);
    }

    @Test
    public void testCMMultipleInstances() {
        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.025);
        RailButton button = new RailButton();
        button.setOuterDiameter(0.05);
        button.setTotalHeight(0.05);
        button.setInstanceCount(3);
        button.setInstanceSeparation(0.2);
        bodyTube.addChild(button);

        // Test normal CG
        Coordinate CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.2, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", -0.05, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.2, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.05, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.2, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.025, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.04330127, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);


        // Change dimensions
        button.setOuterDiameter(0.025);
        button.setTotalHeight(0.02);
        button.setAngleOffset(0);
        button.setInstanceCount(2);
        button.setInstanceSeparation(0.15);

        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.075, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.035, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.00786039, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.075, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.035, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.00786039, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.075, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.0175, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.03031089, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.00786039, CG.weight, EPSILON);
    }

    @Test
    public void testCMMultipleInstancesOverride() {
        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.025);
        RailButton button = new RailButton();
        button.setOuterDiameter(0.05);
        button.setTotalHeight(0.05);
        button.setInstanceCount(3);
        button.setInstanceSeparation(0.2);
        button.setCGOverridden(true);
        button.setOverrideCGX(0.0123);
        bodyTube.addChild(button);

        // Test normal CG
        Coordinate CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", -0.05, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.05, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0123, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.025, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.04330127, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.043307985, CG.weight, EPSILON);


        // Change dimensions
        button.setOuterDiameter(0.025);
        button.setTotalHeight(0.02);
        button.setAngleOffset(0);
        button.setInstanceCount(2);
        button.setInstanceSeparation(0.15);
        button.setOverrideCGX(0.0321);
        button.setMassOverridden(true);
        button.setOverrideMass(0.2);

        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.035, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.2, CG.weight, EPSILON);

        // Test rotated CG
        button.setAngleOffset(Math.PI / 2);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", 0.035, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.2, CG.weight, EPSILON);

        button.setAngleOffset(-Math.PI / 3);
        CG = button.getCG();
        assertEquals(" RailButton CG has the wrong x value: ", 0.0321, CG.x, EPSILON);
        assertEquals(" RailButton CG has the wrong y value: ", 0.0175, CG.y, EPSILON);
        assertEquals(" RailButton CG has the wrong z value: ", -0.03031089, CG.z, EPSILON);
        assertEquals(" RailButton CM has the wrong value: ", 0.2, CG.weight, EPSILON);
    }
    
}
