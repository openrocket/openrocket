package altimeter;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

/**
 * Class to interface the PerfectFlite Alt15K/WD altimeter.
 * 
 * Also includes a main method that retrieves all flight profiles and saves them to files.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class Alt15K {
	public static final int TIMEOUT = 500;
	public static final int RWDELAY = 5;
	
	private static final boolean DEBUG = false;
	
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");
	
	private final CommPortIdentifier portID;
	private SerialPort port = null;
	private InputStream is = null;
	private OutputStream os = null;
	
	

	public static String[] getNames() {
		ArrayList<String> list = new ArrayList<String>();;
		
		Enumeration<?> pids = CommPortIdentifier.getPortIdentifiers();

		while (pids.hasMoreElements()) {
		    CommPortIdentifier pid = (CommPortIdentifier) pids.nextElement();

		    if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL)
		    	list.add(pid.getName());
		}
		return list.toArray(new String[0]);
	}

	

	public Alt15K(String name) throws IOException {
		CommPortIdentifier pID = null;
		
		Enumeration<?> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (portIdentifiers.hasMoreElements()) {
		    CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
		    
		    if(pid.getPortType() == CommPortIdentifier.PORT_SERIAL &&
		       pid.getName().equals(name)) {
		        pID = pid;
		        break;
		    }
		}
		
		if (pID==null) {
			throw new IOException("Port '"+name+"' not found.");
		}
		this.portID = pID;
	}
	

	/**
	 * Get altimeter flight data.  The flight profile is chosen by the parameter n,
	 * 0 = latest flight, 1 = second latest, etc.
	 * 
	 * @param n  Which flight profile to use (0=newest, 1=second newest, etc)
	 * @return   The altimeter flight data
	 * @throws IOException			in case of IOException
	 * @throws PortInUseException	in case of PortInUseException
	 */
	public AltData getData(int n) throws IOException, PortInUseException {
		AltData alt = new AltData();
		ArrayList<Integer> data = new ArrayList<Integer>();
		byte[] buf;
		byte[] buf2 = new byte[0];
		boolean identical = false;  // Whether identical lines have been read
		
		if (DEBUG)
			System.out.println("  Retrieving altimeter data n="+n);
		
		try {
			open();

			// Get version and position data
			byte[] ver = getVersionData();
			alt.setVersion(new byte[] { ver[0],ver[1] });

			// Calculate the position requested
			if (n > 2)
				n = 2;
			int position = ver[2] - n;
			while (position < 0)
				position += 3;

			if (DEBUG)
				System.out.println("  Requesting data from position "+position);
			
			// Request the data
			write("D");
			write((byte)position);
			write("PS");

			sleep();

			// Read preliminary data
			buf = read(4);
			int msl_level = combine(buf[0],buf[1]);
			int datacount = combine(buf[2],buf[3]);

			if (DEBUG)
				System.out.println("  Preliminary data msl="+msl_level+" count="+datacount);
			
			alt.setMslLevel(msl_level-6000);
			alt.setDataSamples(datacount);

			if (DEBUG)
				System.out.println("  Retrieving "+datacount+" samples");

			long t = System.currentTimeMillis();

			int count = 0;
			while (count < datacount) {
				sleep();
				write("G");
				sleep();
				buf = read(17);

				if (buf.length == 17) {
					// Checksum = sum of all bytes + 1
					// (signedness does not change the result)
					byte checksum = 1;
					for (int i=0; i<16; i++)
						checksum += buf[i];
					if (checksum != buf[16]) {
						printBytes("ERROR: Checksum fail on data (computed="+checksum+
								" orig="+buf[16]+")",buf);
						System.out.println("Ignoring error");
					}
				} else {
					System.err.println("ERROR:  Only "+buf.length+" bytes read, should be 17");
				}
				
				for (int i=0; i<buf.length-1; i+=2) {
					data.add(combine(buf[i],buf[i+1]));
					count++;
				}
				
				/*
				 * Check whether the data is identical to the previous data batch.  If reading
				 * too fast, the data seems to become duplicated in the transfer.  We need to check
				 * whether this has happened by attempting to read more data than is normally
				 * available.
				 */
				int c, l=Math.min(buf.length, buf2.length);
				for (c=0; c<l; c++) {
					if (buf[c] != buf2[c])
						break;
				}
				if (c==l && buf.length == buf2.length)
					identical = true;
				buf2 = buf.clone();
			}

			if (DEBUG)
				System.out.println("  Retrieved "+data.size()+" samples in "+
						(System.currentTimeMillis()-t)+" ms");


			// In case of identical lines, check for more data.  This would mean that the
			// transfer was corrupted.
			if (identical) {
				System.err.println("WARNING:  Duplicate data detected, possible error");
			}

			// Test for more data
			if (DEBUG)
				System.out.println("  Testing for more data");
			sleep();
			write("G");
			sleep();
			buf = read(17);
			if (buf.length > 0) {
				System.err.println("ERROR: Data available after transfer! (length="+buf.length+")");
			}

			
			
			
			
			
			// Create an int[] array and set it
			int[] d = new int[data.size()];
			for (int i=0; i<d.length; i++)
				d[i] = data.get(i);
			alt.setData(d);
			
		//  Catch all exceptions, close the port and re-throw the exception
		} catch (PortInUseException e) {
			close();
			throw e;
		} catch (IOException e) {
			close();
			throw e;
		} catch (UnsupportedCommOperationException e) {
			close();
			throw new RuntimeException("Required function of RxTx library not supported",e);
		} catch (RuntimeException e) {
			// Catch-all for all other types of exceptions
			close();
			throw e;
		}

		close();
		return alt;
	}
	

	
	
	private byte[] getVersionData() throws PortInUseException, IOException, 
										   UnsupportedCommOperationException {
		byte[] ver = new byte[3];
		byte[] buf;

		if (DEBUG)
			System.out.println("  Retrieving altimeter version information");
		
		// Signal to altimeter we are here
		write((byte)0);
		sleep(15);  // Sleep for 15ms, data is incoming at 10 samples/sec
		
		// Get altimeter version, skip zeros
		write("PV");
		sleep();
		buf = readSkipZero(2);
		sleep();
		if (buf.length != 2) {
			close();
			throw new IOException("Communication with altimeter failed.");
		}
		ver[0] = buf[0];
		ver[1] = buf[1];
		
		// Get position of newest data
		write("M");
		sleep();
		buf = read(1);
		if (buf.length != 1) {
			close();
			throw new IOException("Communication with altimeter failed.");
		}
		ver[2] = buf[0];

		if (DEBUG)
			System.out.println("  Received version info "+ver[0]+"."+ver[1]+", position "+ver[2]);
		
		return ver;
	}
	
	
	/**
	 * Delay the communication by a small delay (RWDELAY ms).
	 */
	private void sleep() {
		sleep(RWDELAY);
	}
	
	/**
	 * Sleep for the given amount of milliseconds.
	 */
	private void sleep(int n) {
		try {
			Thread.sleep(n);
		} catch (InterruptedException ignore) { }
	}
	
	
	private void open() 
	throws PortInUseException, IOException, UnsupportedCommOperationException {
		if (port != null) {
			System.err.println("ERROR: open() called with port="+port);
			Thread.dumpStack();
			close();
		}
		
		if (DEBUG) {
			System.out.println("  Opening port...");
		}

		port = (SerialPort)portID.open("OpenRocket",1000);
		
		port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, 
				SerialPort.PARITY_NONE);

		port.setInputBufferSize(1);
		port.setOutputBufferSize(1);

		port.enableReceiveTimeout(TIMEOUT);

		is = port.getInputStream();
		os = port.getOutputStream();
	}
	
	
	private byte[] readSkipZero(int n) throws IOException, UnsupportedCommOperationException {
		long t = System.currentTimeMillis() + TIMEOUT*2;
		
		if (DEBUG) {
			System.out.println("    readSkipZero "+n+" bytes");
		}
		
		while (System.currentTimeMillis() < t) {
			byte[] buf = read(n);
			if (DEBUG)
				printBytes("      Received",buf);
			
			if (buf.length == 0)  // No data available
				return buf;
			
			// Skip zeros
			int i;
			for (i=0; i<buf.length; i++)
				if (buf[i] != 0)
					break;
			
			if (i==0)   // No zeros to skip
				return buf;
			
			if (i < buf.length) {
				// Partially read
				int count = buf.length-i;  // No. of data bytes
				byte[] array = new byte[n];
				System.arraycopy(buf, i, array, 0, count);
				buf = read(n-count);
				if (DEBUG)
					printBytes("      Received (partial)",buf);
				System.arraycopy(buf, 0, array, count, buf.length);
				
				if (DEBUG)
					printBytes("    Returning",array);
				return array;
			}
		}
		
		if (DEBUG)
			System.out.println("  No data read, returning empty");
		return new byte[0];  // no data, only zeros
	}
	

	private byte[] read(int n) throws IOException, UnsupportedCommOperationException {
		byte[] bytes = new byte[n];
		
		port.enableReceiveThreshold(n);
		
		long t = System.currentTimeMillis() + TIMEOUT;
		int count = 0;

		if (DEBUG)
			System.out.println("    Reading "+n+" bytes");

		while (count < n && System.currentTimeMillis() < t) {
			byte[] buf = new byte[n-count];
			int c = is.read(buf);
			System.arraycopy(buf, 0, bytes, count, c);
			count += c;
		}
		
		byte[] array = new byte[count];
		System.arraycopy(bytes, 0, array, 0, count);
		
		if (DEBUG)
			printBytes("    Returning",array);
		
		return array;
	}
	
	private void write(String s) throws IOException {
		write(s.getBytes(CHARSET));
	}
	
	private void write(byte ... bytes) throws IOException {
		if (DEBUG)
			printBytes("    Writing",bytes);
		os.write(bytes);
	}
	
	private void close() {
		if (DEBUG)
			System.out.println("  Closing port");
		
		SerialPort p = port;
		port = null;
		is = null;
		os = null;
		if (p != null)
			p.close();
	}
	
	
	

	
	public static void main(String[] arg) {
		
		if (arg.length != 1) {
			System.err.println("Usage:  java Alt15K <basename>");
			System.err.println("Files will be saved <basename>-old.log, -med and -new");
			return;
		}
		
		
		String device = null;
		String[] devices = Alt15K.getNames();
		for (int i=0; i<devices.length; i++) {
			if (devices[i].matches(".*USB.*")) {
				device = devices[i];
				break;
			}
		}
		if (device == null) {
			System.out.println("Device not found.");
			return;
		}
		
		
		System.out.println("Selected device "+device);
		
		AltData alt = null;
		String file;
		try {
			Alt15K p = new Alt15K(device);

			System.out.println("Retrieving newest data...");
			alt = p.getData(0);
			System.out.println("Apogee at "+alt.getApogee()+" feet");

			file = arg[0]+"-new.log";
			System.out.println("Saving data to "+file+"...");
			savefile(file,alt);
			
			
			System.out.println("Retrieving medium data...");
			alt = p.getData(1);
			System.out.println("Apogee at "+alt.getApogee()+" feet");

			file = arg[0]+"-med.log";
			System.out.println("Saving data to "+file+"...");
			savefile(file,alt);
			
			
			System.out.println("Retrieving oldest data...");
			alt = p.getData(2);
			System.out.println("Apogee at "+alt.getApogee()+" feet");

			file = arg[0]+"-old.log";
			System.out.println("Saving data to "+file+"...");
			savefile(file,alt);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		}

//		System.out.println(alt);
//		alt.printData();
		
	}
	
	
	static private void savefile(String file, AltData data) throws FileNotFoundException {
		
		PrintStream output = new PrintStream(file);
		
		// WTF is this so difficult?!?
		DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		TimeZone tz=TimeZone.getTimeZone("GMT+3");
		fmt.setTimeZone(tz);

		output.println("# Alt15K data, file "+file);
		output.println("# Data retrieved at: "+fmt.format(new Date()));
		output.println("# Values are in feet above launch level");
		output.println("# ");
		output.println("# Apogee = "+data.getApogee());
		output.println("# MSL level = "+data.getMslLevel());
		output.println("# Data count = "+data.getDataSamples());
		
		byte[] b = data.getVersion();
		String s="";
		for (int i=0; i<b.length; i++) {
			if (s.equals(""))
				s = ""+((int)b[i]);
			else 
				s = s+"."+((int)b[i]);
		}
		output.println("# Altimeter version = " + s);
		
		int[] values = data.getData();
		for (int i=0; i < values.length; i++) {
			output.println(""+values[i]);
		}
		
		output.close();
	}
	
	
	static private void printBytes(String str, byte[] b) {
		printBytes(str, b,b.length);
	}
	
	static private void printBytes(String str, byte[] b, int n) {
		String s;
		s = str+" "+n+" bytes:";
		for (int i=0; i<n; i++) {
			s += " "+unsign(b[i]);
		}
		System.out.println(s);
	}
	
	static private int unsign(byte b) {
		if (b >= 0)
			return b;
		else
			return 256 + b;
	}
	
	@SuppressWarnings("unused")
	static private int combine(int a, int b) {
		return 256*a + b;
	}
	
	static private int combine(byte a, byte b) {
		int val = 256*unsign(a)+unsign(b);
		if (val <= 32767)
			return val;
		else
			return val-65536;
			
	}
	
}
