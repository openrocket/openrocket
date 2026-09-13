package info.openrocket.swing.gui.adaptors;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.swing.util.BaseTestCase;

class MaterialModelTest extends BaseTestCase {

	@Test
	void negativeIndexReturnsNoMaterial() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		BodyTube bodyTube = new BodyTube();
		document.getRocket().getStage(0).addChild(bodyTube);
		MaterialModel model = new MaterialModel(null, document, bodyTube, Material.Type.BULK);

		assertNull(model.getElementAt(-1));
	}
}
