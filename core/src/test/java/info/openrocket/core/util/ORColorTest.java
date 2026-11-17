package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

public class ORColorTest {

	@Test
	public void gettersAndSettersMutateComponents() {
		ORColor color = new ORColor(10, 20, 30);
		color.setRed(40);
		color.setGreen(50);
		color.setBlue(60);
		color.setAlpha(70);

		assertEquals(40, color.getRed());
		assertEquals(50, color.getGreen());
		assertEquals(60, color.getBlue());
		assertEquals(70, color.getAlpha());
	}

	@Test
	public void equalsComparesComponentValues() {
		ORColor first = new ORColor(10, 20, 30, 40);
		ORColor same = new ORColor(10, 20, 30, 40);
		ORColor different = new ORColor(10, 21, 30, 40);

		assertEquals(first, first);
		assertEquals(first, same);
		assertNotEquals(first, different);
		assertNotEquals("color", first);
	}
}
