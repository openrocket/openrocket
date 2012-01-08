/*
 * Visitor.java
 */
package net.sf.openrocket.rocketcomponent;

/**
 * This interface describes a portion of the Visitor pattern, using generics to assure type-safety.
 * The elements of the concrete object hierarchy are only visitable by an associated hierarchy of visitors, 
 * while these visitors are only able to visit the elements of that hierarchy. 
 * 
 * The key concept regarding the Visitor pattern is to realize that Java will only "discriminate" the type of an 
 * object being called, not the type of an object being passed.
 *
 * In order for the type of two objects to be determinable to the JVM, each object must be the receiver of an
 * invocation. Here, when accept is called on a Visitable, the concrete type of the Visitable becomes "known" but the 
 * concrete type of the argument is still unknown.  <code>visit</code> is then called on the parameter object, passing 
 * the Visitable back, which has type and identity. Flow of control has now been 'double-dispatched' such that the 
 * type (and identity) of both objects are known. 
 * 
 * Specifically, this interface is to be implemented by every class in the RocketComponent hierarchy that
 * can be visited AND which are sufficiently specialized from their super class.  If they only provide 
 * constraints to their superclass (such as TubeCoupler), then the implementation of this interface at 
 * the superclass level is sufficient.
 * 
 * Admittedly, the syntax is a bit contorted here, as it is necessarily self-referential for type-safety.
 * 
 * <V> The visitor type (the concrete class that implements this interface)
 * <T> The visitable 
 */
public interface Visitor<V extends Visitor<V, T>, T extends Visitable<V, T>> {
	
	/**
	 * The callback method.  This method is the 2nd leg of the double-dispatch, having been invoked from a 
	 * corresponding <code>accept</code>.
	 *           
	 * @param visitable  the instance of the Visitable (the target of what is being visiting)
	 */
	void visit(T visitable);
}