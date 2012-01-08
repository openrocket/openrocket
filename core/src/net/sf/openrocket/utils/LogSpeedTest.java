package net.sf.openrocket.utils;

import net.sf.openrocket.logging.DelegatorLogger;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;

public class LogSpeedTest {
	private static LogHelper log;
	
	private static final int COUNT = 1000000;
	
	public static void main(String[] args) {
		
		System.setProperty("openrocket.log.tracelevel", "user");
		log = new DelegatorLogger();
		
		for (LogLevel l : LogLevel.values()) {
			for (int i = 0; i < 3; i++) {
				long t0 = System.currentTimeMillis();
				test(l);
				long t1 = System.currentTimeMillis();
				System.out.println("Level " + l + ": " + (t1 - t0) + " ms for " + COUNT + " lines");
			}
		}
		
	}
	
	
	private static void test(LogLevel level) {
		for (int i = 0; i < COUNT; i++) {
			log.log(level, "Message " + i);
		}
	}
	
}
