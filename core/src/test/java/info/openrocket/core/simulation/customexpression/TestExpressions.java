package info.openrocket.core.simulation.customexpression;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.Test;

public class TestExpressions extends BaseTestCase {

	@Test
	public void testExpressions() {
		// TODO Auto-generated constructor stub

		OpenRocketDocument doc = OpenRocketDocumentFactory.createNewRocket();

		// CustomExpression exp = new CustomExpression(doc, "Kinetic energy", "Ek", "J",
		// ".5*m*Vt^2");

		CustomExpression exp = new CustomExpression(doc, "Average mass", "Mavg", "kg", "mean(m[0:t])");
		// System.out.println(exp.getExpressionString());

	}
}
