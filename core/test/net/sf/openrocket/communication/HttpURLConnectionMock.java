package net.sf.openrocket.communication;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.openrocket.util.BugException;

public class HttpURLConnectionMock extends HttpURLConnection {
	
	private static final URL MOCK_URL;
	static {
		try {
			MOCK_URL = new URL("http://localhost/");
		} catch (MalformedURLException e) {
			throw new BugException(e);
		}
	}
	
	@SuppressWarnings("hiding")
	private volatile boolean instanceFollowRedirects = false;
	
	@SuppressWarnings("hiding")
	private volatile int responseCode;

	private volatile String requestMethod = "";
	private Map<String, String> requestProperties = new HashMap<String, String>();
	private volatile int connectTimeout = -1;
	private volatile String contentEncoding = "";
	
	@SuppressWarnings("hiding")
	private volatile boolean doInput = false;
	@SuppressWarnings("hiding")
	private volatile boolean doOutput = false;
	
	private volatile byte[] content = null;
	private volatile String contentType = null;
	
	@SuppressWarnings("hiding")
	private volatile boolean useCaches = false;

	private volatile InputStream inputStream = null;
	private volatile ByteArrayOutputStream outputStream = null;
	
	private volatile String trueUrl = null;
	
	@SuppressWarnings("hiding")
	private volatile boolean connected = false;
	
	private volatile int connectionDelay = 0;
	
	private volatile boolean failed = false;
	
	
	
	
	public HttpURLConnectionMock() {
		super(MOCK_URL);
	}
	
	public HttpURLConnectionMock(URL u) {
		super(u);
	}
	
	
	
	public String getTrueUrl() {
		return trueUrl;
	}
	
	public void setTrueUrl(String url) {
		assertNull(this.trueUrl);
		this.trueUrl = url;
	}
	
	
	public boolean hasFailed() {
		return failed;
	}
	
	
	public void setConnectionDelay(int delay) {
		this.connectionDelay = delay;
	}
	
	
	
	@Override
	public void connect() {
		if (!connected) {
			try {
				Thread.sleep(connectionDelay);
			} catch (InterruptedException e) {
			}
			connected = true;
		}
	}
	
	@Override
	public void disconnect() {
		
	}
	
	@Override
	public boolean usingProxy() {
		return false;
	}
	
	
	
	
	@Override
	public boolean getInstanceFollowRedirects() {
		return this.instanceFollowRedirects;
	}
	
	@Override
	public void setInstanceFollowRedirects(boolean followRedirects) {
		assertFalse(connected);
		this.instanceFollowRedirects = followRedirects;
	}
	
	@Override
	public String getRequestMethod() {
		return this.requestMethod;
	}
	
	@Override
	public void setRequestMethod(String method) throws ProtocolException {
		assertFalse(connected);
		this.requestMethod = method;
	}
	
	@Override
	public int getResponseCode() throws IOException {
		connect();
		return this.responseCode;
	}
	
	public void setResponseCode(int code) {
		this.responseCode = code;
	}
	
	
	@Override
	public void addRequestProperty(String key, String value) {
		assertFalse(connected);
		assertFalse(this.requestProperties.containsKey(key.toLowerCase(Locale.ENGLISH)));
		this.requestProperties.put(key.toLowerCase(Locale.ENGLISH), value);
	}
	
	
	@Override
	public void setRequestProperty(String key, String value) {
		assertFalse(connected);
		this.requestProperties.put(key.toLowerCase(Locale.ENGLISH), value);
	}
	
	
	@Override
	public String getRequestProperty(String key) {
		return this.requestProperties.get(key.toLowerCase(Locale.ENGLISH));
	}
	
	
	@Override
	public int getConnectTimeout() {
		return this.connectTimeout;
	}
	
	@Override
	public void setConnectTimeout(int timeout) {
		assertFalse(connected);
		this.connectTimeout = timeout;
	}
	
	
	
	@Override
	public String getContentEncoding() {
		connect();
		return this.contentEncoding;
	}
	
