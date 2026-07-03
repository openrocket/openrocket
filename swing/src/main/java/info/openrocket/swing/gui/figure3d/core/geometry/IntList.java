package info.openrocket.swing.gui.figure3d.core.geometry;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Minimal growable list for mesh index buffers without per-index boxing.
 */
public final class IntList implements Iterable<Integer> {
	private static final int DEFAULT_CAPACITY = 16;

	private int[] values;
	private int size;

	public IntList() {
		this(DEFAULT_CAPACITY);
	}

	public IntList(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity must be non-negative");
		}
		values = new int[Math.max(DEFAULT_CAPACITY, initialCapacity)];
	}

	public IntList(IntList source) {
		this(source.size);
		addAll(source);
	}

	public void add(int value) {
		ensureCapacity(size + 1);
		values[size++] = value;
	}

	public void addTriangle(int i1, int i2, int i3) {
		ensureCapacity(size + 3);
		values[size++] = i1;
		values[size++] = i2;
		values[size++] = i3;
	}

	public void addAll(IntList source) {
		addAll(source, 0, source.size);
	}

	public void addAll(IntList source, int fromIndex, int toIndex) {
		checkRange(source, fromIndex, toIndex);
		int count = toIndex - fromIndex;
		ensureCapacity(size + count);
		System.arraycopy(source.values, fromIndex, values, size, count);
		size += count;
	}

	public void addAllOffset(IntList source, int offset) {
		addAllOffset(source, 0, source.size, offset);
	}

	public void addAllOffset(IntList source, int fromIndex, int toIndex, int offset) {
		checkRange(source, fromIndex, toIndex);
		int count = toIndex - fromIndex;
		ensureCapacity(size + count);
		for (int i = fromIndex; i < toIndex; i++) {
			values[size++] = source.values[i] + offset;
		}
	}

	public int get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
		}
		return values[index];
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int[] toArray() {
		return Arrays.copyOf(values, size);
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<>() {
			private int index;

			@Override
			public boolean hasNext() {
				return index < size;
			}

			@Override
			public Integer next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return values[index++];
			}
		};
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity <= values.length) {
			return;
		}
		int newCapacity = values.length + (values.length >> 1);
		if (newCapacity < minCapacity) {
			newCapacity = minCapacity;
		}
		values = Arrays.copyOf(values, newCapacity);
	}

	private static void checkRange(IntList source, int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > source.size || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException(
					"Range [" + fromIndex + ", " + toIndex + ") out of bounds for size " + source.size);
		}
	}
}
