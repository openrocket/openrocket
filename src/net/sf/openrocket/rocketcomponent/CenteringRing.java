package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;


public class CenteringRing extends RadiusRingComponent {

	public CenteringRing() {
		setOuterRadiusAutomatic(true);
		setInnerRadiusAutomatic(true);
		setLength(0.002);
	}
	
	
	@Override
	public double getInnerRadius() {
		// Implement sibling inner radius automation
		if (isInnerRadiusAutomatic()) {
			innerRadius = 0;
			for (RocketComponent sibling: this.getParent().getChildren()) {
				if (!(sibling instanceof RadialParent))  // Excludes itself
					continue;

				double pos1 = this.toRelative(Coordinate.NUL, sibling)[0].x;
				double pos2 = this.toRelative(new Coordinate(getLength()), sibling)[0].x;
				if (pos2 < 0 || pos1 > sibling.getLength())
					continue;
				
				innerRadius = Math.max(innerRadius, ((InnerTube)sibling).getOuterRadius());
			}
			innerRadius = Math.min(innerRadius, getOuterRadius());
		}
		
		return super.getInnerRadius();
	}

	
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}
	
	@Override
	public void setInnerRadiusAutomatic(boolean auto) {
		super.setInnerRadiusAutomatic(auto);
	}
	
	@Override
	public String getComponentName() {
		return "Centering ring";
	}

	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}

}
