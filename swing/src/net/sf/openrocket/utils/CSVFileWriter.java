package net.sf.openrocket.utils;

/*
 *  Copyright (C) 2010 takaji
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * CSV file writer
 * 
 * @author takaji, modified by nubjub
 * 
 */
public final class CSVFileWriter {

    protected CSVFormat format;
    protected final BufferedWriter writer;
    protected final String writerName;
    int row = 1;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public CSVFileWriter(Writer writer) {
        this.writer = new BufferedWriter(writer);
        this.writerName = writer.toString();
        this.format = new CSVFormat();
    }

    public CSVFileWriter(Writer writer, CSVFormat format)
            throws FileNotFoundException {
        this.writer = new BufferedWriter(writer);
        this.writerName = writer.toString();
        this.format = format;
    }

    public CSVFileWriter(BufferedWriter writer) {
        this.writer = writer;
        this.writerName = writer.toString();
        this.format = new CSVFormat();
    }

    public CSVFileWriter(BufferedWriter writer, CSVFormat format)
            throws FileNotFoundException {
        this.writer = writer;
        this.writerName = writer.toString();
        this.format = format;
    }

    public CSVFileWriter(String filename) {
        try {
            this.writer = new BufferedWriter(new FileWriter(new File(filename)));
            this.writerName = filename;
            this.format = new CSVFormat();
        } catch (IOException ex) {
            Logger.getLogger(CSVFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVFileWriter(File f) {
        try {
            this.writer = new BufferedWriter(new FileWriter(f));
            this.writerName = f.getName();
            this.format = new CSVFormat();
        } catch (IOException ex) {
            Logger.getLogger(CSVFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVFileWriter(OutputStream input) {
        writer = new BufferedWriter(new OutputStreamWriter(input));
        this.writerName = input.toString();
        this.format = new CSVFormat();
    }

    public CSVFileWriter(String filename, CSVFormat format) {
        try {
            this.writer = new BufferedWriter(new FileWriter(filename));
            this.writerName = filename;
            this.format = format;
        } catch (IOException ex) {
            Logger.getLogger(CSVFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVFileWriter(File f, CSVFormat format) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(f));
        this.writerName = f.getName();
        this.format = format;
    }

    public CSVFileWriter(OutputStream input, CSVFormat format) {
        writer = new BufferedWriter(new OutputStreamWriter(input));
        this.writerName = input.toString();
        this.format = format;
    }

    // </editor-fold>
    /**
     * Write a line to CSV<br/>
     *
     * @throws IOException
     */
    public CSVFileWriter writeLine(CSVLine line) throws CSVException {
        if (line == null) {
            return this;
        }

        synchronized (this) {

            // if current line index > 1
            // terminate previous line first
            if (row > 1) {
                carriageReturn();
            }

            if (!line.isEmpty()) {
                // try to write each cell in line
                try {
                    Object[] elements = line.getElements();
                    for (int i = 0; i < elements.length; i++) {
                        // not first element -> write field delimiter
                        if (i > 0) {
                            writer.append(format.getFieldDelimiter());
                        }
                        // write cell content
                        Object cell = elements[i];
                        if (cell != null) {
                            writer.append(escapeString(cell.toString()));
                        } else {
                            writer.append("");
                        }
                        row++;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    throw new CSVException(e);
                }
            } else {
                row++;
            }
        }
        return this;
    }

    /**
     * Return carriage
     *
     * @throws IOException
     */
    public CSVFileWriter carriageReturn() throws CSVException {
        try {
            synchronized (writer) {
                writer.append(format.getLineTerminator());
            }
        } catch (IOException e) {
            throw new CSVException("Error while returning carriage.", e);
        }
        return this;
    }

    /**
     * Write a file to underline CSV file
     *
     * @param file
     */
    public void writeFile(CSVFile file) throws CSVException {
        if (file == null) {
            // no need to write
            return;
        }
        CSVLine[] lines = file.getLines();
        synchronized (this) {
            for (int i = 0; i < lines.length; i++) {
                CSVLine line = lines[i];
                writeLine(line);
            }
        }
    }

    /**
     * escape a string with text delimiter if needed
     *
     * @param str
     * @return
     */
    private String escapeString(String str) {

        if (str.indexOf(format.getTextDelimiter()) >= 0
                || str.indexOf(format.getLineTerminator()) >= 0
                || str.indexOf(format.getFieldDelimiter()) >= 0) {
            return format.getTextDelimiter()
                    + str.replace("" + format.getTextDelimiter(), ""
                    + format.getTextDelimiter()
                    + format.getTextDelimiter())
                    + format.getTextDelimiter();
        } else {
            // no need to escape
            return str;
        }
    }
    /**
     * compares the value of writerName to the input string
     * 
     * @param   String  input for comparison
     * @return  boolean
     */
    public boolean nameEquals(String str){
    	return writerName.equals(str);  
    }

    /**
     * close session
     */
    public void close() {
        try {
            flush();
        } catch (CSVException ex) {
            //do nothing
        }
        try {
            //auto flush
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(CSVFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Flush buffer to underline stream
     */
    public void flush() {
        try {
            synchronized (writer) {
                writer.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(CSVFileWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException("Cannot flush", ex);
        }
    }
    /**
     * Instantiate a CSVLine Object
     * @return  CSVLine 
     */
    public CSVLine newCSVLine(){
    	return new CSVLine();
    }
    public class CSVLine {

        private ArrayList<Object> elements;

        /**
         * Default constructor
         */
        public CSVLine() {
            this.elements = new ArrayList<Object>();
        }

        /**
         * Get all elements (cells) inside a line
         * @return empty String array if there's no element found inside
         */
        public Object[] getElements() {
            return this.elements.toArray();
        }

        /**
         * Get element at index
         * @param idx
         * @return
         * @throws IndexOutOfBoundsException
         */
        public Object getElementAt(int idx) throws IndexOutOfBoundsException {
            return elements.get(idx);
        }

        /**
         * Set element at
         * @param idx
         * @param value
         * @throws IndexOutOfBoundsException
         */
        public void setElementAt(int idx, Object value) throws IndexOutOfBoundsException {
            elements.set(idx, value);
        }

        /**
         * count elements inside this line
         * @return
         */
        public int size() {
            return elements.size();
        }

        /**
         * Add a new element
         */
        public CSVLine add(Object obj) {
            this.elements.add(obj);
            return this;
        }

        /**
         * if CSV line is empty
         * @return
         */
        public boolean isEmpty() {
            return this.elements.isEmpty();
        }

        /**
         * Remove all elements
         */
        public void clear() {
            this.elements.clear();
        }

        /**
         * Remove element at a specified index
         * @param idx
         * @throws IndexOutOfBoundsException
         */
        public void remove(int idx) throws IndexOutOfBoundsException {
            elements.remove(idx);
        }

        /**
         * Trim down or expand with null cell to a new length
         * @param newLength
         */
        public void setLength(int newLength) {
            if (elements.size() > newLength) {
                //trim down
                while (elements.size() > newLength) {
                    elements.remove(elements.size() - 1);
                }
            } else {
                //add more
                while (elements.size() < newLength) {
                    add(null);
                }
            }
        }
    }
    /**
     * Instantiate a CSVFile Object
     * @return  CSVLine 
     */
    public CSVFile newCSVFile(){
    	return new CSVFile();
    }
    public class CSVFile {

        private ArrayList<CSVLine> lines; //lines inside a CSV file

        /**
         * Default constructor
         */
        public CSVFile() {
            this.lines = new ArrayList<CSVLine>();
        }

        public CSVLine[] getLines() {
            return this.lines.toArray(new CSVLine[0]);
        }

        /**
         * Get line at a specified index
         * @param idx
         * @return
         * @throws IndexOutOfBoundsException
         */
        public CSVLine getLine(int idx) throws IndexOutOfBoundsException{
            return this.lines.get(idx);
        }

        /**
         * Add a new line to current csv
         * @return new line object
         */
        public CSVLine newLine() {
            CSVLine line = new CSVLine();
            this.lines.add(line);
            return line;
        }

        /**
         * get number of line
         * @return number of line
         */
        public int size() {
            return this.lines.size();
        }

        /**
         * discard a line at the specified index
         * @param idx
         */
        public void discard(int idx) {
            if (idx >= 0 && idx < size()) {
                this.lines.remove(idx);
            }
        }

        /**
         * discard empty record
         */
        public void discardEmpty() {
            for (int i = this.lines.size() - 1; i > -1; i--) {
                if (this.lines.get(i) == null || this.lines.get(i).isEmpty()) {
                    discard(i);
                }
            }
        }

        /**
         * append a CSV line to file (line is ignored if null)
         * @param line
         */
        void append(CSVLine line) {
            if (line == null) {
                return;
            }
            this.lines.add(line);
        }
    }
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
    class CSVException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CSVException() {
        }

        public CSVException(Throwable throwable) {
            super(throwable);
        }

        public CSVException(String message) {
            super(message);
        }

        public CSVException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }
}