/*
 * RocksimContentHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * RocksimContentHandler Tester.
 *
 */
public class RocksimContentHandlerTest extends TestCase {

    /**
     * The class under test.
     */
    public static final Class classUT = RocksimContentHandler.class;

    /**
     * The test class (this class).
     */
    public static final Class testClass = RocksimContentHandlerTest.class;

    /**
     * Create a test suite of all tests within this test class.
     *
     * @return a suite of tests
     */
    public static Test suite() {
        return new TestSuite(RocksimContentHandlerTest.class);
    }

    /**
     * Test constructor.
     *
     * @param name the name of the test to run.
     */
    public RocksimContentHandlerTest(String name) {
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
     *
     * Method: getDocument()
     *
     * @throws Exception  thrown if something goes awry
     */
    public void testGetDocument() throws Exception {
        RocksimContentHandler handler = new RocksimContentHandler();
        assertNotNull(handler.getDocument());
    }

}
