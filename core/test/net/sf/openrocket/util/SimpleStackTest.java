package net.sf.openrocket.util;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SimpleStackTest {

	@Test(expected=NoSuchElementException.class)
	public void testEmptyStack() {
		SimpleStack<Integer> s = new SimpleStack<Integer>();
		
		assertNull(s.peek());
		
		s.pop();
	}
	
	@Test
	public void testPushAndPop() {
		
		SimpleStack<Integer> s = new SimpleStack<Integer>();
		
		for( int i = 0; i< 10; i++ ) {
			s.push(i);
			assertEquals(i+1, s.size());
		}
	
		for( int i=9; i>= 0; i-- ) {
			assertEquals( i, s.peek().intValue() );
			Integer val = s.pop();
			assertEquals( i, val.intValue() );
			assertEquals( i, s.size() );
		}
		
		assertNull( s.peek() );
		assertEquals( 0, s.size() );
		
	}
	
}
