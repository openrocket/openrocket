package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

public class OpenRocketDocumentChangeEventTest extends BaseTestCase {

	@Test
	public void rocketChangeFiresOneDocumentChangeWithTheDocumentUnsaved() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);
		document.setSaved(true);
		List<Boolean> savedAtEvent = new ArrayList<>();
		document.addDocumentChangeListener(event -> savedAtEvent.add(document.isSaved()));

		BodyTube bodyTube = null;
		for (RocketComponent component : rocket) {
			if (component instanceof BodyTube) {
				bodyTube = (BodyTube) component;
				break;
			}
		}
		bodyTube.setLength(bodyTube.getLength() * 1.1);

		assertEquals(List.of(false), savedAtEvent);
		assertFalse(document.isSaved());
	}
}