	public void setContentEncoding(String encoding) {
		this.contentEncoding = encoding;
	}
	
	
	
	@Override
	public int getContentLength() {
		connect();
		if (content == null)
			return 0;
		return content.length;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public void setContent(String content) {
		try {
			this.content = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			fail("UTF-8");
		}
	}
	
	
	@Override
	public String getContentType() {
		connect();
		return this.contentType;
	}
	
	public void setContentType(String type) {
		this.contentType = type;
	}
	
	
	
	@Override
	public boolean getDoInput() {
		return this.doInput;
	}
	
	
	@Override
	public void setDoInput(boolean doinput) {
		assertFalse(connected);
		this.doInput = doinput;
	}
	
	
	@Override
	public boolean getDoOutput() {
		return this.doOutput;
	}
	
	
	@Override
	public void setDoOutput(boolean dooutput) {
		assertFalse(connected);
		this.doOutput = dooutput;
	}
	
	
	@Override
	public InputStream getInputStream() throws IOException {
		assertTrue(doInput);
		assertNull(inputStream);
		assertNotNull(content);
		
		connect();
		inputStream = new ByteArrayInputStream(content);
		return inputStream;
	}
	
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		assertTrue(doOutput);
		assertNull(outputStream);
		outputStream = new ByteArrayOutputStream();
		return outputStream;
	}
	
	public byte[] getOutputStreamData() {
		return outputStream.toByteArray();
	}
	
	public String getOutputStreamString() {
		try {
			return outputStream.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			fail("UTF-8");
			return null;
		}
	}
	
	
	
	@Override
	public void setUseCaches(boolean usecaches) {
		assertFalse(connected);
		this.useCaches = usecaches;
	}
	
	
	
	@Override
	public boolean getUseCaches() {
		return this.useCaches;
	}
	
	
	
	
	
	private void assertNull(Object o) {
		try {
			org.junit.Assert.assertNull(o);
		} catch (AssertionError e) {
			failed = true;
			throw e;
		}
	}
	
	private void assertNotNull(Object o) {
		try {
			org.junit.Assert.assertNotNull(o);
		} catch (AssertionError e) {
			failed = true;
			throw e;
		}
	}
	
	private void assertTrue(boolean o) {
		try {
			org.junit.Assert.assertTrue(o);
		} catch (AssertionError e) {
			failed = true;
			throw e;
		}
	}
	
	private void assertFalse(boolean o) {
		try {
			org.junit.Assert.assertFalse(o);
		} catch (AssertionError e) {
			failed = true;
			throw e;
		}
	}
	
	private void fail(String msg) {
		failed = true;
		org.junit.Assert.fail(msg);
	}
	
	
	
	
	
	@Override
	public InputStream getErrorStream() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public String getHeaderField(int n) {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public long getHeaderFieldDate(String name, long Default) {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public String getHeaderFieldKey(int n) {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public Permission getPermission() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public String getResponseMessage() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public void setChunkedStreamingMode(int chunklen) {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public void setFixedLengthStreamingMode(int contentLength) {
		throw new UnsupportedOperationException();
	}
	
	
	
	
	
	@Override
	public boolean getAllowUserInteraction() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public Object getContent() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public Object getContent(@SuppressWarnings("rawtypes") Class[] classes) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public long getDate() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public boolean getDefaultUseCaches() {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public long getExpiration() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public String getHeaderField(String name) {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public int getHeaderFieldInt(String name, int Default) {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public Map<String, List<String>> getHeaderFields() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public long getIfModifiedSince() {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public long getLastModified() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getReadTimeout() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public Map<String, List<String>> getRequestProperties() {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public URL getURL() {
		throw new UnsupportedOperationException();
	}
	
	
	
	@Override
	public void setAllowUserInteraction(boolean allowuserinteraction) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setDefaultUseCaches(boolean defaultusecaches) {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public void setIfModifiedSince(long ifmodifiedsince) {
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public void setReadTimeout(int timeout) {
		throw new UnsupportedOperationException();
	}
	
	
	
	
	
	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
	
	
	
	
}
