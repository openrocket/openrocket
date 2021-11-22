package net.sf.openrocket.thrustcurve;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.SimpleSAX;

public class DownloadResponseParser implements ElementHandler {
	
	private static final String thrustcurveURI = "http://www.thrustcurve.org/2009/DownloadResponse";
	
	private static final String root_tag = "download-response";
	private static final String results_tag = "results";
	private static final String result_tag = "result";
	private static final String motor_id_tag = "motor-id";
	private static final String simfile_id_tag = "simfile-id";
	private static final String format_tag = "format";
	private static final String source_tag = "source";
	private static final String license_tag = "license";
	private static final String data_tag = "data";
	private static final String error_tag = "error";
	
	private final DownloadResponse response = new DownloadResponse();
	
	private MotorBurnFile motorBurnFile;
	
	private DownloadResponseParser() {
	}

    public static DownloadResponse parse(InputStream in) throws IOException, SAXException {
		
		DownloadResponseParser handler = new DownloadResponseParser();
		WarningSet warnings = new WarningSet();
		SimpleSAX.readXML(new InputSource(in), handler, warnings);
		
		return handler.response;
		
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
		if (result_tag.equals(element)) {
			motorBurnFile = new MotorBurnFile();
		}
		return this;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
		if (result_tag.equals(element)) {
			response.add(motorBurnFile);
		} else if (motor_id_tag.equals(element)) {
			motorBurnFile.setMotorId(Integer.parseInt(content));
		} else if (simfile_id_tag.equals(element)) {
			motorBurnFile.setSimfileId(Integer.parseInt(content));
		} else if (format_tag.equals(element)) {
			motorBurnFile.setFiletype(content);
		} else if (data_tag.equals(element)) {
			try {
				motorBurnFile.decodeFile(content);
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
	}
	
}
