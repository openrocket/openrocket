package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.util.Coordinate;

public class FinRenderer {
	
	private GLUtessellator tobj = GLU.gluNewTess();
	
	private void preTess(final GL2 gl) {
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
	}
	
	private void oneFace(final GL2 gl, final Coordinate finPoints[], final FinSet fs) {
		GLU.gluTessBeginPolygon(tobj, null);
		GLU.gluTessBeginContour(tobj);
		gl.glNormal3f(0, 0, -1);
		for (int i = 0; i < finPoints.length; i++) {
			Coordinate c = finPoints[i];
			double[] p = new double[] { c.x, c.y + fs.getBodyRadius(),
					c.z };
			GLU.gluTessVertex(tobj, p, 0, p);
			
		}
		GLU.gluTessEndContour(tobj);
		GLU.gluTessEndPolygon(tobj);
	}
	
	private void edgeStrip(final GL2 gl, final Coordinate finPoints[], final Coordinate insetPoints[], final FinSet fs) {
		
		//Render each face as a separate QUAD (or two triangles) so that
		//normals can be controlled per vertex & per face
		for (int i = 0; i <= finPoints.length; i++) {
			//The index of the first fin point to use in the quad
			final int i1 = i % finPoints.length;
			//The index of the second fin point to use in the quad
			final int i2 = (i - 1 + finPoints.length)
					% finPoints.length;
			
			//the 'i'nner and 'o'uter coordinates of points 1 & 2
			final Coordinate ic1 = insetPoints[i1].add(0, fs.getBodyRadius(), -fs.getThickness() / 2.0);
			final Coordinate ic2 = insetPoints[i2].add(0, fs.getBodyRadius(), -fs.getThickness() / 2.0);
			final Coordinate oc1 = finPoints[i1].add(0, fs.getBodyRadius(), 0);
			final Coordinate oc2 = finPoints[i2].add(0, fs.getBodyRadius(), 0);
			
			//Base normal for fin point 1, inner & outer
			final Coordinate n1 = ic1.sub(oc1).cross(oc2.sub(oc1)).normalize();
			
			//Base normal for second fin point is the same
			Coordinate n2 = n1;
			
			//Unless we want fin to look smooth then use the third fin point
			//to get the normal for the next edge segment.
			if (fs instanceof EllipticalFinSet) {
				final int i3 = (i - 2 + finPoints.length)
						% finPoints.length;
				Coordinate oc3 = finPoints[i3].add(0, fs.getBodyRadius(), 0);
				n2 = ic2.sub(oc2).cross(oc3.sub(oc2)).normalize();
			}
			
			Coordinate in1;
			Coordinate on1;
			Coordinate in2;
			Coordinate on2;
			
			//Set up the normals based on fin type
			if (fs.getCrossSection() == CrossSection.ROUNDED) {
				in1 = in2 = new Coordinate(0, 0, -1);
				on1 = n1.setZ(0).normalize();
				on2 = n2.setZ(0).normalize();
			} else if (fs.getCrossSection() == CrossSection.AIRFOIL) {
				in1 = in2 = new Coordinate(0, 0, -1);
				double x = n1.x;
				x = Math.max(x, 0);
				double z = n1.z * x;
				on1 = n1.setZ(z).normalize();
				on2 = n2.setZ(z).normalize();
			} else {
				//Square. Will also render Wedge if that ever happens
				in1 = n1;
				in2 = n2;
				on1 = n1;
				on2 = n2;
			}
			
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			
			gl.glNormal3d(in2.x, in2.y, in2.z);
			gl.glTexCoord2d(ic2.x, ic2.y);
			gl.glVertex3d(ic2.x, ic2.y, ic2.z);
			
			gl.glNormal3d(on2.x, on2.y, on2.z);
			gl.glTexCoord2d(oc2.x, oc2.y);
			gl.glVertex3d(oc2.x, oc2.y, oc2.z);
			
			gl.glNormal3d(in1.x, in1.y, in1.z);
			gl.glTexCoord2d(ic1.x, ic1.y);
			gl.glVertex3d(ic1.x, ic1.y, ic1.z);
			
			gl.glNormal3d(on1.x, on1.y, on1.z);
			gl.glTexCoord2d(oc1.x, oc1.y);
			gl.glVertex3d(oc1.x, oc1.y, oc1.z);
			
			gl.glEnd();
		}
		
		
	}
	
