package altimeter;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Class to interface the PerfectFlite Alt15K/WD altimeter.
 * 
 * Also includes a main method that retrieves all flight profiles and saves them to files.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class SerialDownload {
	private static final boolean DEBUG = false;
	
	private static final int MAGIC = 666;
	
	private final CommPortIdentifier portID;
	private SerialPort port = null;
	private InputStream is = null;
	
	

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

	
	
	

	public SerialDownload(String name) throws IOException {
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
		long t0 = -1;
		long t;
		
		int previous = MAGIC;
		
		
		try {
			open();
			
			System.err.println("Ready to read...");
			while (true) {
				int c = is.read();
				t = System.nanoTime();
				if (t0 < 0)
					t0 = t;
				
				System.out.printf("%10.6f %d\n", ((double)t-t0)/1000000000.0, c);
				
				if (previous == MAGIC) {
					previous = c;
				} else {
					System.out.printf("# Altitude: %5d\n", previous*256 + c);
					previous = MAGIC;
				}
				
				if (c < 0)
					break;
			}
			
			
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close();
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
	
	
	

	
	public static void main(String[] arg) throws Exception {
		
		String device = null;
		String[] devices = SerialDownload.getNames();
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
		
		
		SerialDownload p = new SerialDownload(device);
		
		p.readData();
		
	}
	
	
}
