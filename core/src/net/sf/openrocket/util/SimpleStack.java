package net.sf.openrocket.util;

import java.util.NoSuchElementException;
/**
 * SimpleStack implementation backed by an ArrayList.
 * 
 */
public class SimpleStack<T> extends ArrayList<T> {

	public void push( T value ) {
		this.add(value);
	}
	
	public T peek() {
		if ( size() <= 0 ) {
			return null;
		}
		return this.get( size() -1 );
	}
	
	public T pop() {
		if ( size() <= 0 ) {
			throw new NoSuchElementException();
		}
		T value = this.remove( size() -1 );
		return value;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("SimpleStack count=" + size() + "\n");
		for( T element: this ) {
			sb.append("   ").append(element.toString());
		}
		return sb.toString();
	}
}
