package net.sf.openrocket.arch;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/*
 * Note:  These tests have not been tested on Windows, they might fail there
 * due to a different directory separator character.
 */
public class TestSystemInfo {
	
	private String osname;
	private String userhome;
	
	
	public void setup() {
		this.osname = System.getProperty("os.name");
		this.userhome = System.getProperty("user.home");
	}
	
	public void tearDown() {
		System.setProperty("os.name", this.osname);
		System.setProperty("user.home", this.userhome);
	}
	
	@Test
	public void testWindows() {
		setup();
		
		System.setProperty("os.name", "Windows Me");
		System.setProperty("user.home", "C:/Users/my user");
		assertEquals(SystemInfo.Platform.WINDOWS, SystemInfo.getPlatform());
		if (System.getenv("APPDATA") != null) {
			assertEquals(new File(System.getenv("APPDATA") + "/OpenRocket/"), SystemInfo.getUserApplicationDirectory());
		} else {
			assertEquals(new File("C:/Users/my user/OpenRocket/"), SystemInfo.getUserApplicationDirectory());
		}
		
		tearDown();
	}
	
	@Test
	public void testMacOS() {
		setup();
		
		System.setProperty("os.name", "Mac OS X");
		System.setProperty("user.home", "/Users/My User");
		assertEquals(SystemInfo.Platform.MAC_OS, SystemInfo.getPlatform());
		assertEquals(new File("/Users/My User/Library/Application Support/OpenRocket/"),
				SystemInfo.getUserApplicationDirectory());
		
		tearDown();
	}
	
	@Test
	public void testUnix() {
		setup();
		
		System.setProperty("user.home", "/home/myuser");
		for (String os : new String[] { "Linux", "Solaris", "Foobar" }) {
			System.setProperty("os.name", os);
			
			assertEquals(SystemInfo.Platform.UNIX, SystemInfo.getPlatform());
			assertEquals(new File("/home/myuser/.openrocket"), SystemInfo.getUserApplicationDirectory());
		}
		
		tearDown();
	}
	
}
