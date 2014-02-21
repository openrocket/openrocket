package net.sf.openrocket.utils.csv;

import java.util.Arrays;


class CSVFormat {

    private String charset;
    private char fieldDelimiter;
    private char textDelimiter;
    private char lineTerminator;
    private char[] ignoreCharacters;

    /**
     * Construct a standard CSV format
     */
    public CSVFormat() {
        this.setCharset("UTF-8");
        this.setFieldDelimiter(',');
        this.setTextDelimiter('"');

        //auto detect line separator
        String s = System.getProperty("line.separator");
        if (s.length() > 0) {
            this.setLineTerminator(s.charAt(0));
            char[] ignoreChars = new char[s.length() - 1];
            for (int i = 1; i < s.length(); i++) {
                ignoreChars[i-1] = s.charAt(i);
            }
            this.setIgnoreCharacters(ignoreChars);
        } else {
            this.setLineTerminator('\n');
            this.setIgnoreCharacters(new char[]{'\r'});
        }
    }

    /**
     * @param lineTerminator
     *            the lineTerminator to set
     */
    public void setLineTerminator(char lineTerminator) {
        this.lineTerminator = lineTerminator;
    }

    /**
     * @return the lineTerminator
     */
    public char getLineTerminator() {
        return lineTerminator;
    }

    /**
     * @param ignoreCharacters
     *            the ignoreCharacters to set
     */
    public void setIgnoreCharacters(char[] ignoreCharacters) {
        this.ignoreCharacters = ignoreCharacters;
    }

    /**
     * @return the ignoreCharacters
     */
    public char[] getIgnoreCharacters() {
        return ignoreCharacters;
    }

    /**
     * @param charset
     *            the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @param fieldDelimiter
     *            the fieldDelimiter to set
     */
    public void setFieldDelimiter(char fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    /**
     * @return the fieldDelimiter
     */
    public char getFieldDelimiter() {
        return fieldDelimiter;
    }

    /**
     * @param textDelimiter
     *            the textDelimiter to set
     */
    public void setTextDelimiter(char textDelimiter) {
        this.textDelimiter = textDelimiter;
    }

    /**
     * @return the textDelimiter
     */
    public char getTextDelimiter() {
        return textDelimiter;
    }

    /**
     * is ignored characters
     * @param c
     * @return
     */
    public boolean isIgnored(char c) {
        return Arrays.binarySearch(ignoreCharacters, c) >= 0;
    }
}
