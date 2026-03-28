package info.openrocket.swing.gui.figure3d;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.swing.gui.figure3d.geometry.ComponentRenderer;
import info.openrocket.swing.gui.figure3d.geometry.DisplayListComponentRenderer;
import info.openrocket.swing.gui.figure3d.geometry.Geometry;
import info.openrocket.swing.gui.figure3d.geometry.Geometry.Surface;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 * @author Daniel Williams <equipoise@gmail.com>
 */
public abstract class RocketRenderer {
	protected static final Logger log = LoggerFactory.getLogger(RocketRenderer.class);
	private static final boolean isMacOS = SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS;
	
	final ComponentRenderer cr = new DisplayListComponentRenderer();
	
	private final float[] selectedEmissive = { 1, 0, 0, 1 };
	private final float[] colorBlack = { 0, 0, 0, 1 };
	
	public void init(GLAutoDrawable drawable) {
		cr.init(drawable);
	}
	
	public void dispose(GLAutoDrawable drawable) {
	}
	
	public void updateFigure(GLAutoDrawable drawable) {
		cr.updateFigure(drawable);
	}
	
	public abstract void renderComponent(GL2 gl, Geometry geom, float alpha);
    
	public abstract boolean isDrawnTransparent(RocketComponent c);
	
	public abstract void flushTextureCache(GLAutoDrawable drawable);

	/**
	 * This function is a bit.... unusual.  Instead of computing an inverse transform from the UI window into design-space,
	 * this renders each component with a unique identifiable color ... to a dummy, throwaway canvas:
	 *
	 * Then, we read the pixel (RGB) color value at a point on the canvas, and use that color to identify the component
	 *
	 * @param drawable canvas to draw to
	 * @param configuration active configuration
	 * @param p point to select at
	 * @param ignore list of ignore components
	 * @return optional (nullable) component selection result
	 */
	public RocketComponent pick(GLAutoDrawable drawable, FlightConfiguration configuration, Point p, Set<RocketComponent> ignore) {
		if (isMacOS) {
			return pickMacOS(drawable, configuration, p, ignore);
		} else {
			return pickNormal(drawable, configuration, p, ignore);
		}
	}

	/**
	 * Pick implementation for non-Mac platforms.
	 */
	private RocketComponent pickNormal(GLAutoDrawable drawable, FlightConfiguration configuration, Point p, Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Store a vector of pickable parts.
		final Map<Integer, RocketComponent> selectionMap = new HashMap<>();

		Collection<Geometry> geometryList = getTreeGeometry( configuration);
		for(Geometry geom: geometryList ) {
			final RocketComponent comp = geom.getComponent();
			if (ignore != null && ignore.contains(comp))
				continue;

			final int hashCode = comp.hashCode();

			selectionMap.put(hashCode, comp);

			gl.glColor4ub((byte) ((hashCode >> 24) & 0xFF),  // red channel (LSB)
					(byte) ((hashCode >> 16) & 0xFF),  // green channel
					(byte) ((hashCode >> 8) & 0xFF),  // blue channel
					(byte) ((hashCode) & 0xFF));  // alpha channel (MSB)

			if (isDrawnTransparent(comp)) {
				geom.render(gl, Surface.INSIDE);
			} else {
				geom.render(gl, Surface.ALL);
			}
		}

		if (p == null)
			return null; //Allow pick to be called without a point for debugging

		final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		gl.glReadPixels(p.x, p.y, // coordinates of "first" pixel to read
				1, 1, // width, height of rectangle to read
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
				buffer);  // output buffer
		final int pixelValue = buffer.getInt();

		return selectionMap.get(pixelValue);
	}

