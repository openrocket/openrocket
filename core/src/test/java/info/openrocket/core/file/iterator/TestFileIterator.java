package info.openrocket.core.file.iterator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import info.openrocket.core.util.Pair;

import org.junit.jupiter.api.Test;

public class TestFileIterator {

	@Test
	public void testFileIterator() {
		final Pair<File, InputStream> one = new Pair<>(new File("one"), new ByteArrayInputStream(new byte[] { 1 }));
		final Pair<File, InputStream> two = new Pair<>(new File("two"), new ByteArrayInputStream(new byte[] { 2 }));

		FileIterator iterator = new FileIterator() {
			private int count = 0;

			@Override
			protected Pair<File, InputStream> findNext() {
				count++;
				switch (count) {
					case 1:
						return one;
					case 2:
						return two;
					default:
						return null;
				}
			}
		};

		assertTrue(iterator.hasNext());
		assertEquals(one, iterator.next());
		assertEquals(two, iterator.next());
		assertFalse(iterator.hasNext());
	}
}
