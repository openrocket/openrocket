package info.openrocket.swing.gui.figure3d.ui;

/*
 * Adapted from org.lwjgl.opengl.awt.PlatformMacOSXGLCanvas in lwjgl3-awt 0.2.4.
 *
 * MIT License
 * Copyright (c) 2015 Kai Burjack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.CGL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.awt.PlatformGLCanvas;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTFunctions;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;
import org.lwjgl.system.macosx.ObjCRuntime;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * macOS AWT OpenGL bridge for image-presentation mode.
 *
 * <p>This is a small copy of lwjgl3-awt's macOS {@link PlatformGLCanvas}
 * implementation with the explicit CoreAnimation flushes removed. On recent
 * macOS builds those flushes can synchronously display the NSOpenGL backing
 * layer from a Java render thread, which aborts in
 * {@code -[NSOpenGLContext setView:]} because it is not running on AppKit's
 * main thread. OpenRocket never presents through the native layer in this mode;
 * frames are read back and painted by AWT, so the layer is created hidden and is
 * kept hidden without forcing a transaction flush.</p>
 */
final class MacOSXImageSafeGLCanvas implements PlatformGLCanvas {
	private static final int JAWT_VERSION_9 = 0x00010007;

	private static final int NS_OPENGL_PFA_STEREO = 6;
	private static final int NS_OPENGL_PFA_COLOR_SIZE = 8;
	private static final int NS_OPENGL_PFA_ALPHA_SIZE = 11;
	private static final int NS_OPENGL_PFA_DEPTH_SIZE = 12;
	private static final int NS_OPENGL_PFA_STENCIL_SIZE = 13;
	private static final int NS_OPENGL_PFA_ACCUM_SIZE = 14;
	private static final int NS_OPENGL_PFA_SAMPLE_BUFFERS = 55;
	private static final int NS_OPENGL_PFA_SAMPLES = 56;
	private static final int NS_OPENGL_PFA_COLOR_FLOAT = 58;
	private static final int NS_OPENGL_PFA_ALLOW_OFFLINE_RENDERERS = 73;
	private static final int NS_OPENGL_PFA_ACCELERATED_COMPUTE = 74;
	private static final int NS_OPENGL_PFA_OPENGL_PROFILE = 99;
	private static final int NS_OPENGL_PROFILE_VERSION_LEGACY = 0x1000;
	private static final int NS_OPENGL_PROFILE_VERSION_3_2_CORE = 0x3200;
	private static final int NS_OPENGL_PROFILE_VERSION_4_1_CORE = 0x4100;
	private static final int NS_OPENGL_CPS_SURFACE_BACKING_SIZE = 304;
	private static final int NS_OPENGL_CPS_SURFACE_OPACITY = 305;
	private static final long NS_VIEW_WIDTH_SIZABLE = 2L;
	private static final long NS_VIEW_HEIGHT_SIZABLE = 16L;
	private static final long NS_VIEW_SIZE_SIZABLE = NS_VIEW_WIDTH_SIZABLE | NS_VIEW_HEIGHT_SIZABLE;

	private static final JAWT AWT;
	private static final long OBJC_MSG_SEND;
	private static final long NS_OPENGL_PIXEL_FORMAT;

	static {
		AWT = JAWT.calloc();
		AWT.version(JAWT_VERSION_9);
		if (!JAWTFunctions.JAWT_GetAWT(AWT)) {
			throw new AssertionError("GetAWT failed");
		}
		OBJC_MSG_SEND = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
		NS_OPENGL_PIXEL_FORMAT = ObjCRuntime.objc_getClass("NSOpenGLPixelFormat");
	}

	private JAWTDrawingSurface drawingSurface;
	private Canvas canvas;
	private long view;
	private int width;
	private int height;

