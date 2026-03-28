package info.openrocket.core.startup.providers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import info.openrocket.core.startup.jij.ClasspathProvider;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class JarInJarStarterTest {

	public static class SqlSmokeMain {
		public static void main(String[] args) throws Exception {
			Class.forName("java.sql.SQLException");
		}
	}

	@Test
	public void testRunMainCanAccessJavaSqlPlatformModules() {
		Assumptions.assumeTrue(java.sql.SQLException.class.getClassLoader() != null);

		assertThrows(ClassNotFoundException.class,
				() -> new URLClassLoader(new URL[0], null).loadClass("java.sql.SQLException"));

		ClasspathProvider testClasses = () -> List.of(JarInJarStarterTest.class.getProtectionDomain()
				.getCodeSource()
				.getLocation());

		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			assertDoesNotThrow(() -> JarInJarStarter.runMain(SqlSmokeMain.class.getName(), new String[0], testClasses));
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}
}

