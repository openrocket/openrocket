package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class GLShaderTest {

	@Test
	void returnsRequiredUniformLocation() {
		GLShader shader = mock(GLShader.class, CALLS_REAL_METHODS);
		doReturn(7).when(shader).getUniformLocation("projection");

		assertEquals(7, shader.requireUniformLocation("projection"));
	}

	@Test
	void rejectsMissingRequiredUniform() {
		GLShader shader = mock(GLShader.class, CALLS_REAL_METHODS);
		doReturn(-1).when(shader).getUniformLocation("projection");

		ShaderException exception = assertThrows(ShaderException.class,
				() -> shader.requireUniformLocation("projection"));

		assertTrue(exception.getMessage().contains("projection"));
	}
}
