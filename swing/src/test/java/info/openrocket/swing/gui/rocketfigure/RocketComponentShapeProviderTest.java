package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RocketComponentShapeProviderTest extends BaseTestCase {

	@Test
	void resolvesBuiltInProviderIndependentlyOfThreadContextClassLoader() {
		Thread thread = Thread.currentThread();
		ClassLoader originalLoader = thread.getContextClassLoader();
		try {
			thread.setContextClassLoader(new ClassLoader(null) {
			});
			TrapezoidFinSet finSet = new TrapezoidFinSet();

			assertDoesNotThrow(() -> RocketComponentShapeProvider.getShapesSide(finSet, Transformation.IDENTITY));
		} finally {
			thread.setContextClassLoader(originalLoader);
		}
	}
}
