package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ModIDTest {

	@Test
	public void modIDTest() {

		ModID n1 = new ModID();
		ModID n2 = new ModID();
		ModID n3 = new ModID();
		//		assertTrue(n > 0);
		assertEquals(n2, n2);
		assertTrue(n1.toInt() < n2.toInt());
		assertTrue(n3.toInt() > n2.toInt());

	}
}
