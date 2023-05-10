package net.sf.openrocket.rocketcomponent.position;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public enum AngleMethod implements DistanceMethod {
	
	// Defines placement on the outside of the target component.
	RELATIVE (Application.getTranslator().get("RocketComponent.Position.Method.Angle.RELATIVE") ){
		@Override
		public boolean clampToZero() { return true; }
		
		@Override
		public double getAngle( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedAngleOffset_radians ){
			return parentComponent.getAngleOffset() + requestedAngleOffset_radians;
		}
	},
	
	
	// this component is always zero degrees, relative to its parent.
	FIXED (Application.getTranslator().get("RocketComponent.Position.Method.Angle.FIXED") ){
		@Override
		public double getAngle( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedAngleOffset_radians ){
			return 0.;
		}
	},
	
	// Mirror Instances 
	//   - Intended for 2x-instance components...
	//   - Undefined behavior for components with 3 or more instances
	MIRROR_XY (Application.getTranslator().get("RocketComponent.Position.Method.Angle.MIRROR_XY") ){
		@Override
		public boolean clampToZero() { return false; }
		
		@Override
		public double getAngle( final RocketComponent parentComponent, final RocketComponent thisComponent, final double requestedOffset ){
			double combinedAngle = MathUtil.reduce2Pi( parentComponent.getAngleOffset() + requestedOffset );
			
			if( Math.PI > combinedAngle ) {
				combinedAngle = - ( combinedAngle - Math.PI); 
			}
			
			return combinedAngle;
		}
	};
	
	public static final AngleMethod[] choices(){
		return new AngleMethod[]{ AngleMethod.RELATIVE };  
	}

	public final String description;
	
	private AngleMethod( final String descr ) { 
		this.description= descr;
	}

	@Override
	public String toString() {
		return description;
	}
	
	@Override
	public boolean clampToZero() { return true; }
	
	public abstract double getAngle( final RocketComponent parentComponent, final RocketComponent thisComponent, final double angleOffset_radians ) ;
	
}
