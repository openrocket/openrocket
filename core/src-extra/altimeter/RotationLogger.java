package altimeter;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Class to interface the PerfectFlite Alt15K/WD altimeter.
 * 
 * Also includes a main method that retrieves all flight profiles and saves them to files.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class RotationLogger {
	private static final boolean DEBUG = false;
	
	private static final int BYTES = 65536; 
	
	
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

	
	
	

	public RotationLogger(String name) throws IOException {
		CommPortIdentifier myPortID  = null;
		
		Enumeration<?> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		while (portIdentifiers.hasMoreElements()) {
		    CommPortIdentifier pid = (CommPortIdentifier) portIdentifiers.nextElement();
		    
		    if(pid.getPortType() == CommPortIdentifier.PORT_SERIAL &&
		       pid.getName().equals(name)) {
		        myPortID  = pid;
		        break;
		    }
		}
		
		if (myPortID == null) {
			throw new IOException("Port '"+name+"' not found.");
		}
		this.portID = myPortID ;
	}
	
	
	
	
	
	
	public void readData() throws IOException, PortInUseException {
		int c;
		
		int[] data = new int[BYTES];
		
		FileOutputStream rawdump = null;
		
		
		try {
			open();

			System.err.println("Sending dump mode command...");
			
			for (int i=0; i<16; i++) {
				os.write('D');
				try {
					Thread.sleep(10);
				} catch (InterruptedException ignore) { }
			}
			
			System.err.println("Waiting for response...");
			while (true) {
				c = is.read();
				if (c == 'K') {
					break;
				} else {
					System.err.printf("Received spurious c=%d\n",c);
				}
			}
			
			System.err.println("Received response.");
			

			
			System.err.println("Opening 'rawdump'...");
			rawdump = new FileOutputStream("rawdump");
			
			
			
			System.err.println("Performing dump...");

			os.write('A');

			byte[] buffer = new byte[1024];
			int printCount = 0;
			for (int count=0; count < BYTES; ) {
				if ((BYTES-count) < buffer.length) {
					buffer = new byte[BYTES-count];
				}
				
				int n = is.read(buffer);
				if (n < 0) {
					System.err.println("Error condition, n="+n);
					return;
				}
			
				rawdump.write(buffer, 0, n);
				
				for (int i=0; i<n; i++) {
					data[count+i] = unsign(buffer[i]);
				}
				count += n;
				if (count - printCount > 1024) {
					System.err.println("Read "+count+" bytes...");
					printCount = count;
				}
			}


			System.err.println("Verifying checksum...");
			int reported = is.read();
			
			byte computed = 0;
			for (int i=0; i < data.length; i++) {
				computed += data[i];
			}
			if (computed == reported) {
				System.err.println("Checksum ok ("+computed+")");
			} else {
				System.err.println("Error in checksum, computed="+computed+
						" reported="+reported);
			}
			
			System.err.println("Communication done.");
			
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close();
			if (rawdump != null)
				rawdump.close();
		}
		
		convertData(data);
		
	}
	
	
	
	////////////  Data interpretation   //////////////	
	
	
	private static void convertData(int[] data) {

		System.err.println("Converting data...");

		int lastBuffer = data[0xffff];
		if (lastBuffer < 0 || lastBuffer > 3) {
			System.err.println("Illegal last accessed buffer: "+lastBuffer);
			return;
		}
		System.err.println("Last used buffer: "+lastBuffer);
		
		for (int i=4; i>0; i--) {
			int n = (lastBuffer + i) % 4;
			int bufNumber = 4-i;
			
			convertBuffer(data, n * (BYTES/4), bufNumber);
		}
		
	}
	
	
	private static void convertBuffer(int[] data, int position, int bufNumber) {
		int startPosition;
		
		startPosition = data[position + 0xfd] << 8 + data[position+0xfe];

		// 50 samples per 128 bytes 
		int startTime = (startPosition -position) * 50 / 128;
		
		System.err.println("  Buffer "+ bufNumber + " (at position "+position+")...");
		System.err.println("  Start position "+startPosition+" time "+startTime);

		System.out.println("# Buffer "+bufNumber);
		System.out.println("# Start position t="+startTime);
		
		
		int t = 0;
		for (int page = 0; page < 128; page++) {
			int pageStart = position + page * 128;

			if (pageStart == startPosition) {
				System.out.println("# ---clip---");
			}

			for (int i=0; i<125; i += 5) {
				int sample1, sample2;
				
				int start = pageStart + i;
//				System.err.println("page="+page+" i="+i+
//						" position="+position+" pageStart="+pageStart+" start="+start);
				
				sample1 = (data[start] << 2) + (data[start+1] >> 6);
				sample2 = ((data[start+1] & 0x3f) << 4) + (data[start+2] >> 4);
				System.out.printf("%d  %4d  %4d %4d\n", bufNumber, t, sample1, sample2);
				t++;
				
				sample1 = ((data[start+2] & 0x0f) << 6) + (data[start+3] >> 2);
				sample2 = ((data[start+3] & 3) << 8) + data[start+4];
				System.out.printf("%d  %4d  %4d %4d\n", bufNumber, t, sample1, sample2);
				t++;
			}
		}
	}

	
	
	private void open() throws PortInUseException, IOException, 
			UnsupportedCommOperationException {
		
		if (port != null) {
			System.err.println("ERROR: open() called with port="+port);
			Thread.dumpStack();
			close();
		}
		
		if (DEBUG) {
			System.err.println("  Opening port...");
		}

		port = (SerialPort)portID.open("OpenRocket",1000);
		
		port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, 
				SerialPort.PARITY_NONE);

		port.setInputBufferSize(1);
		port.setOutputBufferSize(1);

		is = port.getInputStream();
		os = port.getOutputStream();
	}
	
	
	private void close() {
		if (DEBUG)
			System.err.println("  Closing port");
		
		SerialPort p = port;
		port = null;
		is = null;
		if (p != null)
			p.close();
	}
	
	
	
	private static int unsign(byte b) {
		if (b >= 0)
			return b;
		else
		return 256 + b;
	}
	
	

	
	public static void main(String[] arg) throws Exception {
		
		if (arg.length > 2) {
			System.err.println("Illegal arguments.");
			return;
		}
		if (arg.length == 1) {
			FileInputStream is = new FileInputStream(arg[0]);
			byte[] buffer = new byte[BYTES];
			int n = is.read(buffer);
			if (n != BYTES) {
				System.err.println("Could read only "+n+" bytes");
				return;
			}
			
			int[] data = new int[BYTES];
			for (int i=0; i<BYTES; i++) {
				data[i] = unsign(buffer[i]);
			}

			int checksum=0;
			for (int i=0; i<BYTES; i++) {
				checksum += data[i];
			}
			checksum = checksum%256;
			System.err.println("Checksum: "+checksum);
			
			convertData(data);
			return;			
		}
		
		
		String device = null;
		String[] devices = RotationLogger.getNames();
		for (int i=0; i<devices.length; i++) {
			if (devices[i].matches(".*USB.*")) {
				device = devices[i];
				break;
			}
		}
		if (device == null) {
			System.err.println("Device not found.");
			return;
		}
		
		
		System.err.println("Selected device "+device);
		
		
		RotationLogger p = new RotationLogger(device);
		
		p.readData();
		
	}
	
	
}
