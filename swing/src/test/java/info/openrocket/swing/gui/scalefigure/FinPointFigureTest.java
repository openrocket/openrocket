package info.openrocket.swing.gui.scalefigure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.util.BaseTestCase;

class FinPointFigureTest extends BaseTestCase {

	/**
	 * A fin editor window stays in Window.getWindows() after it is closed, so a later look-and-feel
	 * update (switching theme, or the temporary light theme used for printing) still revalidates its
	 * FinPointFigure. If the fin set was removed from the rocket in the meantime, it has no parent.
	 */
	@Test
	void updatingFigureOfDetachedFinSetDoesNotThrow() {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);
		BodyTube body = new BodyTube();
		stage.addChild(body);
		FreeformFinSet fins = new FreeformFinSet();
		body.addChild(fins);

		FinPointFigure figure = new FinPointFigure(fins);
		ScaleScrollPane pane = new ScaleScrollPane(figure);

		body.removeChild(fins);
		assertNull(fins.getParent());

		assertDoesNotThrow(figure::updateFigure);
		assertDoesNotThrow(() -> SwingUtilities.updateComponentTreeUI(pane));
	}
}
