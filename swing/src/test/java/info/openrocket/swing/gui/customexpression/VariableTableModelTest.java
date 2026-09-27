package info.openrocket.swing.gui.customexpression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.swing.util.BaseTestCase;

class VariableTableModelTest extends BaseTestCase {

	@Test
	void rowEqualToSizeReturnsNoSymbol() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		VariableTableModel model = new VariableTableModel(document);

		assertEquals("", model.getSymbolAt(model.getRowCount()));
	}
}
