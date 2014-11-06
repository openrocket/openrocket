package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class ComponentRenderer {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ComponentRenderer.class);

	private int LOD = 80;

	GLU glu;
	GLUquadric q;
	FinRenderer fr = new FinRenderer();

	public ComponentRenderer() {

	}

	public void init(GLAutoDrawable drawable) {
		glu = new GLU();
		q = glu.gluNewQuadric();
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

	protected void renderGeometry(GL2 gl, RocketComponent c, Surface which) {
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
					fr.renderFinSet(gl, (FinSet) c);
			} else if (c instanceof TubeFinSet) {
				renderTubeFins( gl, (TubeFinSet) c, which);
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

		if (which == Surface.OUTSIDE || which == Surface.INSIDE) {
			gl.glPushMatrix();
			gl.glRotated(90, 0, 1.0, 0);
			if (which == Surface.INSIDE) {
				gl.glFrontFace(GL.GL_CCW);
			}
			TransitionRenderer.drawTransition(gl, t, LOD, t.getType() == Shape.CONICAL ? 4 : LOD / 2, which == Surface.INSIDE ? -t.getThickness() : 0);
			if (which == Surface.INSIDE) {
				gl.glFrontFace(GL.GL_CW);
			}
			gl.glPopMatrix();
		}

		if (which == Surface.EDGES || which == Surface.INSIDE) {
			//Render aft edge
			gl.glPushMatrix();
			gl.glTranslated(t.getLength(), 0, 0);
			if (which == Surface.EDGES) {
				gl.glRotated(90, 0, 1.0, 0);
				glu.gluDisk(q, Math.max(0, t.getAftRadius() - t.getThickness()), t.getAftRadius(), LOD, 2);
			} else {
				gl.glRotated(270, 0, 1.0, 0);
				glu.gluDisk(q, Math.max(0, t.getAftRadius() - t.getThickness()), t.getAftRadius(), LOD, 2);
			}
			gl.glPopMatrix();

			// Render AFT shoulder
			if (t.getAftShoulderLength() > 0) {
				gl.glPushMatrix();
				gl.glTranslated(t.getLength(), 0, 0);
				double iR = (t.isFilled() || t.isAftShoulderCapped()) ? 0 : t.getAftShoulderRadius() - t.getAftShoulderThickness();
				if (which == Surface.EDGES) {
					renderTube(gl, Surface.OUTSIDE, t.getAftShoulderRadius(), iR, t.getAftShoulderLength());
					renderTube(gl, Surface.EDGES, t.getAftShoulderRadius(), iR, t.getAftShoulderLength());
					gl.glPushMatrix();
					gl.glRotated(90, 0, 1.0, 0);
					glu.gluDisk(q, t.getAftShoulderRadius(), t.getAftRadius(), LOD, 2);
					gl.glPopMatrix();

				} else {
					renderTube(gl, Surface.INSIDE, t.getAftShoulderRadius(), iR, t.getAftShoulderLength());
					gl.glPushMatrix();
					gl.glRotated(270, 0, 1.0, 0);
					glu.gluDisk(q, t.getAftShoulderRadius(), t.getAftRadius(), LOD, 2);
					gl.glPopMatrix();
				}
				gl.glPopMatrix();
			}

			//Render Fore edge
			gl.glPushMatrix();
			gl.glRotated(180, 0, 1.0, 0);
			if (which == Surface.EDGES) {
				gl.glRotated(90, 0, 1.0, 0);
				glu.gluDisk(q, Math.max(0, t.getForeRadius() - t.getThickness()), t.getForeRadius(), LOD, 2);
			} else {
				gl.glRotated(270, 0, 1.0, 0);
				glu.gluDisk(q, Math.max(0, t.getForeRadius() - t.getThickness()), t.getForeRadius(), LOD, 2);
			}
			gl.glPopMatrix();

			// Render Fore shoulder
			if (t.getForeShoulderLength() > 0) {
				gl.glPushMatrix();
				gl.glRotated(180, 0, 1.0, 0);
				//gl.glTranslated(t.getLength(), 0, 0);
				double iR = (t.isFilled() || t.isForeShoulderCapped()) ? 0 : t.getForeShoulderRadius() - t.getForeShoulderThickness();
				if (which == Surface.EDGES) {
					renderTube(gl, Surface.OUTSIDE, t.getForeShoulderRadius(), iR, t.getForeShoulderLength());
					renderTube(gl, Surface.EDGES, t.getForeShoulderRadius(), iR, t.getForeShoulderLength());
					gl.glPushMatrix();
					gl.glRotated(90, 0, 1.0, 0);
					glu.gluDisk(q, t.getForeShoulderRadius(), t.getForeRadius(), LOD, 2);
					gl.glPopMatrix();

				} else {
					renderTube(gl, Surface.INSIDE, t.getForeShoulderRadius(), iR, t.getForeShoulderLength());
					gl.glPushMatrix();
					gl.glRotated(270, 0, 1.0, 0);
					glu.gluDisk(q, t.getForeShoulderRadius(), t.getForeRadius(), LOD, 2);
					gl.glPopMatrix();
				}
				gl.glPopMatrix();
			}

		}

	}

	private void renderTube(final GL2 gl, final Surface which, final double oR, final double iR, final double len) {
		gl.glPushMatrix();
		//outside
		gl.glRotated(90, 0, 1.0, 0);
		if (which == Surface.OUTSIDE)
			glu.gluCylinder(q, oR, oR, len, LOD, 1);

		//edges
		gl.glRotated(180, 0, 1.0, 0);
		if (which == Surface.EDGES)
			glu.gluDisk(q, iR, oR, LOD, 2);

		gl.glRotated(180, 0, 1.0, 0);
		gl.glTranslated(0, 0, len);
		if (which == Surface.EDGES)
			glu.gluDisk(q, iR, oR, LOD, 2);

		//inside
		if (which == Surface.INSIDE) {
			glu.gluQuadricOrientation(q, GLU.GLU_INSIDE);
			glu.gluCylinder(q, iR, iR, -len, LOD, 1);
			glu.gluQuadricOrientation(q, GLU.GLU_OUTSIDE);
		}
		gl.glPopMatrix();
	}

	private void renderTube(GL2 gl, BodyTube t, Surface which) {
		renderTube(gl, which, t.getOuterRadius(), t.getInnerRadius(), t.getLength());
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

		glu.gluQuadricOrientation(q, GLU.GLU_INSIDE);
		glu.gluCylinder(q, r.getInnerRadius(), r.getInnerRadius(),
				-r.getLength(), LOD, 1);
		glu.gluQuadricOrientation(q, GLU.GLU_OUTSIDE);

	}

	private void renderLug(GL2 gl, LaunchLug t, Surface which) {
		renderTube(gl, which, t.getOuterRadius(), t.getInnerRadius(), t.getLength());
	}

	private void renderTubeFins(GL2 gl, TubeFinSet fs, Surface which) {
		gl.glPushMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		System.out.println(fs.getBaseRotation());
		gl.glRotated(fs.getBaseRotation() * (180.0 / Math.PI), 1, 0, 0);
		for( int i = 0; i< fs.getFinCount(); i++ ) {
			gl.glPushMatrix();
			gl.glTranslated(0, fs.getOuterRadius() + fs.getBodyRadius(), 0);
			renderTube(gl, which, fs.getOuterRadius(), fs.getInnerRadius(), fs.getLength());
			gl.glPopMatrix();
			gl.glRotated(360.0 / fs.getFinCount(), 1, 0, 0);
		}
		gl.glPopMatrix();
	}

	private void renderMassObject(GL2 gl, MassObject o) {
		gl.glRotated(90, 0, 1.0, 0);

		MassObjectRenderer.drawMassObject(gl, o, LOD / 2, LOD / 2);
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
			gl.glNormal3d(0, 0, -1);
			for (int i = 0; i < LOD + 1; i++) {
				gl.glTexCoord2d(i * dt, .875);
				gl.glVertex3d(r * Math.cos(da * i), r * Math.sin(da * i), 0);
				gl.glTexCoord2d(i * dt, .9);
				gl.glVertex3d(.8 * r * Math.cos(da * i), .8 * r * Math.sin(da * i), 0);
			}
			gl.glEnd();
			gl.glBegin(GL.GL_TRIANGLE_STRIP);

			for (int i = 0; i < LOD + 1; i++) {
				gl.glNormal3d(-Math.cos(da * i), -Math.sin(da * i), -1);
				gl.glTexCoord2d(i * dt, .9);
				gl.glVertex3d(.8 * r * Math.cos(da * i), .8 * r * Math.sin(da * i), 0);
				gl.glTexCoord2d(i * dt, 1);
				gl.glVertex3d(0, 0, l * .05);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
	}
}
