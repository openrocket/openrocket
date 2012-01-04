/*
 * RocksimContentHandlerTest.java
 */
package net.sf.openrocket.file.rocksim;

import org.junit.Assert;

/**
 * RocksimContentHandler Tester.
 *
 */
public class RocksimContentHandlerTest {

    /**
     *
     * Method: getDocument()
     *
     * @throws Exception  thrown if something goes awry
     */
    @org.junit.Test
    public void testGetDocument() throws Exception {
        RocksimContentHandler handler = new RocksimContentHandler();
        Assert.assertNotNull(handler.getDocument());
    }

}
