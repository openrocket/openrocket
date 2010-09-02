package net.sf.openrocket.file.iterator;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import net.sf.openrocket.util.Pair;

import org.junit.Test;

public class TestFileIterator {
	
	@Test
	public void testFileIterator() {
		final Pair<String, InputStream> one = new Pair<String, InputStream>("one", new ByteArrayInputStream(new byte[] { 1 }));
		final Pair<String, InputStream> two = new Pair<String, InputStream>("two", new ByteArrayInputStream(new byte[] { 2 }));
		
		FileIterator iterator = new FileIterator() {
			private int count = 0;
			
			@Override
			protected Pair<String, InputStream> findNext() {
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
