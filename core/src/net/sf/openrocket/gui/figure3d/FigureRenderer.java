package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GL2;

import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;

public class FigureRenderer extends RocketRenderer {

	protected FigureRenderer() {
		super(new FigureRenderStrategy());
	}

	@Override
	public void renderComponent(GL2 gl, RocketComponent c, float alpha) {
		((FigureRenderStrategy)currentStrategy).preGeometry(gl, c, alpha);
		cr.renderGeometry(gl, c);
	}

	@Override
	public boolean isDrawn(RocketComponent c) {
		return true;
	}

	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		if (c instanceof BodyTube)
			return true;
		if (c instanceof NoseCone)
			return false;
		if (c instanceof SymmetricComponent) {
			if (((SymmetricComponent) c).isFilled())
				return false;
		}
		if (c instanceof Transition) {
			Transition t = (Transition) c;
			return !t.isAftShoulderCapped() && !t.isForeShoulderCapped();
		}
		return false;
	}
}
