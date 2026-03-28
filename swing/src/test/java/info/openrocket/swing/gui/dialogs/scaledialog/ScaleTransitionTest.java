package info.openrocket.swing.gui.dialogs.scaledialog;


import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.dialogs.scaledialog.ScaleDialogBaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the scaling logic of the Transition component.
 * After fixing the bug all tests pass
 **/
public class ScaleTransitionTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // ==========================================
    // HELPER METHOD
    // ==========================================


    private Transition createTransitionInRocket(
            double foreBodyRadius,
            double aftBodyRadius,
            boolean foreAuto,
            boolean aftAuto) {

        // Build proper rocket hierarchy
        Rocket rocket = new Rocket();
        AxialStage stage = new AxialStage();
        rocket.addChild(stage);

        // Nose cone (required first component)
        NoseCone nose = new NoseCone();
        nose.setLength(0.100);
        nose.setAftRadius(foreBodyRadius);
        stage.addChild(nose);

        // Fore body tube (for automatic fore radius reference)
        BodyTube foreBody = new BodyTube();
        foreBody.setOuterRadiusAutomatic(false);
        foreBody.setOuterRadius(foreBodyRadius);
        foreBody.setLength(0.100);
        stage.addChild(foreBody);

        // Transition under test
        Transition trans = new Transition();
        trans.setLength(0.100);
        stage.addChild(trans);

        // Set automatic modes
        trans.setForeRadiusAutomatic(foreAuto);
        trans.setAftRadiusAutomatic(aftAuto);

        // Set manual values if not automatic
        if (!foreAuto) {
            trans.setForeRadius(foreBodyRadius);
        }
        if (!aftAuto) {
            trans.setAftRadius(aftBodyRadius);
        }

        // Aft body tube (for automatic aft radius reference, if needed)
        if (aftAuto) {
            BodyTube aftBody = new BodyTube();
            aftBody.setOuterRadiusAutomatic(false);
            aftBody.setOuterRadius(aftBodyRadius);
            aftBody.setLength(0.100);
            stage.addChild(aftBody);
        }

        return trans;
    }

    // ==========================================
    // STANDARD SCALING TESTS (BASELINE - ALWAYS PASS)
    // ==========================================


    @Test
    void testScaleWhenBothRadiiAreManual() throws Exception {
        Transition trans = new Transition();
        trans.setLength(20.0);

        trans.setForeRadius(10.0);
        trans.setForeRadiusAutomatic(false);
        trans.setAftRadius(16.0);
        trans.setAftRadiusAutomatic(false);

        trans.setForeShoulderLength(2.0);
        trans.setAftShoulderLength(4.0);

        // Scale by 0.5
        scaleMethod.invoke(dialogInstance, trans, 0.5, false);

        assertEquals(10.0, trans.getLength(), 0.001);
        assertEquals(5.0, trans.getForeRadius(), 0.001);
        assertEquals(8.0, trans.getAftRadius(), 0.001);
        assertEquals(1.0, trans.getForeShoulderLength(), 0.001);
        assertEquals(2.0, trans.getAftShoulderLength(), 0.001);

        // Scale by 1.5
        scaleMethod.invoke(dialogInstance, trans, 1.5, false);

        assertEquals(15.0, trans.getLength(), 0.001);
        assertEquals(7.5, trans.getForeRadius(), 0.001);
        assertEquals(12.0, trans.getAftRadius(), 0.001);
        assertEquals(1.5, trans.getForeShoulderLength(), 0.001);
        assertEquals(3.0, trans.getAftShoulderLength(), 0.001);
    }


    @Test
    void testManualRadiiScaleCorrectly() throws Exception {
        final double initialForeRadius = 0.025;
        final double initialAftRadius = 0.050;
        final double multiplier = 2.0;

        Transition trans = new Transition();
        trans.setForeRadiusAutomatic(false);
        trans.setAftRadiusAutomatic(false);
        trans.setForeRadius(initialForeRadius);
        trans.setAftRadius(initialAftRadius);

        scaleMethod.invoke(dialogInstance, trans, multiplier, true);

        assertEquals(initialForeRadius * multiplier, trans.getForeRadius(), DELTA,
                "ForeRadius should scale correctly when manual");

        assertEquals(initialAftRadius * multiplier, trans.getAftRadius(), DELTA,
                "AftRadius should scale correctly when manual");
    }



    @Test
    void testAftRadiusScalesWhenForeIsAutomatic() throws Exception {
        final double foreBodyRadius = 0.025;
        final double aftRadius = 0.015;
        final double multiplier = 2.0;

        Transition trans = createTransitionInRocket(
                foreBodyRadius,
                aftRadius,
                true,   // fore automatic (should NOT scale)
                false   // aft manual (SHOULD scale - this is where the bug was)
        );

        // ACT - Scale the transition
        scaleMethod.invoke(dialogInstance, trans, multiplier, true);

        // ASSERT - Fore radius should remain unchanged (automatic mode)
        assertEquals(foreBodyRadius, trans.getForeRadius(), DELTA,
                "Fore radius must not scale when automatic");

        // ASSERT - Aft radius SHOULD scale
        // WITH BUG: This assertion FAILS - actual value is 0.015 (not scaled)
        // AFTER FIX: This assertion PASSES - actual value is 0.030 (scaled correctly)
        assertEquals(aftRadius * multiplier, trans.getAftRadius(), DELTA,
                "Aft radius must scale when manual, regardless of fore radius setting");
    }


    @Test
    void testAftRadiusScalesDownWhenForeIsAutomatic() throws Exception {
        final double foreBodyRadius = 0.030;
        final double aftRadius = 0.040;
        final double multiplier = 0.5;

        Transition trans = createTransitionInRocket(
                foreBodyRadius,
                aftRadius,
                true,   // fore automatic
                false   // aft manual
        );

        scaleMethod.invoke(dialogInstance, trans, multiplier, false);

        // Fore stays same (automatic)
        assertEquals(foreBodyRadius, trans.getForeRadius(), DELTA,
                "Fore radius unchanged (automatic)");

        // Aft scales (manual)

        assertEquals(aftRadius * multiplier, trans.getAftRadius(), DELTA,
                "Aft radius must scale down correctly");
    }


    @Test
    void testAftShouldersScaleWhenAftRadiusIsManual() throws Exception {
        final double foreBodyRadius = 0.025;
        final double aftRadius = 0.020;
        final double shoulderRadius = 0.018;
        final double shoulderLength = 0.015;
        final double multiplier = 2.0;

        Transition trans = createTransitionInRocket(
                foreBodyRadius,
                aftRadius,
                true,   // fore automatic
                false   // aft manual
        );

        trans.setAftShoulderRadius(shoulderRadius);
        trans.setAftShoulderLength(shoulderLength);
        trans.setAftShoulderThickness(0.002);

        scaleMethod.invoke(dialogInstance, trans, multiplier, true);

        // WITH BUG: All three assertions below FAIL
        // AFTER FIX: All three assertions PASS
        assertEquals(aftRadius * multiplier, trans.getAftRadius(), DELTA,
                "Aft radius should scale");
        assertEquals(shoulderRadius * multiplier, trans.getAftShoulderRadius(), DELTA,
                "Aft shoulder radius should scale");
        assertEquals(shoulderLength * multiplier, trans.getAftShoulderLength(), DELTA,
                "Aft shoulder length should scale");
    }

    @Test
    void testNeitherRadiusScalesWhenBothAreAutomatic() throws Exception {
        final double foreBodyRadius = 0.025;
        final double aftBodyRadius = 0.015;
        final double multiplier = 2.0;

        Transition trans = createTransitionInRocket(
                foreBodyRadius,
                aftBodyRadius,
                true,   // fore automatic
                true    // aft automatic
        );

        scaleMethod.invoke(dialogInstance, trans, multiplier, true);

        // Both should remain at their automatic values
        assertEquals(foreBodyRadius, trans.getForeRadius(), DELTA,
                "Fore radius must not scale (automatic)");
        assertEquals(aftBodyRadius, trans.getAftRadius(), DELTA,
                "Aft radius must not scale (automatic)");
    }



    /**
     * INTEGRATION TEST: Complete rocket section scaling.
     */
    @Test
    void testTransitionBetweenTwoBodyTubesScalesCorrectly() throws Exception {
        final double largeBodyRadius = 0.025;
        final double smallBodyRadius = 0.015;
        final double multiplier = 1.5;

        // Build complete rocket structure
        Rocket rocket = new Rocket();
        AxialStage stage = new AxialStage();
        rocket.addChild(stage);

        NoseCone nose = new NoseCone();
        nose.setLength(0.100);
        nose.setAftRadius(largeBodyRadius);
        stage.addChild(nose);

        BodyTube largeBody = new BodyTube();
        largeBody.setOuterRadiusAutomatic(false);
        largeBody.setOuterRadius(largeBodyRadius);
        largeBody.setLength(0.200);
        stage.addChild(largeBody);

        Transition trans = new Transition();
        trans.setLength(0.100);
        trans.setForeRadiusAutomatic(true);   // Matches large body (automatic)
        trans.setAftRadiusAutomatic(false);   // Custom taper (manual) - BUG TRIGGER
        trans.setAftRadius(smallBodyRadius);
        trans.setAftShoulderRadius(0.014);
        trans.setAftShoulderLength(0.020);
        stage.addChild(trans);

        BodyTube smallBody = new BodyTube();
        smallBody.setOuterRadiusAutomatic(false);
        smallBody.setOuterRadius(smallBodyRadius);
        smallBody.setLength(0.150);
        stage.addChild(smallBody);

        // Scale components
        scaleMethod.invoke(dialogInstance, largeBody, multiplier, false);
        scaleMethod.invoke(dialogInstance, trans, multiplier, false);
        scaleMethod.invoke(dialogInstance, smallBody, multiplier, false);

        // Verify scaling
        assertEquals(largeBodyRadius * multiplier, largeBody.getOuterRadius(), DELTA,
                "Large body tube must scale");
        assertEquals(0.100 * multiplier, trans.getLength(), DELTA,
                "Transition length must scale");

        assertEquals(smallBodyRadius * multiplier, trans.getAftRadius(), DELTA,
                "Transition aft radius must scale");

        assertEquals(smallBodyRadius * multiplier, smallBody.getOuterRadius(), DELTA,
                "Small body tube must scale");

        assertEquals(0.014 * multiplier, trans.getAftShoulderRadius(), DELTA,
                "Shoulder must scale with transition");
    }

    // ==========================================
    // EDGE CASES
    // ==========================================

    @Test
    void testScaleByOneResultsInNoChange() throws Exception {
        final double foreRadius = 0.025;
        final double aftRadius = 0.015;

        Transition trans = createTransitionInRocket(
                foreRadius, aftRadius, true, false
        );

        scaleMethod.invoke(dialogInstance, trans, 1.0, false);

        assertEquals(foreRadius, trans.getForeRadius(), DELTA);
        assertEquals(aftRadius, trans.getAftRadius(), DELTA);
    }


    @Test
    void testScaleWithSmallMultiplier() throws Exception {
        final double foreRadius = 0.050;
        final double aftRadius = 0.040;
        final double multiplier = 0.1;

        Transition trans = createTransitionInRocket(
                foreRadius, aftRadius, true, false
        );

        scaleMethod.invoke(dialogInstance, trans, multiplier, false);

        assertEquals(foreRadius, trans.getForeRadius(), DELTA,
                "Fore unchanged (automatic)");
        assertEquals(aftRadius * multiplier, trans.getAftRadius(), DELTA,
                "Aft must scale with small multiplier");
    }


    @Test
    void testScaleWithLargeMultiplier() throws Exception {
        final double foreRadius = 0.010;
        final double aftRadius = 0.008;
        final double multiplier = 10.0;

        Transition trans = createTransitionInRocket(
                foreRadius, aftRadius, true, false
        );

        scaleMethod.invoke(dialogInstance, trans, multiplier, false);

        assertEquals(foreRadius, trans.getForeRadius(), DELTA,
                "Fore unchanged (automatic)");
        assertEquals(aftRadius * multiplier, trans.getAftRadius(), DELTA,
                "Aft must scale with large multiplier");
    }
}