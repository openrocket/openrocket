package net.sf.openrocket.util.watcher;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 */
public class DirectoryMonitorTest {

    @Test
    public void testCheck() throws Exception {
        final File tempDir = DirectoryTest.createTempDir();
        tempDir.setWritable(true);
        Directory directory = new Directory(tempDir);
        File f1 = null;
        File f2 = null;
        File f3 = null;
        try {

            DirectoryMonitor monitor = new DirectoryMonitor();
            monitor.register(directory);

            String baseName = System.currentTimeMillis() + "--";

            f1 = new File(tempDir.getAbsolutePath(), baseName + "1");
            f1.createNewFile();
            f2 = new File(tempDir.getAbsolutePath(), baseName + "2");
            f2.createNewFile();
            f3 = new File(tempDir.getAbsolutePath(), baseName + "3");
            f3.createNewFile();

            Collection<? extends WatchService.WatchKey> keys = monitor.check();
            Assert.assertEquals(1, keys.size());
            WatchService.WatchKey wk = keys.iterator().next();
            List<WatchEvent<?>> events = wk.pollEvents();

            Assert.assertEquals(3, events.size());
            for (int i = 0; i < events.size(); i++) {
                WatchEvent<?> watchEvent = events.get(i);
                if (watchEvent.context().equals(f1)) {
                    Assert.assertEquals(WatchEventKind.ENTRY_CREATE, watchEvent.kind());
                }
                else if (watchEvent.context().equals(f2)) {
                    Assert.assertEquals(WatchEventKind.ENTRY_CREATE, watchEvent.kind());
                }
                else if (watchEvent.context().equals(f3)) {
                    Assert.assertEquals(WatchEventKind.ENTRY_CREATE, watchEvent.kind());
                }
                else {
                    System.err.println(watchEvent.context().toString());
                    Assert.fail("Unknown target file.");
                }
            }

            f1.setLastModified(System.currentTimeMillis() + 10007);
            f1.setReadable(true);
            Thread.sleep(1000);
            keys = monitor.check();
            Assert.assertEquals(1, keys.size());
            WatchService.WatchKey watchEvent = keys.iterator().next();
            Assert.assertEquals(1, watchEvent.pollEvents().size());
            Assert.assertEquals(f1, watchEvent.pollEvents().get(0).context());
            Assert.assertEquals(WatchEventKind.ENTRY_MODIFY, watchEvent.pollEvents().get(0).kind());
        }
        finally {
            if (f1 != null) {
                f1.delete();
            }
            if (f2 != null) {
                f2.delete();
            }
            if (f3 != null) {
                f3.delete();
            }
            directory.getTarget().delete();
        }

    }
}
