package net.sf.openrocket.rocketcomponent.position;

import net.sf.openrocket.startup.Application;

public enum AxialMethod implements DistanceMethod {
	// subject component is simply located absolutely, from the origin (tip-of-rocket) 
	ABSOLUTE (Application.getTranslator().get("RocketComponent.Position.Method.Axial.ABSOLUTE")){
		@Override
		public boolean clampToZero() { return false; }

		@Override
		public double getAsPosition(double offset, double innerLength, double outerLength){
			return offset;
		}

		@Override
		public double getAsOffset(double position, double innerLength, double outerLength){
			return position;
		}
	},
		
	// subject component will trail the target component, in the increasing coordinate direction 
	AFTER (Application.getTranslator().get("RocketComponent.Position.Method.Axial.AFTER")){
		@Override
		public boolean clampToZero() { return false; }

		@Override
		public double getAsPosition(double offset, double innerLength, double outerLength){
			return outerLength + offset;
		}

		@Override
		public double getAsOffset(double position, double innerLength, double outerLength){
		    return position - outerLength;
		    //return outerLength - position;
		}
	},

	// measure from the top of the target component to the top of the subject component
	TOP (Application.getTranslator().get("RocketComponent.Position.Method.Axial.TOP")){
		@Override
		public boolean clampToZero() { return false; }

		@Override
		public double getAsPosition(double offset, double innerLength, double outerLength){
			return offset;
		}

		@Override
		public double getAsOffset(double position, double innerLength, double outerLength){
			return position;
		}
	},
	
	// This coordinate is measured from the middle of the parent component to the middle of this component
	MIDDLE (Application.getTranslator().get("RocketComponent.Position.Method.Axial.MIDDLE")) {
		@Override
		public boolean clampToZero() {
			return false;
		}

		@Override
		public double getAsPosition(double offset, double innerLength, double outerLength){
			return offset + (outerLength - innerLength) / 2;
			// return (outerLength - innerLength) / 2 - offset;
		}

		@Override
		public double getAsOffset(double position, double innerLength, double outerLength){
			return position + (innerLength - outerLength) / 2;
		}
	},
	
	// This coordinate is measured from the bottom of the parent component to the bottom of this component
	BOTTOM (Application.getTranslator().get("RocketComponent.Position.Method.Axial.BOTTOM")){
		@Override
		public boolean clampToZero() { return false; }

		@Override
		public double getAsPosition(double offset, double innerLength, double outerLength){
			return offset + (outerLength - innerLength);
            //return outerLength - innerLength - offset;
		}

		@Override
		public double getAsOffset(double position, double innerLength, double outerLength){
			return position + (innerLength - outerLength);
		}


	};



	// just as a reminder:
	// public T[] getEnumConstants()
	
	public static final AxialMethod[] axialOffsetMethods = { TOP, MIDDLE, BOTTOM };
	
	public final String description;
	
	AxialMethod( final String newDescription ) {
		this.description=newDescription;
	}
	
	public abstract boolean clampToZero();

	public abstract double getAsOffset(double position, double innerLength, double outerLength);

	public abstract double getAsPosition(double offset, double innerLength, double outerLength);

	@Override
	public String toString() {
		return description;
	}
	
}
