package net.sf.openrocket.util.watcher;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 */
public class DirectoryTest {

    @Test
    public void testConstructor() throws Exception {
        try {
            new Directory(null);
            Assert.fail();
        }
        catch (IllegalArgumentException iae) {
            //success
        }
        try {
            new Directory(new File("foo"));
            Assert.fail();
        }
        catch (IllegalArgumentException iae) {
            //success
        }
    }

    @Test
    public void testSize() throws Exception {
        final File tempDir = createTempDir();
        tempDir.setWritable(true);
        Directory directory = new Directory(tempDir);
        File f1 = null;
        File f2 = null;
        File f3 = null;
        try {
            Assert.assertTrue(directory.exists());
            Assert.assertEquals(0, directory.size());

            String baseName = System.currentTimeMillis() + "--";

            f1 = new File(tempDir.getAbsolutePath(), baseName + "1");
            f1.createNewFile();
            f2 = new File(tempDir.getAbsolutePath(), baseName + "2");
            f2.createNewFile();
            f3 = new File(tempDir.getAbsolutePath(), baseName + "3");
            f3.createNewFile();

            Assert.assertEquals(3, directory.size());
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

    @Test
    public void testList() throws Exception {
        final File tempDir = createTempDir();
        tempDir.setWritable(true);
        Directory directory = new Directory(tempDir);
        File f1 = null;
        File f2 = null;
        File f3 = null;
        try {
            Assert.assertTrue(directory.exists());
            Assert.assertEquals(0, directory.size());

            String baseName = System.currentTimeMillis() + "--";

            f1 = new File(tempDir.getAbsolutePath(), baseName + "1");
            f1.createNewFile();
            f2 = new File(tempDir.getAbsolutePath(), baseName + "2");
            f2.createNewFile();
            f3 = new File(tempDir.getAbsolutePath(), baseName + "3");
            f3.createNewFile();

            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                String file = files[i];
                if (file.endsWith("1")) {
                    Assert.assertEquals(baseName + "1", file);
                }
                else if (file.endsWith("2")) {
                    Assert.assertEquals(baseName + "2", file);
                }
                else if (file.endsWith("3")) {
                    Assert.assertEquals(baseName + "3", file);
                }
                else {
                    Assert.fail();
                }
            }
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

    @Test
    public void testContents() throws Exception {
        final File tempDir = createTempDir();
        tempDir.setWritable(true);
        Directory directory = new Directory(tempDir);
        File f1 = null;
        File f2 = null;
        File f3 = null;
        try {
            String baseName = System.currentTimeMillis() + "--";

            f1 = new File(tempDir.getAbsolutePath(), baseName + "1");
            f1.createNewFile();
            f2 = new File(tempDir.getAbsolutePath(), baseName + "2");
            f2.createNewFile();
            f3 = new File(tempDir.getAbsolutePath(), baseName + "3");
            f3.createNewFile();

            Assert.assertEquals(0, directory.getContents().size());

            //Contents is initialized at the time Directory is created.  Since we had to create it for the test,
            //we need a second Directory instance.
            Directory directory1 = new Directory(tempDir);
            Assert.assertEquals(3, directory1.getContents().size());
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

    //Borrowed from Google's Guava.
    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        return createTempDir(baseDir);
    }

    public static File createTempDir(File parent) {
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < 2; counter++) {
            File tempDir = new File(parent, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                                                + 2 + " attempts (tried "
                                                + baseName + "0 to " + baseName + (2 - 1) + ')');
    }
}
