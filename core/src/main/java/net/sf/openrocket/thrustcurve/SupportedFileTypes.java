package net.sf.openrocket.thrustcurve;

public abstract class SupportedFileTypes {

	public final static String ROCKSIM_FORMAT = "RockSim";
	public final static String RASP_FORMAT = "RASP";
	
	public static boolean isSupportedFileType( String arg0 ) {
		return (ROCKSIM_FORMAT.equals(arg0) || RASP_FORMAT.equals(arg0));
	}
}
