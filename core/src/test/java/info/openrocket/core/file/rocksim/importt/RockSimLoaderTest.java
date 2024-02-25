/*
 * RockSimLoaderTest.java
 *
 */
package info.openrocket.core.file.rocksim.importt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.Assertions;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

/**
 * RockSimLoader Tester.
 */
public class RockSimLoaderTest extends BaseTestCase {

    /**
     * Test a bug reported via automated bug report. I have been unable to reproduce
     * this bug (hanging finset off of an inner body tube) when creating
     * a Rocksim file using Rocksim. The bug is reproducible when manually modifying
     * the Rocksim file, which is what is tested here.
     */
    @org.junit.jupiter.api.Test
    public void testFinsOnInnerTube() throws Exception {
        RockSimLoader loader = new RockSimLoader();
        InputStream stream = this.getClass().getResourceAsStream("/file/rocksim/importt/PodFins.rkt");
        Assertions.assertNotNull(stream, "Could not open PodFins.rkt");
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket rocket = doc.getRocket();
            Assertions.assertNotNull(rocket);
        } catch (IllegalStateException ise) {
            Assertions.fail(ise.getMessage());
        }
        Assertions.assertTrue(loader.getWarnings().size() == 2);
    }

    @Test
    public void testFinsOnTransitions() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "FinsOnTransitions.rkt");

        Assertions.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "FinsOnTransitions");
        Assertions.assertTrue(loader.getWarnings().isEmpty());

        InputStream stream = this.getClass().getResourceAsStream("/file/rocksim/importt/FinsOnTransitions.rkt");
        Assertions.assertNotNull(stream, "Could not open FinsOnTransitions.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        RocketComponent transition1 = stage1.getChild(0);
        RocketComponent transition2 = stage1.getChild(1);
        Assertions.assertEquals(Transition.class, transition1.getClass(), " Component should have been transition");
        Assertions.assertEquals(Transition.class, transition2.getClass(), " Component should have been transition");
        Assertions.assertEquals(transition1.getName(), "Transition 1");
        Assertions.assertEquals(transition2.getName(), "Transition 2");
        Assertions.assertEquals(1, transition1.getChildCount());
        Assertions.assertEquals(1, transition2.getChildCount());

        Assertions.assertEquals(0.075, transition1.getLength(), MathUtil.EPSILON, " Transition 1 length does not match");
        Assertions.assertEquals(0.0125,
                ((Transition) transition1).getForeRadius(), MathUtil.EPSILON, " Transition 1 fore radius does not match");
        Assertions.assertEquals(0.025, ((Transition) transition1).getAftRadius(),
                MathUtil.EPSILON, " Transition 1 aft radius does not match");
        Assertions.assertEquals(Transition.Shape.CONICAL,
                ((Transition) transition1).getShapeType(), " Transition 1 shape does not match");

        Assertions.assertEquals(0.075, transition2.getLength(), MathUtil.EPSILON, " Transition 2 length does not match");
        Assertions.assertEquals(0.025,
                ((Transition) transition2).getForeRadius(), MathUtil.EPSILON, " Transition 2 fore radius does not match");
        Assertions.assertEquals(0.0125,
                ((Transition) transition2).getAftRadius(), MathUtil.EPSILON, " Transition 2 aft radius does not match");
        Assertions.assertEquals(Transition.Shape.CONICAL,
                ((Transition) transition2).getShapeType(), " Transition 2 shape does not match");

        RocketComponent finSet1 = transition1.getChild(0);
        RocketComponent finSet2 = transition2.getChild(0);
        Assertions.assertEquals(FreeformFinSet.class, finSet1.getClass(), " Component should have been free form fin set");
        Assertions.assertEquals(FreeformFinSet.class, finSet2.getClass(), " Component should have been free form fin set");
        Assertions.assertEquals(finSet1.getName(), "Fin set 1");
        Assertions.assertEquals(finSet2.getName(), "Fin set 2");

        FreeformFinSet freeformFinSet1 = (FreeformFinSet) finSet1;
        FreeformFinSet freeformFinSet2 = (FreeformFinSet) finSet2;
        Assertions.assertEquals(3, freeformFinSet1.getFinCount());
        Assertions.assertEquals(3, freeformFinSet2.getFinCount());

        Coordinate[] points1 = freeformFinSet1.getFinPoints();
        Coordinate[] expectedPoints1 = new Coordinate[] {
                new Coordinate(0.0, 0.0, 0.0),
                new Coordinate(0.035, 0.03, 0.0),
                new Coordinate(0.07250, 0.03, 0.0),
                new Coordinate(0.07500, 0.01250, 0.0)
        };
        Assertions.assertArrayEquals(expectedPoints1, points1, " Fin set 1 fin points do not match");
        Assertions.assertEquals(0.05, freeformFinSet1.getTabLength(),
                MathUtil.EPSILON, " Fin set 1 fin tab length does not match");
        Assertions.assertEquals(0.0075, freeformFinSet1.getTabHeight(),
                MathUtil.EPSILON, " Fin set 1 fin tab height does not match");
        Assertions.assertEquals(0.01, freeformFinSet1.getTabOffset(),
                MathUtil.EPSILON, " Fin set 1 fin tab offset does not match");

        Coordinate[] points2 = freeformFinSet2.getFinPoints();
        Coordinate[] expectedPoints2 = new Coordinate[] {
                new Coordinate(0.0, 0.0, 0.0),
                new Coordinate(0.025, 0.035, 0.0),
                new Coordinate(0.05, 0.03, 0.0),
                new Coordinate(0.06, -0.01, 0.0)
        };
        Assertions.assertArrayEquals(expectedPoints2, points2, " Fin set 2 fin points do not match");
        Assertions.assertEquals(0.03, freeformFinSet2.getTabLength(),
                MathUtil.EPSILON, " Fin set 2 fin tab length does not match");
        Assertions.assertEquals(0.005, freeformFinSet2.getTabHeight(),
                MathUtil.EPSILON, " Fin set 2 fin tab height does not match");
        Assertions.assertEquals(0, freeformFinSet2.getTabOffset(),
                MathUtil.EPSILON, " Fin set 2 fin tab offset does not match");
    }

    /**
     * Method: loadFromStream(InputStream source)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.jupiter.api.Test
    public void testLoadFromStream() throws Exception {
        RockSimLoader loader = new RockSimLoader();
        // Stupid single stage rocket
        OpenRocketDocument doc = loadRockSimRocket(loader, "rocksimTestRocket1.rkt");
        InputStream stream;

        Assertions.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "FooBar Test");
        Assertions.assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("/file/rocksim/importt/rocksimTestRocket2.rkt");
        Assertions.assertNotNull(stream, "Could not open rocksimTestRocket2.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);

        // Do some simple asserts; the important thing here is just validating that the
        // mass and cg were
        // not overridden for each stage.
        Assertions.assertEquals(doc.getRocket().getName(), "Three Stage Everything Included Rocket");
        Assertions.assertEquals(0, loader.getWarnings().size());
        Assertions.assertEquals(3, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);
        Assertions.assertFalse(stage1.isMassOverridden());
        Assertions.assertFalse(stage1.isCGOverridden());
        AxialStage stage2 = (AxialStage) rocket.getChild(1);
        Assertions.assertFalse(stage2.isMassOverridden());
        Assertions.assertFalse(stage2.isCGOverridden());
        AxialStage stage3 = (AxialStage) rocket.getChild(2);
        Assertions.assertFalse(stage3.isMassOverridden());
        Assertions.assertFalse(stage3.isCGOverridden());

        stream = this.getClass().getResourceAsStream("/file/rocksim/importt/rocksimTestRocket3.rkt");
        Assertions.assertNotNull(stream, "Could not open rocksimTestRocket3.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "Three Stage Everything Included Rocket - Override Total Mass/CG");
        Assertions.assertEquals(3, rocket.getStageCount());
        stage1 = (AxialStage) rocket.getChild(0);
        stage2 = (AxialStage) rocket.getChild(1);
        stage3 = (AxialStage) rocket.getChild(2);

        // Do some 1st level and simple asserts; the idea here is to not do a deep
        // validation as that
        // should have been covered elsewhere. Assert that the stage overrides are
        // correct.
        Assertions.assertEquals(2, stage1.getChildCount());
        Assertions.assertEquals(stage1.getChild(0).getName(), "Nose cone");
        Assertions.assertEquals(stage1.getChild(1).getName(), "Body tube");
        Assertions.assertTrue(stage1.isMassOverridden());
        Assertions.assertEquals(0.185d, stage1.getOverrideMass(), 0.001);
        Assertions.assertTrue(stage1.isCGOverridden());
        Assertions.assertEquals(0.3d, stage1.getOverrideCG().x, 0.001);
        Assertions.assertEquals(2, loader.getWarnings().size());

        NoseCone nc = (NoseCone) stage1.getChild(0);
        Assertions.assertEquals(2, nc.getChildCount());
        Assertions.assertEquals(nc.getChild(0).getName(), "Clay");
        RocketComponent it = nc.getChild(1);
        Assertions.assertEquals(InnerTube.class, it.getClass());
        Assertions.assertEquals(it.getName(), "Attachment Rod");

        Assertions.assertEquals(3, it.getChildCount());
        RocketComponent c = it.getChild(0);
        Assertions.assertEquals(CenteringRing.class, c.getClass());
        Assertions.assertEquals(c.getName(), "Plate");
        c = it.getChild(1);
        Assertions.assertEquals(CenteringRing.class, c.getClass());
        Assertions.assertEquals(c.getName(), "Sleeve ");
        c = it.getChild(2);
        Assertions.assertEquals(Parachute.class, c.getClass());
        Assertions.assertEquals(c.getName(), "Nose Cone Parachute");

        BodyTube bt1 = (BodyTube) stage1.getChild(1);
        Assertions.assertEquals(5, bt1.getChildCount());
        Assertions.assertEquals(bt1.getChild(0).getName(), "Centering ring");
        Assertions.assertEquals(bt1.getChild(1).getName(), "Centering ring");
        c = bt1.getChild(2);
        Assertions.assertEquals(InnerTube.class, c.getClass());
        Assertions.assertEquals(c.getName(), "Body tube");
        Assertions.assertEquals(bt1.getChild(3).getName(), "Launch lug");
        Assertions.assertEquals(bt1.getChild(4).getName(), "Pod");

        PodSet pod = (PodSet) bt1.getChild(4);
        Assertions.assertEquals(1, pod.getChildCount());
        c = pod.getChild(0);
        Assertions.assertEquals(BodyTube.class, c.getClass());
        Assertions.assertEquals(pod.getChild(0).getName(), "Body tube");

        Assertions.assertEquals(1, stage2.getChildCount());
        Assertions.assertEquals(stage2.getChild(0).getName(), "2nd Stage Tube");
        Assertions.assertTrue(stage2.isMassOverridden());
        Assertions.assertEquals(0.21d, stage2.getOverrideMass(), 0.001);
        Assertions.assertTrue(stage2.isCGOverridden());
        Assertions.assertEquals(0.4d, stage2.getOverrideCG().x, 0.001);

        BodyTube bt2 = (BodyTube) stage2.getChild(0);
        LaunchLug ll = (LaunchLug) bt2.getChild(6);
        Assertions.assertEquals(1.22d, ll.getAngleOffset(), 0.001);

        Assertions.assertEquals(2, stage3.getChildCount());
        Assertions.assertEquals(stage3.getChild(0).getName(), "Transition");
        Assertions.assertEquals(stage3.getChild(1).getName(), "Body tube");
        Assertions.assertTrue(stage2.isMassOverridden());
        Assertions.assertEquals(0.33d, stage3.getOverrideMass(), 0.001);
        Assertions.assertTrue(stage2.isCGOverridden());
        Assertions.assertEquals(0.5d, stage3.getOverrideCG().x, 0.001);
    }

    @org.junit.jupiter.api.Test
    public void testBodyTubeChildrenRocket() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        // Stupid single stage rocket
        OpenRocketDocument doc = loadRockSimRocket(loader, "BodyTubeChildrenTest.rkt");
        InputStream stream;

        Assertions.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "Body Tube Children Test");
        Assertions.assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("/file/rocksim/importt/BodyTubeChildrenTest.rkt");
        Assertions.assertNotNull(stream, "Could not open BodyTubeChildrenTest.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);
        Assertions.assertEquals(stage1.getChild(0).getName(), "Nose cone");
        Assertions.assertEquals(stage1.getChild(1).getName(), "Forward Body tube");
        Assertions.assertEquals(stage1.getChild(2).getName(), "Aft Body tube");

        BodyTube subassemblyBodyTube = (BodyTube) stage1.getChild(2);
        Assertions.assertEquals(8, subassemblyBodyTube.getChildCount());
        Assertions.assertEquals(subassemblyBodyTube.getChild(0).getName(), "Engine block");
        Assertions.assertEquals(subassemblyBodyTube.getChild(1).getName(), "Fin set-1");
        Assertions.assertEquals(subassemblyBodyTube.getChild(2).getName(), "Fin set");
        Assertions.assertEquals(subassemblyBodyTube.getChild(3).getName(), "Fin set-2");
        Assertions.assertEquals(subassemblyBodyTube.getChild(4).getName(), "Fin set-3");
        Assertions.assertEquals(subassemblyBodyTube.getChild(5).getName(), "Fin set-4");
        Assertions.assertEquals(subassemblyBodyTube.getChild(6).getName(), "Centering ring");
        Assertions.assertEquals(subassemblyBodyTube.getChild(7).getName(), "Centering ring");
    }

    @Test
    public void testSubAssemblyRocket() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "SubAssemblyTest.rkt");

        Assertions.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "SubAssembly Test");
        Assertions.assertEquals(2, loader.getWarnings().size()); // can't add BodyTube to NoseCone, and can't add Transition
                                                             // to Transition

        InputStream stream = this.getClass().getResourceAsStream("/file/rocksim/importt/SubAssemblyTest.rkt");
        Assertions.assertNotNull(stream, "Could not open SubAssemblyTest.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        Assertions.assertEquals(5, stage1.getChildCount());
        NoseCone noseCone1 = (NoseCone) stage1.getChild(0);
        BodyTube bodyTube2 = (BodyTube) stage1.getChild(1);
        Transition transition1 = (Transition) stage1.getChild(2);
        Transition transition3 = (Transition) stage1.getChild(3);
        BodyTube bodyTube3 = (BodyTube) stage1.getChild(4);
        Assertions.assertEquals(noseCone1.getName(), "Nose cone 1");
        Assertions.assertEquals(bodyTube2.getName(), "Body tube 2");
        Assertions.assertEquals(transition1.getName(), "Transition 1");
        Assertions.assertEquals(transition3.getName(), "Transition 3");
        Assertions.assertEquals(bodyTube3.getName(), "Body tube 3");

        Assertions.assertEquals(1, noseCone1.getChildCount());
        Assertions.assertEquals(noseCone1.getChild(0).getName(), "Mass object 1");

        Assertions.assertEquals(12, bodyTube2.getChildCount());
        Assertions.assertEquals(bodyTube2.getChild(0).getName(), "Mass object 2");
        Assertions.assertEquals(bodyTube2.getChild(1).getName(), "Launch lug 1");
        Assertions.assertEquals(bodyTube2.getChild(2).getName(), "Centering ring 1");
        Assertions.assertEquals(bodyTube2.getChild(3).getName(), "Tube coupler 1");
        Assertions.assertEquals(bodyTube2.getChild(4).getName(), "Fin set 1");
        Assertions.assertEquals(bodyTube2.getChild(5).getName(), "Fin set 2");
        Assertions.assertEquals(bodyTube2.getChild(6).getName(), "Parachute 1");
        Assertions.assertEquals(bodyTube2.getChild(7).getName(), "Streamer 1");
        Assertions.assertEquals(bodyTube2.getChild(8).getName(), "Bulkhead 1");
        Assertions.assertEquals(bodyTube2.getChild(9).getName(), "Engine block 1");
        Assertions.assertEquals(bodyTube2.getChild(10).getName(), "Tube fins 1");
        Assertions.assertEquals(bodyTube2.getChild(11).getName(), "Sleeve 1");

        Assertions.assertEquals(3, transition1.getChildCount());
        Assertions.assertEquals(transition1.getChild(0).getName(), "Mass object 3");
        Assertions.assertEquals(transition1.getChild(1).getName(), "Fin set 3");
        Assertions.assertEquals(transition1.getChild(2).getName(), "Fin set 4");

        Assertions.assertEquals(0, transition3.getChildCount());

        Assertions.assertEquals(0, bodyTube3.getChildCount());
    }

    @Test
    public void testPodRocket() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "PodTest.rkt");

        Assertions.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(doc.getRocket().getName(), "Pod Test");
        Assertions.assertEquals(3, loader.getWarnings().size());

        InputStream stream = this.getClass().getResourceAsStream("/file/rocksim/importt/PodTest.rkt");
        Assertions.assertNotNull(stream, "Could not open PodTest.rkt");

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream), null);

        Assertions.assertNotNull(doc);
        rocket = doc.getRocket();
        Assertions.assertNotNull(rocket);
        Assertions.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        Assertions.assertEquals(3, stage1.getChildCount());
        RocketComponent noseCone1 = stage1.getChild(0);
        RocketComponent bodyTube1 = stage1.getChild(1);
        RocketComponent transition1 = stage1.getChild(2);
        Assertions.assertEquals(NoseCone.class, noseCone1.getClass());
        Assertions.assertEquals(BodyTube.class, bodyTube1.getClass());
        Assertions.assertEquals(Transition.class, transition1.getClass());
        Assertions.assertEquals(noseCone1.getName(), "Nose cone 1");
        Assertions.assertEquals(bodyTube1.getName(), "Body tube 1");
        Assertions.assertEquals(transition1.getName(), "Transition 1");

        Assertions.assertEquals(1, noseCone1.getChildCount());
        RocketComponent component = noseCone1.getChild(0);
        Assertions.assertEquals(MassComponent.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Mass object 1");

        Assertions.assertEquals(2, bodyTube1.getChildCount());
        RocketComponent pod2 = bodyTube1.getChild(0);
        Assertions.assertEquals(PodSet.class, pod2.getClass());
        Assertions.assertEquals(pod2.getName(), "Pod 2");
        component = bodyTube1.getChild(1);
        Assertions.assertEquals(Bulkhead.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Bulkhead 1");

        Assertions.assertEquals(3, pod2.getChildCount());
        RocketComponent noseCone2 = pod2.getChild(0);
        Assertions.assertEquals(NoseCone.class, noseCone2.getClass());
        Assertions.assertEquals(noseCone2.getName(), "Nose cone 2");
        RocketComponent bodyTube2 = pod2.getChild(1);
        Assertions.assertEquals(BodyTube.class, bodyTube2.getClass());
        Assertions.assertEquals(bodyTube2.getName(), "Body tube 2");
        component = pod2.getChild(2);
        Assertions.assertEquals(Transition.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Transition 2");

        Assertions.assertEquals(1, noseCone2.getChildCount());
        component = noseCone2.getChild(0);
        Assertions.assertEquals(MassComponent.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Mass object 2");

        Assertions.assertEquals(3, bodyTube2.getChildCount());
        component = bodyTube2.getChild(0);
        Assertions.assertEquals(TrapezoidFinSet.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Fin set 2");
        RocketComponent pod3 = bodyTube2.getChild(1);
        Assertions.assertEquals(PodSet.class, pod3.getClass());
        Assertions.assertEquals(pod3.getName(), "Pod 3");
        component = bodyTube2.getChild(2);
        Assertions.assertEquals(LaunchLug.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Launch lug 1");

        Assertions.assertEquals(1, pod3.getChildCount());
        component = pod3.getChild(0);
        Assertions.assertEquals(BodyTube.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Body tube 3");
        Assertions.assertEquals(0.04, pod3.getAxialOffset(), MathUtil.EPSILON);
        Assertions.assertEquals(Math.PI / 2, pod3.getAngleOffset(), 0.0001);
        Assertions.assertEquals(0.05, pod3.getRadiusOffset(), MathUtil.EPSILON);

        Assertions.assertEquals(1, transition1.getChildCount());
        component = transition1.getChild(0);
        Assertions.assertEquals(MassComponent.class, component.getClass());
        Assertions.assertEquals(component.getName(), "Mass object 3");
    }

    public static OpenRocketDocument loadRockSimRocket(RockSimLoader theLoader, String fileName)
            throws IOException, RocketLoadException {
        try (InputStream stream = RockSimLoaderTest.class.getResourceAsStream("/file/rocksim/importt/" + fileName)) {
            Assertions.assertNotNull(stream, "Could not open " + fileName);
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            theLoader.loadFromStream(context, new BufferedInputStream(stream), null);
            return doc;
        }
    }
}
