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
package net.sf.openrocket.gui.figure3d.photo.exhaust;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public final class FlameRenderer {

	public interface FlameSettings {
		public double getExhaustScale();
		
		public boolean isFlame();

		public Color getFlameColor();

		public boolean isSmoke();

		public Color getSmokeColor();

		public double getSmokeAlpha();

		public double getFlameAspectRatio();

		public boolean isSparks();
		
		public double getSparkConcentration();
		
		public double getSparkWeight();
	}

	private static final Logger log = LoggerFactory.getLogger(FlameRenderer.class);

	private FlameRenderer() {
	}

	public static void drawExhaust(GL2 gl, FlameSettings fs, Motor motor) {
		final float s = (float) Math.max(.5, Math.sqrt(motor.getAverageThrustEstimate()) / 4.0)
				* (float) fs.getExhaustScale();
		gl.glScalef(s, s, s);

		gl.glRotated(90, 0, 1, 0);
		gl.glTranslated(0, 0, 0);

		gl.glDisable(GLLightingFunc.GL_LIGHTING);

		if (fs.isSparks() && fs.isFlame()) {
			sparks(gl, fs);
		}

		gl.glEnable(GL.GL_BLEND);

		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		gl.glDepthMask(false);

		if (fs.isSmoke()) {
			final float LEN = 10;
			final float MAX_R = .15f;
			final int P = 5;

			final Func radius = new Func() {
				@Override
				public float f(double d) {
					return (float) (Math.atan(d) / (Math.PI / 2.0)) * MAX_R + 0.001f;
				}
			};

			final Func dZ = new Func() {
				@Override
				public float f(double z) {
					return radius.f(z);
				}
			};

			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

			gl.glActiveTexture(GL.GL_TEXTURE0);
			smokeT.bind(gl);

			gl.glActiveTexture(GL.GL_TEXTURE1);
			smokeN.bind(gl);

			gl.glUseProgram(shaderprogram);

			setUniform1i(gl, shaderprogram, "uSmoke", 0);
			setUniform1i(gl, shaderprogram, "uNormal", 1);

			trail(gl, radius, dZ, //
					new Const(0.025f * (fs.getSmokeColor().getAlpha() / 255f)), //
					LEN, P, fs.getSmokeColor(), s);

			gl.glUseProgram(0);

			smokeN.disable(gl);
			gl.glActiveTexture(GL.GL_TEXTURE0);
		}

		if (fs.isFlame()) {
			gl.glScalef(1, 1, (float) fs.getFlameAspectRatio());

			gl.glActiveTexture(GL.GL_TEXTURE0);
			flameT.enable(gl);
			flameT.bind(gl);

			final float FLEN = 0.3f;
			final int FP = 6;
			final Func fr = new Func() {
				@Override
				public float f(double z) {
					z = z / FLEN;
					z = 1 - z;
					return (float) (z * z - z * z * z) * .06f;
				}
			};

			final Func fdZ = new Func() {
				@Override
				public float f(double z) {
					return 0.002f;
				}
			};

			final Func alpha = new Func() {
				@Override
				public float f(double z) {
					return 0.2f * (float) Math.pow((1.0f - (float) z / FLEN), 4);
				};
			};

			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
			trail(gl, fr, fdZ, alpha, FLEN, FP, fs.getFlameColor(), s);

			flameT.disable(gl);
		}

		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glDepthMask(true);

	}

	private static void convertColor(Color color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = (float) color.getRed() / 255f;
			out[1] = (float) color.getGreen() / 255f;
			out[2] = (float) color.getBlue() / 255f;
		}
	}

	static Texture flameT;
	static Texture smokeT;
	static Texture smokeN;
	static int shaderprogram;

	private static interface Func {
		float f(double d);
	}

	private static final class Const implements Func {
		final float val;

		public Const(final float val) {
			this.val = val;
		}

		@Override
		public float f(final double d) {
			return val;
		}
	}

	public static void init(GL2 gl) {
		try {
			log.debug("Loading Textures");
			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(),
					FlameRenderer.class.getResourceAsStream("/datafiles/flame/c-color.png"), GL.GL_RGBA, GL.GL_RGBA,
					true, null);
			smokeT = TextureIO.newTexture(data);
			data = TextureIO.newTextureData(GLProfile.getDefault(),
					FlameRenderer.class.getResourceAsStream("/datafiles/flame/c-normal.png"), GL.GL_RGBA, GL.GL_RGBA,
					true, null);
			smokeN = TextureIO.newTexture(data);
			data = TextureIO.newTextureData(GLProfile.getDefault(),
					FlameRenderer.class.getResourceAsStream("/datafiles/flame/smoke2.png"), GL.GL_RGBA, GL.GL_RGBA,
					true, null);
			flameT = TextureIO.newTexture(data);

			log.debug("Loading Shader");
			String line;
			shaderprogram = gl.glCreateProgram();

			/*
			 * int v = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER); BufferedReader
			 * brv = new BufferedReader(new
			 * InputStreamReader(FlameRenderer.class
			 * .getResourceAsStream("smokeVertex.glsl"))); String vsrc = "";
			 * while ((line = brv.readLine()) != null) { vsrc += line + "\n"; }
			 * gl.glShaderSource(v, 1, new String[] { vsrc }, (int[]) null, 0);
			 * gl.glAttachShader(shaderprogram, v); gl.glCompileShader(v);
			 */

			int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
			BufferedReader brf = new BufferedReader(new InputStreamReader(
					FlameRenderer.class.getResourceAsStream("/datafiles/flame/smokeShader.glsl")));
			String fsrc = "";
			while ((line = brf.readLine()) != null) {
				fsrc += line + "\n";
			}
			gl.glShaderSource(f, 1, new String[] { fsrc }, (int[]) null, 0);
			gl.glCompileShader(f);

			int statusFragmentShader[] = new int[1];
			gl.glGetShaderiv(f, GL2.GL_COMPILE_STATUS, IntBuffer.wrap(statusFragmentShader));
			if (statusFragmentShader[0] == GL2.GL_FALSE) {
				int infoLogLenght[] = new int[1];
				gl.glGetShaderiv(f, GL2.GL_INFO_LOG_LENGTH, IntBuffer.wrap(infoLogLenght));
				ByteBuffer infoLog = Buffers.newDirectByteBuffer(infoLogLenght[0]);
				gl.glGetShaderInfoLog(f, infoLogLenght[0], null, infoLog);
				byte[] infoBytes = new byte[infoLogLenght[0]];
				infoLog.get(infoBytes);
				String out = new String(infoBytes);
				System.err.println("Fragment shader error:\n" + out);
			}

			gl.glAttachShader(shaderprogram, f);

			gl.glLinkProgram(shaderprogram);
			gl.glValidateProgram(shaderprogram);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void dispose(GL2 gl) {
		log.debug("Destroying Textures");
		smokeT.destroy(gl);
		smokeN.destroy(gl);
		flameT.destroy(gl);
		smokeT = null;
		smokeN = null;
		flameT = null;
		log.debug("Deleting Shader {}", shaderprogram);
		//gl.glDeleteShader(shaderprogram); TODO Why is this broken?
		shaderprogram = 0;
	}

	private static void trail(GL2 gl, Func radius, Func dZ, Func alpha, float LEN, int P, Color color, float scale) {
		float[] c = new float[4];
		convertColor(color, c);

		// Figure out if the flame and smoke is point "in" or "out" of the
		// screen
		// in order to draw the particles in the right Z order
		final boolean startAtTop;
		{
			final double[] mvmatrix = new double[16];
			final double[] projmatrix = new double[16];
			final int[] viewport = new int[4];

			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mvmatrix, 0);
			gl.glGetDoublev(GLMatrixFunc.GL_PROJECTION_MATRIX, projmatrix, 0);

			final double out[] = new double[4];
			final double out2[] = new double[4];
			(new GLU()).gluProject(0, 0, 0, mvmatrix, 0, projmatrix, 0, viewport, 0, out, 0);
			(new GLU()).gluProject(0, 0, 0.01f, mvmatrix, 0, projmatrix, 0, viewport, 0, out2, 0);

			startAtTop = out2[2] < out[2];
		}

		final float start;
		final float len;
		final float mult;

		if (startAtTop) {
			start = 0.002f;
			len = LEN;
			mult = 1;
		} else {
			start = LEN;
			len = 0.002f;
			mult = -1;
		}

		// Use the same seed every time
		Random r = new Random(0);

		// Loop forwards or backwards. Technically the dZ is applied differently
		// in either direction, but the difference should be vanishingly small.
		for (float z = start; mult * z < mult * len; z += mult * dZ.f(z)) {
			gl.glPushMatrix();
			gl.glTranslatef(0, 0, z);

			c[3] = alpha.f(z);
			gl.glColor4fv(c, 0);
			
			int[] ii = {0};
			gl.glGetIntegerv(GL2.GL_CURRENT_PROGRAM, ii, 0);
			if ( ii[0] == shaderprogram )
				setUniform1f(gl, shaderprogram, "z", z);

			for (int i = 0; i < P; i++) {
				gl.glPushMatrix();
				float rx = radius.f(z) - ((float) r.nextFloat() * radius.f(z) * 2.0f);
				float ry = radius.f(z) - ((float) r.nextFloat() * radius.f(z) * 2.0f);
				float rz = radius.f(z) - ((float) r.nextFloat() * radius.f(z) * 2.0f);
				gl.glTranslatef(rx, ry, rz);

				final double[] mvmatrix = new double[16];
				gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mvmatrix, 0);
				mvmatrix[0] = mvmatrix[5] = mvmatrix[10] = 1;
				mvmatrix[1] = mvmatrix[2] = mvmatrix[4] = mvmatrix[6] = mvmatrix[8] = mvmatrix[9] = 0;
				gl.glLoadMatrixd(mvmatrix, 0);

				// Add a random rotation to prevent artifacts from texture.
				gl.glRotatef(r.nextFloat() * 45f, 0, 0, 1);

				gl.glBegin(GL.GL_TRIANGLE_FAN);
				float d = radius.f(z) * scale * 2;
				gl.glTexCoord2f(0, 0);
				gl.glVertex3f(-d, -d, 0);
				gl.glTexCoord2f(0, 1);
				gl.glVertex3f(-d, d, 0);
				gl.glTexCoord2f(1, 1);
				gl.glVertex3f(d, d, 0);
				gl.glTexCoord2f(1, 0);
				gl.glVertex3f(d, -d, 0);
				gl.glEnd();

				gl.glPopMatrix();
			}

			gl.glPopMatrix();
		}
	}

	private static void setUniform1i(GL2 inGL, int inProgramID, String inName, int inValue) {
		int tUniformLocation = inGL.glGetUniformLocation(inProgramID, inName);
		if (tUniformLocation != -1) {
			inGL.glUniform1i(tUniformLocation, inValue);
		} else {
			log.warn("UNIFORM COULD NOT BE FOUND! NAME={}", inName);
		}
	}
	
	private static void setUniform1f(GL2 inGL, int inProgramID, String inName, float inValue) {
		int tUniformLocation = inGL.glGetUniformLocation(inProgramID, inName);
		if (tUniformLocation != -1) {
			inGL.glUniform1f(tUniformLocation, inValue);
		} else {
			log.warn("UNIFORM COULD NOT BE FOUND! NAME={}", inName);
		}
	}

	private static void sparks(GL2 gl, FlameSettings fs) {
		// Use the same seed every time
		Random r = new Random(0);

		float[] c = new float[4];
		float[] c2 = new float[4];
		convertColor(fs.getFlameColor(), c);
		for (int i = 0; i < 3; i++) {
			c[i] = c2[i] = c[i] * .2f + .8f;
		}
		c[3] = 1;
		c2[3] = 1;
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL.GL_BLEND);
		gl.glLineWidth(1 + (float)fs.getSparkWeight() * 1.0f);
		for (int i = 0; i < 4000 * fs.getSparkConcentration(); i++) {
			final float z = 0.01f + 2 * (r.nextFloat() * r.nextFloat() * r.nextFloat());
			final float x = z * (r.nextFloat() - 0.5f);
			final float y = z * (r.nextFloat() - 0.5f);
			//gl.glPointSize(1);
			gl.glBegin(GL.GL_LINES);
			gl.glColor4fv(c, 0);
			gl.glVertex3f(x, y, z * 2);
			gl.glColor4fv(c2, 0);
			gl.glVertex3f(x * 1.02f, y * 1.02f, z * 2 + 0.01f + ((float)fs.getSparkWeight() / 20));
			gl.glEnd();
		}
	}

}
