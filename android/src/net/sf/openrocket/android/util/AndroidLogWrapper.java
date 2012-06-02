package net.sf.openrocket.android.util;

import java.text.MessageFormat;

import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLine;

import android.util.Log;

public class AndroidLogWrapper {

	private static boolean logEnabled = false;
	
	public static void setLogEnabled( boolean value ) {
		logEnabled = value;
	}
	
	public static void d( Class clzz, String msg ) {
		
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			Log.d(tag,msg);
		}
	}

	public static void d( Class clzz, String msg, Object ... args ) {
		
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			String formatted = MessageFormat.format(msg, args);
			Log.d(tag,formatted);
		}
	}

	public static void e( Class clzz, String msg ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			Log.e(tag,msg);
		}
	}
	
	public static void e( Class clzz, String msg, Object ... args ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			String formatted = MessageFormat.format(msg, args);
			Log.e(tag,formatted);
		}
	}

	public static void i( Class clzz, String msg ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			Log.i(tag,msg);
		}
	}
	
	public static void i( Class clzz, String msg, Object ... args ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			String formatted = MessageFormat.format(msg, args);
			Log.i(tag,formatted);
		}
	}

	public static void v( Class clzz, String msg ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			Log.v(tag,msg);
		}
	}

	public static void v( Class clzz, String msg, Object ... args ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			String formatted = MessageFormat.format(msg, args);
			Log.v(tag,formatted);
		}
	}

	public static void w( Class clzz, String msg ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			Log.w(tag,msg);
		}
	}

	public static void w( Class clzz, String msg, Object ... args ) {
		if ( logEnabled ) {
			String tag = getTagForClass(clzz);
			String formatted = MessageFormat.format(msg, args);
			Log.w(tag,formatted);
		}
	}
	
	private static String getTagForClass( Class clzz ) {
		String s = clzz.getSimpleName();
		return s;
	}

	public static class LogHelper extends net.sf.openrocket.logging.LogHelper {

		/* (non-Javadoc)
		 * @see net.sf.openrocket.logging.LogHelper#log(net.sf.openrocket.logging.LogLine)
		 */
		@Override
		public void log(LogLine line) {
			
			if ( !logEnabled ) {
				return;
			}
			
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

}
