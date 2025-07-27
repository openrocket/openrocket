package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;

public class CenteringRing extends RadiusRingComponent {

	public CenteringRing() {
		setOuterRadiusAutomatic(true);
		setInnerRadiusAutomatic(true);
		setLength(0.002);
		super.displayOrder_side = 7; // Order for displaying the component in the 2D side view
		super.displayOrder_back = 5; // Order for displaying the component in the 2D back view
	}

	private static final Translator trans = Application.getTranslator();

	@Override
	public double getInnerRadius() {
		// Implement sibling inner radius automation
		if (isInnerRadiusAutomatic()) {
			innerRadius = 0;
			// Component can be parentless if detached from rocket
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
		return trans.get("CenteringRing.CenteringRing");
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

}
