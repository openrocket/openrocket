package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;


public class CenteringRing extends RadiusRingComponent implements LineInstanceable {

	public CenteringRing() {
		setOuterRadiusAutomatic(true);
		setInnerRadiusAutomatic(true);
		setLength(0.002);
	}
	
	private static final Translator trans = Application.getTranslator();

	protected int instanceCount = 1;
	// front-front along the positive rocket axis. i.e. [1,0,0];
	protected double instanceSeparation = 0; 
   
	
	@Override
	public double getInnerRadius() {
		// Implement sibling inner radius automation
		if (isInnerRadiusAutomatic()) {
			innerRadius = 0;
			// Component can be parentless if disattached from rocket
			if (this.getParent() != null) {
				for (RocketComponent sibling : this.getParent().getChildren()) {
					/*
					 * Only InnerTubes are considered when determining the automatic
					 * inner radius (for now).
					 */
					if (!(sibling instanceof InnerTube)) // Excludes itself
						continue;

					double pos1 = this.toRelative(Coordinate.NUL, sibling)[0].x;
					double pos2 = this.toRelative(new Coordinate(getLength()), sibling)[0].x;
					if (pos2 < 0 || pos1 > sibling.getLength())
						continue;

					innerRadius = Math.max(innerRadius, ((InnerTube) sibling).getOuterRadius());
				}
				innerRadius = Math.min(innerRadius, getOuterRadius());
			}
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
		return trans.get ("CenteringRing.CenteringRing");
	}

	@Override
	public boolean allowsChildren() {
		return false;
	}

	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}

	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.CENTERING_RING;
	}
	
	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(final double _separation){
		this.instanceSeparation = _separation;
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		if( 0 < newCount ){
			this.instanceCount = newCount;
		}
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		Coordinate[] toReturn = new Coordinate[this.getInstanceCount()];
		for ( int index=0 ; index < this.getInstanceCount(); index++){
			toReturn[index] = this.position.setX( this.position.x + index*this.instanceSeparation );
		}
		
		return toReturn;
	}
	
	
	@Override
	public int getInstanceCount(){
		return this.instanceCount;
	}
	
	@Override	
	public String getPatternName(){
		return (this.getInstanceCount() + "-Line");
	}
	
}
