package net.sf.openrocket.logging;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;


public class CyclicBufferTest {

	@Test
	public void testBasic() {
		CyclicBuffer<Integer> buffer = new CyclicBuffer<Integer>(5);
		LinkedList<Integer> correct = new LinkedList<Integer>();
		
		Random rnd = new Random();
		for (int i=0; i<50; i++) {
			int n = rnd.nextInt();
			buffer.add(n);
			correct.add(n);
			if (correct.size() > 5)
				correct.remove();
			assertEquals(correct, buffer.asList());
		}
	}
	
	@Test
	public void testComplex() {
		CyclicBuffer<Integer> buffer = new CyclicBuffer<Integer>(5);
		
		testContents(buffer);
		assertEquals(0, buffer.getOverwriteCount());

		buffer.offer(1);
		testContents(buffer, 1);
		assertEquals(0, buffer.getOverwriteCount());
		
		buffer.add(2);
		testContents(buffer, 1, 2);
		assertEquals(0, buffer.getOverwriteCount());
		
		buffer.add(3);
		testContents(buffer, 1, 2, 3);
		assertEquals(0, buffer.getOverwriteCount());
		
		assertEquals(1, (int)buffer.remove());
		testContents(buffer, 2, 3);
		assertEquals(0, buffer.getOverwriteCount());
		
		assertEquals(2, (int)buffer.peek());
		testContents(buffer, 2, 3);
		assertEquals(0, buffer.getOverwriteCount());
		
		buffer.offer(-2);
		testContents(buffer, 2, 3, -2);
		assertEquals(0, buffer.getOverwriteCount());
		
		buffer.offer(-3);
		testContents(buffer, 2, 3, -2, -3);
		assertEquals(0, buffer.getOverwriteCount());

		buffer.offer(-4);
		testContents(buffer, 2, 3, -2, -3, -4);
		assertEquals(0, buffer.getOverwriteCount());
		
		buffer.offer(5);
		testContents(buffer, 3, -2, -3, -4, 5);
		assertEquals(1, buffer.getOverwriteCount());

		buffer.offer(6);
		testContents(buffer, -2, -3, -4, 5, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(-2, (int)buffer.peek());
		testContents(buffer, -2, -3, -4, 5, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(-2, (int)buffer.remove());
		testContents(buffer, -3, -4, 5, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(-3, (int)buffer.remove());
		testContents(buffer, -4, 5, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(-4, (int)buffer.poll());
		testContents(buffer, 5, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(5, (int)buffer.remove());
		testContents(buffer, 6);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertEquals(6, (int)buffer.poll());
		testContents(buffer);
		assertEquals(2, buffer.getOverwriteCount());
		
		assertNull(buffer.peek());
		assertNull(buffer.poll());
		testContents(buffer);
		assertEquals(2, buffer.getOverwriteCount());
	}
	

	@Test
	public void testRandom() {
		CyclicBuffer<Integer> buffer = new CyclicBuffer<Integer>(4);
		LinkedList<Integer> correct = new LinkedList<Integer>();
		
		Random rnd = new Random();
		for (int i=0; i<500; i++) {
			
			if (rnd.nextBoolean()) {
				int n = rnd.nextInt();
				buffer.add(n);
				correct.add(n);
				if (correct.size() > 4)
					correct.remove();
			} else {
				Integer n = buffer.poll();
				if (correct.size() > 0) {
					assertEquals(correct.remove(), n);
				} else {
					assertNull(n);
				}
			}

			assertEquals(correct, buffer.asList());
		}
	}
	
	
	private void testContents(CyclicBuffer<Integer> buffer, int ... values) {
		
		// Test using iterator
		Iterator<Integer> iterator = buffer.iterator();
		for (int v: values) {
			assertTrue(iterator.hasNext());
			assertEquals(v, (int)iterator.next());
		}
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail();
		} catch (NoSuchElementException ignore) { }
		
		// Test using list
		List<Integer> list = buffer.asList();
		assertEquals("List: " + list, values.length, list.size());
		for (int i=0; i<values.length; i++) {
			assertEquals(values[i], (int)list.get(i));
		}
		
	}
	 
	
}
