package net.sf.openrocket.communication;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.junit.Test;

public class CommunicationTest {

	@Test
	public void testIllegalInputUpdateParsing() throws IOException {
		
		UpdateInfo info;
		
		info = Communication.parseUpdateInput(new StringReader(""));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("vj\u00e4avdsads"));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("\u0000\u0001\u0002"));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2"));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2pre"));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2.x"));
		assertNull(info);
		
		info = Communication.parseUpdateInput(new StringReader("\u0000\u0001\u0002"));
		assertNull(info);
		
		// Feed random bad input
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(10000);
		for (int i=0; i<100; i++) {
			int length = rnd.nextInt(10000);
			sb.delete(0, sb.length());
			for (int j=0; j<length; j++) {
				sb.append((char)rnd.nextInt());
			}
			info = Communication.parseUpdateInput(new StringReader(sb.toString()));
			assertNull(info);
		}
		
	}
	
	

	@Test
	public void testValidInputUpdateParsing() throws IOException {

		UpdateInfo info;
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2.3"));
		assertNotNull(info);
		assertEquals("1.2.3", info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2.3pre"));
		assertNotNull(info);
		assertEquals("1.2.3pre", info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2.3-build-3"));
		assertNotNull(info);
		assertEquals("1.2.3-build-3", info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
		
		info = Communication.parseUpdateInput(new StringReader("Version: 1.2.3x\n\n"));
		assertNotNull(info);
		assertEquals("1.2.3x", info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
		
		info = Communication.parseUpdateInput(new StringReader("Version:1.2.3\nfdsacd\u00e4fdsa"));
		assertNotNull(info);
		assertEquals("1.2.3", info.getLatestVersion());
		assertEquals(0, info.getUpdates().size());
		
		info = Communication.parseUpdateInput(new StringReader(
				"Version:   1.2.3  \n" +
				"15: Fifteen\n" +
				"3: Three1  \r\n" +
				"3: Three2\r" +
				"1:One"));
		assertNotNull(info);
		assertEquals("1.2.3", info.getLatestVersion());
		assertEquals(4, info.getUpdates().size());
		assertEquals(15, info.getUpdates().get(0).getU());
		assertEquals(3, info.getUpdates().get(1).getU());
		assertEquals(3, info.getUpdates().get(2).getU());
		assertEquals(1, info.getUpdates().get(3).getU());
		assertEquals("Fifteen", info.getUpdates().get(0).getV());
		assertEquals("Three1", info.getUpdates().get(1).getV());
		assertEquals("Three2", info.getUpdates().get(2).getV());
		assertEquals("One", info.getUpdates().get(3).getV());
		

		info = Communication.parseUpdateInput(new StringReader(
				"Version: 1.2.3\n" +
				"15:   (C) 1234 A&B %23 \\o/  \r\r\n" +
				"5: m\u00e4c\n" +
				"3: Invalid\u0000value\n" +
				"1: One\u0019two"));
		assertNotNull(info);
		assertEquals("1.2.3", info.getLatestVersion());
		assertEquals(1, info.getUpdates().size());
		assertEquals(15, info.getUpdates().get(0).getU());
		assertEquals("(C) 1234 A&B %23 \\o/", info.getUpdates().get(0).getV());
		
		
		
	}
}
