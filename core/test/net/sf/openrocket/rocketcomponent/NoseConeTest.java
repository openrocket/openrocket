package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.MathUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NoseConeTest extends BaseTestCase {
    private final double EPSILON = MathUtil.EPSILON * 1000;

    @Test
    public void testNormalNoseCone() {
        NoseCone noseCone = new NoseCone();

        // First set the parameters using the normal transition setters (i.e. using AftRadius and AftShoulder instead of Base and Shoulder)
        noseCone.setType(Transition.Shape.OGIVE);
        noseCone.setLength(0.06);
        noseCone.setAftRadius(0.1);
        noseCone.setAftShoulderLength(0.01);
        noseCone.setAftShoulderRadius(0.05);
        noseCone.setAftShoulderCapped(false);
        noseCone.setAftShoulderThickness(0.001);

        assertEquals(Transition.Shape.OGIVE, noseCone.getType());
        assertEquals(0.06, noseCone.getLength(), EPSILON);
        assertEquals(0.1, noseCone.getAftRadius(), EPSILON);
        assertEquals(0.1, noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.1, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0.1, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(0.01, noseCone.getAftShoulderLength(), EPSILON);
        assertEquals(0.01, noseCone.getShoulderLength(), EPSILON);
        assertEquals(0.05, noseCone.getAftShoulderRadius(), EPSILON);
        assertEquals(0.05, noseCone.getShoulderRadius(), EPSILON);
        assertFalse(noseCone.isAftShoulderCapped());
        assertFalse(noseCone.isShoulderCapped());
        assertEquals(0.001, noseCone.getAftShoulderThickness(), EPSILON);
        assertEquals(0.001, noseCone.getShoulderThickness(), EPSILON);
        assertFalse(noseCone.isAftRadiusAutomatic());

        assertEquals(0, noseCone.getForeRadius(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderLength(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderRadius(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderThickness(), EPSILON);
        assertFalse(noseCone.isForeShoulderCapped());
        assertFalse(noseCone.isForeRadiusAutomatic());

        // Test setting the specific nose cone setters
        noseCone.setBaseRadius(0.2);
        noseCone.setShoulderLength(0.03);
        noseCone.setShoulderRadius(0.04);
        noseCone.setShoulderCapped(true);
        noseCone.setShoulderThickness(0.005);

        assertEquals(0.2, noseCone.getAftRadius(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.2, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(0.03, noseCone.getAftShoulderLength(), EPSILON);
        assertEquals(0.03, noseCone.getShoulderLength(), EPSILON);
        assertEquals(0.04, noseCone.getAftShoulderRadius(), EPSILON);
        assertEquals(0.04, noseCone.getShoulderRadius(), EPSILON);
        assertTrue(noseCone.isAftShoulderCapped());
        assertTrue(noseCone.isShoulderCapped());
        assertEquals(0.005, noseCone.getAftShoulderThickness(), EPSILON);
        assertEquals(0.005, noseCone.getShoulderThickness(), EPSILON);
        assertFalse(noseCone.isAftRadiusAutomatic());

        assertEquals(0, noseCone.getForeRadius(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderLength(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderRadius(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderThickness(), EPSILON);
        assertFalse(noseCone.isForeShoulderCapped());
        assertFalse(noseCone.isForeRadiusAutomatic());
    }

    @Test
    public void testFlippedNoseCone() {
        NoseCone noseCone = new NoseCone();

        // First set the parameters using the normal transition setters (i.e. using AftRadius and AftShoulder instead of Base and Shoulder)
        noseCone.setType(Transition.Shape.OGIVE);
        noseCone.setLength(0.06);
        noseCone.setAftRadius(0.1);
        noseCone.setAftShoulderLength(0.01);
        noseCone.setAftShoulderRadius(0.05);
        noseCone.setAftShoulderCapped(false);
        noseCone.setAftShoulderThickness(0.001);
        noseCone.setFlipped(true);

        assertEquals(Transition.Shape.OGIVE, noseCone.getType());
        assertEquals(0.06, noseCone.getLength(), EPSILON);
        assertEquals(0.1, noseCone.getForeRadius(), EPSILON);
        assertEquals(0.1, noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.1, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0.1, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(0.01, noseCone.getForeShoulderLength(), EPSILON);
        assertEquals(0.01, noseCone.getShoulderLength(), EPSILON);
        assertEquals(0.05, noseCone.getForeShoulderRadius(), EPSILON);
        assertEquals(0.05, noseCone.getShoulderRadius(), EPSILON);
        assertFalse(noseCone.isForeShoulderCapped());
        assertFalse(noseCone.isShoulderCapped());
        assertEquals(0.001, noseCone.getForeShoulderThickness(), EPSILON);
        assertEquals(0.001, noseCone.getShoulderThickness(), EPSILON);
        assertFalse(noseCone.isForeRadiusAutomatic());

        assertEquals(0, noseCone.getAftRadius(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderLength(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderRadius(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderThickness(), EPSILON);
        assertFalse(noseCone.isAftShoulderCapped());
        assertFalse(noseCone.isAftRadiusAutomatic());

        // Test setting the specific nose cone setters
        noseCone.setBaseRadius(0.2);
        noseCone.setShoulderLength(0.03);
        noseCone.setShoulderRadius(0.04);
        noseCone.setShoulderCapped(true);
        noseCone.setShoulderThickness(0.005);

        assertEquals(0.2, noseCone.getForeRadius(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.2, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(0.03, noseCone.getForeShoulderLength(), EPSILON);
        assertEquals(0.03, noseCone.getShoulderLength(), EPSILON);
        assertEquals(0.04, noseCone.getForeShoulderRadius(), EPSILON);
        assertEquals(0.04, noseCone.getShoulderRadius(), EPSILON);
        assertTrue(noseCone.isForeShoulderCapped());
        assertTrue(noseCone.isShoulderCapped());
        assertEquals(0.005, noseCone.getForeShoulderThickness(), EPSILON);
        assertEquals(0.005, noseCone.getShoulderThickness(), EPSILON);
        assertFalse(noseCone.isForeRadiusAutomatic());

        assertEquals(0, noseCone.getAftRadius(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderLength(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderRadius(), EPSILON);
        assertEquals(0, noseCone.getAftShoulderThickness(), EPSILON);
        assertFalse(noseCone.isAftShoulderCapped());
        assertFalse(noseCone.isAftRadiusAutomatic());

        // Flip back to normal
        noseCone.setFlipped(false);

        assertEquals(0.2, noseCone.getAftRadius(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.2, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0.2, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(0.03, noseCone.getAftShoulderLength(), EPSILON);
        assertEquals(0.03, noseCone.getShoulderLength(), EPSILON);
        assertEquals(0.04, noseCone.getAftShoulderRadius(), EPSILON);
        assertEquals(0.04, noseCone.getShoulderRadius(), EPSILON);
        assertTrue(noseCone.isAftShoulderCapped());
        assertTrue(noseCone.isShoulderCapped());
        assertEquals(0.005, noseCone.getAftShoulderThickness(), EPSILON);
        assertEquals(0.005, noseCone.getShoulderThickness(), EPSILON);
        assertFalse(noseCone.isAftRadiusAutomatic());

        assertEquals(0, noseCone.getForeRadius(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderLength(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderRadius(), EPSILON);
        assertEquals(0, noseCone.getForeShoulderThickness(), EPSILON);
        assertFalse(noseCone.isForeShoulderCapped());
        assertFalse(noseCone.isForeRadiusAutomatic());
    }

    @Test
    public void testNormalNoseConeRadiusAutomatic() {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage stage = rocket.getStage(0);

        NoseCone noseCone = new NoseCone(Transition.Shape.CONICAL, 0.06, 0.01);
        BodyTube tube1 = new BodyTube(0.06, 0.02);
        tube1.setOuterRadiusAutomatic(false);
        BodyTube tube2 = new BodyTube(0.06, 0.03);
        tube2.setOuterRadiusAutomatic(false);

        // Test no previous or next component
        stage.addChild(noseCone);

        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(stage, noseCone.getPreviousComponent());
        assertNull(noseCone.getPreviousSymmetricComponent());
        assertNull(noseCone.getNextComponent());
        assertNull(noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0.01, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setAftRadiusAutomatic(true, true);
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        noseCone.setForeRadiusAutomatic(true, true);
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());

        // Test with next component
        stage.addChild(tube1);

        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(stage, noseCone.getPreviousComponent());
        assertNull(noseCone.getPreviousSymmetricComponent());
        assertSame(tube1, noseCone.getNextComponent());
        assertSame(tube1, noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setAftRadiusAutomatic(true, true);
        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertTrue(noseCone.usesNextCompAutomatic());
        assertSame(stage, noseCone.getPreviousComponent());
        assertNull(noseCone.getPreviousSymmetricComponent());
        assertSame(tube1, noseCone.getNextComponent());
        assertSame(tube1, noseCone.getNextSymmetricComponent());
        assertTrue(noseCone.isAftRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(tube1.getForeRadius(), noseCone.getAftRadius(), EPSILON);
        assertEquals(0.01, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getForeRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setAftRadiusAutomatic(false, true);
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setBaseRadiusAutomatic(true);
        assertEquals(0.01, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getForeRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setForeRadiusAutomatic(true, true);
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());

        // Test with previous component
        stage.addChild(tube2, 0);

        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertTrue(noseCone.usesNextCompAutomatic());
        assertSame(tube2, noseCone.getPreviousComponent());
        assertSame(tube2, noseCone.getPreviousSymmetricComponent());
        assertSame(tube1, noseCone.getNextComponent());
        assertSame(tube1, noseCone.getNextSymmetricComponent());
        assertTrue(noseCone.isAftRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(tube1.getForeRadius(), noseCone.getAftRadius(), EPSILON);
        assertEquals(0.01, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getForeRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        // Do a flip
        noseCone.setFlipped(true);
        assertTrue(noseCone.isForeRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isAftRadiusAutomatic());
    }

    @Test
    public void testFlippedNoseConeRadiusAutomatic() {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage stage = rocket.getStage(0);

        NoseCone noseCone = new NoseCone(Transition.Shape.CONICAL, 0.06, 0.01);
        noseCone.setFlipped(true);
        BodyTube tube1 = new BodyTube(0.06, 0.02);
        tube1.setOuterRadiusAutomatic(false);
        BodyTube tube2 = new BodyTube(0.06, 0.03);
        tube2.setOuterRadiusAutomatic(false);

        // Test no previous or next component
        stage.addChild(noseCone);

        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(stage, noseCone.getPreviousComponent());
        assertNull(noseCone.getPreviousSymmetricComponent());
        assertNull(noseCone.getNextComponent());
        assertNull(noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0.01, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setAftRadiusAutomatic(true, true);
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        noseCone.setForeRadiusAutomatic(true, true);
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());

        // Test with previous component
        stage.addChild(tube1, 0);

        assertFalse(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(tube1, noseCone.getPreviousComponent());
        assertSame(tube1, noseCone.getPreviousSymmetricComponent());
        assertNull(noseCone.getNextComponent());
        assertNull(noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertFalse(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isForeRadiusAutomatic());
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);

        noseCone.setBaseRadiusAutomatic(true);
        assertTrue(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(tube1, noseCone.getPreviousComponent());
        assertSame(tube1, noseCone.getPreviousSymmetricComponent());
        assertNull(noseCone.getNextComponent());
        assertNull(noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertTrue(noseCone.isForeRadiusAutomatic());
        assertEquals(tube1.getAftRadius(), noseCone.getForeRadius(), EPSILON);
        assertEquals(0.01, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getAftRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);

        noseCone.setForeRadiusAutomatic(false, true);
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getBaseRadius(), noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getForeRadius(), noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(0.01, noseCone.getForeRadiusNoAutomatic(), EPSILON);

        noseCone.setBaseRadiusAutomatic(true);
        assertEquals(tube1.getAftRadius(), noseCone.getForeRadius(), EPSILON);
        assertEquals(0.01, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getAftRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftRadiusNoAutomatic(), EPSILON);

        assertTrue(noseCone.isForeRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertFalse(noseCone.isAftRadiusAutomatic());

        // Test with next component
        stage.addChild(tube2);

        assertTrue(noseCone.usesPreviousCompAutomatic());
        assertFalse(noseCone.usesNextCompAutomatic());
        assertSame(tube1, noseCone.getPreviousComponent());
        assertSame(tube1, noseCone.getPreviousSymmetricComponent());
        assertSame(tube2, noseCone.getNextComponent());
        assertSame(tube2, noseCone.getNextSymmetricComponent());
        assertFalse(noseCone.isAftRadiusAutomatic());
        assertTrue(noseCone.isBaseRadiusAutomatic());
        assertTrue(noseCone.isForeRadiusAutomatic());
        assertEquals(tube1.getAftRadius(), noseCone.getForeRadius(), EPSILON);
        assertEquals(0.01, noseCone.getForeRadiusNoAutomatic(), EPSILON);
        assertEquals(tube1.getForeRadius(), noseCone.getBaseRadius(), EPSILON);
        assertEquals(0.01, noseCone.getBaseRadiusNoAutomatic(), EPSILON);
        assertEquals(noseCone.getAftRadius(), noseCone.getAftRadiusNoAutomatic(), EPSILON);
        assertEquals(0, noseCone.getAftRadius(), EPSILON);
    }
}
