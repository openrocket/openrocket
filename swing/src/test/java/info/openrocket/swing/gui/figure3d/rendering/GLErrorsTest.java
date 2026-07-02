package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

/**
 * Tests for the GL error code mapping and exception formatting.
 * These are pure functions and do not require an OpenGL context.
 */
public class GLErrorsTest {

	@Test
	public void testErrorStringMapping() {
		assertEquals("GL_INVALID_ENUM", GLErrors.errorString(GL_INVALID_ENUM));
		assertEquals("GL_INVALID_VALUE", GLErrors.errorString(GL_INVALID_VALUE));
		assertEquals("GL_INVALID_OPERATION", GLErrors.errorString(GL_INVALID_OPERATION));
		assertEquals("GL_STACK_OVERFLOW", GLErrors.errorString(GL_STACK_OVERFLOW));
		assertEquals("GL_STACK_UNDERFLOW", GLErrors.errorString(GL_STACK_UNDERFLOW));
		assertEquals("GL_OUT_OF_MEMORY", GLErrors.errorString(GL_OUT_OF_MEMORY));
		assertEquals("GL_INVALID_FRAMEBUFFER_OPERATION", GLErrors.errorString(GL_INVALID_FRAMEBUFFER_OPERATION));
	}

	@Test
	public void testUnknownErrorCode() {
		assertEquals("UNKNOWN_GL_ERROR(0xBEEF)", GLErrors.errorString(0xBEEF));
	}

	@Test
	public void testExceptionMessageContainsOperationAndErrors() {
		GLException exception = new GLException("mesh buffer upload",
				List.of(GL_INVALID_OPERATION, GL_INVALID_VALUE));

		assertTrue(exception.getMessage().contains("mesh buffer upload"));
		assertTrue(exception.getMessage().contains("GL_INVALID_OPERATION"));
		assertTrue(exception.getMessage().contains("GL_INVALID_VALUE"));
		assertEquals(List.of(GL_INVALID_OPERATION, GL_INVALID_VALUE), exception.getErrorCodes());
	}

	@Test
	public void testDebugDisabledByDefault() {
		System.clearProperty(GLErrors.DEBUG_PROPERTY);
		assertTrue(!GLErrors.isDebugEnabled());
	}
}
