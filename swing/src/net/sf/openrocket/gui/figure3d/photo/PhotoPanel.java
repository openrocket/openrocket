package net.sf.openrocket.gui.figure3d.photo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.document.events.DocumentChangeListener;
import net.sf.openrocket.gui.figure3d.RealisticRenderer;
import net.sf.openrocket.gui.figure3d.RocketRenderer;
import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.exhaust.FlameRenderer;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class PhotoPanel extends JPanel implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PhotoPanel.class);

	static {
		// this allows the GL canvas and things like the motor selection
		// drop down to z-order themselves.
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	private Configuration configuration;
	private Component canvas;
	private TextureCache textureCache = new TextureCache();
	private double ratio;
	private boolean needUpdate = false;

	private List<ImageCallback> imageCallbacks = new java.util.Vector<PhotoPanel.ImageCallback>();

	interface ImageCallback {
		public void performAction(BufferedImage i);
	}

	void addImageCallback(ImageCallback a) {
		imageCallbacks.add(a);
		repaint();
	}

	private RocketRenderer rr;
	private PhotoSettings p;

	void setDoc(final OpenRocketDocument doc) {
		((GLAutoDrawable) canvas).invoke(false, new GLRunnable() {
			@Override
			public boolean run(final GLAutoDrawable drawable) {
				PhotoPanel.this.configuration = doc.getDefaultConfiguration();
				cachedBounds = null;
				rr = new RealisticRenderer(doc);
				rr.init(drawable);

				doc.getDefaultConfiguration().addChangeListener(
						new StateChangeListener() {
							@Override
							public void stateChanged(EventObject e) {
								log.debug("Repainting on config state change");
								needUpdate = true;
								PhotoPanel.this.repaint();
							}
						});

				doc.addDocumentChangeListener(new DocumentChangeListener() {
					@Override
					public void documentChanged(DocumentChangeEvent event) {
						log.debug("Repainting on document change");
						needUpdate = true;
						PhotoPanel.this.repaint();
					}
				});

				return false;
			}
		});
	}

	PhotoSettings getSettings() {
		return p;
	}

	PhotoPanel() {
		this.setLayout(new BorderLayout());

		p = new PhotoSettings();

		// Fixes a linux / X bug: Splash must be closed before GL Init
		SplashScreen splash = Splash.getSplashScreen();
		if (splash != null && splash.isVisible())
			splash.close();

		initGLCanvas();
		setupMouseListeners();

		p.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				log.debug("Repainting on settings state change");
				PhotoPanel.this.repaint();
			}
		});

	}

	private void initGLCanvas() {
		try {
			log.debug("Setting up GL capabilities...");
			final GLProfile glp = GLProfile.get(GLProfile.GL2);

			final GLCapabilities caps = new GLCapabilities(glp);

			if (Application.getPreferences().getBoolean(
					Preferences.OPENGL_ENABLE_AA, true)) {
				caps.setSampleBuffers(true);
				caps.setNumSamples(6);
			} else {
				log.trace("GL - Not enabling AA by user pref");
			}

			if (Application.getPreferences().getBoolean(
					Preferences.OPENGL_USE_FBO, false)) {
				log.trace("GL - Creating GLJPanel");
				canvas = new GLJPanel(caps);
			} else {
				log.trace("GL - Creating GLCanvas");
				canvas = new GLCanvas(caps);
			}

			((GLAutoDrawable) canvas).addGLEventListener(this);
			this.add(canvas, BorderLayout.CENTER);
		} catch (Throwable t) {
			log.error("An error occurred creating 3d View", t);
			canvas = null;
			this.add(new JLabel("Unable to load 3d Libraries: "
					+ t.getMessage()));
		}
	}

	private void setupMouseListeners() {
		MouseInputAdapter a = new MouseInputAdapter() {
			int lastX;
			int lastY;
			MouseEvent pressEvent;

			@Override
			public void mousePressed(final MouseEvent e) {
				lastX = e.getX();
				lastY = e.getY();
				pressEvent = e;
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				p.setViewDistance(p.getViewDistance() + 0.1
						* e.getWheelRotation());
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				// You can get a drag without a press while a modal dialog is
				// shown
				if (pressEvent == null)
					return;

				final double height = canvas.getHeight();
				final double width = canvas.getWidth();
				final double x1 = (width - 2 * lastX) / width;
				final double y1 = (2 * lastY - height) / height;
				final double x2 = (width - 2 * e.getX()) / width;
				final double y2 = (2 * e.getY() - height) / height;

				p.setViewAltAz(p.getViewAlt() - (y1 - y2), p.getViewAz()
						+ (x1 - x2));

				lastX = e.getX();
				lastY = e.getY();
			}
		};

		canvas.addMouseWheelListener(a);
		canvas.addMouseMotionListener(a);
		canvas.addMouseListener(a);
	}

	@Override
	public void paintImmediately(Rectangle r) {
		super.paintImmediately(r);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	@Override
	public void paintImmediately(int x, int y, int w, int h) {
		super.paintImmediately(x, y, w, h);
		if (canvas != null)
			((GLAutoDrawable) canvas).display();
	}

	/*
	 * @Override public void repaint() { if (canvas != null) ((GLAutoDrawable)
	 * canvas).display(); super.repaint(); }
	 */
	@Override
	public void display(final GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		if (needUpdate)
			rr.updateFigure(drawable);
		needUpdate = false;

		draw(drawable, 0);

		if (p.isMotionBlurred()) {
			Bounds b = calculateBounds();

			float m = .6f;
			int c = 10;
			float d = (float) b.xSize / 25.0f;

			gl.glAccum(GL2.GL_LOAD, m);

			for (int i = 1; i <= c; i++) {
				draw(drawable, d / c * i);
				gl.glAccum(GL2.GL_ACCUM, (1.0f - m) / c);
			}

			gl.glAccum(GL2.GL_RETURN, 1.0f);
		}

		if (!imageCallbacks.isEmpty()) {
			BufferedImage i = (new AWTGLReadBufferUtil(
					GLProfile.get(GLProfile.GL2), false))
					.readPixelsToBufferedImage(drawable.getGL(), 0, 0,
							drawable.getWidth(), drawable.getHeight(), true);
			final Vector<ImageCallback> cbs = new Vector<PhotoPanel.ImageCallback>(
					imageCallbacks);
			imageCallbacks.clear();
			for (ImageCallback ia : cbs) {
				try {
					ia.performAction(i);
				} catch (Throwable t) {
					log.error("Image Callback {} threw", i, t);
				}
			}
		}
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

	private void draw(final GLAutoDrawable drawable, float dx) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();

		float[] color = new float[3];

		gl.glEnable(GL.GL_MULTISAMPLE);

		convertColor(p.getSunlight(), color);
		float amb = (float) p.getAmbiance();
		float dif = 1.0f - amb;
		float spc = 1.0f;
		gl.glLightfv(
				GLLightingFunc.GL_LIGHT1,
				GLLightingFunc.GL_AMBIENT,
				new float[] { amb * color[0], amb * color[1], amb * color[2], 1 },
				0);
		gl.glLightfv(
				GLLightingFunc.GL_LIGHT1,
				GLLightingFunc.GL_DIFFUSE,
				new float[] { dif * color[0], dif * color[1], dif * color[2], 1 },
				0);
		gl.glLightfv(
				GLLightingFunc.GL_LIGHT1,
				GLLightingFunc.GL_SPECULAR,
				new float[] { spc * color[0], spc * color[1], spc * color[2], 1 },
				0);

		convertColor(p.getSkyColor(), color);
		gl.glClearColor(color[0], color[1], color[2], 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(p.getFov() * (180.0 / Math.PI), ratio, 0.1f, 50f);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		// Flip textures for LEFT handed coords
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glScaled(-1, 1, 1);
		gl.glTranslated(-1, 0, 0);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glFrontFace(GL.GL_CCW);

		// Draw the sky
		gl.glPushMatrix();
		gl.glDisable(GLLightingFunc.GL_LIGHTING);
		gl.glDepthMask(false);
		gl.glRotated(p.getViewAlt() * (180.0 / Math.PI), 1, 0, 0);
		gl.glRotated(p.getViewAz() * (180.0 / Math.PI), 0, 1, 0);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if (p.getSky() != null) {
			p.getSky().draw(gl, textureCache);
		}
		gl.glDepthMask(true);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glPopMatrix();

		if (rr == null)
			return;

		glu.gluLookAt(0, 0, p.getViewDistance(), 0, 0, 0, 0, 1, 0);
		gl.glRotated(p.getViewAlt() * (180.0 / Math.PI), 1, 0, 0);
		gl.glRotated(p.getViewAz() * (180.0 / Math.PI), 0, 1, 0);

		float[] lightPosition = new float[] {
				(float) Math.cos(p.getLightAlt())
						* (float) Math.sin(p.getLightAz()),//
				(float) Math.sin(p.getLightAlt()),//
				(float) Math.cos(p.getLightAlt())
						* (float) Math.cos(p.getLightAz()), //
				0 };

		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION,
				lightPosition, 0);

		// Change to LEFT Handed coordinates
		gl.glScaled(1, 1, -1);
		gl.glFrontFace(GL.GL_CW);
		setupModel(gl);

		gl.glTranslated(dx - p.getAdvance(), 0, 0);

		if (p.isFlame()) {
			convertColor(p.getFlameColor(), color);

			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_AMBIENT,
					new float[] { 0, 0, 0, 1 }, 0);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_DIFFUSE,
					new float[] { color[0], color[1], color[2], 1 }, 0);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_SPECULAR,
					new float[] { color[0], color[1], color[2], 1 }, 0);

			Bounds b = calculateBounds();
			gl.glLightf(GLLightingFunc.GL_LIGHT2,
					GLLightingFunc.GL_QUADRATIC_ATTENUATION, 20f);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_POSITION,
					new float[] { (float) (b.xMax + .1f), 0, 0, 1 }, 0);
			gl.glEnable(GLLightingFunc.GL_LIGHT2);
		} else {
			gl.glDisable(GLLightingFunc.GL_LIGHT2);
			gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_DIFFUSE,
					new float[] { 0, 0, 0, 1 }, 0);
		}

		rr.render(drawable, configuration, new HashSet<RocketComponent>());
		
		//Figure out the lowest stage shown
		final int currentStageNumber = configuration.getActiveStages()[configuration.getActiveStages().length-1];
		final Stage currentStage = (Stage)configuration.getRocket().getChild(currentStageNumber);
		
		final String motorID = configuration.getFlightConfigurationID();
		final Iterator<MotorMount> iterator = configuration.motorIterator();
		motor: while (iterator.hasNext()) {
			final MotorMount mount = iterator.next();
			
			//If this mount is not in currentStage continue on to the next one.
			RocketComponent parent = ((RocketComponent)mount);
			while ( null != (parent = parent.getParent()) ){
				if ( parent instanceof Stage ){
					if ( parent != currentStage )
						continue motor;
					break;
				}
			}
			
			final Motor motor = mount.getMotorConfiguration().get(motorID).getMotor();
			final double length = motor.getLength();

			Coordinate[] position = ((RocketComponent) mount)
					.toAbsolute(new Coordinate(((RocketComponent) mount)
							.getLength() + mount.getMotorOverhang() - length));

			for (int i = 0; i < position.length; i++) {
				gl.glPushMatrix();
				gl.glTranslated(position[i].x + motor.getLength(),
						position[i].y, position[i].z);
				FlameRenderer.drawExhaust(gl, p, motor);
				gl.glPopMatrix();
			}
		}

		gl.glDisable(GL.GL_BLEND);
		gl.glFrontFace(GL.GL_CCW);
	}

	@Override
	public void dispose(final GLAutoDrawable drawable) {
		log.trace("GL - dispose() called");
		if (rr != null)
			rr.dispose(drawable);
		textureCache.dispose(drawable);
	}

	@Override
	public void init(final GLAutoDrawable drawable) {
		log.trace("GL - init()");
		//drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));

		final GL2 gl = drawable.getGL().getGL2();

		gl.glClearDepth(1.0f); // clear z-buffer to the farthest
		gl.glDepthFunc(GL.GL_LESS); // the type of depth test to do

		textureCache.init(drawable);

		// gl.glDisable(GLLightingFunc.GL_LIGHT1);

		FlameRenderer.init(gl);

	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int x,
			final int y, final int w, final int h) {
		log.trace("GL - reshape()");
		ratio = (double) w / (double) h;
	}

	@SuppressWarnings("unused")
	private static class Bounds {
		double xMin, xMax, xSize;
		double yMin, yMax, ySize;
		double zMin, zMax, zSize;
		double rMax;
	}

	private Bounds cachedBounds = null;

	/**
	 * Calculates the bounds for the current configuration
	 * 
	 * @return
	 */
	private Bounds calculateBounds() {
		if (cachedBounds != null) {
			return cachedBounds;
		} else {
			final Bounds b = new Bounds();
			final Collection<Coordinate> bounds = configuration.getBounds();
			for (Coordinate c : bounds) {
				b.xMax = Math.max(b.xMax, c.x);
				b.xMin = Math.min(b.xMin, c.x);

				b.yMax = Math.max(b.yMax, c.y);
				b.yMin = Math.min(b.yMin, c.y);

				b.zMax = Math.max(b.zMax, c.z);
				b.zMin = Math.min(b.zMin, c.z);

				double r = MathUtil.hypot(c.y, c.z);
				b.rMax = Math.max(b.rMax, r);
			}
			b.xSize = b.xMax - b.xMin;
			b.ySize = b.yMax - b.yMin;
			b.zSize = b.zMax - b.zMin;
			cachedBounds = b;
			return b;
		}
	}

	private void setupModel(final GL2 gl) {
		// Get the bounds
		final Bounds b = calculateBounds();
		gl.glRotated(-p.getPitch() * (180.0 / Math.PI), 0, 0, 1);
		gl.glRotated(p.getYaw() * (180.0 / Math.PI), 0, 1, 0);
		gl.glRotated(p.getRoll() * (180.0 / Math.PI), 1, 0, 0);
		// Center the rocket in the view.
		gl.glTranslated(-b.xMin - b.xSize / 2.0, 0, 0);
	}

}
