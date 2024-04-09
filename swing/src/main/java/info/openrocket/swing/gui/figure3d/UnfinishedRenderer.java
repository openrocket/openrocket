package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class UnfinishedRenderer extends RealisticRenderer {
	
	public UnfinishedRenderer(OpenRocketDocument document) {
		super(document);
	}
	
	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		return c instanceof BodyTube;
	}
	
	@Override
	protected Appearance getAppearance(RocketComponent c) {
		return DefaultAppearance.getDefaultAppearance(c);
	}

	@Override
	protected float[] convertColor(final Appearance a, float alpha) {
		float[] color = new float[4];
		convertColor(a.getPaint(), color);
		color[3] = alpha;//re-set to "alpha" so that Unfinished renderer will show interior parts.
		return color;
	}
}