	/**
	 * Pick implementation for macOS platforms.
	 *
	 * Differs from {@link #pickNormal(GLAutoDrawable, FlightConfiguration, Point, Set)} in two key ways:
	 * - HiDPI GLJPanel on macOS often renders into a multisampled draw FBO and resolves into a separate read FBO.
	 *   This method detects separate draw/read bindings and, when needed, blits (resolves) the color buffer so
	 *   {@code glReadPixels} reads the actual pick-render result.
	 * - It also resets relevant GL state (lighting, blending, textures, etc.) and clears the buffer before the
	 *   color-ID pass to ensure a deterministic readback independent of prior rendering state.
	 */
	private RocketComponent pickMacOS(GLAutoDrawable drawable, FlightConfiguration configuration, Point p, Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		final int[] framebufferBinding = new int[1];
		final int[] drawFramebufferBinding = new int[] { -1 };
		final int[] readFramebufferBinding = new int[] { -1 };
		gl.glGetIntegerv(GL2.GL_FRAMEBUFFER_BINDING, framebufferBinding, 0);

		// Drain any previous GL error state so we can detect unsupported queries below.
		// glGetError() clears one error at a time; cap iterations as a safety net against driver bugs.
		for (int i = 0; i < 16; i++) {
			if (gl.glGetError() == GL.GL_NO_ERROR) {
				break;
			}
		}

		// GLJPanel commonly uses separate read/draw FBO bindings internally (e.g. MSAA resolve),
		// and glReadPixels() reads from the READ binding. Try to capture both when available.
		gl.glGetIntegerv(GL2GL3.GL_DRAW_FRAMEBUFFER_BINDING, drawFramebufferBinding, 0);
		gl.glGetIntegerv(GL2GL3.GL_READ_FRAMEBUFFER_BINDING, readFramebufferBinding, 0);
		final boolean hasSeparateFramebufferBindings = (gl.glGetError() == GL.GL_NO_ERROR)
				&& drawFramebufferBinding[0] >= 0
				&& readFramebufferBinding[0] >= 0;

		final int pickDrawFramebuffer = drawFramebufferBinding[0] > 0 ? drawFramebufferBinding[0] : framebufferBinding[0];
		final int pickReadFramebuffer = readFramebufferBinding[0] > 0 ? readFramebufferBinding[0] : pickDrawFramebuffer;
		final boolean needsResolve = hasSeparateFramebufferBindings
				&& pickDrawFramebuffer > 0
				&& pickReadFramebuffer > 0
				&& pickDrawFramebuffer != pickReadFramebuffer;

		// Ensure predictable output for color-based picking by overriding any state
		// left behind by the main render pass (textures, blending, etc.).
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
		try {
			// Force drawing to the draw framebuffer used by GLJPanel. If it is multisampled, we
			// may need to blit/resolve into the read framebuffer before glReadPixels().
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, pickDrawFramebuffer);

			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glDisable(GL.GL_BLEND);
			gl.glDisable(GL.GL_DITHER);
			gl.glDisable(GL.GL_MULTISAMPLE);
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glDisable(GL2GL3.GL_SCISSOR_TEST);
			gl.glDisable(GL2.GL_COLOR_MATERIAL);
			gl.glDisable(GL2.GL_ALPHA_TEST);
			gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(true);
			gl.glColorMask(true, true, true, true);

			// Use a known background color that does not collide with any id (ids start at 1).
			gl.glClearColor(0, 0, 0, 0);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

			// Store a vector of pickable parts.
			final Map<Integer, RocketComponent> selectionMap = new HashMap<>();

			int id = 1;
			Collection<Geometry> geometryList = getTreeGeometry(configuration);
			for (Geometry geom : geometryList) {
				final RocketComponent comp = geom.getComponent();
				if (ignore != null && ignore.contains(comp)) {
					continue;
				}

				selectionMap.put(id, comp);

				gl.glColor4ub(
						(byte) ((id >> 16) & 0xFF),
						(byte) ((id >> 8) & 0xFF),
						(byte) (id & 0xFF),
						(byte) 0xFF);

				if (isDrawnTransparent(comp)) {
					geom.render(gl, Surface.INSIDE);
				} else {
					geom.render(gl, Surface.ALL);
				}
				id++;
			}

			if (p == null) {
				return null; //Allow pick to be called without a point for debugging
			}

			final int[] viewport = new int[4];
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			final int[] drawBuffer = new int[1];
			gl.glGetIntegerv(GL2.GL_DRAW_BUFFER, drawBuffer, 0);

			if (needsResolve) {
				// Resolve (blit) the rendered colors into the read framebuffer which is what GLJPanel
				// typically uses for presentation and readback.
				gl.glBindFramebuffer(GL2GL3.GL_READ_FRAMEBUFFER, pickDrawFramebuffer);
				gl.glBindFramebuffer(GL2GL3.GL_DRAW_FRAMEBUFFER, pickReadFramebuffer);
				gl.glReadBuffer(drawBuffer[0]);
				gl.glDrawBuffer(drawBuffer[0]);
				gl.glBlitFramebuffer(0, 0, viewport[2], viewport[3],
						0, 0, viewport[2], viewport[3],
						GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);
			}

			// Read from the resolved framebuffer if needed (common with GLJPanel MSAA).
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, needsResolve ? pickReadFramebuffer : pickDrawFramebuffer);
			gl.glReadBuffer(drawBuffer[0]);

