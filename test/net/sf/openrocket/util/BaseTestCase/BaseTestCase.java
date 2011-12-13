package net.sf.openrocket.util.BaseTestCase;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Prefs;

import org.junit.BeforeClass;

public class BaseTestCase {

	@BeforeClass
	public static void setUpApplication () {
		
		Application.setPreferences( new Prefs() );
		
	}
}
