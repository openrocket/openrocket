package info.openrocket.swing.gui.help.tours;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChineseGuidedTourTest {

	@ParameterizedTest
	@CsvSource({ "introduction, OpenRocket \u7b80\u4ecb", "creating_design, \u521b\u5efa\u7b80\u5355\u706b\u7bad\u8bbe\u8ba1" })
	void loadsChineseTextAndExistingImagesForEverySlide(String name, String title) throws IOException {
		String directory = "datafiles/tours/" + name;
		SlideSet chinese = new SlideSetLoader(directory, Locale.SIMPLIFIED_CHINESE).load(name + ".tour");
		SlideSet english = new SlideSetLoader(directory, Locale.ENGLISH).load(name + ".tour");

		assertEquals(title, chinese.getTitle());
		assertEquals(english.getSlideCount(), chinese.getSlideCount());
		for (int i = 0; i < chinese.getSlideCount(); i++) {
			Slide slide = chinese.getSlide(i);
			assertTrue(slide.getText().codePoints().anyMatch(c -> Character.UnicodeScript.of(c)
					== Character.UnicodeScript.HAN), "Chinese text missing from slide " + i);
			assertTrue(slide.getImage().getWidth() > 0, "Image missing from slide " + i);
		}
	}
}