			final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
			gl.glReadPixels(p.x, p.y, 1, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);

			final int r = buffer.get(0) & 0xFF;
			final int g = buffer.get(1) & 0xFF;
			final int b = buffer.get(2) & 0xFF;
			final int pickedId = (r << 16) | (g << 8) | b;
			return selectionMap.get(pickedId);
		} finally {
			gl.glPopAttrib();
			// Restore GLJPanel FBO bindings if we managed to query them; otherwise restore the single binding.
			if (hasSeparateFramebufferBindings) {
				gl.glBindFramebuffer(GL2GL3.GL_DRAW_FRAMEBUFFER, Math.max(0, drawFramebufferBinding[0]));
				gl.glBindFramebuffer(GL2GL3.GL_READ_FRAMEBUFFER, Math.max(0, readFramebufferBinding[0]));
			} else {
				gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebufferBinding[0]);
			}
		}
	}
	
	public void render(GLAutoDrawable drawable, FlightConfiguration configuration, Set<RocketComponent> selection) {
		
		if (cr == null)
			throw new IllegalStateException(this + " Not Initialized");
		

        Collection<Geometry> geometry = getTreeGeometry( configuration);
        
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		{ // Draw selection outline at nearest Z
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, selectedEmissive, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, colorBlack, 0);
			gl.glLineWidth(5.0f);
			
			for (Geometry geom : geometry) {
			    RocketComponent rc = geom.getComponent();
				if (selection.contains( rc)) {
					// Draw as lines, set Z to nearest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
					gl.glDepthRange(0, 0);
					geom.render(gl, Surface.ALL);
					
					// Draw polygons, always passing depth test,
					// setting Z to farthest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
					gl.glDepthRange(1, 1);
					gl.glDepthFunc(GL.GL_ALWAYS);
					geom.render(gl, Surface.ALL);
					gl.glDepthFunc(GL.GL_LESS);
					gl.glDepthRange(0, 1);
				}
			}
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, colorBlack, 0);
		} // done with selection outline
		
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glEnable( GL.GL_BLEND );

		// needs to be rendered before the components
        renderMotors(gl, configuration);

		// render all components
		renderTree( gl, geometry );
		
		gl.glDisable( GL.GL_BLEND );
	}
	
	private Collection<Geometry> getTreeGeometry( FlightConfiguration config){
		// input
		final InstanceMap imap = config.getActiveInstances();

		// output buffer
		final Collection<Geometry> treeGeometry = new ArrayList<>();

		for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent comp = entry.getKey();
			
			final ArrayList<InstanceContext> contextList = entry.getValue();

			for(InstanceContext context: contextList ) {
				Geometry instanceGeometry = cr.getComponentGeometry( comp, context.transform );
				treeGeometry.add( instanceGeometry );
			}
		}
		return treeGeometry;
	}

	private void renderTree( GL2 gl, final Collection<Geometry> geometryList){
		//cycle through opaque components first, then transparent to preserve proper depth testing
		for(Geometry geom: geometryList ) {
			//if not transparent
			if( !isDrawnTransparent( (RocketComponent)geom.obj) ){
				renderComponent(gl, geom, 1.0f);
			}
		}
		for(Geometry geom: geometryList ) {
			if( isDrawnTransparent( (RocketComponent)geom.obj) ){
				// Draw T&T front faces blended, without depth test
				renderComponent(gl, geom, 0.2f);
			}
		}
	}

	private void renderMotors(GL2 gl, FlightConfiguration configuration) {
		for( MotorConfiguration curMotor : configuration.getActiveMotors()){
			MotorMount mount = curMotor.getMount();
			Motor motor = curMotor.getMotor();
			
			if( null == motor ){
				throw new NullPointerException(" null motor from configuration.getActiveMotors...  this is a bug.");
			}

			if (!((RocketComponent) mount).isVisible()) {
				continue;
			}
			double length = motor.getLength();
		
			CoordinateIF[] position = ((RocketComponent) mount).toAbsolute(new Coordinate(((RocketComponent) mount)
					.getLength() + mount.getMotorOverhang() - length));

			for (CoordinateIF coordinate : position) {
				gl.glPushMatrix();
				gl.glTranslated(coordinate.getX(), coordinate.getY(), coordinate.getZ());
				renderMotor(gl, motor);
				gl.glPopMatrix();
			}
			
		}
	}
	
	protected void renderMotor(GL2 gl, Motor motor) {
		cr.getMotorGeometry(motor).render(gl, Surface.ALL);
	}
	
}
