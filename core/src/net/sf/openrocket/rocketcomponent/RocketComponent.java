package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.ComponentChangeAdapter;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Invalidator;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.SafetyMutex;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.UniqueID;

/**
 * 	Master class that defines components of rockets
 *	almost all hardware from the rocket extends from this abstract class
 *	
 */
public abstract class RocketComponent implements ChangeSource, Cloneable, Iterable<RocketComponent> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RocketComponent.class);
	
	// Because of changes to Java 1.7.0-45's mechanism to construct DataFlavor objects (used in Drag and Drop)
	// We cannot access static members of the Application object in this class.  Instead of holding
	// on to the Translator object, we'll just use when we need it.
	//private static final Translator trans = Application.getTranslator();
	
	/**
	 * A safety mutex that can be used to prevent concurrent access to this component.
	 */
	protected SafetyMutex mutex = SafetyMutex.newInstance();
	
	////////  Parent/child trees
	/**
	 * Parent component of the current component, or null if none exists.
	 */
	protected RocketComponent parent = null;
	
	/**
	 * List of child components of this component.
	 */
	protected ArrayList<RocketComponent> children = new ArrayList<RocketComponent>();
	
	
	////////  Parameters common to all components:
	
	/**
	 * Characteristic length of the component.  This is used in calculating the coordinate
	 * transformations and positions of other components in reference to this component.
	 * This may and should be used as the "true" length of the component, where applicable.
	 * By default it is zero, i.e. no translation.
	 */
	protected double length = 0;
	
	/**
	 * How this component is axially positioned, possibly in relative to the parent component.
	 */
	protected AxialMethod axialMethod = AxialMethod.AFTER;
	
	/**
	 * Offset of the position of this component relative to the normal position given by
	 * relativePosition.  By default zero, i.e. no position change.
	 */
	protected double axialOffset = 0;
	
	/**
	 * Position of this component relative to it's parent.  
	 * In case (null == parent ): i.e. the Rocket/root component, the position is constrained to 0,0,0, and is the reference origin for the entire rocket.
	 * Defaults to (0,0,0)
	 */
	protected Coordinate position = new Coordinate();
	
	// Color of the component, null means to use the default color
	private Color color = null;
	private LineStyle lineStyle = null;
	
	
	// Override mass/CG
	private double overrideMass = 0;
	private boolean massOverriden = false;
	private double overrideCGX = 0;
	private boolean cgOverriden = false;
	private double overrideCD = 0;
	private boolean cdOverriden = false;
	
	private boolean overrideSubcomponents = false;
	
	
	// User-given name of the component
	private String name = null;
	
	// User-specified comment
	private String comment = "";
	
	// Unique ID of the component
	private String id = null;
	
	// Preset component this component is based upon
	private ComponentPreset presetComponent = null;
	
	// The realistic appearance of this component
	private Appearance appearance = null;
	
	
	/**
	 * Used to invalidate the component after calling {@link #copyFrom(RocketComponent)}.
	 */
	private Invalidator invalidator = new Invalidator(this);
	
	
	////  NOTE !!!  All fields must be copied in the method copyFrom()!  ////
	
	
	
	/**
	 * Default constructor.  Sets the name of the component to the component's static name
	 * and the relative position of the component.
	 */
	public RocketComponent( AxialMethod newAxialMethod ) {
		// These must not fire any events, due to Rocket undo system initialization
		this.name = getComponentName();
		this.axialMethod = newAxialMethod;
		newID();
	}
	
	////////////  Methods that must be implemented  ////////////
	
	
	/**
	 * Static component name.  The name may not vary of the parameters, it must be static.
	 */
	public abstract String getComponentName(); // Static component type name
	
	/**
	 * Return the component mass (regardless of mass overriding).
	 */
	public abstract double getComponentMass(); // Mass of non-overridden component
	
	/**
	 * Return the component CG and mass (regardless of CG or mass overriding).
	 */
	public abstract Coordinate getComponentCG(); // CG of non-overridden component
	
	
	/**
	 * Return the longitudinal (around the y- or z-axis) unitary moment of inertia.
	 * The unitary moment of inertia is the moment of inertia with the assumption that
	 * the mass of the component is one kilogram.  The inertia is measured in
	 * respect to the non-overridden CG.
	 *
	 * @return   the longitudinal unitary moment of inertia of this component.
	 */
	public abstract double getLongitudinalUnitInertia();
	
	
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
	 * Test whether this component allows any children components.  This method must
	 * return true if and only if {@link #isCompatible(Class)} returns true for any
	 * rocket component class.
	 *
	 * @return	<code>true</code> if children can be attached to this component, <code>false</code> otherwise.
	 */
	public abstract boolean allowsChildren();
	
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
		mutex.verify();
		return isCompatible(c.getClass());
	}
	
	
	
	/**
	 * Return a collection of bounding coordinates.  The coordinates must be such that
	 * the component is fully enclosed in their convex hull.
	 * 
	 * Note: this function gets the bounds only for this component.  Subchildren must be called individually.
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
	 * Convenience method.   
	 *   
	 * @return indicates if a component is positioned via AFTER
	 */
	public boolean isAfter(){ 
		return (AxialMethod.AFTER == this.axialMethod );
	}

	public boolean isAxisymmetric(){
		return true;
	}
	
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
//	protected Coordinate[] shiftCoordinates(Coordinate[] c) {
//		checkState();
//		return c;
//	}
	
	
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
		checkState();
		update();
	}
	
	
	
	/**
	 * Return the user-provided name of the component, or the component base
	 * name if the user-provided name is empty.  This can be used in the UI.
	 *
	 * @return A string describing the component.
	 */
	@Override
	public final String toString() {
		mutex.verify();
		if (name.length() == 0)
			return getComponentName();
		else
			return name;
	}
	
	
	/**
	 * Create a string describing the basic component structure from this component downwards.
	 * @return	a string containing the rocket structure
	 */
	public final String toDebugString() {
		mutex.lock("toDebugString");
		try {
			StringBuilder sb = new StringBuilder();
			toDebugString(sb);
			return sb.toString();
		} finally {
			mutex.unlock("toDebugString");
		}
	}
	
	/**
	 * appends the debug string of the component into the passed builder
	 * @param sb	String builder to be appended
	 */
	private void toDebugString(StringBuilder sb) {
		sb.append(this.getClass().getSimpleName()).append('@').append(System.identityHashCode(this));
		sb.append("[\"").append(this.getName()).append('"');
		for (RocketComponent c : this.children) {
			sb.append("; ");
			c.toDebugString(sb);
		}
		sb.append(']');
	}
	
	
	/**
	 * Make a deep copy of the rocket component tree structure from this component
	 * downwards for copying purposes.  Each component in the copy will be assigned
	 * a new component ID, making it a safe copy.  This method does not fire any events.
	 *
	 * @return A deep copy of the structure.
	 */
	public final RocketComponent copy() {
		RocketComponent clone = copyWithOriginalID();
		
		Iterator<RocketComponent> iterator = clone.iterator(true);
		while (iterator.hasNext()) {
			iterator.next().newID();
		}
		return clone;
	}
	
	
	
	/**
	 * Make a deep copy of the rocket component tree structure from this component
	 * downwards while maintaining the component ID's.  The purpose of this method is
	 * to allow copies to be created with the original ID's for the purpose of the
	 * undo/redo mechanism.  This method should not be used for other purposes,
	 * such as copy/paste.  This method does not fire any events.
	 * <p>
	 * This method must be overridden by any component that refers to mutable objects,
	 * or if some fields should not be copied.  This should be performed by
	 * <code>RocketComponent c = super.copyWithOriginalID();</code> and then cloning/modifying
	 * the appropriate fields.
	 * <p>
	 * This is not performed as serializing/deserializing for performance reasons.
	 *
	 * @return A deep copy of the structure.
	 */
	protected RocketComponent copyWithOriginalID() {
		mutex.lock("copyWithOriginalID");
		try {
			checkState();
			RocketComponent clone;
			try {
				clone = (RocketComponent) this.clone();
			} catch (CloneNotSupportedException e) {
				throw new BugException("CloneNotSupportedException encountered, report a bug!", e);
			}
			
			// Reset the mutex
			clone.mutex = SafetyMutex.newInstance();
			
			// Reset all parent/child information
			clone.parent = null;
			clone.children = new ArrayList<RocketComponent>();
			
			// Add copied children to the structure without firing events.
			for (RocketComponent child : this.children) {
				RocketComponent childCopy = child.copyWithOriginalID();
				// Don't use addChild(...) method since it fires events
				clone.children.add(childCopy);
				childCopy.parent = clone;
			}
			
			this.checkComponentStructure();
			clone.checkComponentStructure();
			
			return clone;
		} finally {
			mutex.unlock("copyWithOriginalID");
		}
	}
	
	
	//////////////  Methods that may not be overridden  ////////////
	
	
	
	////////// Common parameter setting/getting //////////
	
	/**
	 * Get the realistic appearance of this component.
	 *  <code>null</code> = use the default for this material
	 *
	 * @return The component's realistic appearance, or <code>null</code>
	 */
	public Appearance getAppearance() {
		return appearance;
	}
	
	/**
	 * Set the realistic appearance of this component.
	 * Use <code>null</code> for default.
	 *
	 * @param appearance
	 */
	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
		if (this.appearance != null) {
			Decal d = this.appearance.getTexture();
			if (d != null) {
				d.getImage().addChangeListener(new StateChangeListener() {
					
					@Override
					public void stateChanged(EventObject e) {
						fireComponentChangeEvent(ComponentChangeEvent.TEXTURE_CHANGE);
					}
					
				});
			}
		}
		// CHECK - should this be a TEXTURE_CHANGE and not NONFUNCTIONAL_CHANGE?
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	/**
	 * Return the color of the object to use in 2D figures, or <code>null</code>
	 * to use the default color.
	 */
	public final Color getColor() {
		mutex.verify();
		return color;
	}
	
	/**
	 * Set the color of the object to use in 2D figures.
	 */
	public final void setColor(Color c) {
		if ((color == null && c == null) ||
				(color != null && color.equals(c)))
			return;
		
		checkState();
		this.color = c;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public final LineStyle getLineStyle() {
		mutex.verify();
		return lineStyle;
	}
	
	public final void setLineStyle(LineStyle style) {
		if (this.lineStyle == style)
			return;
		checkState();
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
		mutex.verify();
		return overrideMass;
	}
	
	/**
	 * Set the current override mass.  The mass is not set to use by this
	 * method.
	 *
	 * @param m  the override mass
	 */
	public final void setOverrideMass(double m) {
		if (MathUtil.equals(m, overrideMass))
			return;
		checkState();
		overrideMass = Math.max(m, 0);
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
		mutex.verify();
		return massOverriden;
	}
	
	/**
	 * Set whether the mass is currently overridden.
	 *
	 * @param o  whether the mass is overridden
	 */
	public final void setMassOverridden(boolean o) {
		if (massOverriden == o) {
			return;
		}
		checkState();
		massOverriden = o;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	
	
	
	/**
	 * Return the current override CG.  The CG is not necessarily overridden.
	 *
	 * @return  the override CG
	 */
	public final Coordinate getOverrideCG() {
		mutex.verify();
		return getComponentCG().setX(overrideCGX);
	}
	
	/**
	 * Return the x-coordinate of the current override CG.
	 *
	 * @return	the x-coordinate of the override CG.
	 */
	public final double getOverrideCGX() {
		mutex.verify();
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
		checkState();
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
		mutex.verify();
		return cgOverriden;
	}
	
	/**
	 * Set whether the CG is currently overridden.
	 *
	 * @param o  whether the CG is overridden
	 */
	public final void setCGOverridden(boolean o) {
		if (cgOverriden == o) {
			return;
		}
		checkState();
		cgOverriden = o;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}



	/** Return the current override CD. The CD is not neccesarily overriden.
	 * 
	 * @return the override CG.
	 */
	public final double getOverrideCD() {
		mutex.verify();
		return overrideCD;
	}

	/**
	 * Set the current override CD to x.
	 *
	 * @param x the override CD to set.
	 */
	public final void setOverrideCD(double x) {
		if (MathUtil.equals(overrideCD, x))
			return;
		checkState();
		this.overrideCD = x;
		if (isCDOverridden())
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
		


	/**
	 * Return whether the CD is currently overriden.
	 * 
	 * @return whether the CD is overridden
	 */
	public final boolean isCDOverridden() {
		mutex.verify();
		return cdOverriden;
	}


	/**
	 * Set whether the CD is currently overriden.
	 *
	 * @param o whether the CD is overriden
	 */
	public final void setCDOverridden(boolean o) {
		if(cdOverriden == o) {
			return;
		}
		checkState();
		cdOverriden = o;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
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
		mutex.verify();
		return overrideSubcomponents;
	}
	
	
	/**
	 * Set whether the mass and/or CG override overrides all subcomponent values
	 * as well.  See {@link #getOverrideSubcomponents()} for details.
	 *
	 * @param override	whether the mass and/or CG override overrides all subcomponent.
	 */
	public void setOverrideSubcomponents(boolean override) {
		if (overrideSubcomponents == override) {
			return;
		}
		checkState();
		overrideSubcomponents = override;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
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
		mutex.verify();
		return isCGOverridden() || isMassOverridden();
	}
	
	/** 
	 * placeholder. This allows code to generally test if this component represents multiple instances with just one function call. 
	 * 
	 * @return number of instances
	 */
	public int getInstanceCount() {
		return 1;
	}
	
	/**
	 * Get the user-defined name of the component.
	 */
	public final String getName() {
		mutex.verify();
		return name;
	}
	
	/**
	 * Set the user-defined name of the component.  If name==null, sets the name to
	 * the default name, currently the component name.
	 */
	public final void setName(String name) {
		if (this.name.equals(name)) {
			return;
		}
		checkState();
		if (name == null || name.matches("^\\s*$"))
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
		mutex.verify();
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
		checkState();
		if (comment == null)
			this.comment = "";
		else
			this.comment = comment;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	
	/**
	 * Return the preset component that this component is based upon.
	 * 
	 * @return	the preset component, or <code>null</code> if this is not based on a preset.
	 */
	public final ComponentPreset getPresetComponent() {
		return presetComponent;
	}
	
	/**
	 * Return the most compatible preset type for this component.
	 * This method should be overridden by components which have presets
	 * 
	 * @return the most compatible ComponentPreset.Type or <code>null</code> if no presets are compatible.
	 */
	public ComponentPreset.Type getPresetType() {
		return null;
	}
	
	/**
	 * Set the preset component this component is based upon and load all of the 
	 * preset values.
	 * 
	 * @param preset	the preset component to load, or <code>null</code> to clear the preset.
	 */
	public final void loadPreset(ComponentPreset preset) {
		if (presetComponent == preset) {
			return;
		}
		
		if (preset == null) {
			clearPreset();
			return;
		}
		
		// TODO - do we need to this compatibility check?
		/*
		if (preset.getComponentClass() != this.getClass()) {
			throw new IllegalArgumentException("Attempting to load preset of type " + preset.getComponentClass()
					+ " into component of type " + this.getClass());
		}
		 */
		
		RocketComponent root = getRoot();
		final Rocket rocket;
		if (root instanceof Rocket) {
			rocket = (Rocket) root;
		} else {
			rocket = null;
		}
		
		try {
			if (rocket != null) {
				rocket.freeze();
			}
			
			loadFromPreset(preset);
			
			this.presetComponent = preset;
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
			
		} finally {
			if (rocket != null) {
				rocket.thaw();
			}
		}
	}
	
	
	/**
	 * Load component properties from the specified preset.  The preset is guaranteed
	 * to be of the correct type.
	 * <p>
	 * This method should fire the appropriate events related to the changes.  The rocket
	 * is frozen by the caller, so the events will be automatically combined.
	 * <p>
	 * This method must FIRST perform the preset loading and THEN call super.loadFromPreset().
	 * This is because mass setting requires the dimensions to be set beforehand.
	 * 
	 * @param preset	the preset to load from
	 */
	protected void loadFromPreset(ComponentPreset preset) {
		if (preset.has(ComponentPreset.LENGTH)) {
			this.length = preset.get(ComponentPreset.LENGTH);
		}
	}
	
	
	/**
	 * Clear the current component preset.  This does not affect the component properties
	 * otherwise.
	 */
	public final void clearPreset() {
		if (presetComponent == null)
			return;
		presetComponent = null;
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
	
	public final String getDebugName() {
		return (name + "/" + id.substring(0,8));
	}
	
	/**
	 * Generate a new ID for this component.
	 */
	private final void newID() {
		mutex.verify();
		this.id = UniqueID.uuid();
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
		mutex.verify();
		return length;
	}

	/**
	 * Get the positioning of the component relative to its parent component.
	 *
	 * @return This will return one of the enums of {@link AxialMethod}
	 */
	public final AxialMethod getAxialMethod() {
		return axialMethod;
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
	 * @param newAxialMethod	the relative positioning.
	 */
	public void setAxialMethod(final AxialMethod newAxialMethod) {
		if (newAxialMethod == this.axialMethod) {
			// no change.
			return;
		}
		
		// this variable does not change the internal representation
		// the relativePosition (method) is just the lens through which external code may view this component's position. 
		this.axialMethod = newAxialMethod;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	/**
	 * Determine position relative to given position argument.  Note: This is a side-effect free method.  No state
	 * is modified.
	 *
	 * @param asMethod the relative positioning method to be used for the computation
	 *
	 * @return double position of the component relative to the parent, with respect to <code>position</code>
	 */
	public double getAxialOffset(AxialMethod asMethod) {
		double parentLength = 0;
		if (null != this.parent) {
		    parentLength = this.parent.length;
		}

		if(AxialMethod.ABSOLUTE == asMethod){
			return this.getComponentLocations()[0].x;
		}else {
			return asMethod.getAsOffset(this.position.x, this.length, parentLength);
		}
	}
	
	public double getAxialOffset() {
		return this.axialOffset;
	}

	public double getAxialFront(){
	    return this.position.x;
    }

	public double getRadiusOffset() {
		mutex.verify();
		return 0;
	}
	
	public RadiusMethod getRadiusMethod() {
		return RadiusMethod.COAXIAL;
	}
	
	public double getAngleOffset() {
		mutex.verify();
		return 0;
	}
	
	public boolean isAncestor(final RocketComponent testComp) {
		RocketComponent curComp = testComp.parent;
		while (curComp != null) {
			if (this == curComp) {
				return true;
			}
			curComp = curComp.parent;
		}
		return false;
	}
	
	protected void setAfter() {
		checkState();
		
		if (null == this.parent) {
			// Probably initialization order issue.  Ignore for now.
			return;
		}
		
		this.axialMethod = AxialMethod.AFTER;
		this.axialOffset = 0.;
		
		// if first component in the stage. => position from the top of the parent
		final int thisIndex = this.parent.getChildPosition( this );
		if( 0 == thisIndex ) {
			this.position = this.position.setX(0.);
		}else if( 0 < thisIndex ) {
			RocketComponent referenceComponent = parent.getChild( thisIndex - 1 );
		
			double refLength = referenceComponent.getLength();
			double refRelX = referenceComponent.getPosition().x;

			this.position = this.position.setX(refRelX + refLength);
		}
	}
	
	/**
	 * Set the position value of the component.  The exact meaning of the value
	 * depends on the current relative positioning.
	 *
	 * @param newOffset  the desired offset of this component, using the components current axial-method
     */
    public void setAxialOffset(double newOffset) {
		this.setAxialOffset(this.axialMethod, newOffset);
		this.fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	protected void setAxialOffset( final AxialMethod requestedMethod, final double requestedOffset) {
		checkState();

		double newX = Double.NaN;

		if (null == this.parent) {
			// best-effort approximation.  this should be corrected later on in the initialization process.
			newX = requestedOffset;
		} else if (AxialMethod.ABSOLUTE == requestedMethod){
			// in this case, this is simply the intended result
			newX = requestedOffset - this.parent.getComponentLocations()[0].x;
		} else if ( this.isAfter()){
			this.setAfter();
			return;
		} else {
			newX = requestedMethod.getAsPosition(requestedOffset, this.length, this.parent.getLength());
		}
		
		// snap to zero if less than the threshold 'EPSILON'
		final double EPSILON = 0.000001;
		if (EPSILON > Math.abs(newX)) {
			newX = 0.0;
		} else if (Double.isNaN(newX)){
			throw new BugException("setAxialOffset is broken -- attempted to update as NaN: " + this.toDebugDetail());
		}

		// store for later:
		this.axialMethod = requestedMethod;
		this.axialOffset = requestedOffset;
		this.position = this.position.setX( newX );
	}
	
	protected void update() {
		this.setAxialOffset(this.axialMethod, this.axialOffset);
	}

	private final void updateChildren(){
		this.update();
		for( RocketComponent rc : children ){
			rc.updateChildren();
		}
	}

	public Coordinate getPosition() {
		return this.position;
	}	
	
	/**
	 * Returns coordinates of this component's instances in relation to this.parent.
	 * <p>
	 * For example, the absolute position of any given instance is the parent's position 
	 * plus the instance position returned by this method   
	 * <p>
	 * NOTE: the length of this array returned always equals this.getInstanceCount()
	 *
	 * @return    a generated (i.e. new) array of instance locations
	 */
	// @Override Me !
	public Coordinate[] getInstanceLocations(){
		checkState();

		Coordinate center = this.position;
		Coordinate[] offsets = getInstanceOffsets();

		Coordinate[] locations= new Coordinate[offsets.length];
		for (int instanceNumber = 0; instanceNumber < locations.length; instanceNumber++) {
			locations[instanceNumber] = center.add( offsets[instanceNumber] );
		}

		return locations;
	}

	/** 
	 * Provides locations of all instances of component relative to this component's reference point
	 *
	 * <p>
	 * NOTE: the length of this array returned always equals this.getInstanceCount()
	 * NOTE: default implementation just returns (0,0,0)
	 *
	 * @returns returns an array of coordinates, relative to its parent's position
	 */
	public Coordinate[] getInstanceOffsets(){
		return new Coordinate[]{Coordinate.ZERO};
	}

	// this is an inefficient way to calculate all of the locations;
	// it also breaks locality, (i.e. is a rocket-wide calculation )
	@Deprecated
	public Coordinate[] getLocations() {
		return getComponentLocations();
	}
	
	/** 
	 * Provides locations of all instances of component *accounting for all parent instancing*
	 * 
	 * <p>
	 * NOTE: the length of this array MAY OR MAY NOT EQUAL this.getInstanceCount()
     *    --> RocketComponent::getInstanceCount() counts how many times this component replicates on its own
     *    --> vs. the total instance count due to parent assembly instancing
     *
	 * @return Coordinates of all instance locations in the rocket, relative to the rocket's origin
	 */
	public Coordinate[] getComponentLocations() {
		if (null == this.parent) {
			// == improperly initialized components OR the root Rocket instance 
			return getInstanceOffsets();
		} else {
			Coordinate[] parentPositions = this.parent.getComponentLocations();
			int parentCount = parentPositions.length;
			
			// override <instance>.getInstanceLocations() in each subclass
			Coordinate[] instanceLocations = this.getInstanceLocations();
			int instanceCount = instanceLocations.length;
			
			// usual case optimization
			if((1 == parentCount)&&(1 == instanceCount)){
				return new Coordinate[]{parentPositions[0].add(instanceLocations[0])};
			}
			
			int thisCount = instanceCount*parentCount;
			Coordinate[] thesePositions = new Coordinate[thisCount];
			for (int pi = 0; pi < parentCount; pi++) {
				for( int ii = 0; ii < instanceCount; ii++ ){
					thesePositions[pi + parentCount*ii] = parentPositions[pi].add(instanceLocations[ii]);
				}
			}
			return thesePositions;
		}
	}
	
	public double[] getInstanceAngles(){
		return new double[getInstanceCount()]; 
	}
	
	///////////  Coordinate changes  ///////////
	
	/**
	 * Returns coordinate c in absolute/global/rocket coordinates.  Equivalent to toComponent(c,null).
	 * Input coordinate C is interpreted to be position relative to this component's *center*, just as 
	 * this component's center is the root of the component coordinate frame. 
	 * 
	 * @param c    Coordinate in the component's coordinate system.
	 * @return     an array of coordinates describing <code>c</code> in global coordinates.
	 */
	public Coordinate[] toAbsolute(Coordinate c) {
		checkState();
		final String lockText = "toAbsolute";
		mutex.lock(lockText);
		Coordinate[] thesePositions = this.getComponentLocations();
		
		final int instanceCount = thesePositions.length;
		
		Coordinate[] toReturn = new Coordinate[instanceCount];
		for (int coordIndex = 0; coordIndex < instanceCount; coordIndex++) {
			toReturn[coordIndex] = thesePositions[coordIndex].add(c);
		}
		
		mutex.unlock(lockText);
		return toReturn;
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
		if (null == dest) {
			throw new BugException("calling toRelative(c,null) is being refactored. ");
		}
		
		checkState();
		mutex.lock("toRelative");
		
		// not sure if this will give us an answer, or THE answer... 
		//final Coordinate sourceLoc = this.getLocation()[0];
		final Coordinate[] destLocs = dest.getLocations();
		Coordinate[] toReturn = new Coordinate[destLocs.length];
		for (int coordIndex = 0; coordIndex < destLocs.length; coordIndex++) {
			toReturn[coordIndex] = this.getLocations()[0].add(c).sub(destLocs[coordIndex]);
		}
		
		mutex.unlock("toRelative");
		return toReturn;
	}
	
//	protected static final Coordinate[] rebase(final Coordinate toMove[], final Coordinate source, final Coordinate dest) {
//		final Coordinate delta = source.sub(dest);
//		Coordinate[] toReturn = new Coordinate[toMove.length];
//		for (int coordIndex = 0; coordIndex < toMove.length; coordIndex++) {
//			toReturn[coordIndex] = toMove[coordIndex].add(delta);
//		}
//		
//		return toReturn;
//	}
	
	
	/**
	 * Iteratively sum the lengths of all subcomponents that have position
	 * Position.AFTER.
	 *
	 * @return  Sum of the lengths.
	 */
//	private final double getTotalLength() {
//		checkState();
//		this.checkComponentStructure();
//		mutex.lock("getTotalLength");
//		try {
//			double l = 0;
//			if (relativePosition == Position.AFTER)
//				l = length;
//			for (int i = 0; i < children.size(); i++)
//				l += children.get(i).getTotalLength();
//			return l;
//		} finally {
//			mutex.unlock("getTotalLength");
//		}
//	}
	
	
	/////////// Total mass and CG calculation ////////////
	
	/**
	 * Return the (possibly overridden) mass of component.
	 *
	 * @return The mass of the component or the given override mass.
	 */
	public final double getMass() {
		mutex.verify();
		if (massOverriden)
			return overrideMass;
		return getComponentMass();
	}
	
	/**
	 * Return the mass of this component and all of its subcomponents.
	 */
	public final double getSectionMass() {
		Double massSubtotal = getMass();
		mutex.verify();
		for (RocketComponent rc : children) {
			massSubtotal += rc.getSectionMass();
		}
		
		return massSubtotal;
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
		checkState();
		if (cgOverriden)
			return getOverrideCG().setWeight(getMass());
		
		if (massOverriden)
			return getComponentCG().setWeight(getMass());
		
		return getComponentCG();
	}
	
	
	/**
	 * Return the longitudinal (around the y- or z-axis) moment of inertia of this component.
	 * The moment of inertia is scaled in reference to the (possibly overridden) mass
	 * and is relative to the non-overridden CG.
	 *
	 * @return    the longitudinal moment of inertia of this component.
	 */
	public final double getLongitudinalInertia() {
		checkState();
		return getLongitudinalUnitInertia() * getMass();
	}
	
	/**
	 * Return the rotational (around the y- or z-axis) moment of inertia of this component.
	 * The moment of inertia is scaled in reference to the (possibly overridden) mass
	 * and is relative to the non-overridden CG.
	 *
	 * @return    the rotational moment of inertia of this component.
	 */
	public final double getRotationalInertia() {
		checkState();
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
		checkState();
		addChild(component, children.size());
	}
	
	/**
	 * Adds a child to the rocket component tree.  The component is added to
	 * the given position of the component's child list.
	 * <p>
	 * This method may be overridden to enforce more strict component addition rules.
	 * The tests should be performed first and then this method called.
	 *
	 * @param component	The component to add.
	 * @param index		Position to add component to.
	 * @throws IllegalArgumentException  If the component is already part of
	 * 									 some component tree.
	 */
	public void addChild(RocketComponent component, int index) {
		checkState();
		
		if (component.parent != null) {
			throw new IllegalArgumentException("component " + component.getComponentName() +
					" is already in a tree");
		}
		
		// Ensure that the no loops are created in component tree [A -> X -> Y -> B, B.addChild(A)]
		if (this.getRoot().equals(component)) {
			throw new IllegalStateException("Component " + component.getComponentName() +
					" is a parent of " + this.getComponentName() + ", attempting to create cycle in tree.");
		}
		
		if (!isCompatible(component)) {
			throw new IllegalStateException("Component: " + component.getComponentName() +
					" not currently compatible with component: " + getComponentName());
		}
		
		children.add(index, component);
		component.parent = this;
		
		if (component instanceof AxialStage) {
			AxialStage nStage = (AxialStage) component;
			this.getRocket().trackStage(nStage);
		}
		
		this.checkComponentStructure();
		component.checkComponentStructure();
		
		fireAddRemoveEvent(component);
	}
	
	/**
	 * Removes a child from the rocket component tree.
	 * (redirect to the removed-by-component
	 *
	 * @param n  remove the n'th child.
	 * @throws IndexOutOfBoundsException  if n is out of bounds
	 */
	public final void removeChild(int n) {
		checkState();
		RocketComponent component = this.getChild(n); 
		this.removeChild(component);
	}
	
	/**
	 * Removes a child from the rocket component tree.  Does nothing if the component
	 * is not present as a child.
	 *
	 * @param component		the component to remove
	 * @return				whether the component was a child
	 */
	public final boolean removeChild(RocketComponent component) {
		checkState();
		
		component.checkComponentStructure();
		

		if (children.remove(component)) {
			component.parent = null;
			
			if (component instanceof AxialStage) {
				AxialStage stage = (AxialStage) component;
				this.getRocket().forgetStage(stage);
			}
			
			this.checkComponentStructure();
			component.checkComponentStructure();
			
			fireAddRemoveEvent(component);
			updateBounds();
			
			return true;
		}
		return false;
	}
	
	
	
	
	/**
	 * Move a child to another position.
	 *
	 * @param component	the component to move
	 * @param index	the component's new position
	 * @throws IllegalArgumentException If an illegal placement was attempted.
	 */
	public final void moveChild(RocketComponent component, int index) {
		checkState();
		if (children.remove(component)) {
			children.add(index, component);
			
			this.checkComponentStructure();
			component.checkComponentStructure();
			
			updateBounds();
			fireAddRemoveEvent(component);
		}
	}
	
	
	/**
	 * Fires an AERODYNAMIC_CHANGE, MASS_CHANGE or OTHER_CHANGE event depending on the
	 * type of component removed.
	 */
	private void fireAddRemoveEvent(RocketComponent component) {
		Iterator<RocketComponent> iter = component.iterator(true);
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
		checkState();
		this.checkComponentStructure();
		return children.size();
	}
	
	public final RocketComponent getChild(int n) {
		checkState();
		this.checkComponentStructure();
		return children.get(n);
	}
	
	public final List<RocketComponent> getChildren() {
		checkState();
		this.checkComponentStructure();
		return children.clone();
	}
	
	
	/**
	 * Returns the position of the child in this components child list, or -1 if the
	 * component is not a child of this component.
	 *
	 * @param child  The child to search for.
	 * @return  Position in the list or -1 if not found.
	 */
	public final int getChildPosition(RocketComponent child) {
		checkState();
		this.checkComponentStructure();
		return children.indexOf(child);
	}
	
	/**
	 * Get the parent component of this component.  Returns <code>null</code> if the component
	 * has no parent.
	 *
	 * @return  The parent of this component or <code>null</code>.
	 */
	public final RocketComponent getParent() {
		checkState();
		return parent;
	}
	
	/**
	 * Get the root component of the component tree.
	 *
	 * @return  The root component of the component tree.
	 */
	public final RocketComponent getRoot() {
		checkState();
		RocketComponent gp = this;
		while (gp.parent != null){
			gp = gp.parent;
		}
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
		checkState();
		RocketComponent r = getRoot();
		if (r instanceof Rocket)
			return (Rocket) r;
		throw new IllegalStateException("getRocket() called with root component "
				+ r.getComponentName());
	}
	
	
	/**
	 * Return the Stage component that this component belongs to.  Throws an
	 * IllegalStateException if a Stage is not in the parentage of this component.
	 *
	 * @return	The Stage component this component belongs to.
	 * @throws	IllegalStateException   if we cannot find an AxialStage above <code>this</code> 
	 */
	public final AxialStage getStage() {
		checkState();

		RocketComponent curComponent = this;
		while ( null != curComponent ) {
			if (curComponent instanceof AxialStage)
				return (AxialStage) curComponent;
			curComponent = curComponent.parent;
		}
		throw new IllegalStateException("getStage() called on hierarchy without an AxialStage.");
	}
	
	/**
	 * Return the first component assembly component that this component belongs to.
	 *
	 * @return	The Stage component this component belongs to.
	 * @throws	IllegalStateException   if we cannot find an AxialStage above <code>this</code> 
	 */
	public final ComponentAssembly getAssembly() {
		checkState();

		RocketComponent curComponent = this;
		while ( null != curComponent ) {
			if( ComponentAssembly.class.isAssignableFrom( curComponent.getClass()))
				return (ComponentAssembly) curComponent;
			curComponent = curComponent.parent;
		}
		throw new IllegalStateException("getAssembly() called on hierarchy without a ComponentAssembly.");
	}
	
	
	/**
	 * Return the stage number of the stage this component belongs to.  The stages
	 * are numbered from zero upwards.
	 *
	 * @return   the stage number this component belongs to.
	 */
	public int getStageNumber() {
		checkState();
		
		// obviously, this depends on AxialStage overriding <code> .getStageNumber() </code>.
		// It does as of this writing, but check it just to be sure.... 
		return this.getStage().getStageNumber();
	}
	
	/**
	 * Find a component with the given ID.  The component tree is searched from this component
	 * down (including this component) for the ID and the corresponding component is returned,
	 * or null if not found.
	 *
	 * @param idToFind  ID to search for.
	 * @return    The component with the ID, or null if not found.
	 */
	public final RocketComponent findComponent(String idToFind) {
		checkState();
		Iterator<RocketComponent> iter = this.iterator(true);
		while (iter.hasNext()) {
			RocketComponent c = iter.next();
			if (c.getID().equals(idToFind))
				return c;
		}
		return null;
	}

	public final RocketComponent getNextComponent() {
		checkState();
		if (getChildCount() > 0)
			return getChild(0);

		RocketComponent current = this;
		RocketComponent nextParent = this.parent;

		while (nextParent != null) {
			int pos = nextParent.getChildPosition(current);
			if (pos < nextParent.getChildCount() - 1)
				return nextParent.getChild(pos + 1);

			current = nextParent;
			nextParent = current.parent;
		}
		return null;
	}

	public final RocketComponent getPreviousComponent() {
		checkState();
		this.checkComponentStructure();
		if (parent == null)
			return null;
		int pos = parent.getChildPosition(this);
		if (pos < 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Inconsistent internal state: ");
			sb.append("this=").append(this).append('[')
					.append(System.identityHashCode(this)).append(']');
			sb.append(" parent.children=[");
			for (int i = 0; i < parent.children.size(); i++) {
				RocketComponent c = parent.children.get(i);
				sb.append(c).append('[').append(System.identityHashCode(c)).append(']');
				if (i < parent.children.size() - 1)
					sb.append(", ");
			}
			sb.append(']');
			throw new IllegalStateException(sb.toString());
		}
		assert (pos >= 0);
		if (pos == 0)
			return parent;
		RocketComponent c = parent.getChild(pos - 1);
		while (c.getChildCount() > 0)
			c = c.getChild(c.getChildCount() - 1);
		return c;
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
		checkState();
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
	@Override
	public final void addChangeListener(StateChangeListener l) {
		addComponentChangeListener(new ComponentChangeAdapter(l));
	}
	
	/**
	 * Removes a ChangeListener from the rocket tree.  This is identical to
	 * removeComponentChangeListener() except it uses a ChangeListener.
	 * Does nothing if the root component is not a Rocket.  (The asymmetry is so
	 * that listeners can always be removed just in case.)
	 *
	 * @param l  Listener to remove
	 */
	@Override
	public final void removeChangeListener(StateChangeListener l) {
		removeComponentChangeListener(new ComponentChangeAdapter(l));
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
		checkState();
		if (parent == null) {
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
	public void fireComponentChangeEvent(int type) {
		fireComponentChangeEvent(new ComponentChangeEvent(this, type));
	}
	
	
	/**
	 * Checks whether this component has been invalidated and should no longer be used.
	 * This is a safety check that in-place replaced components are no longer used.
	 * All non-trivial methods (with the exception of methods simply getting a property)
	 * should call this method before changing or computing anything.
	 *
	 * @throws	BugException	if this component has been invalidated by {@link #copyFrom(RocketComponent)}.
	 */
	protected void checkState() {
		invalidator.check(true);
		mutex.verify();
	}
	
	
	/**
	 * Check that the local component structure is correct.  This can be called after changing
	 * the component structure in order to verify the integrity.
	 * <p>
	 * TODO: Remove this after the "inconsistent internal state" bug has been corrected
	 */
	public void checkComponentStructure() {
		if (this.parent != null) {
			// Test that this component is found in parent's children with == operator
			if (!containsExact(this.parent.children, this)) {
				throw new BugException("Inconsistent component structure detected, parent does not contain this " +
						"component as a child, parent=" + parent.toDebugString() + " this=" + this.toDebugString());
			}
		}
		for (RocketComponent child : this.children) {
			if (child.parent != this) {
				throw new BugException("Inconsistent component structure detected, child does not have this component " +
						"as the parent, this=" + this.toDebugString() + " child=" + child.toDebugString() +
						" child.parent=" + (child.parent == null ? "null" : child.parent.toDebugString()));
			}
		}
	}
	
	// Check whether the list contains exactly the searched-for component (with == operator)
	private boolean containsExact(List<RocketComponent> haystack, RocketComponent needle) {
		for (RocketComponent c : haystack) {
			if (needle == c) {
				return true;
			}
		}
		return false;
	}
	
	
	///////////  Iterators  //////////	
	
	/**
	 * Returns an iterator that iterates over all children and sub-children.
	 * <p>
	 * The iterator iterates through all children below this object, including itself if
	 * <code>returnSelf</code> is true.  The order of the iteration is not specified
	 * (it may be specified in the future).
	 * <p>
	 * If an iterator iterating over only the direct children of the component is required,
	 * use <code>component.getChildren().iterator()</code>.
	 *
	 * @param returnSelf boolean value specifying whether the component itself should be
	 * 					 returned
	 * @return An iterator for the children and sub-children.
	 */
	public final Iterator<RocketComponent> iterator(boolean returnSelf) {
		checkState();
		return new RocketComponentIterator(this, returnSelf);
	}
	
	
	/**
	 * Returns an iterator that iterates over this component, its children and sub-children.
	 * <p>
	 * This method is equivalent to <code>iterator(true)</code>.
	 *
	 * @return An iterator for this component, its children and sub-children.
	 */
	@Override
	public final Iterator<RocketComponent> iterator() {
		return iterator(true);
	}
	
	/**
	 * Compare component equality based on the ID of this component.  Only the
	 * ID and class type is used for a basis of comparison.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		RocketComponent other = (RocketComponent) obj;
		return this.id.equals(other.id);
	}
	
	
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	/** 
	 * the default implementation is mostly a placeholder here, however in inheriting classes, 
	 * this function is useful to indicate adjacent placements and view sizes
	 */
	protected void updateBounds() {
		return;
	}
	
	///////////// Visitor pattern implementation
	public <R> R accept(RocketComponentVisitor<R> visitor) {
		visitor.visit(this);
		return visitor.getResult();
	}
	
	////////////  Helper methods for subclasses
	
	
	
	/**
	 * Helper method to add two points on opposite corners of a box around the rocket centerline.  This box will be (x_max - x_min) long, and 2*r wide/high.
	 */
	protected static final void addBoundingBox(Collection<Coordinate> bounds, double x_min, double x_max, double r) {
		bounds.add(new Coordinate(x_min, -r, -r));
		bounds.add(new Coordinate(x_max, r, r));
	}
	
	/**
	 * Helper method to add four bounds rotated around the given x coordinate at radius 'r', and 90deg between each.
	 * The X-axis value is <code>x</code> and the radius at the specified position is
	 * <code>r</code>.
	 */
	protected static final void addBound(Collection<Coordinate> bounds, double x, double r) {
		bounds.add(new Coordinate(x, -r, -r));
		bounds.add(new Coordinate(x, r, -r));
		bounds.add(new Coordinate(x, r, r));
		bounds.add(new Coordinate(x, -r, r));
	}
	
	
	protected static final Coordinate ringCG(double outerRadius, double innerRadius,
			double x1, double x2, double density) {
		return new Coordinate((x1 + x2) / 2, 0, 0,
				ringMass(outerRadius, innerRadius, x2 - x1, density));
	}
	
	protected static final double ringVolume(double outerRadius, double innerRadius, double length) {
		return ringMass(outerRadius, innerRadius, length, 1.0);
	}
	
	protected static final double ringMass(double outerRadius, double innerRadius,
			double length, double density) {
		return Math.PI * Math.max(MathUtil.pow2(outerRadius) - MathUtil.pow2(innerRadius),0) *
				length * density;
	}
	
	protected static final double ringLongitudinalUnitInertia(double outerRadius,
			double innerRadius, double length) {
		// 1/12 * (3 * (r1^2 + r2^2) + h^2)
		return (3 * (MathUtil.pow2(innerRadius) + MathUtil.pow2(outerRadius)) + MathUtil.pow2(length)) / 12;
	}
	
	protected static final double ringRotationalUnitInertia(double outerRadius,
			double innerRadius) {
		// 1/2 * (r1^2 + r2^2)
		return (MathUtil.pow2(innerRadius) + MathUtil.pow2(outerRadius)) / 2;
	}
	
	////////////  OTHER
	
	
	/**
	 * Loads the RocketComponent fields from the given component.  This method is meant
	 * for in-place replacement of a component.  It is used with the undo/redo
	 * mechanism and when converting a finset into a freeform fin set.
	 * This component must not have a parent, otherwise this method will fail.
	 * <p>
	 * The child components in the source tree are copied into the current tree, however,
	 * the original components should not be used since they represent old copies of the
	 * components.  It is recommended to invalidate them by calling {@link #invalidate()}.
	 * <p>
	 * This method returns a list of components that should be invalidated after references
	 * to them have been removed (for example by firing appropriate events).  The list contains
	 * all children and sub-children of the current component and the entire component
	 * tree of <code>src</code>.
	 *
	 * @return	a list of components that should not be used after this call.
	 */
	protected List<RocketComponent> copyFrom(RocketComponent src) {
		checkState();
		List<RocketComponent> toInvalidate = new ArrayList<RocketComponent>();
		
		if (this.parent != null) {
			throw new UnsupportedOperationException("copyFrom called for non-root component, parent=" +
					this.parent.toDebugString() + ", this=" + this.toDebugString());
		}
		
		// Add current structure to be invalidated
		Iterator<RocketComponent> iterator = this.iterator(false);
		while (iterator.hasNext()) {
			toInvalidate.add(iterator.next());
		}
		
		// Remove previous components
		for (RocketComponent child : this.children) {
			child.parent = null;
		}
		this.children.clear();
		
		// Copy new children to this component
		for (RocketComponent c : src.children) {
			RocketComponent copy = c.copyWithOriginalID();
			this.children.add(copy);
			copy.parent = this;
		}
		
		this.checkComponentStructure();
		src.checkComponentStructure();
		
		// Set all parameters
		this.length = src.length;
		this.axialMethod = src.axialMethod;
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
		
		// Add source components to invalidation tree
		for (RocketComponent c : src) {
			toInvalidate.add(c);
		}
		
		return toInvalidate;
	}
	
	protected void invalidate() {
		invalidator.invalidate();
	}
	
	
	//////////  Iterator implementation  ///////////
	
	/**
	 * Private inner class to implement the Iterator.
	 *
	 * This iterator is fail-fast if the root of the structure is a Rocket.
	 */
	private static class RocketComponentIterator implements Iterator<RocketComponent> {
		// Stack holds iterators which still have some components left.
		private final Deque<Iterator<RocketComponent>> iteratorStack = new ArrayDeque<Iterator<RocketComponent>>();
		
		private final Rocket root;
		private final int treeModID;
		
		private final RocketComponent original;
		private boolean returnSelf = false;
		
		// Construct iterator with component's child's iterator, if it has elements
		public RocketComponentIterator(RocketComponent c, boolean returnSelf) {
			
			RocketComponent gp = c.getRoot();
			if (gp instanceof Rocket) {
				root = (Rocket) gp;
				treeModID = root.getTreeModID();
			} else {
				root = null;
				treeModID = -1;
			}
			
			Iterator<RocketComponent> i = c.children.iterator();
			if (i.hasNext())
				iteratorStack.push(i);
			
			this.original = c;
			this.returnSelf = returnSelf;
		}
		
		@Override
		public boolean hasNext() {
			checkID();
			if (returnSelf)
				return true;
			return !iteratorStack.isEmpty(); // Elements remain if stack is not empty
		}
		
		@Override
		public RocketComponent next() {
			Iterator<RocketComponent> i;
			
			checkID();
			
			// Return original component first
			if (returnSelf) {
				returnSelf = false;
				return original;
			}
			
			// Peek first iterator from stack, throw exception if empty
			i = iteratorStack.peek();
			if (i == null) {
				throw new NoSuchElementException("No further elements in RocketComponent iterator");
			}
			
			// Retrieve next component of the iterator, remove iterator from stack if empty
			RocketComponent c = i.next();
			if (!i.hasNext())
				iteratorStack.pop();
			
			// Add iterator of component children to stack if it has children
			i = c.children.iterator();
			if (i.hasNext())
				iteratorStack.push(i);
			
			return c;
		}
		
		private void checkID() {
			if (root != null) {
				if (root.getTreeModID() != treeModID) {
					throw new IllegalStateException("Rocket modified while being iterated");
				}
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported by " +
					"RocketComponent iterator");
		}
	}

	/// debug functions
	public String toDebugName(){
		return this.getName()+"<"+this.getClass().getSimpleName()+">("+this.getID().substring(0,8)+")";
	}
	
	// multi-line output
	protected StringBuilder toDebugDetail() {
		StringBuilder buf = new StringBuilder();

		// infer the calling method name
		StackTraceElement[] stackTrace = (new Exception()).getStackTrace();
		String callingMethod = stackTrace[1].getMethodName();
		for( StackTraceElement el : stackTrace ){
			if( ! "toDebugDetail".equals(el.getMethodName())){
				callingMethod = el.getMethodName();
				break;
			}
		}

		buf.append(String.format(" >> Dumping Detailed Information from: %s\n", callingMethod));
		buf.append(String.format("      At Component: %s, of class: %s \n", this.getName(), this.getClass().getSimpleName()));
		buf.append(String.format("      position: %.6f    at offset: %.4f via: %s\n", this.position.x, this.axialOffset, this.axialMethod.name()));
		buf.append(String.format("      length: %.4f\n", this.length ));
		return buf;
	}
	
	// Primarily for debug use
	public String toDebugTree() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("\n   ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ======\n");
		buffer.append("     [Name]                               [Length]          [Rel Pos]                [Abs Pos]  \n");
		this.toDebugTreeHelper(buffer, "");
		buffer.append("\n   ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ====== ======\n");
		return buffer.toString();
	}
	

	public void toDebugTreeHelper(StringBuilder buffer, final String indent) {
		this.toDebugTreeNode(buffer, indent);
		
		Iterator<RocketComponent> iterator = this.children.iterator();
		while (iterator.hasNext()) {
			iterator.next().toDebugTreeHelper(buffer, indent + "....");
		}
	}
	
	
	public void toDebugTreeNode(final StringBuilder buffer, final String indent) {
		String prefix = String.format("%s%s (x%d)", indent, this.getName(), this.getInstanceCount() );
		
		// 1) instanced vs non-instanced
		if( 1 == getInstanceCount() ){
			// un-instanced RocketComponents (usual case)
			buffer.append(String.format("%-40s|  %5.3f; %24s; %24s; ", prefix, this.getLength(), this.axialOffset, this.getComponentLocations()[0]));
			buffer.append(String.format("(offset: %4.1f  via: %s )\n", this.getAxialOffset(), this.axialMethod.name()));
		}else if( this instanceof Instanceable ){
			// instanced components -- think motor clusters or booster stage clusters
			final String patternName = ((Instanceable)this).getPatternName();
			buffer.append(String.format("%-40s (cluster: %s )", prefix, patternName));
			buffer.append(String.format("(offset: %4.1f  via: %s )\n", this.getAxialOffset(), this.axialMethod.name()));
			
			for (int instanceNumber = 0; instanceNumber < this.getInstanceCount(); instanceNumber++) {
				final String instancePrefix = String.format("%s    [%2d/%2d]", indent, instanceNumber+1, getInstanceCount());
				buffer.append(String.format("%-40s|  %5.3f; %24s; %24s;\n", instancePrefix, getLength(), this.axialOffset, getLocations()[instanceNumber]));
			}
		}else{
			throw new IllegalStateException("This is a developer error! If you implement an instanced class, please subclass the Instanceable interface.");
		}
		
		// 2) if this is an ACTING motor mount:
		if(( this instanceof MotorMount ) &&( ((MotorMount)this).isMotorMount())){ 
			// because InnerTube and BodyTube don't really share a common ancestor besides RocketComponent
			// ... and it's easier to have all this code in one place...
			toDebugMountNode( buffer, indent);
		}
	}
	
	
	public void toDebugMountNode(final StringBuilder buffer, final String indent) {		
		MotorMount mnt = (MotorMount)this;

//		Coordinate[] relCoords = this.getInstanceOffsets();
		Coordinate[] absCoords = this.getComponentLocations();
		FlightConfigurationId curId = this.getRocket().getSelectedConfiguration().getFlightConfigurationID();
//		final int instanceCount = this.getInstanceCount();
		MotorConfiguration curInstance = mnt.getMotorConfig( curId);
		if( curInstance.isEmpty() ){
			// print just the tube locations
			buffer.append(indent+"    [X] This Instance doesn't have any motors for the active configuration.\n");
		}else{
			// curInstance has a motor ... 
			Motor curMotor = curInstance.getMotor();
			final double motorOffset = this.getLength() - curMotor.getLength();
			final String instancePrefix = String.format("%s    [ */%2d]", indent, getInstanceCount());
			
			buffer.append(String.format("%-40sThrust: %f N; \n", 
					indent+"  Mounted: "+curMotor.getDesignation(), curMotor.getMaxThrustEstimate() ));
			
			Coordinate motorRelativePosition = new Coordinate(motorOffset, 0, 0);
			Coordinate tubeAbs = absCoords[0];
			Coordinate motorAbsolutePosition = new Coordinate(tubeAbs.x+motorOffset,tubeAbs.y,tubeAbs.z);
			buffer.append(String.format("%-40s|  %5.3f; %24s; %24s;\n", instancePrefix, curMotor.getLength(), motorRelativePosition, motorAbsolutePosition));
			
		}
	}

	public boolean isMotorMount() {
		return false;
	}

}
