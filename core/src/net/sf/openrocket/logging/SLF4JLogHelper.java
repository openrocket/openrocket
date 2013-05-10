package net.sf.openrocket.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SLF4JLogHelper extends DelegatorLogger {

	private static final Logger log = LoggerFactory.getLogger(SLF4JLogHelper.class);
	static final Marker USER_MARKER = MarkerFactory.getMarker("User");
	
	@Override
	public void log(LogLine line) {
		super.log(line);
		switch (line.getLevel()) {
		case VBOSE:
			log.trace(line.getMessage(), line.getCause());
			break;
		case DEBUG:
			log.debug(line.getMessage(), line.getCause());
			break;
		case INFO:
			log.info(line.getMessage(), line.getCause());
			break;
		case USER:
			log.info(USER_MARKER, line.getMessage(), line.getCause());
			break;
		case WARN:
			log.warn(line.getMessage(), line.getCause());
			break;
		case ERROR:
			log.error(line.getMessage(), line.getCause());
			break;
		}
	}

}
