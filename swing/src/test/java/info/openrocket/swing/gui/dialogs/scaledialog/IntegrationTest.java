package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Iterator;

/**
 * Integration test verifying that the entire component hierarchy of a rocket
 * scales correctly when the "Scale Rocket" option is chosen.
 * Simulates calling scale() and scaleOffset() on every component in the tree
 * (dimensions, mass, positional offsets, and overrides).
 */
public class IntegrationTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    /**
     * Simulates the core logic of ScaleDialog.doScale() by iterating and scaling every component
     * instance in the rocket tree using the reflected private methods.
     */
    private void scaleEntireRocket(Rocket rocket, double multiplier, boolean scaleMass, boolean scaleOffsets) throws Exception {
        // The Rocket.iterator(true) iterates over the Rocket component itself and all its descendants.
        Iterator<RocketComponent> iterator = rocket.iterator(true);

        while (iterator.hasNext()) {
            RocketComponent component = iterator.next();

            // 1. Scale Offsets (AxialOffset, RadialPosition, CG overrides, M^1)
            if (scaleOffsets) {
                scaleOffsetMethod.invoke(dialogInstance, component, multiplier, scaleMass);
            }

            // 2. Scale Dimensions and Mass (Length, Radius, Mass^3, M^1/M^3)
            scaleMethod.invoke(dialogInstance, component, multiplier, scaleMass);
        }
    }


    /**
     * Builds a complex hierarchy and tests that all linear (x2) and cubic (x8) scaling rules
     * are applied simultaneously across all relevant components.
     */
    @Test
    void testScaleRocket_AllPropertiesScaleCorrectly() throws Exception {
        // ARRANGE: Build a typical rocket structure
        Rocket rocket = new Rocket();
        AxialStage stage = new AxialStage();
        BodyTube bt = new BodyTube();
        MassComponent mass = new MassComponent();
        Streamer streamer = new Streamer();

        // 1. Build Hierarchy (Rocket -> Stage -> BT -> (Mass, Streamer))
        rocket.addChild(stage);
        stage.addChild(bt);
        bt.addChild(mass);
        bt.addChild(streamer);

        // 2. Set Initial Values
        final double multiplier = 2.0;

        // --- Mass Component (Mass & Positional) ---
        mass.setComponentMass(100.0); // Mass
        mass.setLength(0.500);       // Dimension
        mass.setRadialPosition(0.010); // Positional Offset

        // --- Body Tube (Dimensional & Positional) ---
        bt.setOuterRadiusAutomatic(false);
        bt.setOuterRadius(0.040);
        bt.setLength(1.000);

        // FIX: Ensure AxialOffset is scaled by forcing the component to use the raw offset value.
        bt.setAxialMethod(AxialMethod.ABSOLUTE);
        bt.setAxialOffset(0.100);

        // --- Streamer (Simple Dimensional) ---
        streamer.setStripLength(2.000);

        // --- Rocket Root (Overrides, handled by RocketComponent's OverrideScaler) ---
        rocket.setCGOverridden(true);
        rocket.setOverrideCGX(0.200);   // Positional CG Override
        rocket.setMassOverridden(true);
        rocket.setOverrideMass(5.0);    // Mass Override


        // ACT: Scale the entire hierarchy (scaleMass=true, scaleOffsets=true)
        scaleEntireRocket(rocket, multiplier, true, true);

        // ASSERT: Verify all properties have scaled correctly (x2 or x8)

        // --- Mass Component Assertions ---
        assertEquals(1.000, mass.getLength(), DELTA, "MassComp Length must scale linearly (0.5 * 2).");
        assertEquals(0.020, mass.getRadialPosition(), DELTA, "MassComp RadialPosition must scale linearly (0.01 * 2).");
        assertEquals(800.0, mass.getComponentMass(), DELTA, "MassComp Mass must scale cubically (100 * 8).");

        // --- Body Tube Assertions ---
        assertEquals(2.000, bt.getLength(), DELTA, "BodyTube Length must scale linearly (1.0 * 2).");
        assertEquals(0.080, bt.getOuterRadius(), DELTA, "BodyTube OuterRadius must scale linearly (0.04 * 2).");
        assertEquals(0.200, bt.getAxialOffset(), DELTA, "BodyTube AxialOffset must scale linearly (0.1 * 2).");

        // --- Streamer Assertions ---
        assertEquals(4.000, streamer.getStripLength(), DELTA, "Streamer Length must scale linearly (2.0 * 2).");

        // --- Rocket Root Overrides Assertions ---
        assertEquals(0.400, rocket.getOverrideCGX(), DELTA, "Rocket CG Override must scale linearly (0.2 * 2).");
        assertEquals(40.0, rocket.getOverrideMass(), DELTA, "Rocket Mass Override must scale cubically (5 * 8).");
    }
}