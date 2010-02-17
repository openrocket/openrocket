package net.sf.openrocket.logging;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A cyclic buffer with a fixed size.  When more data is inserted, the newest
 * data will overwrite the oldest data.
 * <p>
 * Though this class implements the Queue interface, it specifically breaks the
 * contract by overwriting (removing) data without specific removal.  It also
 * currently does not support removing arbitrary elements from the set.
 * <p>
 * The methods in this class are synchronized for concurrent modification.
 * However, iterating over the set is not thread-safe.  To obtain a snapshot
 * of the state of the buffer, use {@link #asList()}.
 * 
 * @param <E>	the object type that is stored.
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class CyclicBuffer<E> extends AbstractQueue<E> {

	private final ArrayList<E> buffer;
	private final int maxSize;
	
	private int startPosition = 0;
	private int size = 0;
	private int overwriteCount = 0;
	
	private int modCount = 0;
	

	/**
	 * Create a cyclic buffer of the specified size.
	 * 
	 * @param size	the size of the cyclic buffer.
	 */
	public CyclicBuffer(int size) {
		this.buffer = new ArrayList<E>(size);
		for (int i=0; i<size; i++) {
			this.buffer.add(null);
		}
		this.maxSize = size;
	}

	

	@Override
	public synchronized boolean offer(E e) {
		buffer.set((startPosition + size) % maxSize, e);
		if (size < maxSize) {
			size++;
		} else {
			startPosition = next(startPosition);
			overwriteCount++;
		}
		
		modCount++;
		return true;
	}

	
	@Override
	public synchronized E peek() {
		if (size == 0)
			return null;
		return buffer.get(startPosition);
	}

	
	@Override
	public synchronized E poll() {
		if (size == 0)
			return null;
		
		E element = buffer.get(startPosition);
		startPosition = next(startPosition);
		size--;
		
		modCount++;
		return element;
	}

	
	@Override
	public synchronized int size() {
		return size;
	}
	

	@Override
	public synchronized Iterator<E> iterator() {
		return new CyclicBufferIterator();
	}


	/**
	 * Return a snapshot of the current buffered objects in the order they
	 * were placed in the buffer.  The list is independent of the buffer.
	 * 
	 * @return	a list of the buffered objects.
	 */
	public synchronized List<E> asList() {
		ArrayList<E> list = new ArrayList<E>(size);
		if (startPosition + size > maxSize) {
			list.addAll(buffer.subList(startPosition, maxSize));
			list.addAll(buffer.subList(0, startPosition + size - maxSize));
		} else {
			list.addAll(buffer.subList(startPosition, startPosition+size));
		}
		return list;
	}

	
	/**
	 * Return the number of elements that have been overwritten in the buffer.
	 * The overwritten elements are the elements that have been added to the
	 * buffer, have not been explicitly removed but are not present in the list.
	 * 
	 * @return	the number of overwritten elements this far.
	 */
	public synchronized int getOverwriteCount() {
		return overwriteCount;
	}
	
	
	private int next(int n) {
		return (n+1) % maxSize;
	}
	
	
	private class CyclicBufferIterator implements Iterator<E> {

		private int expectedModCount;
		private int n = 0;

		public CyclicBufferIterator() {
			this.expectedModCount = modCount;
		}

		@Override
		public boolean hasNext() {
			synchronized (CyclicBuffer.this) {
				if (expectedModCount != modCount) {
					throw new ConcurrentModificationException("expectedModCount="+
							expectedModCount+" modCount=" + modCount);
				}
				return (n < size);
			}
		}

		@Override
		public E next() {
			synchronized (CyclicBuffer.this) {
				if (expectedModCount != modCount) {
					throw new ConcurrentModificationException("expectedModCount="+
							expectedModCount+" modCount=" + modCount);
				}
				if (n >= size) {
					throw new NoSuchElementException("n="+n+" size="+size);
				}
				n++;
				return buffer.get((startPosition + n-1) % maxSize);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("random remove not supported");
		}
	}
}
