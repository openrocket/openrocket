package net.sf.openrocket.utils.csv;

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
 * @author takaji, modified by nubjub (JRE 1.7)
 * 
 */
public final class CSVWriter {

    protected CSVFormat format;
    protected final BufferedWriter writer;
    protected final String writerName;
    int row = 1;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public CSVWriter(Writer writer) {
        this.writer = new BufferedWriter(writer);
        this.writerName = writer.toString();
        this.format = new CSVFormat();
    }

    public CSVWriter(Writer writer, CSVFormat format)
            throws FileNotFoundException {
        this.writer = new BufferedWriter(writer);
        this.writerName = writer.toString();
        this.format = format;
    }

    public CSVWriter(BufferedWriter writer) {
        this.writer = writer;
        this.writerName = writer.toString();
        this.format = new CSVFormat();
    }

    public CSVWriter(BufferedWriter writer, CSVFormat format)
            throws FileNotFoundException {
        this.writer = writer;
        this.writerName = writer.toString();
        this.format = format;
    }

    public CSVWriter(String filename) {
        try {
            this.writer = new BufferedWriter(new FileWriter(new File(filename)));
            this.writerName = filename;
            this.format = new CSVFormat();
        } catch (IOException ex) {
            Logger.getLogger(CSVWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVWriter(File f) {
        try {
            this.writer = new BufferedWriter(new FileWriter(f));
            this.writerName = f.getName();
            this.format = new CSVFormat();
        } catch (IOException ex) {
            Logger.getLogger(CSVWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVWriter(OutputStream input) {
        writer = new BufferedWriter(new OutputStreamWriter(input));
        this.writerName = input.toString();
        this.format = new CSVFormat();
    }

    public CSVWriter(String filename, CSVFormat format) {
        try {
            this.writer = new BufferedWriter(new FileWriter(filename));
            this.writerName = filename;
            this.format = format;
        } catch (IOException ex) {
            Logger.getLogger(CSVWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException(ex);
        }
    }

    public CSVWriter(File f, CSVFormat format) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(f));
        this.writerName = f.getName();
        this.format = format;
    }

    public CSVWriter(OutputStream input, CSVFormat format) {
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
    public CSVWriter writeLine(CSVLine line) throws CSVException {
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
    public CSVWriter carriageReturn() throws CSVException {
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
            Logger.getLogger(CSVWriter.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CSVWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new CSVException("Cannot flush", ex);
        }
    }
}
