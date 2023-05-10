package net.sf.openrocket.utils;

import java.text.Normalizer;

import net.sf.openrocket.util.Chars;

/**
 * This class is used to generate the map used in L10N class
 * due to the fact that Android does not support java.text.Normalizer.
 */
public class L10NGenerator {
	
	public static void main(String[] args) throws Exception {
		
		// Latin chars
		for (char ch = 128; ch < 0x250; ch++) {
			output(ch);
		}
		// Superscript numbers
		for (char ch = 0x2070; ch <= 0x2079; ch++) {
			output(ch);
		}
		// Subscript numbers
		for (char ch = 0x2080; ch <= 0x2089; ch++) {
			output(ch);
		}
		output(Chars.FRACTION);
		print(Chars.ZWSP, " ");
		print(Chars.NBSP, " ");
	}
	
	private static void output(char ch) {
		String text = "" + ch;
		StringBuilder sb = new StringBuilder(text.length());
		//			s = normalize(s);
		text = Normalizer.normalize(text, Normalizer.Form.NFKD);
		
		for (char c : text.toCharArray()) {
			if (c < 128) {
				sb.append(c);
			} else if (c == Chars.FRACTION) {
				sb.append('/');
			}
		}
		
		text = sb.toString().trim();
		
		if (text.length() > 0) {
			print(ch, text);
		}
	}
	
	private static void print(char ch, String text) {
		System.out.printf("m.put('\\u%04x', \"%s\");\n", (int) ch, text);
	}
}
