package net.sf.openrocket.simulation.customexpression;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class TestExpressions extends BaseTestCase {
	
	@Test
	public void testExpressions() {
		// TODO Auto-generated constructor stub
		
		OpenRocketDocument doc = OpenRocketDocumentFactory.createNewRocket();
		
		//CustomExpression exp = new CustomExpression(doc, "Kinetic energy", "Ek", "J", ".5*m*Vt^2");
		
		CustomExpression exp = new CustomExpression(doc, "Average mass", "Mavg", "kg", "mean(m[0:t])");
		//System.out.println(exp.getExpressionString());
		
	}
}
