/*
 * Coaxial.java
 */
package net.sf.openrocket.rocketcomponent;

/**
 * This interface defines the API for components that are axially
 * symmetric.  It differs from RadialParent in that RadialParent applies
 * to axially symmetric components whose radius varies with position, while
 * this interface is for components that have a constant radius over it's length.
 */
public interface Coaxial {

    /**
     * Get the length of the radius of the inside dimension, in standard units.
     * 
     * @return the inner radius
     */
    double getInnerRadius();

    /**
     * Set the length of the radius of the inside dimension, in standard units.
     * 
     * @param v  the length of the inner radius
     */
    void setInnerRadius(double v);
    
    /**
     * Get the length of the radius of the outside dimension, in standard units.
     * 
     * @return the outer radius
     */
    double getOuterRadius();
    
    /**
     * Set the length of the radius of the outside dimension, in standard units.
     * 
     * @param v  the length of the outer radius
     */
    void setOuterRadius(double v);

    /**
     * Get the wall thickness of the component.  Typically this is just
     * the outer radius - inner radius.
     * 
     * @return  the thickness of the wall
     */
    double getThickness();
    
}
