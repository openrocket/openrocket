package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UniqueIDTest {

	@Test
	public void integerTest() {

		int n = UniqueID.next();
		assertTrue(n > 0);
		assertEquals(n + 1, UniqueID.next());
		assertEquals(n + 2, UniqueID.next());
		assertEquals(n + 3, UniqueID.next());

	}

	@Test
	public void stringTest() {
		String id = UniqueID.uuid();
		assertNotNull(id);
		assertNotSame(id, UniqueID.uuid());
		assertNotSame(id, UniqueID.uuid());
	}

}
