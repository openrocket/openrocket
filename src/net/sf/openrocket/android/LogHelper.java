package net.sf.openrocket.android;

import android.util.Log;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLine;


public class LogHelper extends net.sf.openrocket.logging.LogHelper {

	/* (non-Javadoc)
	 * @see net.sf.openrocket.logging.LogHelper#log(net.sf.openrocket.logging.LogLine)
	 */
	@Override
	public void log(LogLine line) {
		
		LogLevel level = line.getLevel();
		
		switch ( level ) {
		case ERROR:
			Log.e("OpenRocket", line.toString());
			break;
		case WARN:
			Log.w("OpenRocket", line.toString());
			break;
		case INFO:
			Log.i("OpenRocket", line.toString());
			break;
		case DEBUG:
			Log.d("OpenRocket", line.toString());
			break;
		default:
			Log.v("OpenRocket", line.toString());
		}
	}

	
}
