package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RASAeroLoaderTest extends BaseTestCase {
    private static final double EPSILON = 0.0001;

    /**
     * Test loading a three stage RASAero rocket and verifying the parameters.
     */
    @Test
    public void testThreeStageRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("/file/rasaero/importt/Three-stage rocket.CDX1");
        assertNotNull(stream, "Could not open Three-stage rocket.CDX1");
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), "Three-stage rocket");
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);

            // Test number and type of components
            assertEquals(3, rocket.getChildCount(), "Incorrect amount of stages");
            AxialStage sustainer = rocket.getStage(0);
            AxialStage booster1 = rocket.getStage(1);
            AxialStage booster2 = rocket.getStage(2);

            assertEquals(2, sustainer.getChildCount(), "Incorrect amount of sustainer children");
            assertEquals(1, booster1.getChildCount(), "Incorrect amount of booster 1 children");
            assertEquals(1, booster2.getChildCount(), "Incorrect amount of booster 2 children");

            RocketComponent noseCone = sustainer.getChild(0);
            assertTrue(noseCone instanceof NoseCone, "First component should be nose cone");
            assertEquals(0, noseCone.getChildCount());
            RocketComponent bodyTube = sustainer.getChild(1);
            assertTrue(bodyTube instanceof BodyTube, "Second component should be body tube");
            assertEquals(1, bodyTube.getChildCount());
            RocketComponent finSet = bodyTube.getChild(0);
            assertTrue(finSet instanceof TrapezoidFinSet, "Body tube child should be trapezoid fin set");

            RocketComponent booster1Tube = booster1.getChild(0);
            assertTrue(booster1Tube instanceof BodyTube, "Booster child should be nose cone");
            assertEquals(1, booster1Tube.getChildCount());
            RocketComponent booster1FinSet = booster1Tube.getChild(0);
            assertTrue(booster1FinSet instanceof TrapezoidFinSet, "Booster 1 tube child should be trapezoid fin set");

            RocketComponent booster2Tube = booster2.getChild(0);
            assertTrue(booster2Tube instanceof BodyTube, "Booster child should be nose cone");
            assertEquals(1, booster2Tube.getChildCount());
            RocketComponent booster2FinSet = booster2Tube.getChild(0);
            assertTrue(booster2FinSet instanceof TrapezoidFinSet, "Booster 1 tube child should be trapezoid fin set");

            // Test component parameters
            assertEquals(rocket.getName(), "Three-stage rocket");

            //// Sustainer
            NoseCone nose = (NoseCone) noseCone;
            assertEquals(Transition.Shape.OGIVE, nose.getShapeType());
            assertEquals(0.0125, nose.getBaseRadius(), EPSILON);
            assertEquals(0.1, nose.getLength(), EPSILON);
            assertEquals(0.002, nose.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, nose.getFinish());

            BodyTube tube = (BodyTube) bodyTube;
            assertEquals(0.0125, tube.getOuterRadius(), EPSILON);
            assertTrue(tube.isOuterRadiusAutomatic());
            assertEquals(0.3, tube.getLength(), EPSILON);
            assertEquals(0.002, tube.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, tube.getFinish());

            TrapezoidFinSet fins = (TrapezoidFinSet) finSet;
            assertEquals(4, fins.getFinCount());
            assertEquals(0, fins.getCantAngle(), EPSILON);
            assertEquals(0.05, fins.getRootChord(), EPSILON);
            assertEquals(0.05, fins.getTipChord(), EPSILON);
            assertEquals(0.03, fins.getHeight(), EPSILON);
            assertEquals(0.6947, fins.getSweepAngle(), EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins.getCrossSection());
            assertEquals(0.00201, fins.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, fins.getFinish());

            //// Booster 1
            BodyTube tube1 = (BodyTube) booster1Tube;
            assertEquals(0.0125, tube1.getOuterRadius(), EPSILON);
            assertFalse(tube1.isOuterRadiusAutomatic());
            assertEquals(0.08, tube1.getLength(), EPSILON);
            assertEquals(0.002, tube1.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, tube1.getFinish());

            TrapezoidFinSet fins1 = (TrapezoidFinSet) booster1FinSet;
            assertEquals(4, fins1.getFinCount());
            assertEquals(0, fins1.getCantAngle(), EPSILON);
            assertEquals(0.08, fins1.getRootChord(), EPSILON);
            assertEquals(0.073, fins1.getTipChord(), EPSILON);
            assertEquals(0.03, fins1.getHeight(), EPSILON);
            assertEquals(0.6947, fins1.getSweepAngle(), EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins1.getCrossSection());
            assertEquals(0.00201, fins1.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, fins1.getFinish());

            //// Booster 2
            BodyTube tube2 = (BodyTube) booster2Tube;
            assertEquals(0.0125, tube2.getOuterRadius(), EPSILON);
            assertFalse(tube2.isOuterRadiusAutomatic());
            assertEquals(0.08, tube2.getLength(), EPSILON);
            assertEquals(0.002, tube2.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, tube2.getFinish());

            TrapezoidFinSet fins2 = (TrapezoidFinSet) booster2FinSet;
            assertEquals(4, fins2.getFinCount());
            assertEquals(0, fins2.getCantAngle(), EPSILON);
            assertEquals(0.08, fins2.getRootChord(), EPSILON);
            assertEquals(0.03, fins2.getTipChord(), EPSILON);
            assertEquals(0.04, fins2.getHeight(), EPSILON);
            assertEquals(0.5584, fins2.getSweepAngle(), EPSILON);
            assertEquals(FinSet.CrossSection.SQUARE, fins2.getCrossSection());
            assertEquals(0.00201, fins2.getThickness(), EPSILON);
            assertEquals(ExternalComponent.Finish.MIRROR, fins2.getFinish());
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(loader.getWarnings().isEmpty());
    }

    /**
     * Test whether we can load a very complex, unrealistic rocket with practically
     * all RASAero features.
     */
    @Test
    public void testShowRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("/file/rasaero/importt/Show-off.CDX1");
        assertNotNull(stream, "Could not open Show-off.CDX1");
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
        // Includes the unsupported fin can offset
        assertEquals(6, loader.getWarnings().size());
    }

    /**
     * Test a complex two-stage rocket with practically all RASAero features.
     */
    @Test
    public void testComplexTwoStageRocket() {
        RASAeroLoader loader = new RASAeroLoader();
        InputStream stream = this.getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1");
        assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);

            AxialStage sustainer = rocket.getStage(0);
            AxialStage booster = rocket.getStage(1);

            // The test motor database is empty.  RASAero's loaded values therefore
            // cannot safely be converted to motor-free OpenRocket overrides.
            assertFalse(sustainer.isMassOverridden(), "Unresolved sustainer motor must not be included in dry mass");
            assertFalse(sustainer.isCGOverridden(), "Unresolved sustainer motor must not be included in dry CG");
            assertFalse(booster.isMassOverridden(), "Unresolved booster motor must not be included in dry mass");
            assertFalse(booster.isCGOverridden(), "Unresolved booster motor must not be included in dry CG");

            // TODO: fetch components and test their parameters
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
        // TODO: this also includes all motor warnings, so change motor db in setUp() to
        // include OR motors so the total
        // warning size decreases
        assertEquals(7, loader.getWarnings().size());
        assertTrue(loader.getWarnings().stream()
                .anyMatch(warning -> warning.toString().contains("mass and CG overrides were skipped")));
        // Unsupported inputs that affect the aerodynamics
        assertTrue(loader.getWarnings().stream().anyMatch(warning -> warning.toString().contains("Blunt nose cone")));
        assertTrue(loader.getWarnings().stream().anyMatch(warning -> warning.toString().contains("Protuberances")));
        assertTrue(loader.getWarnings().stream().anyMatch(warning -> warning.toString().contains("offset of fin cans")));
    }

    /**
     * RASAero writes numbers with the decimal separator of the user's locale, so a design saved with a comma
     * separator must load the same as one saved with a period.
     */
    @Test
    public void testCommaDecimalSeparator() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        int start = design.indexOf("<RocketDesign>");
        int end = design.indexOf("</RocketDesign>");
        String commaDesign = design.substring(0, start)
                + design.substring(start, end).replaceAll(">(-?\\d+)\\.(\\d+)<", ">$1,$2<")
                + design.substring(end);
        assertNotEquals(design, commaDesign);

        Rocket expected = loadFromString(design).getRocket();
        Rocket actual = loadFromString(commaDesign).getRocket();
        List<RocketComponent> expectedComponents = expected.getAllChildren();
        List<RocketComponent> actualComponents = actual.getAllChildren();
        assertEquals(expectedComponents.size(), actualComponents.size());
        for (int i = 0; i < expectedComponents.size(); i++) {
            RocketComponent e = expectedComponents.get(i);
            RocketComponent a = actualComponents.get(i);
            assertEquals(e.getName(), a.getName());
            assertEquals(e.getLength(), a.getLength(), EPSILON, e.getName());
            assertEquals(e.getComponentLocations()[0].getX(), a.getComponentLocations()[0].getX(), EPSILON,
                    e.getName());
        }
    }

    /**
     * The simulations and launch site of a RASAero file written with a comma decimal separator must import the same
     * as with a point.
     */
    @Test
    public void testCommaDecimalSeparatorInSimulations() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        int end = design.indexOf("</RocketDesign>");
        String commaDesign = design.substring(0, end)
                + design.substring(end).replaceAll(">(-?\\d+)\\.(\\d+)<", ">$1,$2<");
        assertNotEquals(design, commaDesign);

        OpenRocketDocument expected = loadFromString(design);
        OpenRocketDocument actual = loadFromString(commaDesign);
        assertEquals(expected.getSimulations().size(), actual.getSimulations().size());
        for (int i = 0; i < expected.getSimulations().size(); i++) {
            SimulationOptions e = expected.getSimulations().get(i).getOptions();
            SimulationOptions a = actual.getSimulations().get(i).getOptions();
            assertEquals(e.getLaunchAltitude(), a.getLaunchAltitude(), EPSILON);
            assertEquals(e.getLaunchTemperature(), a.getLaunchTemperature(), EPSILON);
            assertEquals(e.getLaunchRodLength(), a.getLaunchRodLength(), EPSILON);
            assertEquals(e.getLaunchRodAngle(), a.getLaunchRodAngle(), EPSILON);
        }
    }

    /**
     * RASAero reads the legacy nose cone shapes "Ogive" and "Conic" as "Tangent Ogive" and "Conical". An unknown
     * shape must give a warning instead of failing the import.
     */
    @Test
    public void testLegacyAndUnknownNoseConeShapes() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        NoseCone ogive = (NoseCone) loadFromString(design.replace("<Shape>Tangent Ogive</Shape>", "<Shape>Ogive</Shape>"))
                .getRocket().getStage(0).getChild(0);
        assertEquals(Transition.Shape.OGIVE, ogive.getShapeType());
        assertEquals(1.0, ogive.getShapeParameter(), EPSILON);

        NoseCone cone = (NoseCone) loadFromString(design.replace("<Shape>Tangent Ogive</Shape>", "<Shape>Conic</Shape>"))
                .getRocket().getStage(0).getChild(0);
        assertEquals(Transition.Shape.CONICAL, cone.getShapeType());

        RASAeroLoader loader = new RASAeroLoader();
        OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new ByteArrayInputStream(
                design.replace("<Shape>Tangent Ogive</Shape>", "<Shape>Foo</Shape>").getBytes(StandardCharsets.UTF_8)),
                null);
        assertTrue(loader.getWarnings().stream().anyMatch(w -> w.toString().contains("Unknown nose cone shape 'Foo'")));
    }

    /**
     * RASAero's LV-Haack nose cone is a Haack series with a shape parameter of 1/3.
     */
    @Test
    public void testLVHaackNoseCone() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        NoseCone noseCone = (NoseCone) loadFromString(
                design.replace("<Shape>Tangent Ogive</Shape>", "<Shape>LV-Haack</Shape>")).getRocket().getStage(0)
                .getChild(0);
        assertEquals(Transition.Shape.HAACK, noseCone.getShapeType());
        assertEquals(1.0 / 3, noseCone.getShapeParameter(), 1e-9);
    }

    /**
     * RASAero's RockSim importer writes the "Round" fin section, which is RASAero's "Rounded". RASAero's sharp-edged
     * sections, which OpenRocket does not have, are approximated as an airfoil with a warning.
     */
    @Test
    public void testFinAirfoilSections() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        String square = "<AirfoilSection>Square</AirfoilSection>";

        RASAeroLoader loader = new RASAeroLoader();
        FinSet round = getFirstFinSet(load(loader,
                design.replaceFirst(square, "<AirfoilSection>Round</AirfoilSection>")).getRocket());
        assertEquals(FinSet.CrossSection.ROUNDED, round.getCrossSection());
        assertFalse(loader.getWarnings().stream().anyMatch(w -> w.toString().contains("cross section")));

        loader = new RASAeroLoader();
        FinSet wedge = getFirstFinSet(load(loader, design.replaceFirst(square,
                "<AirfoilSection>Double Wedge</AirfoilSection>").replaceFirst("<LERadius>0</LERadius>",
                "<LERadius>0.1</LERadius>")).getRocket());
        assertEquals(FinSet.CrossSection.AIRFOIL, wedge.getCrossSection());
        assertTrue(loader.getWarnings().stream()
                .anyMatch(w -> w.toString().contains("Double Wedge is not supported, approximating it as an airfoil")));
        assertTrue(loader.getWarnings().stream().anyMatch(w -> w.toString().contains("leading edge radius")));
    }

    /**
     * A fin can's shoulder is imported from its inside diameter and shoulder length.
     */
    @Test
    public void testFinCanShoulder() throws Exception {
        String design;
        try (InputStream stream = getClass().getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            design = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        Transition shoulder = null;
        for (RocketComponent component : loadFromString(design).getRocket()) {
            if ("Fin Can Shoulder".equals(component.getName())) {
                shoulder = (Transition) component;
            }
        }
        assertNotNull(shoulder);
        assertEquals(0.25 * 0.0254, shoulder.getLength(), EPSILON);
        assertEquals(3 * 0.0254 / 2, shoulder.getForeRadius(), EPSILON);
    }

    private FinSet getFirstFinSet(Rocket rocket) {
        for (RocketComponent component : rocket) {
            if (component instanceof FinSet) {
                return (FinSet) component;
            }
        }
        fail("No fin set found");
        return null;
    }

    private OpenRocketDocument load(RASAeroLoader loader, String design) throws RocketLoadException, IOException {
        OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new ByteArrayInputStream(design.getBytes(StandardCharsets.UTF_8)), null);
        return doc;
    }

    private OpenRocketDocument loadFromString(String design) throws RocketLoadException, IOException {
        OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        new RASAeroLoader().loadFromStream(context,
                new ByteArrayInputStream(design.getBytes(StandardCharsets.UTF_8)), null);
        return doc;
    }
}
