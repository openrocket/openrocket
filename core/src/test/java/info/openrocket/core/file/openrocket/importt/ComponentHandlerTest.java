package info.openrocket.core.file.openrocket.importt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BaseTestCase;

/**
 * Tests recovery from invalid component hierarchies in OpenRocket files.
 */
public class ComponentHandlerTest extends BaseTestCase {

	/**
	 * A legacy development version allowed pod sets directly below stages. The
	 * invalid pod and its descendants should be ignored without losing valid
	 * siblings that follow it.
	 */
	@Test
	public void testIncompatibleComponentIsIgnoredWithWarning() throws RocketLoadException {
		String xml = """
				<?xml version="1.0" encoding="utf-8"?>
				<openrocket version="1.10" creator="OpenRocket 15.03dev">
				  <rocket>
				    <name>Invalid component hierarchy</name>
				    <subcomponents>
				      <stage>
				        <name>Sustainer</name>
				        <subcomponents>
				          <podset>
				            <name>Invalid pod set</name>
				            <subcomponents>
				              <bodytube>
				                <name>Ignored pod body</name>
				              </bodytube>
				            </subcomponents>
				          </podset>
				          <bodytube>
				            <name>Valid body tube</name>
				            <length>0.3</length>
				            <thickness>0.001</thickness>
				            <radius>0.02</radius>
				          </bodytube>
				        </subcomponents>
				      </stage>
				    </subcomponents>
				  </rocket>
				</openrocket>
				""";

		GeneralRocketLoader loader = new GeneralRocketLoader((File) null);
		ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		OpenRocketDocument document = loader.load(input, "invalid-component-hierarchy");

		Rocket rocket = document.getRocket();
		AxialStage stage = rocket.getStage(0);
		assertEquals(1, stage.getChildCount());
		BodyTube bodyTube = assertInstanceOf(BodyTube.class, stage.getChild(0));
		assertEquals("Valid body tube", bodyTube.getName());
		assertEquals(1, loader.getWarnings().size());
		String expectedWarning = new PodSet().getComponentName() + " cannot be attached to "
				+ stage.getComponentName() + "; ignoring this component and its subcomponents.";
		assertEquals(expectedWarning,
				loader.getWarnings().iterator().next().toString());
	}
}
