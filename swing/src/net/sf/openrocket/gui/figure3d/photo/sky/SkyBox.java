package net.sf.openrocket.gui.figure3d.photo.sky;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.sf.openrocket.gui.figure3d.TextureCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.util.texture.Texture;

public class SkyBox extends Sky {
	private static final Logger log = LoggerFactory.getLogger(SkyBox.class);
	
	private static final String NAME[][] = {
			{ "North", "East", "South", "West", "Up", "Down" },
			{ "posz", "posx", "negz", "negx", "posy", "negy" },
			{ "pos_z", "pos_x", "neg_z", "neg_x", "pos_y", "neg_y" },
			//{ "ft", "rt", "bk", "lf", "up", "dn" }
			//
			
			{ "rt", "ft", "lf", "bk", "up", "dn" }
	};
	private static final String[] TYPE = { ".jpg", ".jpeg", ".png" };
	
	private final String prefix;
	private String suffix;
	private String[] dir;
	
	public SkyBox(final String prefix) {
		this.prefix = prefix;
		this.suffix = ".jpg";
		
		found: for (String trySuf : TYPE) {
			suffix = trySuf;
			for (String[] tryDir : NAME) {
				dir = tryDir;
				try {
					URL u = url(dir[0]);
					log.debug("Trying URL {}", u);
					InputStream is = u.openStream();
					is.close();
					break found;
				} catch (IOException e) {
					log.debug("Nope, {}", e.getMessage());
				}
			}
		}
		
	}
	
	private URL url(String s) {
		try {
			return new URL(prefix + s + suffix);
		} catch (MalformedURLException e) {
			throw new Error(e);
		}
	}
	
	
	
	@Override
	public void draw(GL2 gl, final TextureCache cache) {
		gl.glPushMatrix();
		gl.glColor3d(1, 1, 1);
		
		square(gl, cache.getTexture(url(dir[0])));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(url(dir[1])));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(url(dir[2])));
		
		gl.glRotatef(90, 0, 1, 0);
		square(gl, cache.getTexture(url(dir[3])));
		
		gl.glRotatef(-90, 1, 0, 0);
		gl.glRotatef(90, 0, 0, 1);
		square(gl, cache.getTexture(url(dir[4])));
		
		gl.glRotatef(180, 1, 0, 0);
		square(gl, cache.getTexture(url(dir[5])));
		
		gl.glPopMatrix();
	}
	
	private static final void square(GL2 gl, Texture t) {
		t.bind(gl);
		t.enable(gl);
		gl.glBegin(GL.GL_TRIANGLE_STRIP);
		gl.glNormal3f(0, 0, -1);
		
		gl.glTexCoord2f(1, 1);
		gl.glVertex3f(-1, -1, 1);
		
		gl.glTexCoord2f(1, 0);
		gl.glVertex3f(-1, 1, 1);
		
		gl.glTexCoord2f(0, 1);
		gl.glVertex3f(1, -1, 1);
		
		gl.glTexCoord2f(0, 0);
		gl.glVertex3f(1, 1, 1);
		
		gl.glEnd();
		t.disable(gl);
	}
}