	@Override
	public long create(Canvas canvas, GLData data, GLData effective) throws AWTException {
		this.canvas = canvas;
		this.drawingSurface = JAWTFunctions.JAWT_GetDrawingSurface(canvas, AWT.GetDrawingSurface());
		canvas.addHierarchyListener(this::handleHierarchyChanged);

		JAWTDrawingSurface createSurface = JAWTFunctions.JAWT_GetDrawingSurface(canvas, AWT.GetDrawingSurface());
		boolean locked = false;
		JAWTDrawingSurfaceInfo info = null;
		try {
			int lockResult = JAWTFunctions.JAWT_DrawingSurface_Lock(createSurface, createSurface.Lock());
			if ((lockResult & JAWTFunctions.JAWT_LOCK_ERROR) != 0) {
				throw new AWTException("JAWT_DrawingSurface_Lock() failed");
			}
			locked = true;

			info = JAWTFunctions.JAWT_DrawingSurface_GetDrawingSurfaceInfo(createSurface, createSurface.GetDrawingSurfaceInfo());
			JRootPane rootPane = SwingUtilities.getRootPane(canvas);
			if (rootPane != null) {
				Point p = SwingUtilities.convertPoint(canvas, new Point(), rootPane);
				info.bounds().x(p.x);
				info.bounds().y(p.y);
			}

			width = info.bounds().width();
			height = info.bounds().height();

			long pixelFormat = createPixelFormat(data);
			view = createNSOpenGLView(info.platformInfo(), pixelFormat,
					info.bounds().x(), info.bounds().y(), width, height);
			hideOpenGLLayer();

			long openGLContext = JNI.invokePPP(view, ObjCRuntime.sel_getUid("openGLContext"), OBJC_MSG_SEND);
			return JNI.invokePPP(openGLContext, ObjCRuntime.sel_getUid("CGLContextObj"), OBJC_MSG_SEND);
		} finally {
			if (info != null) {
				JAWTFunctions.JAWT_DrawingSurface_FreeDrawingSurfaceInfo(info, createSurface.FreeDrawingSurfaceInfo());
			}
			if (locked) {
				JAWTFunctions.JAWT_DrawingSurface_Unlock(createSurface, createSurface.Unlock());
			}
			JAWTFunctions.JAWT_FreeDrawingSurface(createSurface, AWT.FreeDrawingSurface());
		}
	}

	private long createPixelFormat(GLData data) {
		ByteBuffer attributes = ByteBuffer.allocateDirect(400).order(ByteOrder.nativeOrder());
		attributes.putInt(NS_OPENGL_PFA_ALLOW_OFFLINE_RENDERERS);
		attributes.putInt(NS_OPENGL_PFA_ACCELERATED_COMPUTE);
		if (data.stereo) {
			attributes.putInt(NS_OPENGL_PFA_STEREO);
		}
		if (data.pixelFormatFloat) {
			attributes.putInt(NS_OPENGL_PFA_COLOR_FLOAT);
		}

		attributes.putInt(NS_OPENGL_PFA_ACCUM_SIZE);
		attributes.putInt(data.accumRedSize + data.accumGreenSize + data.accumBlueSize + data.accumAlphaSize);

		int colorSize = data.redSize + data.greenSize + data.blueSize;
		if (colorSize == 0) {
			colorSize = 24;
		} else if (colorSize < 15) {
			colorSize = 15;
		}
		attributes.putInt(NS_OPENGL_PFA_COLOR_SIZE);
		attributes.putInt(colorSize);

		attributes.putInt(NS_OPENGL_PFA_ALPHA_SIZE);
		attributes.putInt(data.alphaSize);
		attributes.putInt(NS_OPENGL_PFA_DEPTH_SIZE);
		attributes.putInt(data.depthSize);
		attributes.putInt(NS_OPENGL_PFA_STENCIL_SIZE);
		attributes.putInt(data.stencilSize);

		attributes.putInt(NS_OPENGL_PFA_SAMPLE_BUFFERS);
		attributes.putInt(data.samples == 0 ? 0 : 1);
		if (data.samples != 0) {
			attributes.putInt(NS_OPENGL_PFA_SAMPLES);
			attributes.putInt(data.samples);
		}

		attributes.putInt(NS_OPENGL_PFA_OPENGL_PROFILE);
		if (data.profile == GLData.Profile.COMPATIBILITY) {
			attributes.putInt(NS_OPENGL_PROFILE_VERSION_LEGACY);
		} else if (data.profile == GLData.Profile.CORE || data.majorVersion == 3) {
			attributes.putInt(NS_OPENGL_PROFILE_VERSION_3_2_CORE);
		} else if (data.majorVersion >= 4) {
			attributes.putInt(NS_OPENGL_PROFILE_VERSION_4_1_CORE);
		} else {
			attributes.putInt(NS_OPENGL_PROFILE_VERSION_LEGACY);
		}

		attributes.putInt(0);
		attributes.rewind();

		long pixelFormat = JNI.invokePPP(NS_OPENGL_PIXEL_FORMAT, ObjCRuntime.sel_getUid("alloc"), OBJC_MSG_SEND);
		return JNI.invokePPPP(pixelFormat, ObjCRuntime.sel_getUid("initWithAttributes:"),
				MemoryUtil.memAddress(attributes), OBJC_MSG_SEND);
	}

