package net.sf.openrocket.rocketcomponent.position;

import net.sf.openrocket.startup.Application;

public enum AxialMethod implements DistanceMethod {
	// subject component is simply located absolutely, from the origin (tip-of-rocket) 
	ABSOLUTE (Application.getTranslator().get("RocketComponent.Position.Method.Axial.ABSOLUTE")){
		@Override
		public boolean clampToZero() { return false; }
	},
		
	// subject component will trail the target component, in the increasing coordinate direction 
	AFTER (Application.getTranslator().get("RocketComponent.Position.Method.Axial.AFTER")),

	// measure from the top of the target component to the top of the subject component
	TOP (Application.getTranslator().get("RocketComponent.Position.Method.Axial.TOP")){
		@Override
		public boolean clampToZero() { return false; }
	},
	
	// This coordinate is measured from the middle of the parent component to the middle of this component
	MIDDLE (Application.getTranslator().get("RocketComponent.Position.Method.Axial.MIDDLE")){
		@Override
		public boolean clampToZero() { return false; }
	},
	
	// This coordinate is measured from the bottom of the parent component to the bottom of this component
	BOTTOM (Application.getTranslator().get("RocketComponent.Position.Method.Axial.BOTTOM")){
		@Override
		public boolean clampToZero() { return false; }
	};

	// just as a reminder:
	// public T[] getEnumConstants()
	
	public static final AxialMethod[] axialOffsetMethods = { TOP, MIDDLE, BOTTOM };
	
	public final String name;
	
	private AxialMethod( final String _name ) { 
		this.name=_name;
	}
	
	@Override
	public boolean clampToZero() { return true; }
	
}
