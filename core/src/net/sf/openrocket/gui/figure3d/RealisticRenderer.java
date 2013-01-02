package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GL2;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class RealisticRenderer extends RocketRenderer {
	public RealisticRenderer(OpenRocketDocument doc) {
		super(new RealisticRenderStrategy(doc));
	}
	
	@Override
	public void renderComponent(GL2 gl, RocketComponent c, float alpha) {
		((RealisticRenderStrategy)currentStrategy).preGeometry(gl, c, alpha);
		cr.renderGeometry(gl, c);
		((RealisticRenderStrategy)currentStrategy).postGeometry(gl, c, alpha);
	}
	

	@Override
	public boolean isDrawn(RocketComponent c) {
		return true;
	}

	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		return false;
	}

}
