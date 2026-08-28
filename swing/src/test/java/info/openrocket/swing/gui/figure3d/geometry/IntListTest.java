package info.openrocket.swing.gui.figure3d.geometry;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntListTest {

	@Test
	void growsWhilePreservingPrimitiveValues() {
		IntList list = new IntList(0);
		for (int i = 0; i < 100; i++) {
			list.add(i * 3);
		}

		assertEquals(100, list.size());
		assertEquals(0, list.get(0));
		assertEquals(297, list.get(99));
		assertEquals(100, list.toArray().length);
	}

	@Test
	void copiesRangesWithAndWithoutOffset() {
		IntList source = new IntList();
		source.addTriangle(1, 2, 3);
		source.addTriangle(4, 5, 6);

		IntList destination = new IntList();
		destination.addAll(source, 1, 4);
		destination.addAllOffset(source, 3, 6, 10);

		assertArrayEquals(new int[] {2, 3, 4, 14, 15, 16}, destination.toArray());
	}

	@Test
	void rejectsInvalidIndicesAndRanges() {
		IntList list = new IntList();
		list.add(7);

		assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
		assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(list, 1, 0));
		assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(list, 0, 2));
	}

	@Test
	void iteratorReportsExhaustion() {
		IntList list = new IntList();
		list.add(42);
		Iterator<Integer> iterator = list.iterator();

		assertEquals(42, iterator.next());
		assertThrows(NoSuchElementException.class, iterator::next);
	}
}
