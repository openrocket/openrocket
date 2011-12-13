package net.sf.openrocket.util.BaseTestCase;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.startup.Application;

import org.junit.BeforeClass;

public class BaseTestCase {

	@BeforeClass
	public static void setUpApplication () {
		
		Application.setPreferences( new SwingPreferences() );
		
	}
}