	private long createNSOpenGLView(long platformInfo, long pixelFormat, int x, int y, int width, int height) {
		long nsOpenGLViewClass = ObjCRuntime.objc_getClass("NSOpenGLView");
		long nsOpenGLView = JNI.invokePPP(nsOpenGLViewClass, ObjCRuntime.sel_getUid("alloc"), OBJC_MSG_SEND);
		long initializedView = nsOpenGLViewInitWithFrame(nsOpenGLView, new double[]{0.0, 0.0, width, height}, pixelFormat);

		JNI.invokePPV(nsOpenGLView, ObjCRuntime.sel_getUid("setWantsLayer:"), true, OBJC_MSG_SEND);
		long openGLLayer = JNI.invokePPJ(nsOpenGLView, ObjCRuntime.sel_getUid("layer"), OBJC_MSG_SEND);
		JNI.invokePPPV(openGLLayer, ObjCRuntime.sel_getUid("setHidden:"), 1L, OBJC_MSG_SEND);
		JNI.callPPPV(openGLLayer, ObjCRuntime.sel_getUid("setAutoresizingMask:"), NS_VIEW_SIZE_SIZABLE, OBJC_MSG_SEND);

		long caLayerClass = ObjCRuntime.objc_getClass("CALayer");
		long hostLayer = JNI.invokePPP(caLayerClass, ObjCRuntime.sel_getUid("layer"), OBJC_MSG_SEND);
		setLayerFrame(hostLayer, new double[]{x, y, width, height});
		JNI.callPPPV(hostLayer, ObjCRuntime.sel_getUid("addSublayer:"), openGLLayer, OBJC_MSG_SEND);

		JNI.callPPPPV(platformInfo,
				ObjCRuntime.sel_getUid("performSelectorOnMainThread:withObject:waitUntilDone:"),
				ObjCRuntime.sel_getUid("setLayer:"), hostLayer, 1, OBJC_MSG_SEND);
		return initializedView;
	}

	private static long nsOpenGLViewInitWithFrame(long openGLView, double[] frame, long pixelFormat) {
		FFICIF cif = FFICIF.malloc();
		try {
			PointerBuffer argumentTypes = BufferUtils.createPointerBuffer(7);
			argumentTypes.put(0, LibFFI.ffi_type_pointer);
			argumentTypes.put(1, LibFFI.ffi_type_pointer);
			argumentTypes.put(2, LibFFI.ffi_type_double);
			argumentTypes.put(3, LibFFI.ffi_type_double);
			argumentTypes.put(4, LibFFI.ffi_type_double);
			argumentTypes.put(5, LibFFI.ffi_type_double);
			argumentTypes.put(6, LibFFI.ffi_type_pointer);
			int result = LibFFI.ffi_prep_cif(cif, LibFFI.FFI_DEFAULT_ABI, LibFFI.ffi_type_pointer, argumentTypes);
			if (result != LibFFI.FFI_OK) {
				throw new IllegalStateException("ffi_prep_cif failed: " + result);
			}

			PointerBuffer arguments = BufferUtils.createPointerBuffer(7);
			ByteBuffer values = BufferUtils.createByteBuffer(Pointer.POINTER_SIZE * 3 + 4 * Double.BYTES);
			arguments.put(MemoryUtil.memAddress(values));
			PointerBuffer.put(values, openGLView);
			arguments.put(MemoryUtil.memAddress(values));
			PointerBuffer.put(values, ObjCRuntime.sel_getUid("initWithFrame:pixelFormat:"));
			for (double value : frame) {
				arguments.put(MemoryUtil.memAddress(values));
				values.putDouble(value);
			}
			arguments.put(MemoryUtil.memAddress(values));
			values.putLong(pixelFormat);
			arguments.flip();
			values.flip();

			ByteBuffer returnValue = BufferUtils.createByteBuffer(Long.BYTES);
			LibFFI.ffi_call(cif, OBJC_MSG_SEND, returnValue, arguments);
			long initializedView = returnValue.asLongBuffer().get(0);
			if (initializedView == 0) {
				throw new IllegalStateException("[NSOpenGLView initWithFrame:pixelFormat:] returned null.");
			}
			return initializedView;
		} finally {
			cif.free();
		}
	}

