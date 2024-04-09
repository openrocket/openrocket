package info.openrocket.swing.gui.configdialog;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.swing.gui.components.SVGOptionPanel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.swing.util.BaseTestCase;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class FinSetConfigTest extends BaseTestCase {

    static Method method;

    @BeforeAll
    public static void classSetup() throws Exception {
        method = FinSetConfig.class.getDeclaredMethod("computeFinTabLength", List.class, Double.class, Double.class, DoubleModel.class, RocketComponent.class);
        Assertions.assertNotNull(method);
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

        RocketComponent parent = new BodyTube();

        Double result = (Double)method.invoke(null, rings, 10d, 11d, dm, parent);
        Assertions.assertEquals(0.0001, 11d, result.doubleValue());
        result = (Double)method.invoke(null, null, 10d, 11d, dm, parent);
        Assertions.assertEquals(11d, result.doubleValue(), 0.0001);
    }

    /**
     * Test 2 rings both ahead of the fin.
     */
    @Test
    public void testCompute2LeadingRings() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        RocketComponent parent = new BodyTube();

        CenteringRing ring1 = new CenteringRing();
        ring1.setLength(0.004);
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setAxialOffset(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setAxialOffset(0.45);
        rings.add(ring1);
        rings.add(ring2);
        parent.addChild(ring1);
        parent.addChild(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.01, result.doubleValue(), 0.0001);
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
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setAxialOffset(0.43);
        rings.add(ring1);

        RocketComponent parent = new BodyTube();
        parent.addChild(ring1);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.01, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring ahead of the fin, one ring within the root chord.
     */
    @Test
    public void testComputeOneLeadingOneRingWithinRoot() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setLength(0.004);
        ring2.setAxialOffset(0.45);
        rings.add(ring1);
        rings.add(ring2);

        RocketComponent parent = new BodyTube(1d, 0.01);
        parent.addChild(ring1);
        parent.addChild(ring2);

        Double result = (Double)method.invoke(null, rings, 0.45d, 0.01, dm, parent);
        Assertions.assertEquals(0.01 - 0.004, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring ahead of the fin, one ring beyond the root chord.
     */
    @Test
    public void testComputeOneLeadingOneTrailingRing() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.43);
        CenteringRing ring2 = new CenteringRing();
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setLength(0.004);
        ring2.setAxialOffset(0.48);
        rings.add(ring1);
        rings.add(ring2);

        @SuppressWarnings("unused")
		RocketComponent parent = new BodyTube();
        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, ring1);
        Assertions.assertEquals(0.01, result.doubleValue(), 0.0001);
    }

    /**
     * Test one ring within the root chord, another ring beyond the root chord.
     */
    @Test
    public void testComputeOneWithinRootOneTrailingRing() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.4701);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setAxialOffset(0.48);
        rings.add(ring1);
        rings.add(ring2);
        RocketComponent parent = new BodyTube(1.0d, 0.1d);
        parent.addChild(ring1);
        parent.addChild(ring2);
        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.0059, result.doubleValue(), 0.0001);
    }
    
    /**
     * Test both rings within the root chord.
     */
    @Test
    public void testBothRingsWithinRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        RocketComponent parent = new BodyTube(1.0000d, 0.1d);
        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.4701);
        parent.addChild(ring1);
        CenteringRing ring2 = new CenteringRing();
        ring2.setLength(0.004);
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setAxialOffset(0.4750);
        parent.addChild(ring2);
        rings.add(ring1);
        rings.add(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.0009, result.doubleValue(), 0.0002);
    }


    /**
     * Test both rings beyond the root chord.
     */
    @Test
    public void testBothRingsBeyondRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.TOP);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.48);
        CenteringRing ring2 = new CenteringRing();
        ring2.setAxialMethod(AxialMethod.TOP);
        ring2.setLength(0.004);
        ring2.setAxialOffset(0.49);
        rings.add(ring1);
        rings.add(ring2);
        RocketComponent parent = new BodyTube(1.0d, 0.1d);
        parent.addChild(ring1);
        parent.addChild(ring2);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.0, result.doubleValue(), 0.0001);
    }

    /**
     * Test both rings within the root chord - the top ring has an adjacent ring (so 3 rings total).
     */
    @Test
    public void test3RingsWithinRootChord() throws Exception {
        DoubleModel dm = new DoubleModel(1d);
        List<CenteringRing> rings = new ArrayList<CenteringRing>();

        CenteringRing ring1 = new CenteringRing();
        ring1.setAxialMethod(AxialMethod.ABSOLUTE);
        ring1.setLength(0.004);
        ring1.setAxialOffset(0.47);
        CenteringRing ring2 = new CenteringRing();
        ring2.setAxialMethod(AxialMethod.ABSOLUTE);
        ring2.setLength(0.004);
        ring2.setAxialOffset(0.4702);
        CenteringRing ring3 = new CenteringRing();
        ring3.setAxialMethod(AxialMethod.ABSOLUTE);
        ring3.setLength(0.004);
        ring3.setAxialOffset(0.4770);
        rings.add(ring1);
        rings.add(ring2);
        rings.add(ring3);
        BodyTube parent = new BodyTube(1.0d, 0.1d);
        parent.setAxialOffset(0);
        parent.addChild(ring1);
        parent.addChild(ring2);
        parent.addChild(ring3);

        Double result = (Double)method.invoke(null, rings, 0.47d, 0.01, dm, parent);
        Assertions.assertEquals(0.0028, result.doubleValue(), 0.0001);
    }

    @Test
    public void testExportToSVG() throws IOException, ParserConfigurationException, TransformerException {
        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.06);
        bodyTube.setLength(0.2);

        TrapezoidFinSet finSet = new TrapezoidFinSet();
        finSet.setCantAngle(0);
        finSet.setRootChord(0.06);
        finSet.setRootChord(0.05);
        finSet.setHeight(0.03);
        finSet.setSweep(0.02);
        finSet.setSweepAngle(Math.toRadians(20));
        finSet.setThickness(0.002);
        finSet.setTabLength(0.02);
        finSet.setTabHeight(0.012);
        finSet.setTabOffsetMethod(AxialMethod.MIDDLE);
        finSet.setTabOffset(0);

        bodyTube.addChild(finSet);

        SVGOptionPanel options = new SVGOptionPanel();
        options.setStrokeWidth(0.1);
        options.setStrokeColor(Color.BLACK);

        File destFile = File.createTempFile("test", ".svg");

        FinSetConfig.writeSVGFile(finSet, destFile, options);

        // TODO: load the file and check the contents
    }

}
