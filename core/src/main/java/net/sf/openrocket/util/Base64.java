package net.sf.openrocket.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Base64 {

	public static final int DEFAULT_CHARS_PER_LINE = 72;
	
	private static final char[] ALPHABET = new char[] {
			'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
			'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
			'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
			'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
	};
	private static final char PAD = '=';

//	private static final byte[] REVERSE;
//	static {
//		REVERSE = new byte[128];
//		Arrays.fill(REVERSE, (byte)-1);
//		for (int i=0; i<64; i++) {
//			REVERSE[ALPHABET[i]] = (byte)i;
//		}
//		REVERSE['-'] = 62;
//		REVERSE['_'] = 63;
//		REVERSE[PAD] = 0;
//	}
	
	private static final Map<Character,Integer> REVERSE = new HashMap<Character,Integer>();
	static {
		for (int i=0; i<64; i++) {
			REVERSE.put(ALPHABET[i], i);
		}
		REVERSE.put('-', 62);
		REVERSE.put('_', 63);
		REVERSE.put(PAD, 0);
	}
	
	
	public static String encode(byte[] data) {
		return encode(data, DEFAULT_CHARS_PER_LINE);
	}
	
	public static String encode(byte[] data, int maxColumn) {
		StringBuilder builder = new StringBuilder();
		int column = 0;
		
		for (int position=0; position < data.length; position+=3) {
			if (column+4 > maxColumn) {
				builder.append('\n');
				column = 0;
			}
			builder.append(encodeGroup(data, position));
			column += 4;
		}
		builder.append('\n');
		return builder.toString();
	}
	
	

	
	public static byte[] decode(String data) {
		byte[] array = new byte[data.length()*3/4];
		char[] block = new char[4];
		int length = 0;
		
		for (int position=0; position < data.length(); ) {
			int p;
			for (p=0; p<4 && position < data.length(); position++) {
				char c = data.charAt(position);
				if (!Character.isWhitespace(c)) {
					block[p] = c;
					p++;
				}
			}
			
			if (p==0)
				break;
			if (p!=4) {
				throw new IllegalArgumentException("Data ended when decoding Base64, data=" + data + ", p="+p);
			}
			
			int l = decodeGroup(block, array, length);
			length += l;
			if (l < 3)
				break;
		}
		return Arrays.copyOf(array, length);
	}
	
	
	////  Helper methods
	
	
	/**
	 * Encode three bytes of data into four characters.
	 */
	private static char[] encodeGroup(byte[] data, int position) {
		char[] c = new char[] { '=','=','=','=' };
		int b1=0, b2=0, b3=0;
		int length = data.length - position;
		
		if (length == 0)
			return c;
		
		if (length >= 1) {
			b1 = ((int)data[position])&0xFF;
		}
		if (length >= 2) {
			b2 = ((int)data[position+1])&0xFF;
		}
		if (length >= 3) {
			b3 = ((int)data[position+2])&0xFF;
		}
		
		c[0] = ALPHABET[b1>>2];
		c[1] = ALPHABET[(b1 & 3)<<4 | (b2>>4)];
		if (length == 1)
			return c;
		c[2] = ALPHABET[(b2 & 15)<<2 | (b3>>6)];
		if (length == 2)
			return c;
		c[3] = ALPHABET[b3 & 0x3f];
		return c;
	}
	
	
	/**
	 * Decode four chars from data into 0-3 bytes of data starting at position in array.
	 * @return	the number of bytes decoded.
	 */
	private static int decodeGroup(char[] data, byte[] array, int position) {
		int b1, b2, b3, b4;
		
		try {
			b1 = REVERSE.get(data[0]);
			b2 = REVERSE.get(data[1]);
			b3 = REVERSE.get(data[2]);
			b4 = REVERSE.get(data[3]);
		} catch (NullPointerException e) {
			// If auto-boxing fails
			throw new IllegalArgumentException("Illegal characters in the sequence to be "+
					"decoded: "+Arrays.toString(data));
		}
		
		array[position]   = (byte)((b1 << 2) | (b2 >> 4)); 
		array[position+1] = (byte)((b2 << 4) | (b3 >> 2)); 
		array[position+2] = (byte)((b3 << 6) | (b4)); 
		
		// Check the amount of data decoded
		if (data[0] == PAD)
			return 0;
		if (data[1] == PAD) {
			throw new IllegalArgumentException("Illegal character padding in sequence to be "+
					"decoded: "+Arrays.toString(data));
		}
		if (data[2] == PAD)
			return 1;
		if (data[3] == PAD)
			return 2;
		
		return 3;
	}
	
}
