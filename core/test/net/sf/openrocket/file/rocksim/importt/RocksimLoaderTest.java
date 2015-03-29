/*
 * RocksimLoaderTest.java
 *
 */
package net.sf.openrocket.file.rocksim.importt;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import org.junit.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * RocksimLoader Tester.
 */
public class RocksimLoaderTest extends BaseTestCase {

    /**
     * Test a bug reported via automated bug report.  I have been unable to reproduce this bug (hanging finset off of an inner body tube) when creating
     * a Rocksim file using Rocksim.  The bug is reproducible when manually modifying the Rocksim file, which is what is tested here.
     */
    @org.junit.Test
    public void testFinsOnInnerTube() throws Exception {
        RocksimLoader loader = new RocksimLoader();
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

    /**
     * Method: loadFromStream(InputStream source)
     *
     * @throws Exception thrown if something goes awry
     */
    @org.junit.Test
    public void testLoadFromStream() throws Exception {
        RocksimLoader loader = new RocksimLoader();
        //Stupid single stage rocket
        OpenRocketDocument doc = loadRocksimRocket(loader);
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
        Stage stage1 = (Stage) rocket.getChild(0);
        Assert.assertFalse(stage1.isMassOverridden());
        Assert.assertFalse(stage1.isCGOverridden());
        Stage stage2 = (Stage) rocket.getChild(1);
        Assert.assertFalse(stage2.isMassOverridden());
        Assert.assertFalse(stage2.isCGOverridden());
        Stage stage3 = (Stage) rocket.getChild(2);
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
        stage1 = (Stage) rocket.getChild(0);
        stage2 = (Stage) rocket.getChild(1);
        stage3 = (Stage) rocket.getChild(2);

        //Do some 1st level and simple asserts; the idea here is to not do a deep validation as that
        //should have been covered elsewhere.  Assert that the stage overrides are correct.
        Assert.assertEquals(2, stage1.getChildCount());
        Assert.assertEquals("Nose cone", stage1.getChild(0).getName());
        Assert.assertEquals("Body tube", stage1.getChild(1).getName());
        Assert.assertTrue(stage1.isMassOverridden());
        Assert.assertEquals(0.185d, stage1.getOverrideMass(), 0.001);
        Assert.assertTrue(stage1.isCGOverridden());
        Assert.assertEquals(0.3d, stage1.getOverrideCG().x, 0.001);
        Assert.assertEquals(3, loader.getWarnings().size());

        Assert.assertEquals(1, stage2.getChildCount());
        Assert.assertEquals("2nd Stage Tube", stage2.getChild(0).getName());
        Assert.assertTrue(stage2.isMassOverridden());
        Assert.assertEquals(0.21d, stage2.getOverrideMass(), 0.001);
        Assert.assertTrue(stage2.isCGOverridden());
        Assert.assertEquals(0.4d, stage2.getOverrideCG().x, 0.001);

        BodyTube bt = (BodyTube) stage2.getChild(0);
        LaunchLug ll = (LaunchLug) bt.getChild(6);
        Assert.assertEquals(1.22d, ll.getRadialDirection(), 0.001);

        Assert.assertEquals(2, stage3.getChildCount());
        Assert.assertEquals("Transition", stage3.getChild(0).getName());
        Assert.assertEquals("Body tube", stage3.getChild(1).getName());
        Assert.assertTrue(stage2.isMassOverridden());
        Assert.assertEquals(0.33d, stage3.getOverrideMass(), 0.001);
        Assert.assertTrue(stage2.isCGOverridden());
        Assert.assertEquals(0.5d, stage3.getOverrideCG().x, 0.001);
    }

    @org.junit.Test
    public void testSubAssemblyRocket() throws IOException, RocketLoadException {
        RocksimLoader loader = new RocksimLoader();
        //Stupid single stage rocket
        OpenRocketDocument doc = loadRocksimSubassemblyRocket(loader);
        InputStream stream;

        Assert.assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        Assert.assertNotNull(rocket);
        Assert.assertEquals("SubAssembly Element Test", doc.getRocket().getName());
        Assert.assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("SubAssemblyTest.rkt");
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
        Stage stage1 = (Stage) rocket.getChild(0);
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

    public static OpenRocketDocument loadRocksimRocket(RocksimLoader theLoader) throws IOException, RocketLoadException {
        InputStream stream = RocksimLoaderTest.class.getResourceAsStream("rocksimTestRocket1.rkt");
        try {
            Assert.assertNotNull("Could not open rocksimTestRocket1.rkt", stream);
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            theLoader.loadFromStream(context, new BufferedInputStream(stream));
            return doc;
        }
        finally {
            stream.close();
        }
    }

    public static OpenRocketDocument loadRocksimRocket3(RocksimLoader theLoader) throws IOException, RocketLoadException {
        InputStream stream = RocksimLoaderTest.class.getResourceAsStream("rocksimTestRocket3.rkt");
        try {
            Assert.assertNotNull("Could not open rocksimTestRocket3.rkt", stream);
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            theLoader.loadFromStream(context, new BufferedInputStream(stream));
            return doc;
        }
        finally {
            stream.close();
        }
    }

    public static OpenRocketDocument loadRocksimSubassemblyRocket(RocksimLoader theLoader) throws IOException, RocketLoadException {
        InputStream stream = RocksimLoaderTest.class.getResourceAsStream("SubAssemblyTest.rkt");
        try {
            Assert.assertNotNull("Could not open SubAssemblyTest.rkt", stream);
            OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(doc);
            context.setMotorFinder(new DatabaseMotorFinder());
            theLoader.loadFromStream(context, new BufferedInputStream(stream));
            return doc;
        }
        finally {
            stream.close();
        }
    }

}
