package net.sf.openrocket.util;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class Base64Test {
	
	@Test
	public void oldMainTest() throws Exception {
		
		// TODO - this test case should probably be less random and more targeted to
		// special cases such as:
		// null input
		// empty input
		// decoding bad string
		
		Random rnd = new Random();
		
		for (int round = 0; round < 1000; round++) {
			int n = rnd.nextInt(1000);
			n = 100000;
			
			byte[] array = new byte[n];
			rnd.nextBytes(array);
			
			String encoded = Base64.encode(array);
			
			byte[] decoded = null;
			decoded = Base64.decode(encoded);
			
			if (!Arrays.equals(array, decoded)) {
				fail("Data differs!  n=" + n);
			}
			
		}
	}
}