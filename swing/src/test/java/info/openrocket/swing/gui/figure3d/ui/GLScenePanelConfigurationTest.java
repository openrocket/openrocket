package info.openrocket.swing.gui.figure3d.ui;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.awt.GLData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GLScenePanelConfigurationTest {

	@Test
	void requestsTheHighestDesktopContextAtOrAboveOpenGL31() {
		GLData data = GLScenePanel.createGLData();

		assertEquals(GLData.API.GL, data.api);
		assertEquals(3, data.majorVersion);
		assertEquals(1, data.minorVersion);
		assertEquals(GLData.VersionPolicy.AT_LEAST, data.versionPolicy);
		assertNull(data.profile, "OpenGL 3.1 predates profile selection");
	}
}