	void renderFinSet(final GL2 gl, FinSet fs) {
		
		Coordinate finPoints[] = fs.getFinPointsWithTab();
		Coordinate insetPoints[];
		
		double loa, hoa;
		{ //Scale texture & calculate overall length
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
			
			loa = maxX - minX;
			hoa = maxY - minY;
		}
		
		//Calculate the inset points for a fin
		if (fs.getCrossSection() == CrossSection.SQUARE || fs.getThickness() == 0) {
			//unchanged if square
			insetPoints = finPoints;
		} else {
			//Otherwise inset the polygon
			insetPoints = new Coordinate[finPoints.length];
			System.arraycopy(finPoints, 0, insetPoints, 0, finPoints.length);
			double inset = Math.min(loa, hoa) / 40.0;
			insetPolygon(insetPoints, inset);
		}
		
		//This is the rotation of the whole finset around the body
		gl.glRotated(fs.getBaseRotation() * (180.0 / Math.PI), 1, 0, 0);
		
		for (int fin = 0; fin < fs.getFinCount(); fin++) {
			
			gl.glPushMatrix();
			
			//Cant this fin around its center
			gl.glTranslated(fs.getLength() / 2, 0, 0);
			gl.glRotated(fs.getCantAngle() * (180.0 / Math.PI), 0, 1, 0);
			gl.glTranslated(-fs.getLength() / 2, 0, 0);
			
			preTess(gl);
			
			//Draw one side
			gl.glPushMatrix();
			gl.glTranslated(0, 0, -fs.getThickness() / 2.0);
			oneFace(gl, insetPoints, fs);
			gl.glPopMatrix();
			
			if (fs.getThickness() > 0)
				edgeStrip(gl, finPoints, insetPoints, fs);
			
			//Draw the other side
			gl.glPushMatrix();
			gl.glScalef(1, 1, -1);
			{
				gl.glFrontFace(GL.GL_CCW);
				gl.glPushMatrix();
				gl.glTranslated(0, 0, -fs.getThickness() / 2.0);
				oneFace(gl, insetPoints, fs);
				gl.glPopMatrix();
				if (fs.getThickness() > 0)
					edgeStrip(gl, finPoints, insetPoints, fs);
				gl.glFrontFace(GL.GL_CW);
			}
			gl.glPopMatrix();
			
			gl.glPopMatrix();
			
			gl.glRotated(360.0 / fs.getFinCount(), 1, 0, 0);
		}
		
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		
	}
	
	//  Based on public-domain code by Darel Rex Finley, 2007
	//  See diagrams at http://alienryderflex.com/polygon_inset
	private void insetPolygon(Coordinate[] C, double insetDist) {
		
		double startX = C[0].x, startY = C[0].y, a, b, c, d, e, f;
		int i;
		final int corners = C.length;
		//  Polygon must have at least three corners to be inset.
		if (corners < 3)
			return;
		
		//  Inset the polygon.
		c = C[corners - 1].x;
		d = C[corners - 1].y;
		e = C[0].x;
		f = C[0].y;
		for (i = 0; i < corners - 1; i++) {
			a = c;
			b = d;
			c = e;
			d = f;
			e = C[i + 1].x;
			f = C[i + 1].y;
			C[i] = insetCorner(a, b, c, d, e, f, C[i], insetDist);
		}
		C[i] = insetCorner(c, d, e, f, startX, startY, C[i], insetDist);
	}
	
	
	
