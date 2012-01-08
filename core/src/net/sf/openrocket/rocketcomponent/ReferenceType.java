/**
 * 
 */
package net.sf.openrocket.rocketcomponent;

public enum ReferenceType {
	
	NOSECONE {
		@Override
		public double getReferenceLength(Configuration config) {
			for (RocketComponent c: config) {
				if (c instanceof SymmetricComponent) {
					SymmetricComponent s = (SymmetricComponent)c;
					if (s.getForeRadius() >= 0.0005)
						return s.getForeRadius() * 2;
					if (s.getAftRadius() >= 0.0005)
						return s.getAftRadius() * 2;
				}
			}
			return Rocket.DEFAULT_REFERENCE_LENGTH;
		}
	},
	
	MAXIMUM {
		@Override
		public double getReferenceLength(Configuration config) {
			double r = 0;
			for (RocketComponent c: config) {
				if (c instanceof SymmetricComponent) {
					SymmetricComponent s = (SymmetricComponent)c;
					r = Math.max(r, s.getForeRadius());
					r = Math.max(r, s.getAftRadius());
				}
			}
			r *= 2;
			if (r < 0.001)
				r = Rocket.DEFAULT_REFERENCE_LENGTH;
			return r;
		}
	}, 

	CUSTOM {
		@Override
		public double getReferenceLength(Configuration config) {
			return config.getRocket().getCustomReferenceLength();
		}
	};
	
	public abstract double getReferenceLength(Configuration rocket);
}
