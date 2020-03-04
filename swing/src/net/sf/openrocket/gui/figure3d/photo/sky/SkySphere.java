package net.sf.openrocket.gui.figure3d.photo.sky;

import java.net.URL;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import net.sf.openrocket.gui.figure3d.TextureCache;

import com.jogamp.opengl.util.texture.Texture;

public class SkySphere extends Sky {
	
	private final URL imageURL;
	
	public SkySphere(final URL imageURL) {
		this.imageURL = imageURL;
	}
	
	@Override
	public void draw(GL2 gl, final TextureCache cache) {
		gl.glCullFace(GL.GL_FRONT);
		gl.glPushMatrix();
		GLU glu = new GLU();
		gl.glRotatef(90, 1, 0, 0);
		Texture sky = cache.getTexture(imageURL);
		sky.enable(gl);
		sky.bind(gl);
		gl.glColor3d(1, 1, 1);
		GLUquadric q = glu.gluNewQuadric();
		glu.gluQuadricTexture(q, true);
		glu.gluQuadricOrientation(q, GLU.GLU_OUTSIDE);
		glu.gluSphere(q, 1f, 20, 20);
		sky.disable(gl);
		gl.glPopMatrix();
		gl.glCullFace(GL.GL_BACK);
	}
}
