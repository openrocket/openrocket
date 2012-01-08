package altimeter;

public class AltData {

	private int mslLevel = 0;
	private int samples = 0;
	private int[] data = null;
	private byte[] version = null;
	
	
	public void setMslLevel(int msl) {
		mslLevel = msl;
	}
	public int getMslLevel() {
		return mslLevel;
	}
	
	public void setDataSamples(int s) {
		samples = s;
	}
	public int getDataSamples() {
		return samples;
	}
	
	public void setVersion(byte[] v) {
		if (v==null)
			version = null;
		else 
			version = v.clone();
	}
	public byte[] getVersion() {
		if (version == null)
			return null;
		return version.clone();		
	}
	
	public void setData(int[] data) {
		if (data==null)
			this.data = null;
		else 
			this.data = data.clone();
	}
	public int[] getData() {
		if (data == null)
			return null;
		return data.clone();
	}

	public int getApogee() {
		if (data == null || data.length==0)
			return 0;
		int max = Integer.MIN_VALUE;
		for (int i=0; i<data.length; i++) {
			if (data[i] > max)
				max = data[i];
		}
		return max;
	}
	
	@Override
	public String toString() {
		String s = "AltData(";
		s += "MSL:"+getMslLevel()+",";
		s += "Apogee:"+getApogee()+",";
		s += "Samples:"+getDataSamples();
		s += ")";
		return s;
	}
	
	public void printData() {
		System.out.println(toString()+":");
		for (int i=0; i<data.length; i+=8) {
			String s = "  "+i+":";
			for (int j=0; j<8 && (i+j)<data.length; j++) {
				s += " "+data[i+j];
			}
			System.out.println(s);
		}
	}
	
}
