package net.sf.openrocket.util.watcher;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 */
public class DirectoryChangeReactorImplTest {

    @Test
    public void testRegisterHandler() throws Exception {
        DirectoryChangeReactorImpl impl = new DirectoryChangeReactorImpl();

        final File tempDir = DirectoryTest.createTempDir();
        tempDir.setWritable(true);
        Directory directory = new Directory(tempDir);
        File f1 = null;
        File f2 = null;

        File sub = null;
        Directory subdir;

        try {
            WatchedEventHandlerImpl testHandler = new WatchedEventHandlerImpl(directory);
            impl.registerHandler(testHandler);

            String baseName = System.currentTimeMillis() + "--";

            f1 = new File(tempDir.getAbsolutePath(), baseName + "1");
            f1.createNewFile();
            long totalSleepTime = 0;
            WatchEvent.Kind<?> kind = null;
            while (totalSleepTime < DirectoryChangeReactorImpl.DEFAULT_POLLING_DELAY + 1000) {
                kind = testHandler.getKind();
                if (kind != null) {
                    break;
                }
                Thread.sleep(1000);
                totalSleepTime += 1000;
            }
            Assert.assertEquals(WatchEventKind.ENTRY_CREATE, kind);
            Assert.assertEquals(testHandler.eventTarget, f1);

            f1.setLastModified(System.currentTimeMillis() + 10000);
            totalSleepTime = 0;
            kind = null;
            while (totalSleepTime < DirectoryChangeReactorImpl.DEFAULT_POLLING_DELAY + 400) {
                kind = testHandler.getKind();
                if (kind != null) {
                    break;
                }
                Thread.sleep(1000);
                totalSleepTime += 1000;
            }
            Assert.assertEquals(WatchEventKind.ENTRY_MODIFY, kind);
            Assert.assertEquals(testHandler.eventTarget, f1);

            f1.delete();
            totalSleepTime = 0;
            kind = null;
            while (totalSleepTime < DirectoryChangeReactorImpl.DEFAULT_POLLING_DELAY + 400) {
                kind = testHandler.getKind();
                if (kind != null) {
                    break;
                }
                Thread.sleep(1000);
                totalSleepTime += 1000;
            }
            Assert.assertEquals(WatchEventKind.ENTRY_DELETE, kind);
            Assert.assertEquals(testHandler.eventTarget, f1);

            //test recursive nature of monitoring subdirectories
            sub = DirectoryTest.createTempDir(tempDir);
            subdir = new Directory(sub);

            totalSleepTime = 0;
            kind = null;
            while (totalSleepTime < DirectoryChangeReactorImpl.DEFAULT_POLLING_DELAY + 400) {
                kind = testHandler.getKind();
                if (kind != null) {
                    break;
                }
                Thread.sleep(1000);
                totalSleepTime += 1000;
            }
            Assert.assertEquals(WatchEventKind.ENTRY_CREATE, kind);
            Assert.assertEquals(testHandler.eventTarget, sub);

            f2 = new File(sub.getAbsolutePath(), baseName + "2");
            f2.createNewFile();
            totalSleepTime = 0;
            kind = null;
            while (totalSleepTime < DirectoryChangeReactorImpl.DEFAULT_POLLING_DELAY + 400) {
                kind = testHandler.getKind();
                if (kind != null) {
                    break;
                }
                Thread.sleep(1000);
                totalSleepTime += 1000;
            }
            //Eventually, both events will show up, but it's system dependent which one we get first, and if
            //they both arrive together or on different polling cycles.  This could be embellished, but for now
            //just see if at least one arrives.
            if (kind.equals(WatchEventKind.ENTRY_CREATE)) {
                Assert.assertEquals(f2, testHandler.eventTarget);
            }
            else if (kind.equals(WatchEventKind.ENTRY_MODIFY)) {
                Assert.assertEquals(sub, testHandler.eventTarget);
            }

        }
        finally {
            if (f1 != null) {
                f1.delete();
            }
            if (sub != null) {
                sub.delete();
            }
            directory.getTarget().delete();
            impl.shutdown();
        }
    }

    static class WatchedEventHandlerImpl implements WatchedEventHandler<Directory> {
        Directory file        = null;
        Object    eventTarget = null;

        WatchEvent.Kind<?> kind = null;

        WatchedEventHandlerImpl(final Directory theFile) {
            file = theFile;
        }

        @Override
        public Directory watchTarget() {
            return file;
        }

        @Override
        public boolean watchRecursively() {
            return true;
        }

        public WatchEvent.Kind<?> getKind() {
            WatchEvent.Kind<?> tmp = kind;
            kind = null;
            return tmp;
        }

        @Override
        public void handleEvents(final List<WatchEvent<?>> theEvents) {
            for (int i = 0; i < theEvents.size(); i++) {
                WatchEvent<?> watchEvent = theEvents.get(i);
                kind = watchEvent.kind();
                eventTarget = watchEvent.context();
            }
        }
    }
}
