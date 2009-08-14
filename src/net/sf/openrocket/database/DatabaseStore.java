package net.sf.openrocket.database;

public interface DatabaseStore<T> {

	public void elementAdded(T element);
	
	public void elementRemoved(T element);
	
}
