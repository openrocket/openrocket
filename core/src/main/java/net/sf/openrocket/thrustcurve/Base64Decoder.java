package net.sf.openrocket.thrustcurve;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public abstract class Base64Decoder {

	private static final String BASE64_CHARS =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final char PAD_CHAR = '=';

	private final static short[]  _charToBits = new short[128];

	static {

		for (int i = 0; i < _charToBits.length; i++)
			_charToBits[i] = -1;

		for (int i = 0; i < BASE64_CHARS.length(); i++)
			_charToBits[BASE64_CHARS.charAt(i)] = (byte) i;
		_charToBits[PAD_CHAR] = 0;

	}
	
	/**
	 * Decode the specified Base64 string and write binary data
	 * to the given stream.
	 * @param str Base64 encoded string
	 * @param w output stream
	 */
	public static String decodeData(String str) throws IOException
	{
		StringReader  r;
		int           c1;

		if (str == null || str.length() < 1)
			return null;

		r = new StringReader(str);
		
		StringWriter w = new StringWriter();

		// spin through the input string
		c1 = readToNonSpace(r);
		while (c1 > 0)
		{
			int  c2, c3, c4;
			int  p1, p2, p3, p4;
			int  pad, n;

			pad = 0;

			c2 = readToNonSpace(r);
			c3 = readToNonSpace(r);
			c4 = readToNonSpace(r);
			if (c4 < 0)
				throw new IllegalArgumentException("Encoded string ends prematurely.");

			p1 = charToBits(c1);
			p2 = charToBits(c2);

			if (c3 == PAD_CHAR)
			{
				p3 = 0;
				pad++;
			}
			else
				p3 = charToBits(c3);

			if (c4 == PAD_CHAR)
			{
				p4 = 0;
				pad++;
			}
			else
				p4 = charToBits(c4);

			if (p1 < 0 || p2 < 0 || p3 < 0 || p4 < 0)
				throw new IllegalArgumentException("Encoded string contains invalid characters.");

			n = (p1 << 18) | (p2 << 12) | (p3 << 6) | p4;

			w.write((byte) ((n & 0xFF0000) >> 16));
			if (pad < 2)
				w.write((byte) ((n & 0x00FF00) >> 8));
			if (pad < 1)
				w.write((byte) (n & 0x0000FF));

			c1 = readToNonSpace(r);
			if (c1 > 0 && pad > 0)
				throw new IllegalArgumentException("Extra characters found after padding.");
		}
		
		return w.toString();
	}


	private static int readToNonSpace(Reader r)
	throws IOException
	{
		int  c;

		c = r.read();
		while (c >= 0 && Character.isWhitespace(c))
			c = r.read();

		return c;
	}

	private static int charToBits(int c)
	{
		// use it to look up the value
		if (c < 0 || c >= _charToBits.length)
			return -1;
		else
			return _charToBits[c];
	}


}
