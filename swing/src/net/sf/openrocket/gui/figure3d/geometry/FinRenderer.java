package net.sf.openrocket.gui.figure3d.geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.Coordinate;

public class FinRenderer {
	private GLUtessellator tobj = GLU.gluNewTess();
	
	public void renderFinSet(final GL2 gl, FinSet finSet ) {
		
	    BoundingBox bounds = finSet.getInstanceBoundingBox();
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glScaled(1 / (bounds.max.x - bounds.min.x), 1 / (bounds.max.y - bounds.min.y), 0);
		gl.glTranslated(-bounds.min.x, -bounds.min.y - finSet.getBodyRadius(), 0);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		Coordinate finPoints[] = finSet.getFinPointsWithTab();
		{
		    gl.glPushMatrix();
            
			gl.glTranslated(finSet.getLength() / 2, 0, 0);

            gl.glTranslated(0, - finSet.getBodyRadius(), 0);
            
            gl.glRotated( Math.toDegrees(finSet.getCantAngle()), 0, 1, 0);
            gl.glTranslated(-finSet.getLength() / 2, 0, 0);

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
			
			// fin side: +z
			GLU.gluTessBeginPolygon(tobj, null);
			GLU.gluTessBeginContour(tobj);
			gl.glNormal3f(0, 0, 1);
			for (int i = finPoints.length - 1; i >= 0; i--) {
				Coordinate c = finPoints[i];
				double[] p = new double[] { c.x, c.y + finSet.getBodyRadius(),
						c.z + finSet.getThickness() / 2.0 };
				GLU.gluTessVertex(tobj, p, 0, p);
				
			}
			GLU.gluTessEndContour(tobj);
			GLU.gluTessEndPolygon(tobj);
			
	         // fin side: -z
			GLU.gluTessBeginPolygon(tobj, null);
			GLU.gluTessBeginContour(tobj);
			gl.glNormal3f(0, 0, -1);
			for (int i = 0; i < finPoints.length; i++) {
				Coordinate c = finPoints[i];
				double[] p = new double[] { c.x, c.y + finSet.getBodyRadius(),
						c.z - finSet.getThickness() / 2.0 };
				GLU.gluTessVertex(tobj, p, 0, p);
				
			}
			GLU.gluTessEndContour(tobj);
			GLU.gluTessEndPolygon(tobj);
			
			// Strip around the edge
			if (!(finSet instanceof EllipticalFinSet))
				gl.glShadeModel(GLLightingFunc.GL_FLAT);
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int i = 0; i <= finPoints.length; i++) {
				Coordinate c = finPoints[i % finPoints.length];
				// if ( i > 1 ){
				Coordinate c2 = finPoints[(i - 1 + finPoints.length)
						% finPoints.length];
				gl.glNormal3d(c2.y - c.y, c.x - c2.x, 0);
				// }
				gl.glTexCoord2d(c.x, c.y + finSet.getBodyRadius());
				gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
						c.z - finSet.getThickness() / 2.0);
				gl.glVertex3d(c.x, c.y + finSet.getBodyRadius(),
						c.z + finSet.getThickness() / 2.0);
			}
			gl.glEnd();
			if (!(finSet instanceof EllipticalFinSet))
				gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
			
			gl.glPopMatrix();
		}
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
	}
}
