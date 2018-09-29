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
	},
	
	FREE(Application.getTranslator().get("RocketComponent.Position.Method.Radius.FREE") ){
		@Override
		public boolean clampToZero() { return false; }
		
		@Override
		public double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			return requestedOffset;
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
	
	public abstract double getRadius( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset );
}