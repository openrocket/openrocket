package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderContractTest {

	@Test
	void mainShaderConstantsMatchJavaContracts() throws IOException {
		String shader = readMainFragmentShader();

		assertDefines(shader, "MAX_LIGHTS", RenderingConstants.MAX_LIGHTS);
		assertDefines(shader, "LIGHT_DIRECTIONAL", Light.LightType.DIRECTIONAL.getShaderValue());
		assertDefines(shader, "LIGHT_POINT", Light.LightType.POINT.getShaderValue());
		for (TransparencyOutputMode mode : TransparencyOutputMode.values()) {
			assertDefines(shader, "OUTPUT_" + mode.name(), mode.getShaderValue());
		}
	}

	private static String readMainFragmentShader() throws IOException {
		try (InputStream stream = ShaderContractTest.class.getResourceAsStream("/shaders/fragment.glsl")) {
			assertNotNull(stream, "Main fragment shader resource should exist");
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static void assertDefines(String shader, String name, int value) {
		boolean defined = shader.contains("#define " + name + " " + value)
				|| shader.contains("const int " + name + " = " + value + ";");
		assertTrue(defined, () -> name + " should map to " + value + " in fragment.glsl");
	}
}
