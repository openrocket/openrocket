package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class MassObjectTest extends BaseTestCase {
        @Test
        public void testAutoRadius() {
                MassObject sc = new ShockCord();
                MassObject mc = new MassComponent();
                MassObject pc = new Parachute();
                MassObject st = new Streamer();
                MassObject[] massObjects = { sc, mc, pc, st };

                for (MassObject mo : massObjects) {
                        // Test no auto
                        mo.setRadiusAutomatic(false);
                        mo.setRadius(0.1);
                        mo.setLength(0.1);
                        Assertions.assertEquals(0.1, mo.getRadius(), MathUtil.EPSILON,
                                String.format(" No auto %s incorrect radius", mo.getClass().getName()));
                        Assertions.assertEquals(0.1, mo.getLength(), MathUtil.EPSILON,
                                String.format(" No auto %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.05, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" No auto %s incorrect CG", mo.getClass().getName()));

                        mo.setLength(0.1);
                        Assertions.assertEquals(0.1, mo.getLength(), MathUtil.EPSILON,
                                String.format(" No auto 2 %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.05, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" No auto %s incorrect CG", mo.getClass().getName()));

                        // Test auto
                        BodyTube parent = new BodyTube();
                        parent.setOuterRadius(0.05);
                        parent.setInnerRadius(0.05);
                        parent.addChild(mo);
                        mo.setRadiusAutomatic(true);
                        Assertions.assertEquals(0.05, mo.getRadius(), MathUtil.EPSILON,
                                String.format(" Auto 1 %s incorrect radius", mo.getClass().getName()));
                        Assertions.assertEquals(0.4, mo.getLength(), MathUtil.EPSILON,
                                String.format(" Auto 1 %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.2, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" Auto 1 %s incorrect CG", mo.getClass().getName()));

                        parent.setOuterRadius(0.1);
                        parent.setInnerRadius(0.1);
                        Assertions.assertEquals(0.1, mo.getRadius(), MathUtil.EPSILON,
                                String.format(" Auto 2 %s incorrect radius", mo.getClass().getName()));
                        Assertions.assertEquals(0.1, mo.getLength(), MathUtil.EPSILON,
                                String.format(" Auto 2 %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.05, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" Auto 2 %s incorrect CG", mo.getClass().getName()));

                        parent.setOuterRadius(0.075);
                        parent.setInnerRadius(0.075);
                        mo.setLength(0.075);
                        Assertions.assertEquals(0.075, mo.getRadius(), MathUtil.EPSILON,
                                String.format(" Auto 3 %s incorrect radius", mo.getClass().getName()));
                        Assertions.assertEquals(0.075, mo.getLength(), MathUtil.EPSILON,
                                String.format(" Auto 3 %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.0375, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" Auto 3 %s incorrect CG", mo.getClass().getName()));

                        mo.setLength(0.05);
                        Assertions.assertEquals(0.075, mo.getRadius(), MathUtil.EPSILON,
                                String.format(" Auto 4 %s incorrect radius", mo.getClass().getName()));
                        Assertions.assertEquals(0.05, mo.getLength(), 0.001,
                                String.format(" Auto 4 %s incorrect length", mo.getClass().getName()));
                        Assertions.assertEquals(0.025, mo.getComponentCG().x, MathUtil.EPSILON,
                                String.format(" Auto 4 %s incorrect CG", mo.getClass().getName()));
                }
        }
}
