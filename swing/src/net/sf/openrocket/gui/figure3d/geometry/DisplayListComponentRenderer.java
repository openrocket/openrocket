package net.sf.openrocket.gui.figure3d.geometry;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class DisplayListComponentRenderer extends ComponentRenderer {
	private Map<Key, Integer> lists = new HashMap<Key, Integer>();
	
	@Override
	public void updateFigure(GLAutoDrawable drawable) {
		super.updateFigure(drawable);
		
		GL2 gl = drawable.getGL().getGL2();
		for (int i : lists.values()) {
			gl.glDeleteLists(i, 1);
		}
		lists.clear();
	}
	
	@Override
	protected void renderGeometry(GL2 gl, RocketComponent c, Surface which) {
		Key k = new Key(c, which);
		if (lists.containsKey(k)) {
			gl.glCallList(lists.get(k));
		} else {
			int list = gl.glGenLists(1);
			gl.glNewList(list, GL2.GL_COMPILE_AND_EXECUTE);
			super.renderGeometry(gl, c, which);
			gl.glEndList();
			lists.put(k, list);
		}
	}
	
	private static class Key {
		final RocketComponent c;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((c == null) ? 0 : c.hashCode());
			result = prime * result + ((which == null) ? 0 : which.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (c == null) {
				if (other.c != null)
					return false;
			} else if (!c.equals(other.c))
				return false;
			if (which != other.which)
				return false;
			return true;
		}
		
		final Surface which;
		
		Key(final RocketComponent c, final Surface which) {
			this.c = c;
			this.which = which;
		}
	}
}
