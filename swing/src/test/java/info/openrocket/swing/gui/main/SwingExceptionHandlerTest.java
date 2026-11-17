package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SwingExceptionHandlerTest {
	private static IllegalArgumentException exception(StackTraceElement... trace) {
		IllegalArgumentException exception = new IllegalArgumentException("Width (0) and height (0) cannot be <= 0");
		exception.setStackTrace(trace);
		return exception;
	}

	private static StackTraceElement frame(String className, String methodName) {
		return new StackTraceElement(className, methodName, null, -1);
	}

	/** The trace reported in issue #3459 after a Linux display went to sleep. */
	@Test
	public void testZeroSizeRepaintBufferIsIgnored() {
		assertTrue(SwingExceptionHandler.isZeroSizeVolatileBufferBug(exception(
				frame("sun.awt.image.SunVolatileImage", "<init>"),
				frame("sun.awt.image.SunVolatileImage", "<init>"),
				frame("java.awt.GraphicsConfiguration", "createCompatibleVolatileImage"),
				frame("java.awt.GraphicsConfiguration", "createCompatibleVolatileImage"),
				frame("javax.swing.RepaintManager", "getVolatileOffscreenBuffer"),
				frame("javax.swing.RepaintManager$PaintManager", "paint"))));
	}

	/** The same message from OpenRocket code must still reach the bug report dialog. */
	@Test
	public void testOtherZeroSizeImageFailuresAreReported() {
		assertFalse(SwingExceptionHandler.isZeroSizeVolatileBufferBug(exception(
				frame("sun.awt.image.SunVolatileImage", "<init>"),
				frame("java.awt.GraphicsConfiguration", "createCompatibleVolatileImage"),
				frame("info.openrocket.swing.gui.figure3d.RocketFigure3d", "paintComponent"))));
		assertFalse(SwingExceptionHandler.isZeroSizeVolatileBufferBug(exception(
				frame("java.awt.image.BufferedImage", "<init>"),
				frame("javax.swing.RepaintManager", "getVolatileOffscreenBuffer"))));
		assertFalse(SwingExceptionHandler.isZeroSizeVolatileBufferBug(new NullPointerException()));
	}
}
