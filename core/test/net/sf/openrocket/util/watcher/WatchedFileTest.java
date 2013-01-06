package net.sf.openrocket.util.watcher;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 */
public class WatchedFileTest {

    @Test
    public void testConstructor() throws Exception {
        try {
            new WatchedFile(null);
            Assert.fail();
        }
        catch (IllegalArgumentException iae) {
            //success
        }

        final File blah = new File("blah");
        WatchedFile wf = new WatchedFile(blah);
        Assert.assertEquals(blah, wf.getTarget());
    }

    @Test
    public void testCreateEvent() throws Exception {
        final File blah = new File("blah");
        WatchedFile wf = new WatchedFile(blah);
        Assert.assertEquals(blah, wf.createEvent().context());
        Assert.assertEquals(WatchEventKind.ENTRY_CREATE, wf.createEvent().kind());
    }

    @Test
    public void testExists() throws Exception {
        final File blah = new File("blah");
        WatchedFile wf = new WatchedFile(blah);
        Assert.assertFalse(wf.exists());
    }

    @Test
    public void testCheck() throws Exception {
        final File blah = new File("blah");
        WatchedFile wf = new WatchedFile(blah);

        WatchEvent check = wf.check();
        Assert.assertEquals(WatchEventKind.ENTRY_DELETE, check.kind());

        File f = File.createTempFile("tmp", "tmp");
        wf = new WatchedFile(f);

        check = wf.check();
        Assert.assertEquals(WatchEvent.NO_EVENT, check);

        f.setLastModified(System.currentTimeMillis() - 60000);
        check = wf.check();
        Assert.assertEquals(WatchEventKind.ENTRY_MODIFY, check.kind());
        Assert.assertEquals(f, check.context());

        //Check for reset of state
        check = wf.check();
        Assert.assertEquals(WatchEvent.NO_EVENT, check);
    }

    @Test
    public void testEquals() throws Exception {
        final File blah = new File("blah");
        final File blech = new File("blech");
        WatchedFile wf1 = new WatchedFile(blah);
        WatchedFile wf2 = new WatchedFile(blah);
        WatchedFile wf3 = new WatchedFile(blech);

        Assert.assertEquals(wf1, wf1);
        Assert.assertEquals(wf1, wf2);
        Assert.assertFalse(wf1.equals(wf3));
        Assert.assertFalse(wf1.equals(null));
        Assert.assertFalse(wf1.equals(new Object()));

        Assert.assertEquals(wf1.hashCode(), wf2.hashCode());
    }
}