	//  Given the sequentially connected points (a,b), (c,d), and (e,f), this
	//  function returns, in (C,D), a bevel-inset replacement for point (c,d).
	//
	//  Note:  If vectors (a,b)->(c,d) and (c,d)->(e,f) are exactly 180° opposed,
	//         or if either segment is zero-length, this function will do
	//         nothing; i.e. point (C,D) will not be set.
	private Coordinate insetCorner(
			double a, double b, //  previous point
			double c, double d, //  current point that needs to be inset
			double e, double f, //  next point
			Coordinate old, //  storage location for new, inset point
			double insetDist) { //  amount of inset (perpendicular to each line segment)
	
		double c1 = c, d1 = d, c2 = c, d2 = d, dx1, dy1, dist1, dx2, dy2, dist2, insetX, insetY;
		
		//  Calculate length of line segments.
		dx1 = c - a;
		dy1 = d - b;
		dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
		
		dx2 = e - c;
		dy2 = f - d;
		dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
		
		//  Exit if either segment is zero-length.
		if (dist1 == 0. || dist2 == 0.)
			return old;
		
		//Leave lines in the tab or along the bottom not inset.
		//This is OpenRocket fin-specific, remove for general poly inset.
		boolean intab = false; //b < 0 || d < 0;
		boolean inset1 = !intab && (Math.abs(dy1) > 0.0001f || b > 0);
		boolean inset2 = !intab && (Math.abs(dy2) > 0.0001f || d > 0);
		
		//  Inset each of the two line segments.
		if (inset1) {
			insetX = dy1 / dist1 * insetDist;
			a += insetX;
			c1 += insetX;
			insetY = -dx1 / dist1 * insetDist;
			b += insetY;
			d1 += insetY;
		}
		
		if (inset2) {
			insetX = dy2 / dist2 * insetDist;
			e += insetX;
			c2 += insetX;
			insetY = -dx2 / dist2 * insetDist;
			f += insetY;
			d2 += insetY;
		}
		
		//  If inset segments connect perfectly, return the connection point.
		if (c1 == c2 && d1 == d2) {
			return new Coordinate(c1, d1, 0);
			
		}
		
		//  Return the intersection point of the two inset segments (if any).
		Coordinate intersection = lineIntersection(a, b, c1, d1, c2, d2, e, f);
		if (intersection != null) {
			return new Coordinate(intersection.x, intersection.y, 0);
		}
		
		return old;
	}
	
	private Coordinate lineIntersection(
			double Ax, double Ay,
			double Bx, double By,
			double Cx, double Cy,
			double Dx, double Dy) {
		
		double distAB, theCos, theSin, newX, ABpos;
		
		//  Fail if either line is undefined.
		if (Ax == Bx && Ay == By || Cx == Dx && Cy == Dy)
			return null;
		
		//  (1) Translate the system so that point A is on the origin.
		Bx -= Ax;
		By -= Ay;
		Cx -= Ax;
		Cy -= Ay;
		Dx -= Ax;
		Dy -= Ay;
		
		//  Discover the length of segment A-B.
		distAB = Math.sqrt(Bx * Bx + By * By);
		
		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos = Bx / distAB;
		theSin = By / distAB;
		newX = Cx * theCos + Cy * theSin;
		Cy = Cy * theCos - Cx * theSin;
		Cx = newX;
		newX = Dx * theCos + Dy * theSin;
		Dy = Dy * theCos - Dx * theSin;
		Dx = newX;
		
		//  Fail if the lines are parallel.
		if (Cy == Dy)
			return null;
		
		//  (3) Discover the position of the intersection point along line A-B.
		ABpos = Dx + (Cx - Dx) * Dy / (Dy - Cy);
		
		//  (4) Apply the discovered position to line A-B in the original coordinate system.
		
		return new Coordinate(Ax + ABpos * theCos, Ay + ABpos * theSin, 0);
	}
	
}
