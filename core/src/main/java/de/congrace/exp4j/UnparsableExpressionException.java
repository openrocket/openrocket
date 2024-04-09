/*
   Copyright 2011 frank asseg

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.congrace.exp4j;

/**
 * Exception for invalid expressions
 * 
 * @author fas@congrace.de
 */
public class UnparsableExpressionException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * construct a new {@link UnparsableExpressionException}
	 * 
	 * @param c
	 *            the character which could not be parsed
	 * @param pos
	 *            the position of the character in the expression
	 */
	public UnparsableExpressionException(char c, int pos) {
		super("Unable to parse character at position " + pos + ": '" + String.valueOf(c) + "'");
	}
	/**
	 * construct a new {@link UnparsableExpressionException}
	 * 
	 * @param msg 
	 *            the error message
	 */
	public UnparsableExpressionException(String msg) {
		super(msg);
	}
}
