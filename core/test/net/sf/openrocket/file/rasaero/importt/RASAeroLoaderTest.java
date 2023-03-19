package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RASAeroLoaderTest extends BaseTestCase {
    private static final double EPSILON = 0.0001;

    /**
     * Test loading a three stage RASAero rocket and verifying the parameters.
     */
    @Test
    public void testThreeStageRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("Three-stage rocket.CDX1");
        assertNotNull("Could not open Three-stage rocket.CDX1", stream);
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), "Three-stage rocket");
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);

            // Test number and type of components
            assertEquals("Incorrect amount of stages", 3, rocket.getChildCount());
            AxialStage sustainer = rocket.getStage(0);
            AxialStage booster1 = rocket.getStage(1);
            AxialStage booster2 = rocket.getStage(2);

            assertEquals("Incorrect amount of sustainer children", 2, sustainer.getChildCount());
            assertEquals("Incorrect amount of booster 1 children", 1, booster1.getChildCount());
            assertEquals("Incorrect amount of booster 2 children", 1, booster2.getChildCount());

            RocketComponent noseCone = sustainer.getChild(0);
            assertTrue("First component should be nose cone", noseCone instanceof NoseCone);
            assertEquals(0, noseCone.getChildCount());
            RocketComponent bodyTube = sustainer.getChild(1);
            assertTrue("Second component should be body tube", bodyTube instanceof BodyTube);
            assertEquals(1, bodyTube.getChildCount());
            RocketComponent finSet = bodyTube.getChild(0);
            assertTrue("Body tube child should be trapezoid fin set", finSet instanceof TrapezoidFinSet);

            RocketComponent booster1Tube = booster1.getChild(0);
            assertTrue("Booster child should be nose cone", booster1Tube instanceof BodyTube);
            assertEquals(1, booster1Tube.getChildCount());
            RocketComponent booster1FinSet = booster1Tube.getChild(0);
            assertTrue("Booster 1 tube child should be trapezoid fin set", booster1FinSet instanceof TrapezoidFinSet);

            RocketComponent booster2Tube = booster2.getChild(0);
            assertTrue("Booster child should be nose cone", booster2Tube instanceof BodyTube);
            assertEquals(1, booster2Tube.getChildCount());
            RocketComponent booster2FinSet = booster2Tube.getChild(0);
            assertTrue("Booster 1 tube child should be trapezoid fin set", booster2FinSet instanceof TrapezoidFinSet);

            // Test component parameters
            assertEquals("Three-stage rocket", rocket.getName());

            //// Sustainer
            NoseCone nose = (NoseCone) noseCone;
            assertEquals(Transition.Shape.OGIVE, nose.getShapeType());
            assertEquals(0.0125, nose.getBaseRadius(), EPSILON);
            assertEquals(0.1, nose.getLength(), EPSILON);
            assertEquals(0.002, nose.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , nose.getFinish());

            BodyTube tube = (BodyTube) bodyTube;
            assertEquals(0.0125, tube.getOuterRadius(), EPSILON);
            assertTrue(tube.isOuterRadiusAutomatic());
            assertEquals(0.3, tube.getLength(), EPSILON);
            assertEquals(0.002, tube.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , tube.getFinish());

            TrapezoidFinSet fins = (TrapezoidFinSet) finSet;
            assertEquals(4, fins.getFinCount());
            assertEquals(0, fins.getCantAngle(), EPSILON);
            assertEquals(0.05, fins.getRootChord(), EPSILON);
            assertEquals(0.05, fins.getTipChord(), EPSILON);
            assertEquals(0.03, fins.getHeight(), EPSILON);
            assertEquals(0.6947, fins.getSweepAngle() , EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins.getCrossSection());
            assertEquals(0.00201, fins.getThickness() , EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , fins.getFinish());

            //// Booster 1
            BodyTube tube1 = (BodyTube) booster1Tube;
            assertEquals(0.0125, tube1.getOuterRadius(), EPSILON);
            assertFalse(tube1.isOuterRadiusAutomatic());
            assertEquals(0.08, tube1.getLength(), EPSILON);
            assertEquals(0.002, tube1.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , tube1.getFinish());

            TrapezoidFinSet fins1 = (TrapezoidFinSet) booster1FinSet;
            assertEquals(4, fins1.getFinCount());
            assertEquals(0, fins1.getCantAngle(), EPSILON);
            assertEquals(0.08, fins1.getRootChord(), EPSILON);
            assertEquals(0.073, fins1.getTipChord(), EPSILON);
            assertEquals(0.03, fins1.getHeight(), EPSILON);
            assertEquals(0.6947, fins1.getSweepAngle() , EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins1.getCrossSection());
            assertEquals(0.00201, fins1.getThickness() , EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , fins1.getFinish());

            //// Booster 2
            BodyTube tube2 = (BodyTube) booster2Tube;
            assertEquals(0.0125, tube2.getOuterRadius(), EPSILON);
            assertFalse(tube2.isOuterRadiusAutomatic());
            assertEquals(0.08, tube2.getLength(), EPSILON);
            assertEquals(0.002, tube2.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , tube2.getFinish());

            TrapezoidFinSet fins2 = (TrapezoidFinSet) booster2FinSet;
            assertEquals(4, fins2.getFinCount());
            assertEquals(0, fins2.getCantAngle(), EPSILON);
            assertEquals(0.08, fins2.getRootChord(), EPSILON);
            assertEquals(0.03, fins2.getTipChord(), EPSILON);
            assertEquals(0.04, fins2.getHeight(), EPSILON);
            assertEquals(0.5584, fins2.getSweepAngle() , EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins2.getCrossSection());
            assertEquals(0.00201, fins2.getThickness() , EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR , fins2.getFinish());
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(loader.getWarnings().isEmpty());
    }

    /**
     * Test whether we can load a very complex, unrealistic rocket with practically all RASAero features.
     */
    @Test
    public void testShowRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("Show-off.CDX1");
        assertNotNull("Could not open Show-off.CDX1", stream);
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(5, loader.getWarnings().size());
    }

    /**
     * Test a complex two-stage rocket with practically all RASAero features.
     */
    @Test
    public void testComplexTwoStageRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("Complex.Two-Stage.CDX1");
        assertNotNull("Could not open Complex.Two-Stage.CDX1", stream);
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);

            // TODO: fetch components and test their parameters
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
        // TODO: this also includes all motor warnings, so change motor db in setUp() to include OR motors so the total
        //  warning size decreases
        assertEquals(4, loader.getWarnings().size());
    }
}
