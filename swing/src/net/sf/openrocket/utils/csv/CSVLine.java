package net.sf.openrocket.utils.csv;

import java.util.ArrayList;

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
