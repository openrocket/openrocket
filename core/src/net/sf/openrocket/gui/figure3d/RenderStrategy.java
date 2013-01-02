package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GLAutoDrawable;

import net.sf.openrocket.document.OpenRocketDocument;

public abstract class RenderStrategy {

	protected final OpenRocketDocument document;

	public RenderStrategy(OpenRocketDocument document) {
		this.document = document;
	}

	public void updateFigure() {

	}

	public void init(GLAutoDrawable drawable) {

	}

	public void dispose(GLAutoDrawable drawable) {

	}

}
