package net.sf.openrocket.rocketcomponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.UUID;

import javax.swing.event.ChangeListener;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.MathUtil;


public abstract class RocketComponent implements ChangeSource, Cloneable, 
		Iterable<RocketComponent> {

	/*
	 * Text is suitable to the form
	 *    Position relative to:  <title>
	 */
	public enum Position {
		/** Position relative to the top of the parent component. */
		TOP("Top of the parent component"),
		/** Position relative to the middle of the parent component. */
		MIDDLE("Middle of the parent component"),
		/** Position relative to the bottom of the parent component. */
		BOTTOM("Bottom of the parent component"),
		/** Position after the parent component (for body components). */
		AFTER("After the parent component"),
		/** Specify an absolute X-coordinate position. */
		ABSOLUTE("Tip of the nose cone");
		
		private String title;
		Position(String title) {
			this.title = title;
		}
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	////////  Parent/child trees
	/**
	 * Parent component of the current component, or null if none exists.
	 */
	private RocketComponent parent = null;
	
	/**
	 * List of child components of this component.
	 */
	private List<RocketComponent> children = new ArrayList<RocketComponent>();

	
	////////  Parameters common to all components:
	
	/**
	 * Characteristic length of the component.  This is used in calculating the coordinate
	 * transformations and positions of other components in reference to this component.
	 * This may and should be used as the "true" length of the component, where applicable.
	 * By default it is zero, i.e. no translation.
	 */
	protected double length = 0;

	/**
	 * Positioning of this component relative to the parent component.
	 */
	protected Position relativePosition;
	
	/**
	 * Offset of the position of this component relative to the normal position given by
	 * relativePosition.  By default zero, i.e. no position change.
	 */
	protected double position = 0;
	
	
	// Color of the component, null means to use the default color
	private Color color = null;
	private LineStyle lineStyle = null;
	
	
	// Override mass/CG
	private double overrideMass = 0;
	private boolean massOverriden = false;
	private double overrideCGX = 0;
	private boolean cgOverriden = false;
	
	private boolean overrideSubcomponents = false;
	

	// User-given name of the component
	private String name = null;
	
	// User-specified comment
	private String comment = "";

	// Unique ID of the component
	private String id = null;
	
	////  NOTE !!!  All fields must be copied in the method copyFrom()!  ////
	
	
	
	/**
	 * Default constructor.  Sets the name of the component to the component's static name
	 * and the relative position of the component.
	 */
	public RocketComponent(Position relativePosition) {
		// These must not fire any events, due to Rocket undo system initialization
		this.name = getComponentName();
		this.relativePosition = relativePosition;
		this.id = UUID.randomUUID().toString();
	}
	
	
	
	
	
	////////////  Methods that must be implemented  ////////////


	/**
	 * Static component name.  The name may not vary of the parameters, it must be static.
	 */
	public abstract String getComponentName();  // Static component type name

	/**
	 * Return the component mass (regardless of mass overriding).
	 */
	public abstract double getComponentMass();  // Mass of non-overridden component

	/**
	 * Return the component CG and mass (regardless of CG or mass overriding).
	 */
	public abstract Coordinate getComponentCG();    // CG of non-overridden component
	
	
	/**
	 * Return the longitudal (around the y- or z-axis) unitary moment of inertia.  
	 * The unitary moment of inertia is the moment of inertia with the assumption that
	 * the mass of the component is one kilogram.  The inertia is measured in
	 * respect to the non-overridden CG.
	 * 
	 * @return   the longitudal unitary moment of inertia of this component.
	 */
	public abstract double getLongitudalUnitInertia();
	
	
	/**
	 * Return the rotational (around the x-axis) unitary moment of inertia.  
	 * The unitary moment of inertia is the moment of inertia with the assumption that
	 * the mass of the component is one kilogram.  The inertia is measured in
	 * respect to the non-overridden CG.
	 * 
	 * @return   the rotational unitary moment of inertia of this component.
	 */
	public abstract double getRotationalUnitInertia();
	
	
	
	
	/**
	 * Test whether the given component type can be added to this component.  This type safety
	 * is enforced by the <code>addChild()</code> methods.  The return value of this method
	 * may change to reflect the current state of this component (e.g. two components of some
	 * type cannot be placed as children).
	 * 
	 * @param type  The RocketComponent class type to add.
	 * @return      Whether such a component can be added.
	 */
	public abstract boolean isCompatible(Class<? extends RocketComponent> type);
	
	
	/* Non-abstract helper method */
	/**
	 * Test whether the given component can be added to this component.  This is equivalent
	 * to calling <code>isCompatible(c.getClass())</code>.
	 * 
	 * @param c  Component to test.
	 * @return   Whether the component can be added.
	 * @see #isCompatible(Class)
	 */
	public final boolean isCompatible(RocketComponent c) {
		return isCompatible(c.getClass());
	}
	
	
	
	/**
	 * Return a collection of bounding coordinates.  The coordinates must be such that
	 * the component is fully enclosed in their convex hull.
	 * 
	 * @return	a collection of coordinates that bound the component.
	 */
	public abstract Collection<Coordinate> getComponentBounds();

	/**
	 * Return true if the component may have an aerodynamic effect on the rocket.
	 */
	public abstract boolean isAerodynamic();

	/**
	 * Return true if the component may have an effect on the rocket's mass.
	 */
	public abstract boolean isMassive();
	
	
	
	

	////////////  Methods that may be overridden  ////////////

	
	/**
	 * Shift the coordinates in the array corresponding to radial movement.  A component
	 * that has a radial position must shift the coordinates in this array suitably.
	 * If the component is clustered, then a new array must be returned with a
	 * coordinate for each cluster.
	 * <p>
	 * The default implementation simply returns the array, and thus produces no shift.
	 * 
	 * @param c   an array of coordinates to shift.
	 * @return    an array of shifted coordinates.  The method may modify the contents
	 * 			  of the passed array and return the array itself.
	 */
	public Coordinate[] shiftCoordinates(Coordinate[] c) {
		return c;
	}
	
	
	/**
	 * Called when any component in the tree fires a ComponentChangeEvent.  This is by 
	 * default a no-op, but subclasses may override this method to e.g. invalidate 
	 * cached data.  The overriding method *must* call 
	 * <code>super.componentChanged(e)</code> at some point.
	 * 
	 * @param e  The event fired
	 */
	protected void componentChanged(ComponentChangeEvent e) {
		// No-op
	}
	

	
	
	/**
	 * Return a descriptive name of the component.
	 * 
	 * The description may include extra information about the type of component,
	 * e.g. "Conical nose cone".
	 * 
	 * @return A string describing the component.
	 */
	@Override
	public final String toString() {
		if (name.equals(""))
			return getComponentName();
		else
			return name;
	}

	
	public final void printStructure() {
		System.out.println("Rocket structure from '"+this.toString()+"':");
		printStructure(0);
	}
	
	private void printStructure(int level) {
		String s = "";
		
		for (int i=0; i < level; i++) {
			s += "  ";
		}
		s += this.toString() + " (" + this.getComponentName()+")";
		System.out.println(s);
		
		for (RocketComponent c: children) {
			c.printStructure(level+1);
		}
	}
	
	
	/**
	 * Make a deep copy of the rocket component tree structure from this component
	 * downwards.  This method does not fire any events.
	 * <p>
	 * This method must be overridden by any component that refers to mutable objects, 
	 * or if some fields should not be copied.  This should be performed by
	 * <code>RocketComponent c = super.copy();</code> and then cloning/modifying the
	 * appropriate fields.
	 * <p>
	 * This is not performed as serializing/deserializing for performance reasons.
	 * 
	 * @return A deep copy of the structure.
	 */
	public RocketComponent copy() {
		RocketComponent clone;
		try {
			clone = (RocketComponent)this.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException encountered, " +
					"report a bug!",e);
		}

		// Reset all parent/child information
		clone.parent = null;
		clone.children = new ArrayList<RocketComponent>();

		// Add copied children to the structure without firing events.
		for (RocketComponent child: this.children) {
			RocketComponent childCopy = child.copy();
			// Don't use add method since it fires events
			clone.children.add(childCopy);
			childCopy.parent = clone;
		}

		return clone;
	}


	//////////////  Methods that may not be overridden  ////////////
	
	

	////////// Common parameter setting/getting //////////
	
	/**
	 * Return the color of the object to use in 2D figures, or <code>null</code>
	 * to use the default color.
	 */
	public final Color getColor() {
		return color;
	}
	
	/**
	 * Set the color of the object to use in 2D figures.  
	 */
	public final void setColor(Color c) {
		if ((color == null && c == null) ||
				(color != null && color.equals(c)))
			return;
		
		this.color = c;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public final LineStyle getLineStyle() {
		return lineStyle;
	}
	
	public final void setLineStyle(LineStyle style) {
		if (this.lineStyle == style)
			return;
		this.lineStyle = style;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	
	
	
	/**
	 * Get the current override mass.  The mass is not necessarily in use
	 * at the moment.
	 * 
	 * @return  the override mass
	 */
	public final double getOverrideMass() {
		return overrideMass;
	}
	
	/**
	 * Set the current override mass.  The mass is not set to use by this
	 * method.
	 * 
	 * @param m  the override mass
	 */
	public final void setOverrideMass(double m) {
		overrideMass = Math.max(m,0);
		if (massOverriden)
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	/**
	 * Return whether mass override is active for this component.  This does NOT
	 * take into account whether a parent component is overriding the mass.
	 * 
	 * @return  whether the mass is overridden
	 */
	public final boolean isMassOverridden() {
		return massOverriden;
	}
	
	/**
	 * Set whether the mass is currently overridden.
	 * 
	 * @param o  whether the mass is overridden
	 */
	public final void setMassOverridden(boolean o) {
		if (massOverriden != o) {
			massOverriden = o;
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		}
	}

	
	
	
	
	/**
	 * Return the current override CG.  The CG is not necessarily overridden.
	 * 
	 * @return  the override CG
	 */
	public final Coordinate getOverrideCG() {
		return getComponentCG().setX(overrideCGX);
	}

	/**
	 * Return the x-coordinate of the current override CG.
	 * 
	 * @return	the x-coordinate of the override CG.
	 */
	public final double getOverrideCGX() {
		return overrideCGX;
	}
	
	/**
	 * Set the current override CG to (x,0,0).
	 * 
	 * @param x  the x-coordinate of the override CG to set.
	 */
	public final void setOverrideCGX(double x) {
		if (MathUtil.equals(overrideCGX, x))
			return;
		this.overrideCGX = x;
		if (isCGOverridden())
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	/**
	 * Return whether the CG is currently overridden.
	 * 
	 * @return  whether the CG is overridden
	 */
	public final boolean isCGOverridden() {
		return cgOverriden;
	}
	
	/**
	 * Set whether the CG is currently overridden.
	 * 
	 * @param o  whether the CG is overridden
	 */
	public final void setCGOverridden(boolean o) {
		if (cgOverriden != o) {
			cgOverriden = o;
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		}
	}

	
	
	/**
	 * Return whether the mass and/or CG override overrides all subcomponent values
	 * as well.  The default implementation is a normal getter/setter implementation,
	 * however, subclasses are allowed to override this behavior if some subclass
	 * always or never overrides subcomponents.  In this case the subclass should
	 * also override {@link #isOverrideSubcomponentsEnabled()} to return
	 * <code>false</code>.
	 * 
	 * @return	whether the current mass and/or CG override overrides subcomponents as well.
	 */
	public boolean getOverrideSubcomponents() {
		return overrideSubcomponents;
	}
	
	
	/**
	 * Set whether the mass and/or CG override overrides all subcomponent values
	 * as well.  See {@link #getOverrideSubcomponents()} for details.
	 * 
	 * @param override	whether the mass and/or CG override overrides all subcomponent.
	 */
	public void setOverrideSubcomponents(boolean override) {
		if (overrideSubcomponents != override) {
			overrideSubcomponents = override;
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		}
	}
	
	/**
	 * Return whether the option to override all subcomponents is enabled or not.
	 * The default implementation returns <code>false</code> if neither mass nor
	 * CG is overridden, <code>true</code> otherwise.
	 * <p>
	 * This method may be overridden if the setting of overriding subcomponents
	 * cannot be set.
	 * 
	 * @return	whether the option to override subcomponents is currently enabled.
	 */
	public boolean isOverrideSubcomponentsEnabled() {
		return isCGOverridden() || isMassOverridden();
	}
	
	
	
	
	/**
	 * Get the user-defined name of the component.
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Set the user-defined name of the component.  If name==null, sets the name to
	 * the default name, currently the component name.
	 */
	public final void setName(String name) {
//		System.out.println("Set name called:"+name+" orig:"+this.name);
		if (name==null || name.matches("^\\s*$"))
			this.name = getComponentName();
		else
			this.name = name;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	/**
	 * Return the comment of the component.  The component may contain multiple lines
	 * using \n as a newline separator.
	 * 
	 * @return  the comment of the component.
	 */
	public final String getComment() {
		return comment;
	}
	
	/**
	 * Set the comment of the component.
	 * 
	 * @param comment  the comment of the component.
	 */
	public final void setComment(String comment) {
		if (this.comment.equals(comment))
			return;
		if (comment == null)
			this.comment = "";
		else
			this.comment = comment;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	

	
	/**
	 * Returns the unique ID of the component.
	 * 
	 * @return	the ID of the component.
	 */
	public final String getID() {
		return id;
	}
	
	
	/**
	 * Set the unique ID of the component.  If <code>id</code> in <code>null</code> then
	 * this method generates a new unique ID for the component.
	 * <p>
	 * This method should be used only in special cases, such as when creating database
	 * entries with empty IDs.
	 * 
	 * @param id	the ID to set.
	 */
	public final void setID(String id) {
		if (id == null) {
			this.id = UUID.randomUUID().toString();
		} else {
			this.id = id;
		}
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);	 
	}
	
	
	
	
	/**
	 * Get the characteristic length of the component, for example the length of a body tube
	 * of the length of the root chord of a fin.  This is used in positioning the component
	 * relative to its parent.
	 * 
	 * If the length of a component is settable, the class must define the setter method
	 * itself.
	 */
	public final double getLength() {
		return length;
	}

	/**
	 * Get the positioning of the component relative to its parent component.
	 * This is one of the enums of {@link Position}.  A setter method is not provided,
	 * but can be provided by a subclass.
	 */
	public final Position getRelativePosition() {
		return relativePosition;
	}
	
	
	/**
	 * Set the positioning of the component relative to its parent component.
	 * The actual position of the component is maintained to the best ability.
	 * <p>
	 * The default implementation is of protected visibility, since many components
	 * do not support setting the relative position.  A component that does support
	 * it should override this with a public method that simply calls this
	 * supermethod AND fire a suitable ComponentChangeEvent.
	 * 
	 * @param position	the relative positioning.
	 */
	protected void setRelativePosition(RocketComponent.Position position) {
		if (this.relativePosition == position)
			return;
		
		// Update position so as not to move the component
		if (this.parent != null) {
			double thisPos = this.toRelative(Coordinate.NUL,this.parent)[0].x;

			switch (position) {
			case ABSOLUTE:
				this.position = this.toAbsolute(Coordinate.NUL)[0].x;
				break;
				
			case TOP:
				this.position = thisPos;
				break;
				
			case MIDDLE:
				this.position = thisPos - (this.parent.length - this.length)/2;
				break;
				
			case BOTTOM:
				this.position = thisPos - (this.parent.length - this.length);
				break;
				
			default:
				assert(false): "Should not occur";
			}
		}
		
		this.relativePosition = position;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	
	
	/**
	 * Get the position value of the component.  The exact meaning of the value is
	 * dependent on the current relative positioning.
	 * 
	 * @return  the positional value.
	 */
	public final double getPositionValue() {
		return position;
	}
	

	/**
	 * Set the position value of the component.  The exact meaning of the value
	 * depends on the current relative positioning.
	 * <p>
	 * The default implementation is of protected visibility, since many components
	 * do not support setting the relative position.  A component that does support
	 * it should override this with a public method that simply calls this
	 * supermethod AND fire a suitable ComponentChangeEvent.
	 * 
	 * @param value		the position value of the component.
	 */
	public void setPositionValue(double value) {
		if (MathUtil.equals(this.position, value))
			return;
		this.position = value;
	}

	
	
	///////////  Coordinate changes  ///////////

	/**
	 * Returns coordinate c in absolute coordinates.  Equivalent to toComponent(c,null).
	 */
	public Coordinate[] toAbsolute(Coordinate c) {
		return toRelative(c,null);
	}
	

	/**
	 * Return coordinate <code>c</code> described in the coordinate system of 
	 * <code>dest</code>.  If <code>dest</code> is <code>null</code> returns
	 * absolute coordinates.
	 * <p>
	 * This method returns an array of coordinates, each of which represents a
	 * position of the coordinate in clustered cases.  The array is guaranteed
	 * to contain at least one element.  
	 * <p>
	 * The current implementation does not support rotating components.
	 * 
	 * @param c    Coordinate in the component's coordinate system.
	 * @param dest Destination component coordinate system.
	 * @return     an array of coordinates describing <code>c</code> in coordinates
	 * 			   relative to <code>dest</code>.
	 */
	public final Coordinate[] toRelative(Coordinate c, RocketComponent dest) {
		double absoluteX = Double.NaN;
		RocketComponent search = dest;
		Coordinate[] array = new Coordinate[1];
		array[0] = c;
		
		RocketComponent component = this;
		while ((component != search) && (component.parent != null)) {

			array = component.shiftCoordinates(array);
			
			switch (component.relativePosition) {
			case TOP:
				for (int i=0; i < array.length; i++) {
					array[i] = array[i].add(component.position,0,0);
				}
				break;
				
			case MIDDLE:
				for (int i=0; i < array.length; i++) {
					array[i] = array[i].add(component.position + 
							(component.parent.length-component.length)/2,0,0);
				}
				break;
				
			case BOTTOM:
				for (int i=0; i < array.length; i++) {
					array[i] = array[i].add(component.position + 
							(component.parent.length-component.length),0,0);
				}
				break;
				
			case AFTER:
				// Add length of all previous brother-components with POSITION_RELATIVE_AFTER
				int index = component.parent.children.indexOf(component);
				assert(index >= 0);
				for (index--; index >= 0; index--) {
					RocketComponent comp = component.parent.children.get(index);
					double length = comp.getTotalLength();
					for (int i=0; i < array.length; i++) {
						array[i] = array[i].add(length,0,0);
					}
				}
				for (int i=0; i < array.length; i++) {
					array[i] = array[i].add(component.position + component.parent.length,0,0);
				}
				break;
				
			case ABSOLUTE:
				search = null;  // Requires back-search if dest!=null
				if (Double.isNaN(absoluteX)) {
					absoluteX = component.position;
				}
				break;
				
			default:
				throw new BugException("Unknown relative positioning type of component"+
						component+": "+component.relativePosition);
			}

			component = component.parent;  // parent != null
		}

		if (!Double.isNaN(absoluteX)) {
			for (int i=0; i < array.length; i++) {
				array[i] = array[i].setX(absoluteX + c.x);
			}
		}

		// Check whether destination has been found or whether to backtrack
		// TODO: LOW: Backtracking into clustered components uses only one component 
		if ((dest != null) && (component != dest)) {
			Coordinate[] origin = dest.toAbsolute(Coordinate.NUL);
			for (int i=0; i < array.length; i++) {
				array[i] = array[i].sub(origin[0]);
			}
		}
		
		return array;
	}
	
	
	/**
	 * Recursively sum the lengths of all subcomponents that have position 
	 * Position.AFTER.
	 * 
	 * @return  Sum of the lengths.
	 */
	private final double getTotalLength() {
		double l=0;
		if (relativePosition == Position.AFTER)
			l = length;
		for (int i=0; i<children.size(); i++)
			l += children.get(i).getTotalLength();
		return l;
	}
	

	
	/////////// Total mass and CG calculation ////////////

	/**
	 * Return the (possibly overridden) mass of component.
	 * 
	 * @return The mass of the component or the given override mass.
	 */
	public final double getMass() {
		if (massOverriden)
			return overrideMass;
		return getComponentMass();
	}
	
	/**
	 * Return the (possibly overridden) center of gravity and mass.
	 * 
	 * Returns the CG with the weight of the coordinate set to the weight of the component.
	 * Both CG and mass may be separately overridden.
	 * 
	 * @return The CG of the component or the given override CG.
	 */
	public final Coordinate getCG() {
		if (cgOverriden)
			return getOverrideCG().setWeight(getMass());

		if (massOverriden)
			return getComponentCG().setWeight(getMass());
		
		return getComponentCG();
	}
	

	/**
	 * Return the longitudal (around the y- or z-axis) moment of inertia of this component.
	 * The moment of inertia is scaled in reference to the (possibly overridden) mass
	 * and is relative to the non-overridden CG.
	 * 
	 * @return    the longitudal moment of inertia of this component.
	 */
	public final double getLongitudalInertia() {
		return getLongitudalUnitInertia() * getMass();
	}
	
	/**
	 * Return the rotational (around the y- or z-axis) moment of inertia of this component.
	 * The moment of inertia is scaled in reference to the (possibly overridden) mass
	 * and is relative to the non-overridden CG.
	 * 
	 * @return    the rotational moment of inertia of this component.
	 */
	public final double getRotationalInertia() {
		return getRotationalUnitInertia() * getMass();
	}
	
	
	
	///////////  Children handling  ///////////
	

	/**
	 * Adds a child to the rocket component tree.  The component is added to the end
	 * of the component's child list.  This is a helper method that calls 
	 * {@link #addChild(RocketComponent,int)}.
	 * 
	 * @param component  The component to add.
	 * @throws IllegalArgumentException  if the component is already part of some 
	 * 									 component tree.
	 * @see #addChild(RocketComponent,int)
	 */
	public final void addChild(RocketComponent component) {
		addChild(component,children.size());
	}

	
	/**
	 * Adds a child to the rocket component tree.  The component is added to 
	 * the given position of the component's child list.
	 * <p>
	 * This method may be overridden to enforce more strict component addition rules.  
	 * The tests should be performed first and then this method called.
	 * 
	 * @param component  The component to add.
	 * @param position   Position to add component to.
	 * @throws IllegalArgumentException  If the component is already part of 
	 * 									 some component tree.
	 */
	public void addChild(RocketComponent component, int position) {
		if (component.parent != null) {
			throw new IllegalArgumentException("component "+component.getComponentName()+
					" is already in a tree");
		}
		if (!isCompatible(component)) {
			throw new IllegalStateException("Component "+component.getComponentName()+
					" not currently compatible with component "+getComponentName());
		}
		
		children.add(position,component);
		component.parent = this;
		
		fireAddRemoveEvent(component);
	}
	
	
	/**
	 * Removes a child from the rocket component tree.
	 * 
	 * @param n  remove the n'th child.
	 * @throws IndexOutOfBoundsException  if n is out of bounds
	 */
	public final void removeChild(int n) {
		RocketComponent component = children.remove(n);
		component.parent = null;
		fireAddRemoveEvent(component);
	}
	
	/**
	 * Removes a child from the rocket component tree.  Does nothing if the component
	 * is not present as a child.
	 * 
	 * @param component  the component to remove
	 */
	public final void removeChild(RocketComponent component) {
		if (children.remove(component)) {
			component.parent = null;
			
			fireAddRemoveEvent(component);
		}
	}

	

	
	/**
	 * Move a child to another position.
	 * 
	 * @param component	the component to move
	 * @param position	the component's new position
	 * @throws IllegalArgumentException If an illegal placement was attempted.
	 */
	public final void moveChild(RocketComponent component, int position) {
		if (children.remove(component)) {
			children.add(position, component);
			fireAddRemoveEvent(component);
		}
	}
	
	
	/**
	 * Fires an AERODYNAMIC_CHANGE, MASS_CHANGE or OTHER_CHANGE event depending on the
	 * type of component removed.
	 */
	private void fireAddRemoveEvent(RocketComponent component) {
		Iterator<RocketComponent> iter = component.deepIterator(true);
		int type = ComponentChangeEvent.TREE_CHANGE;
		while (iter.hasNext()) {
			RocketComponent c = iter.next();
			if (c.isAerodynamic())
				type |= ComponentChangeEvent.AERODYNAMIC_CHANGE;
			if (c.isMassive())
				type |= ComponentChangeEvent.MASS_CHANGE;
		}
		
		fireComponentChangeEvent(type);
	}
	
	
	public final int getChildCount() {
		return children.size();
	}
	
	public final RocketComponent getChild(int n) {
		return children.get(n);
	}
	
	public final RocketComponent[] getChildren() {
		return children.toArray(new RocketComponent[0]);
	}
	
	
	/**
	 * Returns the position of the child in this components child list, or -1 if the
	 * component is not a child of this component.
	 * 
	 * @param child  The child to search for.
	 * @return  Position in the list or -1 if not found.
	 */
	public final int getChildPosition(RocketComponent child) {
		return children.indexOf(child);
	}
	
	/**
	 * Get the parent component of this component.  Returns <code>null</code> if the component
	 * has no parent.
	 * 
	 * @return  The parent of this component or <code>null</code>.
	 */
	public final RocketComponent getParent() {
		return parent;
	}
	
	/**
	 * Get the root component of the component tree.
	 * 
	 * @return  The root component of the component tree.
	 */
	public final RocketComponent getRoot() {
		RocketComponent gp = this;
		while (gp.parent != null)
			gp = gp.parent;
		return gp;
	}
	
	/**
	 * Returns the root Rocket component of this component tree.  Throws an 
	 * IllegalStateException if the root component is not a Rocket.
	 * 
	 * @return  The root Rocket component of the component tree.
	 * @throws  IllegalStateException  If the root component is not a Rocket.
	 */
	public final Rocket getRocket() {
		RocketComponent r = getRoot();
		if (r instanceof Rocket)
			return (Rocket)r;
		throw new IllegalStateException("getRocket() called with root component "
				+r.getComponentName());
	}
	
	
	/**
	 * Return the Stage component that this component belongs to.  Throws an
	 * IllegalStateException if a Stage is not in the parentage of this component.
	 * 
	 * @return	The Stage component this component belongs to.
	 * @throws	IllegalStateException   if a Stage component is not in the parentage.
	 */
	public final Stage getStage() {
		RocketComponent c = this;
		while (c != null) {
			if (c instanceof Stage)
				return (Stage)c;
			c = c.getParent();
		}
		throw new IllegalStateException("getStage() called without Stage as a parent.");
	}
	
	/**
	 * Return the stage number of the stage this component belongs to.  The stages
	 * are numbered from zero upwards.
	 * 
	 * @return   the stage number this component belongs to.
	 */
	public final int getStageNumber() {
		if (parent == null) {
			throw new IllegalArgumentException("getStageNumber() called for root component");
		}
		
		RocketComponent stage = this;
		while (!(stage instanceof Stage)) {
			stage = stage.parent;
			if (stage == null || stage.parent == null) {
				throw new IllegalStateException("getStageNumber() could not find parent " +
						"stage.");
			}
		}
		return stage.parent.getChildPosition(stage);
	}
	
	
	/**
	 * Find a component with the given ID.  The component tree is searched from this component
	 * down (including this component) for the ID and the corresponding component is returned,
	 * or null if not found.
	 * 
	 * @param id  ID to search for.
	 * @return    The component with the ID, or null if not found.
	 */
	public final RocketComponent findComponent(String id) {
		Iterator<RocketComponent> iter = this.deepIterator(true);
		while (iter.hasNext()) {
			RocketComponent c = iter.next();
			if (c.id.equals(id))
				return c;
		}
		return null;
	}

	
	public final RocketComponent getPreviousComponent() {
		if (parent == null)
			return null;
		int pos = parent.getChildPosition(this);
		if (pos < 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Inconsistent internal state: ");
			sb.append("this=").append(this).append('[')
				.append(System.identityHashCode(this)).append(']');
			sb.append(" parent.children=[");
			for (int i=0; i < parent.children.size(); i++) {
				RocketComponent c = parent.children.get(i);
				sb.append(c).append('[').append(System.identityHashCode(c)).append(']');
				if (i < parent.children.size()-1)
					sb.append(", ");
			}
			sb.append(']');
			throw new IllegalStateException(sb.toString());
		}
		assert(pos >= 0);
		if (pos == 0)
			return parent;
		RocketComponent c = parent.getChild(pos-1);
		while (c.getChildCount() > 0)
			c = c.getChild(c.getChildCount()-1);
		return c;
	}
	
	public final RocketComponent getNextComponent() {
		if (getChildCount() > 0)
			return getChild(0);
		
		RocketComponent current = this;
		RocketComponent parent = this.parent;
		
		while (parent != null) {
			int pos = parent.getChildPosition(current);
			if (pos < parent.getChildCount()-1)
				return parent.getChild(pos+1);
				
			current = parent;
			parent = current.parent;
		}
		return null;
	}
	
	
	///////////  Event handling  //////////
	//
	// Listener lists are provided by the root Rocket component,
	// a single listener list for the whole rocket.
	//
	
	/**
	 * Adds a ComponentChangeListener to the rocket tree.  The listener is added to the root
	 * component, which must be of type Rocket (which overrides this method).  Events of all
	 * subcomponents are sent to all listeners.
	 * 
	 * @throws IllegalStateException - if the root component is not a Rocket
	 */
	public void addComponentChangeListener(ComponentChangeListener l) {
		getRocket().addComponentChangeListener(l);
	}
	
	/**
	 * Removes a ComponentChangeListener from the rocket tree.  The listener is removed from
	 * the root component, which must be of type Rocket (which overrides this method).
	 * Does nothing if the root component is not a Rocket.  (The asymmetry is so
	 * that listeners can always be removed just in case.)
	 * 
	 * @param l  Listener to remove
	 */
	public void removeComponentChangeListener(ComponentChangeListener l) {
		if (parent != null) {
			getRoot().removeComponentChangeListener(l);
		}
	}
	

	/**
	 * Adds a <code>ChangeListener</code> to the rocket tree.  This is identical to 
	 * <code>addComponentChangeListener()</code> except that it uses a 
	 * <code>ChangeListener</code>.  The same events are dispatched to the
	 * <code>ChangeListener</code>, as <code>ComponentChangeEvent</code> is a subclass 
	 * of <code>ChangeEvent</code>.
	 * 
	 * @throws IllegalStateException - if the root component is not a <code>Rocket</code>
	 */
	public void addChangeListener(ChangeListener l) {
		getRocket().addChangeListener(l);
	}
	
	/**
	 * Removes a ChangeListener from the rocket tree.  This is identical to
	 * removeComponentChangeListener() except it uses a ChangeListener.
	 * Does nothing if the root component is not a Rocket.  (The asymmetry is so
	 * that listeners can always be removed just in case.)
	 * 
	 * @param l  Listener to remove
	 */
	public void removeChangeListener(ChangeListener l) {
		if (this.parent != null) {
			getRoot().removeChangeListener(l);
		}
	}
	
	
	/**
	 * Fires a ComponentChangeEvent on the rocket structure.  The call is passed to the 
	 * root component, which must be of type Rocket (which overrides this method).
	 * Events of all subcomponents are sent to all listeners.
	 * 
	 * If the component tree root is not a Rocket, the event is ignored.  This is the 
	 * case when constructing components not in any Rocket tree.  In this case it 
	 * would be impossible for the component to have listeners in any case.
	 *  
	 * @param e  Event to send
	 */
	protected void fireComponentChangeEvent(ComponentChangeEvent e) {
		if (parent==null) {
			/* Ignore if root invalid. */
			return;
		}
		getRoot().fireComponentChangeEvent(e);
	}

	
	/**
	 * Fires a ComponentChangeEvent of the given type.  The source of the event is set to
	 * this component.
	 * 
	 * @param type  Type of event
	 * @see #fireComponentChangeEvent(ComponentChangeEvent)
	 */
	protected void fireComponentChangeEvent(int type) {
		fireComponentChangeEvent(new ComponentChangeEvent(this,type));
	}
	

	
	///////////  Iterator implementation  //////////
	
	/**
	 * Private inner class to implement the Iterator.
	 * 
	 * This iterator is fail-fast if the root of the structure is a Rocket.
	 */
	private class RocketComponentIterator implements Iterator<RocketComponent> {
		// Stack holds iterators which still have some components left.
		private final Stack<Iterator<RocketComponent>> iteratorstack =
					new Stack<Iterator<RocketComponent>>();
		
		private final Rocket root;
		private final int treeModID;

		private final RocketComponent original;
		private boolean returnSelf=false;
		
		// Construct iterator with component's child's iterator, if it has elements
		public RocketComponentIterator(RocketComponent c, boolean returnSelf) {
			
			RocketComponent gp = c.getRoot();
			if (gp instanceof Rocket) {
				root = (Rocket)gp;
				treeModID = root.getTreeModID();
			} else {
				root = null;
				treeModID = -1;
			}
			
			Iterator<RocketComponent> i = c.children.iterator();
			if (i.hasNext())
				iteratorstack.push(i);
			
			this.original = c;
			this.returnSelf = returnSelf;
		}
		
		public boolean hasNext() {
			checkID();
			if (returnSelf)
				return true;
			return !iteratorstack.empty();  // Elements remain if stack is not empty
		}

		public RocketComponent next() {
			Iterator<RocketComponent> i;

			checkID();
			
			// Return original component first
			if (returnSelf) {
				returnSelf=false;
				return original;
			}
			
			// Peek first iterator from stack, throw exception if empty
			try {
				i = iteratorstack.peek();
			} catch (EmptyStackException e) {
				throw new NoSuchElementException("No further elements in " +
						"RocketComponent iterator");
			}
			
			// Retrieve next component of the iterator, remove iterator from stack if empty
			RocketComponent c = i.next();
			if (!i.hasNext())
				iteratorstack.pop();
			
			// Add iterator of component children to stack if it has children
			i = c.children.iterator();
			if (i.hasNext())
				iteratorstack.push(i);
			
			return c;
		}

		private void checkID() {
			if (root != null) {
				if (root.getTreeModID() != treeModID) {
					throw new IllegalStateException("Rocket modified while being iterated");
				}
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported by " +
					"RocketComponent iterator");
		}
	}

	/**
	 * Returns an iterator that iterates over all children and sub-children.
	 * 
	 * The iterator iterates through all children below this object, including itself if
	 * returnSelf is true.  The order of the iteration is not specified
	 * (it may be specified in the future).
	 * 
	 * If an iterator iterating over only the direct children of the component is required,
	 * use  component.getChildren().iterator()
	 * 
	 * @param returnSelf boolean value specifying whether the component itself should be 
	 * 					 returned
	 * @return An iterator for the children and sub-children.
	 */
	public final Iterator<RocketComponent> deepIterator(boolean returnSelf) {
		return new RocketComponentIterator(this,returnSelf);
	}
	
	/**
	 * Returns an iterator that iterates over all children and sub-children.
	 * 
	 * The iterator does NOT return the component itself.  It is thus equivalent to
	 * deepIterator(false).
	 * 
	 * @see #iterator()
	 * @return An iterator for the children and sub-children.
	 */
	public final Iterator<RocketComponent> deepIterator() {
		return new RocketComponentIterator(this,false);
	}
	
	
	/**
	 * Return an iterator that iterates of the children of the component.  The iterator
	 * does NOT recurse to sub-children nor return itself.
	 * 
	 * @return An iterator for the children.
	 */
	public final Iterator<RocketComponent> iterator() {
		return Collections.unmodifiableList(children).iterator();
	}
	
	////////////  Helper methods for subclasses
	
	/**
	 * Helper method to add rotationally symmetric bounds at the specified coordinates.
	 * The X-axis value is <code>x</code> and the radius at the specified position is
	 * <code>r</code>. 
	 */
	protected static final void addBound(Collection<Coordinate> bounds, double x, double r) {
		bounds.add(new Coordinate(x,-r,-r));
		bounds.add(new Coordinate(x, r,-r));
		bounds.add(new Coordinate(x, r, r));
		bounds.add(new Coordinate(x,-r, r));
	}
	
	
	protected static final Coordinate ringCG(double outerRadius, double innerRadius, 
			double x1, double x2, double density) {
		return new Coordinate((x1+x2)/2, 0, 0, 
				ringMass(outerRadius, innerRadius, x2-x1, density));
	}
	
	protected static final double ringMass(double outerRadius, double innerRadius,
			double length, double density) {
		return Math.PI*(MathUtil.pow2(outerRadius) - MathUtil.pow2(innerRadius)) *
					length * density;
	}

	protected static final double ringLongitudalUnitInertia(double outerRadius, 
			double innerRadius, double length) {
		// 1/12 * (3 * (r1^2 + r2^2) + h^2)
		return (3 * (MathUtil.pow2(innerRadius) + MathUtil.pow2(outerRadius)) +
				MathUtil.pow2(length)) / 12;
	}

	protected static final double ringRotationalUnitInertia(double outerRadius, 
			double innerRadius) {
		// 1/2 * (r1^2 + r2^2)
		return (MathUtil.pow2(innerRadius) + MathUtil.pow2(outerRadius))/2;
	}


	
	////////////  OTHER

	
	/**
	 * Loads the RocketComponent fields from the given component.  This method is meant
	 * for in-place replacement of a component.  It is used with the undo/redo
	 * mechanism and when converting a finset into a freeform fin set.
	 * This component must not have a parent, otherwise this method will fail.
	 * <p>
	 * The fields are copied by reference, and the supplied component must not be used
	 * after the call, as it is in an undefined state.
	 * 
	 * TODO: MEDIUM: Make general to copy all private/protected fields...
	 */
	protected void copyFrom(RocketComponent src) {
		
		if (this.parent != null) {
			throw new UnsupportedOperationException("copyFrom called for non-root component " 
					+ this);
		}
		
		// Set parents and children
		this.children = src.children;
		src.children = new ArrayList<RocketComponent>();
		
		for (RocketComponent c: this.children) {
			c.parent = this;
		}

		// Set all parameters
		this.length = src.length;
		this.relativePosition = src.relativePosition;
		this.position = src.position;
		this.color = src.color;
		this.lineStyle = src.lineStyle;
		this.overrideMass = src.overrideMass;
		this.massOverriden = src.massOverriden;
		this.overrideCGX = src.overrideCGX;
		this.cgOverriden = src.cgOverriden;
		this.overrideSubcomponents = src.overrideSubcomponents;
		this.name = src.name;
		this.comment = src.comment;
		this.id = src.id;
	}
	
}
