package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class ComponentRenderer {
	@SuppressWarnings("unused")
	private static final LogHelper log = Application.getLogger();
	
	private int LOD = 80;
	
	GLU glu;
	GLUquadric q;
	GLUtessellator tobj;
	
	public ComponentRenderer() {
		
	}
	
	public void init(GLAutoDrawable drawable) {
		glu = new GLU();
		q = glu.gluNewQuadric();
		tobj = GLU.gluNewTess();
		glu.gluQuadricTexture(q, true);
	}
	
	
	public void updateFigure(GLAutoDrawable drawable) {
		
	}
	
	public Geometry getGeometry(final RocketComponent c, final Surface which) {
		return new Geometry() {
			@Override
			public void render(GL2 gl) {
				if (which == Surface.ALL) {
					renderGeometry(gl, c, Surface.INSIDE);
					renderGeometry(gl, c, Surface.EDGES);
					renderGeometry(gl, c, Surface.OUTSIDE);
				} else {
					renderGeometry(gl, c, which);
				}
			}
		};
	}
	
	public Geometry getGeometry(final Motor motor, Surface which) {
		return new Geometry() {
			@Override
			public void render(GL2 gl) {
				renderMotor(gl, motor);
			}
		};
	}
	
	private void renderGeometry(GL2 gl, RocketComponent c, Surface which) {
		if (glu == null)
			throw new IllegalStateException(this + " Not Initialized");
		
		glu.gluQuadricNormals(q, GLU.GLU_SMOOTH);
		
		Coordinate[] oo = c.toAbsolute(new Coordinate(0, 0, 0));
		
		for (Coordinate o : oo) {
			gl.glPushMatrix();
			
			gl.glTranslated(o.x, o.y, o.z);
			
			if (c instanceof BodyTube) {
				renderTube(gl, (BodyTube) c, which);
			} else if (c instanceof LaunchLug) {
				renderLug(gl, (LaunchLug) c, which);
			} else if (c instanceof RingComponent) {
				if (which == Surface.OUTSIDE)
					renderRing(gl, (RingComponent) c);
			} else if (c instanceof Transition) {
				renderTransition(gl, (Transition) c, which);
			} else if (c instanceof MassObject) {
				if (which == Surface.OUTSIDE)
					renderMassObject(gl, (MassObject) c);
			} else if (c instanceof FinSet) {
				if (which == Surface.OUTSIDE)
					renderFinSet(gl, (FinSet) c);
			} else {
				renderOther(gl, c);
			}
			gl.glPopMatrix();
		}
		
	}
	
	private void renderOther(GL2 gl, RocketComponent c) {
		gl.glBegin(GL.GL_LINES);
		for (Coordinate cc : c.getComponentBounds()) {
			for (Coordinate ccc : c.getComponentBounds()) {
				gl.glVertex3d(cc.x, cc.y, cc.z);
				gl.glVertex3d(ccc.x, ccc.y, ccc.z);
			}
		}
		gl.glEnd();
	}
	
	private void renderTransition(GL2 gl, Transition t, Surface which) {
		//TODO take into account thickness.
		
		if (which == Surface.INSIDE) {
			gl.glFrontFace(GL.GL_CCW);
		}
		
		gl.glRotated(90, 0, 1.0, 0);
		
		if (which == Surface.OUTSIDE || which == Surface.INSIDE) {
			if (t.getType() == Transition.Shape.CONICAL) {
				glu.gluCylinder(q, t.getForeRadius(), t.getAftRadius(),
						t.getLength(), LOD, 1);
			} else {
				TransitionRenderer.drawTransition(gl, t, LOD, LOD);
			}
		}
		
		if (which == Surface.EDGES || which == Surface.INSIDE) {
			// Render AFT shoulder
			gl.glPushMatrix();
			gl.glTranslated(0, 0, t.getLength());
			
			glu.gluCylinder(q, t.getAftShoulderRadius(), t.getAftShoulderRadius(),
					t.getAftShoulderLength(), LOD, 1);
			
			gl.glRotated(180, 0, 1.0, 0);
			
			glu.gluDisk(q, t.getAftRadius(), t.getAftShoulderRadius(), LOD, 2);
			
			gl.glTranslated(0, 0, -t.getAftShoulderLength());
			
			if (t.isFilled() || t.isAftShoulderCapped()) {
				glu.gluDisk(q, t.getAftShoulderRadius(), 0, LOD, 2);
			}
			gl.glPopMatrix();
			
			// Render Fore Shoulder
			gl.glPushMatrix();
			gl.glRotated(180, 0, 1.0, 0);
			
			glu.gluCylinder(q, t.getForeShoulderRadius(),
					t.getForeShoulderRadius(), t.getForeShoulderLength(), LOD, 1);
			
			gl.glRotated(180, 0, 1.0, 0);
			
			glu.gluDisk(q, t.getForeRadius(), t.getForeShoulderRadius(), LOD, 2);
			
			gl.glTranslated(0, 0, -t.getForeShoulderLength());
			
			if (t.isFilled() || t.isForeShoulderCapped()) {
				glu.gluDisk(q, t.getForeShoulderRadius(), 0, LOD, 2);
			}
			gl.glPopMatrix();
		}
		
		if (which == Surface.INSIDE) {
			gl.glFrontFace(GL.GL_CW);
		}
	}
	
	private void renderTube(GL2 gl, BodyTube t, Surface which) {
		//outside
		gl.glRotated(90, 0, 1.0, 0);
		if (which == Surface.OUTSIDE)
			glu.gluCylinder(q, t.getOuterRadius(), t.getOuterRadius(),
					t.getLength(), LOD, 1);
		
		//edges
		gl.glRotated(180, 0, 1.0, 0);
		if (which == Surface.EDGES)
			glu.gluDisk(q, t.getInnerRadius(), t.getOuterRadius(), LOD, 2);
		
		gl.glRotated(180, 0, 1.0, 0);
		gl.glTranslated(0, 0, t.getLength());
		if (which == Surface.EDGES)
			glu.gluDisk(q, t.getInnerRadius(), t.getOuterRadius(), LOD, 2);
		
		
		//inside
		if (which == Surface.INSIDE)
			glu.gluCylinder(q, t.getInnerRadius(), t.getInnerRadius(),
					-t.getLength(), LOD, 1);
		
	}
	
	private void renderRing(GL2 gl, RingComponent r) {
		
		gl.glRotated(90, 0, 1.0, 0);
		glu.gluCylinder(q, r.getOuterRadius(), r.getOuterRadius(),
				r.getLength(), LOD, 1);
		
		gl.glRotated(180, 0, 1.0, 0);
		glu.gluDisk(q, r.getInnerRadius(), r.getOuterRadius(), LOD, 2);
		
		gl.glRotated(180, 0, 1.0, 0);
		gl.glTranslated(0, 0, r.getLength());
		glu.gluDisk(q, r.getInnerRadius(), r.getOuterRadius(), LOD, 2);
		
		glu.gluCylinder(q, r.getInnerRadius(), r.getInnerRadius(),
				-r.getLength(), LOD, 1);
		
	}
	
	private void renderLug(GL2 gl, LaunchLug t, Surface which) {
		//outside
		gl.glRotated(90, 0, 1.0, 0);
		if (which == Surface.OUTSIDE)
			glu.gluCylinder(q, t.getOuterRadius(), t.getOuterRadius(),
					t.getLength(), LOD, 1);
		
		//edges
		gl.glRotated(180, 0, 1.0, 0);
		if (which == Surface.EDGES)
			glu.gluDisk(q, t.getInnerRadius(), t.getOuterRadius(), LOD, 2);
		
		gl.glRotated(180, 0, 1.0, 0);
		gl.glTranslated(0, 0, t.getLength());
		if (which == Surface.EDGES)
			glu.gluDisk(q, t.getInnerRadius(), t.getOuterRadius(), LOD, 2);
		
		
		//inside
		if (which == Surface.INSIDE)
			glu.gluCylinder(q, t.getInnerRadius(), t.getInnerRadius(),
					-t.getLength(), LOD, 1);
	}
	
	private void renderMassObject(GL2 gl, MassObject o) {
		gl.glRotated(90, 0, 1.0, 0);
		
		MassObjectRenderer.drawMassObject(gl, o, LOD, LOD);
	}
	
	private void renderFinSet(final GL2 gl, FinSet fs) {
		
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
	
	private void renderMotor(final GL2 gl, Motor motor) {
		double l = motor.getLength();
		double r = motor.getDiameter() / 2;
		
		gl.glPushMatrix();
		
		gl.glRotated(90, 0, 1.0, 0);
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glTranslated(0, .125, 0);
		gl.glScaled(1, .75, 0);
		
		glu.gluCylinder(q, r, r, l, LOD, 1);
		
		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
		{
			final double da = (2.0f * Math.PI) / LOD;
			final double dt = 1.0 / LOD;
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			gl.glNormal3d(0, 0, 1);
			for (int i = 0; i < LOD + 1; i++) {
				gl.glTexCoord2d(i * dt, .125);
				gl.glVertex3d(r * Math.cos(da * i), r * Math.sin(da * i), 0);
				gl.glTexCoord2d(i * dt, 0);
				gl.glVertex3d(0, 0, 0);
				
			}
			gl.glEnd();
		}
		
		gl.glTranslated(0, 0, l);
		gl.glRotated(180, 0, 1.0, 0);
		
		{
			final double da = (2.0f * Math.PI) / LOD;
			final double dt = 1.0 / LOD;
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			gl.glNormal3d(0, 0, 1);
			for (int i = 0; i < LOD + 1; i++) {
				gl.glTexCoord2d(i * dt, .875);
				gl.glVertex3d(r * Math.cos(da * i), r * Math.sin(da * i), 0);
				gl.glTexCoord2d(i * dt, 1);
				gl.glVertex3d(0, 0, 0);
				
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
}
