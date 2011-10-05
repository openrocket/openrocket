package net.sf.openrocket.gui.configdialog;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FinSetConfigTest {

    static Method method;

    @BeforeClass
    public static void classSetup() throws Exception {
        method = FinSetConfig.class.getDeclaredMethod("computeFinTabLength", List.class, Double.class, Double.class, DoubleModel.class);
        Assert.assertNotNull(method);
        method.setAccessible(true);
    }

    /**
     * Test no centering rings.
     *
     * @throws Exception
     */
    @Test
    public void testComputeFinTabLength() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();
        
        Double result = (Double)method.invoke(null, rings, 10d, 11d, dm);
        Assert.assertEquals(0.0001, 11d, result.doubleValue());
        result = (Double)method.invoke(null, null, 10d, 11d, dm);
        Assert.assertEquals(11d, result.doubleValue(), 0.0001);
    }

    /**
     * Test 2 rings both ahead of the fin.
     */
    @Test
    public void testCompute2LeadingRings() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.45);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.01, result.doubleValue(), 0.0001);
        
    }

    /**
     * Test one ring, ahead of the fin.
     */
    @Test
    public void testCompute1Ring() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.43);
        rings.add(ring1);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.01, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring ahead of the fin, one ring within the root chord.
     */
    @Test
    public void testComputeOneLeadingOneRingWithinRoot() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.45);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.45d, 0.01, dm);
        Assert.assertEquals(0.01 - 0.004, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring ahead of the fin, one ring beyond the root chord.
     */
    @Test
    public void testComputeOneLeadingOneTrailingRing() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.48);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.01, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring within the root chord, another ring beyond the root chord.
     */
    @Test
    public void testComputeOneWithinRootOneTrailingRing() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.4701);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.48);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.0059, result.doubleValue(), 0.0001);
    }
    
    /**
     * Test both rings within the root chord.
     */
    @Test
    public void testBothRingsWithinRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.4701);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.4750);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.0009, result.doubleValue(), 0.0001);
    }


    /**
     * Test both rings beyond the root chord.
     */
    @Test
    public void testBothRingsBeyondRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.48);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.49);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.006, result.doubleValue(), 0.0001);
    }

    /**
     * Test both rings within the root chord - the top ring has an adjacent ring (so 3 rings total).
     */
    @Test
    public void test3RingsWithinRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setPositionValue(0.47);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setPositionValue(0.4702);
        CenteringRing ring3 = new CenteringRing();
        ring3.setLength(0.004);
        ring3.setPositionValue(0.4770);
        rings.add(ring1);
        rings.add(ring2);
        rings.add(ring3);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm);
        Assert.assertEquals(0.0028, result.doubleValue(), 0.0001);
    }

}
