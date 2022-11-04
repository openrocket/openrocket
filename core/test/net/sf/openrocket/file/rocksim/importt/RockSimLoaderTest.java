/*
 * RockSimLoaderTest.java
 *
 */
package net.sf.openrocket.file.rocksim.importt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import org.junit.Assert;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import org.junit.Test;

/**
 * RockSimLoader Tester.
 */
public class RockSimLoaderTest extends BaseTestCase {

    /**
     * Test a bug reported via automated bug report.  I have been unable to reproduce this bug (hanging finset off of an inner body tube) when creating
     * a Rocksim file using Rocksim.  The bug is reproducible when manually modifying the Rocksim file, which is what is tested here.
     */
    @org.junit.Test
    public void testFinsOnInnerTube() throws Exception {
        RockSimLoader loader = new RockSimLoader();
        InputStream stream = this.getClass().getResourceAsStream("PodFins.rkt");
        Assert.assertNotNull("Could not open PodFins.rkt", stream);
        try {
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream));
            Rocket rocket = doc.getRocket();
            Assert.assertNotNull(rocket);
        }
        catch (IllegalStateException ise) {
            Assert.fail(ise.getMessage());
        }
        Assert.assertTrue(loader.getWarnings().size() == 2);
    }

    @Test
    public void testFinsOnTransitions() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "FinsOnTransitions.rkt");

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("FinsOnTransitions", doc.getRocket().getName());
        Assert.assertTrue(loader.getWarnings().isEmpty());

        InputStream stream = this.getClass().getResourceAsStream("FinsOnTransitions.rkt");
        Assert.assertNotNull("Could not open FinsOnTransitions.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        RocketComponent transition1 = stage1.getChild(0);
        RocketComponent transition2 = stage1.getChild(1);
        Assert.assertEquals(" Component should have been transition", Transition.class, transition1.getClass());
        Assert.assertEquals(" Component should have been transition", Transition.class, transition2.getClass());
        Assert.assertEquals("Transition 1", transition1.getName());
        Assert.assertEquals("Transition 2", transition2.getName());
        Assert.assertEquals(1, transition1.getChildCount());
        Assert.assertEquals(1, transition2.getChildCount());

        Assert.assertEquals(" Transition 1 length does not match", 0.075, transition1.getLength(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 1 fore radius does not match", 0.0125,((Transition) transition1).getForeRadius(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 1 aft radius does not match", 0.025, ((Transition) transition1).getAftRadius(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 1 shape does not match", Transition.Shape.CONICAL, ((Transition) transition1).getType());

        Assert.assertEquals(" Transition 2 length does not match", 0.075, transition2.getLength(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 2 fore radius does not match", 0.025,((Transition) transition2).getForeRadius(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 2 aft radius does not match", 0.0125, ((Transition) transition2).getAftRadius(), MathUtil.EPSILON);
        Assert.assertEquals(" Transition 2 shape does not match", Transition.Shape.CONICAL, ((Transition) transition2).getType());

        RocketComponent finSet1 = transition1.getChild(0);
        RocketComponent finSet2 = transition2.getChild(0);
        Assert.assertEquals(" Component should have been free form fin set", FreeformFinSet.class, finSet1.getClass());
        Assert.assertEquals(" Component should have been free form fin set", FreeformFinSet.class, finSet2.getClass());
        Assert.assertEquals("Fin set 1", finSet1.getName());
        Assert.assertEquals("Fin set 2", finSet2.getName());

        FreeformFinSet freeformFinSet1 = (FreeformFinSet) finSet1;
        FreeformFinSet freeformFinSet2 = (FreeformFinSet) finSet2;
        Assert.assertEquals(3, freeformFinSet1.getFinCount());
        Assert.assertEquals(3, freeformFinSet2.getFinCount());

        Coordinate[] points1 =  freeformFinSet1.getFinPoints();
        Coordinate[] expectedPoints1 = new Coordinate[] {
                new Coordinate(0.0, 0.0, 0.0),
                new Coordinate(0.035, 0.03, 0.0),
                new Coordinate(0.07250, 0.03, 0.0),
                new Coordinate(0.07500, 0.01250, 0.0)
        };
        Assert.assertArrayEquals(" Fin set 1 fin points do not match", expectedPoints1, points1);
        Assert.assertEquals(" Fin set 1 fin tab length does not match", 0.05, freeformFinSet1.getTabLength(), MathUtil.EPSILON);
        Assert.assertEquals(" Fin set 1 fin tab height does not match", 0.0075, freeformFinSet1.getTabHeight(), MathUtil.EPSILON);
        Assert.assertEquals(" Fin set 1 fin tab offset does not match", 0.01, freeformFinSet1.getTabOffset(), MathUtil.EPSILON);

        Coordinate[] points2 =  freeformFinSet2.getFinPoints();
        Coordinate[] expectedPoints2 = new Coordinate[] {
                new Coordinate(0.0, 0.0, 0.0),
                new Coordinate(0.025, 0.035, 0.0),
                new Coordinate(0.05, 0.03, 0.0),
                new Coordinate(0.06, -0.01, 0.0)
        };
        Assert.assertArrayEquals(" Fin set 2 fin points do not match", expectedPoints2, points2);
        Assert.assertEquals(" Fin set 2 fin tab length does not match", 0.03, freeformFinSet2.getTabLength(), MathUtil.EPSILON);
        Assert.assertEquals(" Fin set 2 fin tab height does not match", 0.005, freeformFinSet2.getTabHeight(), MathUtil.EPSILON);
        Assert.assertEquals(" Fin set 2 fin tab offset does not match", 0, freeformFinSet2.getTabOffset(), MathUtil.EPSILON);
    }

    /**
     * Method: loadFromStream(InputStream source)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testLoadFromStream() throws Exception {
        RockSimLoader loader = new RockSimLoader();
        //Stupid single stage rocket
        OpenRocketDocument doc = loadRockSimRocket(loader, "rocksimTestRocket1.rkt");
        InputStream stream;

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("FooBar Test", doc.getRocket().getName());
        Assert.assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("rocksimTestRocket2.rkt");
        Assert.assertNotNull("Could not open rocksimTestRocket2.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);

        //Do some simple asserts;  the important thing here is just validating that the mass and cg were
        //not overridden for each stage.
        Assert.assertEquals("Three Stage Everything Included Rocket", doc.getRocket().getName());
        Assert.assertEquals(0, loader.getWarnings().size());
        Assert.assertEquals(3, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);
        Assert.assertFalse(stage1.isMassOverridden());
        Assert.assertFalse(stage1.isCGOverridden());
        AxialStage stage2 = (AxialStage) rocket.getChild(1);
        Assert.assertFalse(stage2.isMassOverridden());
        Assert.assertFalse(stage2.isCGOverridden());
        AxialStage stage3 = (AxialStage) rocket.getChild(2);
        Assert.assertFalse(stage3.isMassOverridden());
        Assert.assertFalse(stage3.isCGOverridden());

        stream = this.getClass().getResourceAsStream("rocksimTestRocket3.rkt");
        Assert.assertNotNull("Could not open rocksimTestRocket3.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("Three Stage Everything Included Rocket - Override Total Mass/CG", doc.getRocket().getName());
        Assert.assertEquals(3, rocket.getStageCount());
        stage1 = (AxialStage) rocket.getChild(0);
        stage2 = (AxialStage) rocket.getChild(1);
        stage3 = (AxialStage) rocket.getChild(2);

        //Do some 1st level and simple asserts; the idea here is to not do a deep validation as that
        //should have been covered elsewhere.  Assert that the stage overrides are correct.
        Assert.assertEquals(2, stage1.getChildCount());
        Assert.assertEquals("Nose cone", stage1.getChild(0).getName());
        Assert.assertEquals("Body tube", stage1.getChild(1).getName());
        Assert.assertTrue(stage1.isMassOverridden());
        Assert.assertEquals(0.185d, stage1.getOverrideMass(), 0.001);
        Assert.assertTrue(stage1.isCGOverridden());
        Assert.assertEquals(0.3d, stage1.getOverrideCG().x, 0.001);
        Assert.assertEquals(2, loader.getWarnings().size());

        NoseCone nc = (NoseCone) stage1.getChild(0);
        Assert.assertEquals(2, nc.getChildCount());
        Assert.assertEquals("Clay", nc.getChild(0).getName());
        RocketComponent it = nc.getChild(1);
        Assert.assertEquals(InnerTube.class, it.getClass());
        Assert.assertEquals("Attachment Rod", it.getName());

        Assert.assertEquals(3, it.getChildCount());
        RocketComponent c = it.getChild(0);
        Assert.assertEquals(CenteringRing.class, c.getClass());
        Assert.assertEquals("Plate", c.getName());
        c = it.getChild(1);
        Assert.assertEquals(CenteringRing.class, c.getClass());
        Assert.assertEquals("Sleeve ", c.getName());
        c = it.getChild(2);
        Assert.assertEquals(Parachute.class, c.getClass());
        Assert.assertEquals("Nose Cone Parachute", c.getName());

        BodyTube bt1 = (BodyTube) stage1.getChild(1);
        Assert.assertEquals(5, bt1.getChildCount());
        Assert.assertEquals("Centering ring", bt1.getChild(0).getName());
        Assert.assertEquals("Centering ring", bt1.getChild(1).getName());
        c = bt1.getChild(2);
        Assert.assertEquals(InnerTube.class, c.getClass());
        Assert.assertEquals("Body tube", c.getName());
        Assert.assertEquals("Launch lug", bt1.getChild(3).getName());
        Assert.assertEquals("Pod", bt1.getChild(4).getName());

        PodSet pod = (PodSet) bt1.getChild(4);
        Assert.assertEquals(1, pod.getChildCount());
        c = pod.getChild(0);
        Assert.assertEquals(BodyTube.class, c.getClass());
        Assert.assertEquals("Body tube", pod.getChild(0).getName());

        Assert.assertEquals(1, stage2.getChildCount());
        Assert.assertEquals("2nd Stage Tube", stage2.getChild(0).getName());
        Assert.assertTrue(stage2.isMassOverridden());
        Assert.assertEquals(0.21d, stage2.getOverrideMass(), 0.001);
        Assert.assertTrue(stage2.isCGOverridden());
        Assert.assertEquals(0.4d, stage2.getOverrideCG().x, 0.001);

        BodyTube bt2 = (BodyTube) stage2.getChild(0);
        LaunchLug ll = (LaunchLug) bt2.getChild(6);
        Assert.assertEquals(1.22d, ll.getAngleOffset(), 0.001);

        Assert.assertEquals(2, stage3.getChildCount());
        Assert.assertEquals("Transition", stage3.getChild(0).getName());
        Assert.assertEquals("Body tube", stage3.getChild(1).getName());
        Assert.assertTrue(stage2.isMassOverridden());
        Assert.assertEquals(0.33d, stage3.getOverrideMass(), 0.001);
        Assert.assertTrue(stage2.isCGOverridden());
        Assert.assertEquals(0.5d, stage3.getOverrideCG().x, 0.001);
    }

    @org.junit.Test
    public void testBodyTubeChildrenRocket() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        //Stupid single stage rocket
        OpenRocketDocument doc = loadRockSimRocket(loader, "BodyTubeChildrenTest.rkt");
        InputStream stream;

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("Body Tube Children Test", doc.getRocket().getName());
        Assert.assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("BodyTubeChildrenTest.rkt");
        Assert.assertNotNull("Could not open BodyTubeChildrenTest.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);
        Assert.assertEquals("Nose cone", stage1.getChild(0).getName());
        Assert.assertEquals("Forward Body tube", stage1.getChild(1).getName());
        Assert.assertEquals("Aft Body tube", stage1.getChild(2).getName());

        BodyTube subassemblyBodyTube = (BodyTube)stage1.getChild(2);
        Assert.assertEquals(8, subassemblyBodyTube.getChildCount());
        Assert.assertEquals("Engine block", subassemblyBodyTube.getChild(0).getName());
        Assert.assertEquals("Fin set-1", subassemblyBodyTube.getChild(1).getName());
        Assert.assertEquals("Fin set", subassemblyBodyTube.getChild(2).getName());
        Assert.assertEquals("Fin set-2", subassemblyBodyTube.getChild(3).getName());
        Assert.assertEquals("Fin set-3", subassemblyBodyTube.getChild(4).getName());
        Assert.assertEquals("Fin set-4", subassemblyBodyTube.getChild(5).getName());
        Assert.assertEquals("Centering ring", subassemblyBodyTube.getChild(6).getName());
        Assert.assertEquals("Centering ring", subassemblyBodyTube.getChild(7).getName());
    }

    @Test
    public void testSubAssemblyRocket() throws IOException, RocketLoadException {
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "SubAssemblyTest.rkt");

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("SubAssembly Test", doc.getRocket().getName());
        Assert.assertEquals(2, loader.getWarnings().size());    // can't add BodyTube to NoseCone, and can't add Transition to Transition

        InputStream stream = this.getClass().getResourceAsStream("SubAssemblyTest.rkt");
        Assert.assertNotNull("Could not open SubAssemblyTest.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        Assert.assertEquals(5, stage1.getChildCount());
        NoseCone noseCone1 = (NoseCone) stage1.getChild(0);
        BodyTube bodyTube2 = (BodyTube) stage1.getChild(1);
        Transition transition1 = (Transition) stage1.getChild(2);
        Transition transition3 = (Transition) stage1.getChild(3);
        BodyTube bodyTube3 = (BodyTube) stage1.getChild(4);
        Assert.assertEquals("Nose cone 1", noseCone1.getName());
        Assert.assertEquals("Body tube 2", bodyTube2.getName());
        Assert.assertEquals("Transition 1", transition1.getName());
        Assert.assertEquals("Transition 3", transition3.getName());
        Assert.assertEquals("Body tube 3", bodyTube3.getName());

        Assert.assertEquals(1, noseCone1.getChildCount());
        Assert.assertEquals("Mass object 1", noseCone1.getChild(0).getName());

        Assert.assertEquals(12, bodyTube2.getChildCount());
        Assert.assertEquals("Mass object 2", bodyTube2.getChild(0).getName());
        Assert.assertEquals("Launch lug 1", bodyTube2.getChild(1).getName());
        Assert.assertEquals("Centering ring 1", bodyTube2.getChild(2).getName());
        Assert.assertEquals("Tube coupler 1", bodyTube2.getChild(3).getName());
        Assert.assertEquals("Fin set 1", bodyTube2.getChild(4).getName());
        Assert.assertEquals("Fin set 2", bodyTube2.getChild(5).getName());
        Assert.assertEquals("Parachute 1", bodyTube2.getChild(6).getName());
        Assert.assertEquals("Streamer 1", bodyTube2.getChild(7).getName());
        Assert.assertEquals("Bulkhead 1", bodyTube2.getChild(8).getName());
        Assert.assertEquals("Engine block 1", bodyTube2.getChild(9).getName());
        Assert.assertEquals("Tube fins 1", bodyTube2.getChild(10).getName());
        Assert.assertEquals("Sleeve 1", bodyTube2.getChild(11).getName());

        Assert.assertEquals(3, transition1.getChildCount());
        Assert.assertEquals("Mass object 3", transition1.getChild(0).getName());
        Assert.assertEquals("Fin set 3", transition1.getChild(1).getName());
        Assert.assertEquals("Fin set 4", transition1.getChild(2).getName());

        Assert.assertEquals(0, transition3.getChildCount());

        Assert.assertEquals(0, bodyTube3.getChildCount());
    }

    @Test
    public void testPodRocket() throws IOException, RocketLoadException{
        RockSimLoader loader = new RockSimLoader();
        OpenRocketDocument doc = loadRockSimRocket(loader, "PodTest.rkt");

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("Pod Test", doc.getRocket().getName());
        Assert.assertEquals(3, loader.getWarnings().size());

        InputStream stream = this.getClass().getResourceAsStream("PodTest.rkt");
        Assert.assertNotNull("Could not open PodTest.rkt", stream);

        doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new BufferedInputStream(stream));

        Assert.assertNotNull(doc);
        rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals(1, rocket.getStageCount());
        AxialStage stage1 = (AxialStage) rocket.getChild(0);

        Assert.assertEquals(3, stage1.getChildCount());
        RocketComponent noseCone1 = stage1.getChild(0);
        RocketComponent bodyTube1 = stage1.getChild(1);
        RocketComponent transition1 = stage1.getChild(2);
        Assert.assertEquals(NoseCone.class, noseCone1.getClass());
        Assert.assertEquals(BodyTube.class, bodyTube1.getClass());
        Assert.assertEquals(Transition.class, transition1.getClass());
        Assert.assertEquals("Nose cone 1", noseCone1.getName());
        Assert.assertEquals("Body tube 1", bodyTube1.getName());
        Assert.assertEquals("Transition 1", transition1.getName());

        Assert.assertEquals(1, noseCone1.getChildCount());
        RocketComponent component = noseCone1.getChild(0);
        Assert.assertEquals(MassComponent.class, component.getClass());
        Assert.assertEquals("Mass object 1", component.getName());

        Assert.assertEquals(2, bodyTube1.getChildCount());
        RocketComponent pod2 = bodyTube1.getChild(0);
        Assert.assertEquals(PodSet.class, pod2.getClass());
        Assert.assertEquals("Pod 2", pod2.getName());
        component = bodyTube1.getChild(1);
        Assert.assertEquals(Bulkhead.class, component.getClass());
        Assert.assertEquals("Bulkhead 1", component.getName());

        Assert.assertEquals(3, pod2.getChildCount());
        RocketComponent noseCone2 = pod2.getChild(0);
        Assert.assertEquals(NoseCone.class, noseCone2.getClass());
        Assert.assertEquals("Nose cone 2", noseCone2.getName());
        RocketComponent bodyTube2 = pod2.getChild(1);
        Assert.assertEquals(BodyTube.class, bodyTube2.getClass());
        Assert.assertEquals("Body tube 2", bodyTube2.getName());
        component = pod2.getChild(2);
        Assert.assertEquals(Transition.class, component.getClass());
        Assert.assertEquals("Transition 2", component.getName());

        Assert.assertEquals(1, noseCone2.getChildCount());
        component = noseCone2.getChild(0);
        Assert.assertEquals(MassComponent.class, component.getClass());
        Assert.assertEquals("Mass object 2", component.getName());

        Assert.assertEquals(3, bodyTube2.getChildCount());
        component = bodyTube2.getChild(0);
        Assert.assertEquals(TrapezoidFinSet.class, component.getClass());
        Assert.assertEquals("Fin set 2", component.getName());
        RocketComponent pod3 = bodyTube2.getChild(1);
        Assert.assertEquals(PodSet.class, pod3.getClass());
        Assert.assertEquals("Pod 3", pod3.getName());
        component = bodyTube2.getChild(2);
        Assert.assertEquals(LaunchLug.class, component.getClass());
        Assert.assertEquals("Launch lug 1", component.getName());

        Assert.assertEquals(1, pod3.getChildCount());
        component = pod3.getChild(0);
        Assert.assertEquals(BodyTube.class, component.getClass());
        Assert.assertEquals("Body tube 3", component.getName());
        Assert.assertEquals(0.04, pod3.getAxialOffset(), MathUtil.EPSILON);
        Assert.assertEquals(Math.PI / 2, pod3.getAngleOffset(), 0.0001);
        Assert.assertEquals(0.05, pod3.getRadiusOffset(), MathUtil.EPSILON);

        Assert.assertEquals(1, transition1.getChildCount());
        component = transition1.getChild(0);
        Assert.assertEquals(MassComponent.class, component.getClass());
        Assert.assertEquals("Mass object 3", component.getName());
    }

    public static OpenRocketDocument loadRockSimRocket(RockSimLoader theLoader, String fileName) throws IOException, RocketLoadException {
        try (InputStream stream = RockSimLoaderTest.class.getResourceAsStream(fileName)) {
            Assert.assertNotNull("Could not open " + fileName, stream);
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            theLoader.loadFromStream(context, new BufferedInputStream(stream));
            return doc;
        }
    }
}
