package net.sf.openrocket.gui.figure3d;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Redirects system err into OpenRocket's log.debug() if openrocket.debug is enabled.
 * This lets me capture JOGL debug as log events, which can be included in error reports etc.
 * I wish I could get the JOGL debug log on a separate printstream, so I do not have to take
 * over all of ERR.
 * 
 * @author bkuker
 *
 */
final class JoglDebugAdaptor {
	private static final LogHelper log = Application.getLogger();

	final static void plumbJoglDebug() {
		if (RocketFigure3d.is3dEnabled() && System.getProperty("openrocket.debug") != null) {
			System.setProperty("jogl.debug", "all");

			System.setErr(new PrintStream(new OutputStream() {
				StringBuilder sb = new StringBuilder();

				@Override
				public synchronized void write(int b) throws IOException {
					if (b == '\r' || b == '\n') {
						if (sb.toString().trim().length() > 0){
							String s = sb.toString();
							if ( Character.isWhitespace(s.charAt(0))){
								log.verbose(sb.toString());
							} else {
								log.debug(sb.toString());
							}
						}
						sb = new StringBuilder();
					} else {
						sb.append((char) b);
					}
				}
			}));
		}
	}
}
