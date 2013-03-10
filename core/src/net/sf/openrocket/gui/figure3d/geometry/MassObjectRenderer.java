/*
 ** License Applicability. Except to the extent portions of this file are
 ** made subject to an alternative license as permitted in the SGI Free
 ** Software License B, Version 2.0 (the "License"), the contents of this
 ** file are subject only to the provisions of the License. You may not use
 ** this file except in compliance with the License. You may obtain a copy
 ** of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
 ** Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
 ** 
 ** http://oss.sgi.com/projects/FreeB
 ** 
 ** Note that, as provided in the License, the Software is distributed on an
 ** "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
 ** DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
 ** CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
 ** PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
 ** 
 ** NOTE:  The Original Code (as defined below) has been licensed to Sun
 ** Microsystems, Inc. ("Sun") under the SGI Free Software License B
 ** (Version 1.1), shown above ("SGI License").   Pursuant to Section
 ** 3.2(3) of the SGI License, Sun is distributing the Covered Code to
 ** you under an alternative license ("Alternative License").  This
 ** Alternative License includes all of the provisions of the SGI License
 ** except that Section 2.2 and 11 are omitted.  Any differences between
 ** the Alternative License and the SGI License are offered solely by Sun
 ** and not by SGI.
 **
 ** Original Code. The Original Code is: OpenGL Sample Implementation,
 ** Version 1.2.1, released January 26, 2000, developed by Silicon Graphics,
 ** Inc. The Original Code is Copyright (c) 1991-2000 Silicon Graphics, Inc.
 ** Copyright in any portions created by third parties is as indicated
 ** elsewhere herein. All Rights Reserved.
 ** 
 ** Additional Notice Provisions: The application programming interfaces
 ** established by SGI in conjunction with the Original Code are The
 ** OpenGL(R) Graphics System: A Specification (Version 1.2.1), released
 ** April 1, 1999; The OpenGL(R) Graphics System Utility Library (Version
 ** 1.3), released November 4, 1998; and OpenGL(R) Graphics with the X
 ** Window System(R) (Version 1.3), released October 19, 1998. This software
 ** was created using the OpenGL(R) version 1.2.1 Sample Implementation
 ** published by SGI, but has not been independently verified as being
 ** compliant with the OpenGL(R) version 1.2.1 Specification.
 **
 ** $Date: 2009-03-04 17:23:34 -0800 (Wed, 04 Mar 2009) $ $Revision: 1856 $
 ** $Header$
 */

/* 
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */
package net.sf.openrocket.gui.figure3d.geometry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import net.sf.openrocket.rocketcomponent.MassObject;

final class MassObjectRenderer {
	private static final boolean textureFlag = true;
	
	private MassObjectRenderer() {
	}
	
	static final void drawMassObject(final GL2 gl, final MassObject o,
			final int slices, final int stacks) {
		
		double da, r, dz;
		double x, y, z;
		int i, j;
		
		da = 2.0f * PI / slices;
		dz = o.getLength() / stacks;
		
		double ds = 1.0f / slices;
		double dt = 1.0f / stacks;
		double t = 0.0f;
		z = 0.0f;
		for (j = 0; j < stacks; j++) {
			r = getRadius(o, z);
			double rNext = getRadius(o, z + dz);
			
			if (j == stacks - 1)
				rNext = 0;
			
			double s = 0.0f;
			glBegin(gl, GL2.GL_QUAD_STRIP);
			for (i = 0; i <= slices; i++) {
				if (i == slices) {
					x = sin(0.0f);
					y = cos(0.0f);
				} else {
					x = sin((i * da));
					y = cos((i * da));
				}
				
				if (r == 0)
					normal3d(gl, 0, 0, 1);
				else
					normal3d(gl, x, y, z);
				TXTR_COORD(gl, s, t);
				glVertex3d(gl, (x * r), (y * r), z);
				
				if (rNext == 0)
					normal3d(gl, 0, 0, -1);
				else
					normal3d(gl, x, y, z);
				TXTR_COORD(gl, s, t + dt);
				glVertex3d(gl, (x * rNext), (y * rNext), (z + dz));
				
				s += ds;
			} // for slices
			glEnd(gl);
			// r += dr;
			t += dt;
			z += dz;
		} // for stacks
	}
	
	private static final double getRadius(MassObject o, double z) {
		double arc = Math.min(o.getLength(), 2 * o.getRadius()) * 0.35f;
		double r = o.getRadius();
		if (z == 0 || z == o.getLength())
			return 0;
		if (z < arc) {
			double zz = z - arc;
			return (r - arc) + Math.sqrt(arc * arc - zz * zz);
		}
		if (z > o.getLength() - arc) {
			double zz = (z - o.getLength() + arc);
			return (r - arc) + Math.sqrt(arc * arc - zz * zz);
		}
		return o.getRadius();
	}
	
	// ----------------------------------------------------------------------
	// Internals only below this point
	//
	
	private static final double PI = Math.PI;
	
	private static final void glBegin(GL gl, int mode) {
		gl.getGL2().glBegin(mode);
	}
	
	private static final void glEnd(GL gl) {
		gl.getGL2().glEnd();
	}
	
	private static final void glVertex3d(GL gl, double x, double y, double z) {
		gl.getGL2().glVertex3d(x, y, z);
	}
	
	private static final void glNormal3d(GL gl, double x, double y, double z) {
		gl.getGL2().glNormal3d(x, y, z);
	}
	
	private static final void glTexCoord2d(GL gl, double x, double y) {
		gl.getGL2().glTexCoord2d(x, y);
	}
	
	/**
	 * Call glNormal3f after scaling normal to unit length.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	private static final void normal3d(GL gl, double x, double y, double z) {
		double mag;
		
		mag = Math.sqrt(x * x + y * y + z * z);
		if (mag > 0.00001F) {
			x /= mag;
			y /= mag;
			z /= mag;
		}
		glNormal3d(gl, x, y, z);
	}
	
	private static final void TXTR_COORD(GL gl, double x, double y) {
		if (textureFlag)
			glTexCoord2d(gl, x, y);
	}
	
	private static final double sin(double r) {
		return Math.sin(r);
	}
	
	private static final double cos(double r) {
		return Math.cos(r);
	}
}
