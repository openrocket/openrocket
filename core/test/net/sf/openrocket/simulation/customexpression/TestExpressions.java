package net.sf.openrocket.simulation.customexpression;

import org.junit.Test;

import static org.junit.Assert.*;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.Rocket;

public class TestExpressions {

	@Test
	public void testExpressions() {
		// TODO Auto-generated constructor stub
		
		OpenRocketDocument doc = new OpenRocketDocument(new Rocket());
		
		//CustomExpression exp = new CustomExpression(doc, "Kinetic energy", "Ek", "J", ".5*m*Vt^2");
		
		CustomExpression exp = new CustomExpression(doc, "Average mass", "Mavg", "kg", "mean(m[0:t])");
		System.out.println( exp.getExpressionString() );
		
	}
}
