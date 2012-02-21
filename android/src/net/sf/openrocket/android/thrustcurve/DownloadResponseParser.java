package net.sf.openrocket.android.thrustcurve;

import java.io.InputStream;

import net.sf.openrocket.android.util.AndroidLogWrapper;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class DownloadResponseParser {

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

	public static DownloadResponse parse( InputStream in ) {

		final DownloadResponse ret = new DownloadResponse();
		final MotorBurnFile currentMotor = new MotorBurnFile();

		// Have a place to put the data string and format.
		// We hold on to these here, then push them into the currentMotor
		// only if it a supported filetype
		final StringHolder current_format = new StringHolder();
		final StringHolder current_data = new StringHolder();

		RootElement rootEl = new RootElement(thrustcurveURI, root_tag);
		/*
		rootEl.setStartElementListener(
				new StartElementListener() {
					public void start(Attributes arg0) {
						AndroidLogWrapper.d(TAG,"Start Element error");
						ret.setError("IsError");
					}
				}
				);
				*/
		Element resultsEl = rootEl.getChild( thrustcurveURI, results_tag);
		Element resultEl = resultsEl.getChild( thrustcurveURI, result_tag);
		resultEl.setStartElementListener(
				new StartElementListener() {
					@Override
					public void start(Attributes arg0) {
						AndroidLogWrapper.d(DownloadResponseParser.class,"Start Element result");
						currentMotor.init();
					}
				}
				);

		resultEl.setEndElementListener(
				new EndElementListener() {
					@Override
					public void end() {
						if ( SupportedFileTypes.isSupportedFileType(current_format.s) ) {
							currentMotor.setFiletype(current_format.s);
							String s = null;
							try {
								s = Base64Decoder.decodeData(current_data.s);
							} catch ( Exception ex ) {
								AndroidLogWrapper.d(DownloadResponseParser.class,"base64: " + ex.getMessage());
							}
							currentMotor.decodeFile( s );
							ret.add((MotorBurnFile)currentMotor.clone());
						}
					}
				}
				);

		resultEl.getChild(thrustcurveURI,motor_id_tag).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						currentMotor.setMotorId(Integer.parseInt(arg0));
					}
				}
				);
		resultEl.getChild(thrustcurveURI,format_tag).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						current_format.s = arg0;
					}
				}
				);
		resultEl.getChild(thrustcurveURI,data_tag).setEndTextElementListener(
				new EndTextElementListener() {
					@Override
					public void end(String arg0) {
						current_data.s = arg0;
					}
				}
				);
		try {
			Xml.parse(in, Xml.Encoding.UTF_8,  rootEl.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	private static class StringHolder {
		public String s;
	}

}