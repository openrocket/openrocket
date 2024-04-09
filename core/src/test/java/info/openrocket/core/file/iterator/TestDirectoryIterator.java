package info.openrocket.core.file.iterator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TestDirectoryIterator {

	@Test
	public void testDirectoryIterator() throws IOException {
		DirectoryIterator iterator = new DirectoryIterator(new File("src/test/java/info/openrocket/core/file"),
				new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.getName().matches("^Test(Directory|File)Iterator.java");
					}
				}, true);

		while (iterator.hasNext()) {
			// TODO need checks here to ensure correct things were done
			// System.out.println("" + iterator.next());
			iterator.next();
		}

	}
}
