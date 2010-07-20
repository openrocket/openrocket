/*
 * RocksimLoaderTest.java
 *
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * RocksimLoader Tester.
 *
 */
public class RocksimLoaderTest extends TestCase {

    /**
     * The class under test.
     */
    public static final Class classUT = RocksimLoader.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = RocksimLoaderTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(RocksimLoaderTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public RocksimLoaderTest(String name) {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Teardown the fixture.
     */
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test a bug reported via automated bug report.  I have been unable to reproduce this bug
     * (hanging finset off of an inner body tube) when creating a Rocksim file using Rocksim.  The bug
     * is reproducible when manually modifying the Rocksim file, which is what is tested here.
     */
    public void testFinsOnInnerTube() throws Exception {
        RocksimLoader loader = new RocksimLoader();
        InputStream stream = this.getClass().getResourceAsStream("PodFins.rkt");
        assertNotNull("Could not open PodFins.rkt", stream);
        try {
            OpenRocketDocument doc = loader.loadFromStream(new BufferedInputStream(stream));
            assertNotNull(doc);
            Rocket rocket = doc.getRocket();
            assertNotNull(rocket);
        }
        catch (IllegalStateException ise) {
            fail(ise.getMessage());            
        }
        assertTrue(loader.getWarnings().size() == 2);
    }

    /**
     *
     * Method: loadFromStream(InputStream source)
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testLoadFromStream() throws Exception {
        RocksimLoader loader = new RocksimLoader();
        //Stupid single stage rocket
        InputStream stream = this.getClass().getResourceAsStream("rocksimTestRocket1.rkt");
        assertNotNull("Could not open rocksimTestRocket1.rkt", stream);
        OpenRocketDocument doc = loader.loadFromStream(new BufferedInputStream(stream));
        
        assertNotNull(doc);
        Rocket rocket = doc.getRocket();
        assertNotNull(rocket);
        assertEquals("FooBar Test", doc.getRocket().getName());
        assertTrue(loader.getWarnings().isEmpty());

        stream = this.getClass().getResourceAsStream("rocksimTestRocket2.rkt");
        assertNotNull("Could not open rocksimTestRocket2.rkt", stream);
        doc = loader.loadFromStream(new BufferedInputStream(stream));
        
        assertNotNull(doc);
        rocket = doc.getRocket();
        assertNotNull(rocket);

        //Do some simple asserts;  the important thing here is just validating that the mass and cg were
        //not overridden for each stage.
        assertEquals("Three Stage Everything Included Rocket", doc.getRocket().getName());
        assertEquals(1, loader.getWarnings().size());
        assertEquals(3, rocket.getStageCount());  
        Stage stage1 = (Stage)rocket.getChild(0);
        assertFalse(stage1.isMassOverridden());
        assertFalse(stage1.isCGOverridden());
        Stage stage2 = (Stage)rocket.getChild(1);
        assertFalse(stage2.isMassOverridden());
        assertFalse(stage2.isCGOverridden());
        Stage stage3 = (Stage)rocket.getChild(2);
        assertFalse(stage3.isMassOverridden());
        assertFalse(stage3.isCGOverridden());

        stream = this.getClass().getResourceAsStream("rocksimTestRocket3.rkt");
        assertNotNull("Could not open rocksimTestRocket3.rkt", stream);
        doc = loader.loadFromStream(new BufferedInputStream(stream));
        
        assertNotNull(doc);
        rocket = doc.getRocket();
        assertNotNull(rocket);
        assertEquals("Three Stage Everything Included Rocket - Override Total Mass/CG", doc.getRocket().getName());
        assertEquals(3, rocket.getStageCount());  
        stage1 = (Stage)rocket.getChild(0);
        stage2 = (Stage)rocket.getChild(1);
        stage3 = (Stage)rocket.getChild(2);
        
        //Do some 1st level and simple asserts; the idea here is to not do a deep validation as that 
        //should have been covered elsewhere.  Assert that the stage overrides are correct.
        assertEquals(2, stage1.getChildCount());
        assertEquals("Nose cone", stage1.getChild(0).getName());
        assertEquals("Body tube", stage1.getChild(1).getName());
        assertTrue(stage1.isMassOverridden());
        assertEquals(0.185d, stage1.getOverrideMass());
        assertTrue(stage1.isCGOverridden());
        assertEquals(0.3d, stage1.getOverrideCG().x);
        assertEquals(4, loader.getWarnings().size());
        
        assertEquals(1, stage2.getChildCount());
        assertEquals("2nd Stage Tube", stage2.getChild(0).getName());
        assertTrue(stage2.isMassOverridden());
        assertEquals(0.21d, stage2.getOverrideMass());
        assertTrue(stage2.isCGOverridden());
        assertEquals(0.4d, stage2.getOverrideCG().x);
        
        assertEquals(2, stage3.getChildCount());
        assertEquals("Transition", stage3.getChild(0).getName());
        assertEquals("Body tube", stage3.getChild(1).getName());
        assertTrue(stage2.isMassOverridden());
        assertEquals(0.33d, stage3.getOverrideMass());
        assertTrue(stage2.isCGOverridden());
        assertEquals(0.5d, stage3.getOverrideCG().x);
    }

}
