package net.sf.openrocket.file.iterator;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import net.sf.openrocket.util.Pair;

import org.junit.Test;

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
