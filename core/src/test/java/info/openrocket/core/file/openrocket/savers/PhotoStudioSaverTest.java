package info.openrocket.core.file.openrocket.savers;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PhotoStudioSaverTest {

	@Test
	void legacySettingsDoNotSerializeMissingFieldsAsNull() {
		Map<String, String> legacySettings = new HashMap<>();
		legacySettings.put("roll", "1.25");
		legacySettings.put("motionBlurred", "false");

		List<String> elements = PhotoStudioSaver.getElements(legacySettings);

		assertEquals(List.of("<roll>1.25</roll>", "<motionBlurred>false</motionBlurred>"), elements);
		assertFalse(elements.stream().anyMatch(element -> element.contains(">null<")));
	}
}