	private static void setLayerFrame(long layer, double[] frame) {
		FFICIF cif = FFICIF.malloc();
		try {
			PointerBuffer argumentTypes = BufferUtils.createPointerBuffer(6);
			argumentTypes.put(0, LibFFI.ffi_type_pointer);
			argumentTypes.put(1, LibFFI.ffi_type_pointer);
			argumentTypes.put(2, LibFFI.ffi_type_double);
			argumentTypes.put(3, LibFFI.ffi_type_double);
			argumentTypes.put(4, LibFFI.ffi_type_double);
			argumentTypes.put(5, LibFFI.ffi_type_double);
			int result = LibFFI.ffi_prep_cif(cif, LibFFI.FFI_DEFAULT_ABI, LibFFI.ffi_type_pointer, argumentTypes);
			if (result != LibFFI.FFI_OK) {
				throw new IllegalStateException("ffi_prep_cif failed: " + result);
			}

			PointerBuffer arguments = BufferUtils.createPointerBuffer(6);
			ByteBuffer values = BufferUtils.createByteBuffer(Pointer.POINTER_SIZE * 2 + 4 * Double.BYTES);
			arguments.put(MemoryUtil.memAddress(values));
			PointerBuffer.put(values, layer);
			arguments.put(MemoryUtil.memAddress(values));
			PointerBuffer.put(values, ObjCRuntime.sel_getUid("setFrame:"));
			for (double value : frame) {
				arguments.put(MemoryUtil.memAddress(values));
				values.putDouble(value);
			}
			arguments.flip();
			values.flip();

			ByteBuffer returnValue = BufferUtils.createByteBuffer(Long.BYTES);
			LibFFI.ffi_call(cif, OBJC_MSG_SEND, returnValue, arguments);
		} finally {
			cif.free();
		}
	}

	@Override
	public boolean deleteContext(long context) {
		if (view == 0) {
			return false;
		}
		JNI.invokePPP(view, ObjCRuntime.sel_getUid("removeFromSuperviewWithoutNeedingDisplay"), OBJC_MSG_SEND);
		JNI.invokePPP(view, ObjCRuntime.sel_getUid("clearGLContext"), OBJC_MSG_SEND);
		JNI.invokePPP(view, ObjCRuntime.sel_getUid("release"), OBJC_MSG_SEND);
		view = 0;
		return false;
	}

	@Override
	public boolean makeCurrent(long context) {
		CGL.CGLSetCurrentContext(context);
		if (context == 0) {
			return true;
		}

		JAWTDrawingSurfaceInfo info = JAWTFunctions.JAWT_DrawingSurface_GetDrawingSurfaceInfo(
				drawingSurface, drawingSurface.GetDrawingSurfaceInfo());
		try {
			int newWidth = info.bounds().width();
			int newHeight = info.bounds().height();
			if (newWidth != width || newHeight != height) {
				AffineTransform transform = getCanvasTransform();
				int surfaceWidth = (int) (newWidth * transform.getScaleX());
				int surfaceHeight = (int) (newHeight * transform.getScaleY());
				CGL.CGLSetParameter(context, NS_OPENGL_CPS_SURFACE_BACKING_SIZE,
						new int[]{surfaceWidth, surfaceHeight});
				CGL.CGLEnable(context, NS_OPENGL_CPS_SURFACE_OPACITY);
				width = newWidth;
				height = newHeight;
			}
		} finally {
			JAWTFunctions.JAWT_DrawingSurface_FreeDrawingSurfaceInfo(info, drawingSurface.FreeDrawingSurfaceInfo());
		}
		return true;
	}

	private AffineTransform getCanvasTransform() {
		GraphicsConfiguration configuration = canvas.getGraphicsConfiguration();
		return configuration != null ? configuration.getDefaultTransform() : new AffineTransform();
	}

	@Override
	public boolean isCurrent(long context) {
		return CGL.CGLGetCurrentContext() == context;
	}

	@Override
	public boolean swapBuffers() {
		GL11.glFlush();
		return true;
	}

	@Override
	public boolean delayBeforeSwapNV(float seconds) {
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public void lock() throws AWTException {
		int lockResult = JAWTFunctions.JAWT_DrawingSurface_Lock(drawingSurface, drawingSurface.Lock());
		if ((lockResult & JAWTFunctions.JAWT_LOCK_ERROR) != 0) {
			throw new AWTException("JAWT_DrawingSurface_Lock() failed");
		}
	}

	@Override
	public void unlock() {
		JAWTFunctions.JAWT_DrawingSurface_Unlock(drawingSurface, drawingSurface.Unlock());
	}

	@Override
	public void dispose() {
		if (drawingSurface != null) {
			JAWTFunctions.JAWT_FreeDrawingSurface(drawingSurface, AWT.FreeDrawingSurface());
			drawingSurface = null;
		}
	}

	private void handleHierarchyChanged(HierarchyEvent event) {
		if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
			hideOpenGLLayer();
		}
	}

	private void hideOpenGLLayer() {
		if (view == 0) {
			return;
		}
		long layer = JNI.invokePPP(view, ObjCRuntime.sel_getUid("layer"), OBJC_MSG_SEND);
		if (layer != 0) {
			JNI.invokePPPV(layer, ObjCRuntime.sel_getUid("setHidden:"), 1L, OBJC_MSG_SEND);
		}
	}
}
