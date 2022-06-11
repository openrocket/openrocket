package net.sf.openrocket.rocketcomponent.position;


import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public enum RadiusMethod implements DistanceMethod {

	// just as a reminder:
	// public T[] getEnumConstants()

	// both components are on the same axis
	COAXIAL ( Application.getTranslator().get("RocketComponent.Position.Method.Radius.COAXIAL") ){
		@Override
		public double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			return 0.;
		}

		@Override
		public double getAsOffset(final RocketComponent parentComponent, final RocketComponent thisComponent, double radius) {
			return 0;
		}
	},
	
	FREE(Application.getTranslator().get("RocketComponent.Position.Method.Radius.FREE") ){
		@Override
		public boolean clampToZero() { return false; }
		
		@Override
		public double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			return requestedOffset;
		}

		@Override
		public double getAsOffset(final RocketComponent parentComponent, final RocketComponent thisComponent, double radius) {
			return radius;
		}
	},
	
	RELATIVE ( Application.getTranslator().get("RocketComponent.Position.Method.Radius.RELATIVE") ){
		@Override
		public boolean clampToZero() { return false; }
		
		@Override
		public double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			double radius = requestedOffset;
			if( parentComponent instanceof BodyTube ) {
				radius += ((BodyTube)parentComponent).getOuterRadius();
			}
			if( thisComponent instanceof RadiusPositionable ) {
				radius += ((RadiusPositionable)thisComponent).getBoundingRadius();
			}
			return radius;
		}

		@Override
		public double getAsOffset(final RocketComponent parentComponent, final RocketComponent thisComponent, double radius) {
			double offset = radius;
			if (parentComponent instanceof BodyTube) {
				offset -= ((BodyTube)parentComponent).getOuterRadius();
			}
			if (thisComponent instanceof RadiusPositionable) {
				offset -= ((RadiusPositionable)thisComponent).getBoundingRadius();
			}
			return offset;
		}
	},

	// Defines placement relative to the outside of the target component
	//   (a) launchlug => parent-body-tube: SURFACE @ 0
	//   (b) pod => parent-assembly; SURFACE @ distance
	SURFACE ( Application.getTranslator().get("RocketComponent.Position.Method.Radius.SURFACE") ) {
		@Override
		public double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			double radius = 0.;
			if( parentComponent instanceof BodyTube ) {
				radius += ((BodyTube)parentComponent).getOuterRadius();
			}
			if( thisComponent instanceof RadiusPositionable ) {
				radius += ((RadiusPositionable)thisComponent).getBoundingRadius();
			}
			return radius;
		}

		@Override
		public double getAsOffset(RocketComponent parentComponent, RocketComponent thisComponent, double radius) {
			return 0;
		}
	};

	public static final RadiusMethod[] choices(){
		return new RadiusMethod[]{ RadiusMethod.FREE, RadiusMethod.RELATIVE };
	}
	
	public final String description;
	
	// =============
	
	private RadiusMethod( final String descr ) {
		this.description = descr;	
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public boolean clampToZero() { return true; }
	
	public abstract double getRadius(final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset );

	/**
	 * Returns the radius offset argument (starting from the center of its parent) as an offset value for this
	 * RadiusMethod.
	 * @param parentComponent parent of this component
	 * @param thisComponent the component for which the offset is requested
	 * @param radius the radius offset argument
	 * @return the offset value of this RadiusMethod that yields the given radius
	 */
	public abstract double getAsOffset(final RocketComponent parentComponent, final RocketComponent thisComponent, final double radius);
}