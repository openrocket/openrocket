package net.sf.openrocket.utils.csv;

import java.util.ArrayList;

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