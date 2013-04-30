package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.util.Coordinate;

public class FinRenderer {
	private GLUtessellator tobj = GLU.gluNewTess();
	
	public void renderFinSet(final GL2 gl, FinSet fs) {
		
		Coordinate finPoints[] = fs.getFinPointsWithTab();
		
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (int i = 0; i < finPoints.length; i++) {
			Coordinate c = finPoints[i];
			minX = Math.min(c.x, minX);
			minY = Math.min(c.y, minY);
			maxX = Math.max(c.x, maxX);
			maxY = Math.max(c.y, maxY);
		}
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glScaled(1 / (maxX - minX), 1 / (maxY - minY), 0);
		gl.glTranslated(-minX, -minY - fs.getBodyRadius(), 0);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		gl.glRotated(fs.getBaseRotation() * (180.0 / Math.PI), 1, 0, 0);
		
		for (int fin = 0; fin < fs.getFinCount(); fin++) {
			
			gl.glPushMatrix();
			
			gl.glTranslated(fs.getLength() / 2, 0, 0);
			gl.glRotated(fs.getCantAngle() * (180.0 / Math.PI), 0, 1, 0);
			gl.glTranslated(-fs.getLength() / 2, 0, 0);
			
			GLUtessellatorCallback cb = new GLUtessellatorCallbackAdapter() {
				@Override
				public void vertex(Object vertexData) {
					double d[] = (double[]) vertexData;
					gl.glTexCoord2d(d[0], d[1]);
					gl.glVertex3dv(d, 0);
				}
				
				@Override
				public void begin(int type) {
					gl.glBegin(type);
				}
				
				@Override
				public void end() {
					gl.glEnd();
				}
			};
			
			GLU.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, cb);
			GLU.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, cb);
			GLU.gluTessCallback(tobj, GLU.GLU_TESS_END, cb);
			
			GLU.gluTessBeginPolygon(tobj, null);
			GLU.gluTessBeginContour(tobj);
			gl.glNormal3f(0, 0, 1);
			for (int i = finPoints.length - 1; i >= 0; i--) {
				Coordinate c = finPoints[i];
				double[] p = new double[] { c.x, c.y + fs.getBodyRadius(),
						c.z + fs.getThickness() / 2.0 };
				GLU.gluTessVertex(tobj, p, 0, p);
				
			}
			GLU.gluTessEndContour(tobj);
			GLU.gluTessEndPolygon(tobj);
			
			GLU.gluTessBeginPolygon(tobj, null);
			GLU.gluTessBeginContour(tobj);
			gl.glNormal3f(0, 0, -1);
			for (int i = 0; i < finPoints.length; i++) {
				Coordinate c = finPoints[i];
				double[] p = new double[] { c.x, c.y + fs.getBodyRadius(),
						c.z - fs.getThickness() / 2.0 };
				GLU.gluTessVertex(tobj, p, 0, p);
				
			}
			GLU.gluTessEndContour(tobj);
			GLU.gluTessEndPolygon(tobj);
			
			// Strip around the edge
			if (!(fs instanceof EllipticalFinSet))
				gl.glShadeModel(GLLightingFunc.GL_FLAT);
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int i = 0; i <= finPoints.length; i++) {
				Coordinate c = finPoints[i % finPoints.length];
				// if ( i > 1 ){
				Coordinate c2 = finPoints[(i - 1 + finPoints.length)
						% finPoints.length];
				gl.glNormal3d(c2.y - c.y, c.x - c2.x, 0);
				// }
				gl.glTexCoord2d(c.x, c.y + fs.getBodyRadius());
				gl.glVertex3d(c.x, c.y + fs.getBodyRadius(),
						c.z - fs.getThickness() / 2.0);
				gl.glVertex3d(c.x, c.y + fs.getBodyRadius(),
						c.z + fs.getThickness() / 2.0);
			}
			gl.glEnd();
			if (!(fs instanceof EllipticalFinSet))
				gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
			
			gl.glPopMatrix();
			
			gl.glRotated(360.0 / fs.getFinCount(), 1, 0, 0);
		}
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
	}
}
