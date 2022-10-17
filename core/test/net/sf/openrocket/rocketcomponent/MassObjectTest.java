package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.MathUtil;
import org.junit.Test;
import org.junit.Assert;

public class MassObjectTest extends BaseTestCase {
    @Test
    public void testAutoRadius() {
        MassObject sc = new ShockCord();
        MassObject mc = new MassComponent();
        MassObject pc = new Parachute();
        MassObject st = new Streamer();
        MassObject[] massObjects = {sc, mc, pc, st};

        for (MassObject mo : massObjects) {
            // Test no auto
            mo.setRadiusAutomatic(false);
            mo.setRadius(0.1);
            mo.setLength(0.1);
            Assert.assertEquals(String.format(" No auto %s incorrect radius", mo.getClass().getName()),
                    0.1, mo.getRadius(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" No auto %s incorrect no auto radius", mo.getClass().getName()),
                    0.1, mo.getRadiusNoAuto(),MathUtil.EPSILON);
            Assert.assertEquals(String.format(" No auto %s incorrect length", mo.getClass().getName()),
                    0.1, mo.getLength(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" No auto %s incorrect no auto length", mo.getClass().getName()),
                    0.1, mo.getLengthNoAuto(), MathUtil.EPSILON);

            mo.setLengthNoAuto(0.1);
            Assert.assertEquals(String.format(" No auto 2 %s incorrect length", mo.getClass().getName()),
                    0.1, mo.getLength(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" No auto 2 %s incorrect no auto length", mo.getClass().getName()),
                    0.1, mo.getLengthNoAuto(), MathUtil.EPSILON);

            // Test auto
            BodyTube parent = new BodyTube();
            parent.setOuterRadius(0.05);
            parent.setInnerRadius(0.05);
            parent.addChild(mo);
            mo.setRadiusAutomatic(true);
            Assert.assertEquals(String.format(" Auto 1 %s incorrect radius", mo.getClass().getName()),
                    0.05, mo.getRadius(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 1 %s incorrect no auto radius", mo.getClass().getName()),
                    0.1, mo.getRadiusNoAuto(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 1 %s incorrect length", mo.getClass().getName()),
                    0.4, mo.getLength(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 1 %s incorrect no auto length", mo.getClass().getName()),
                    0.1, mo.getLengthNoAuto(), MathUtil.EPSILON);

            parent.setOuterRadius(0.1);
            parent.setInnerRadius(0.1);
            Assert.assertEquals(String.format(" Auto 2 %s incorrect radius", mo.getClass().getName()),
                    0.1, mo.getRadius(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 2 %s incorrect no auto radius", mo.getClass().getName()),
                    0.1, mo.getRadiusNoAuto(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 2 %s incorrect length", mo.getClass().getName()),
                    0.1, mo.getLength(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 2 %s incorrect no auto length", mo.getClass().getName()),
                    0.1, mo.getLengthNoAuto(), MathUtil.EPSILON);

            parent.setOuterRadius(0.075);
            parent.setInnerRadius(0.075);
            mo.setLength(0.075);
            Assert.assertEquals(String.format(" Auto 3 %s incorrect radius", mo.getClass().getName()),
                    0.075, mo.getRadius(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 3 %s incorrect no auto radius", mo.getClass().getName()),
                    0.1, mo.getRadiusNoAuto(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 3 %s incorrect length", mo.getClass().getName()),
                    0.075, mo.getLength(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 3 %s incorrect no auto length", mo.getClass().getName()),
                    0.0422, mo.getLengthNoAuto(), 0.001);

            mo.setLengthNoAuto(0.05);
            Assert.assertEquals(String.format(" Auto 4 %s incorrect radius", mo.getClass().getName()),
                    0.075, mo.getRadius(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 4 %s incorrect no auto radius", mo.getClass().getName()),
                    0.1, mo.getRadiusNoAuto(), MathUtil.EPSILON);
            Assert.assertEquals(String.format(" Auto 4 %s incorrect length", mo.getClass().getName()),
                    0.0889, mo.getLength(), 0.001);
            Assert.assertEquals(String.format(" Auto 4 %s incorrect no auto length", mo.getClass().getName()),
                    0.05, mo.getLengthNoAuto(), MathUtil.EPSILON);
        }
    }
}
