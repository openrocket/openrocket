package net.sf.openrocket.database;

public interface DatabaseListener<T extends Comparable<T>> {

	public void elementAdded(T element, Database<T> source);
	
	public void elementRemoved(T element, Database<T> source);
	
}
