package info.openrocket.core.file.wavefrontobj;

/*
 * www.javagl.de - Obj
 *
 * Copyright (c) 2008-2015 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import de.javagl.obj.FloatTuple;

import java.util.Arrays;

/**
 * Default implementation of a {@link FloatTuple}
 */
public final class DefaultFloatTuple implements FloatTuple {
    /**
     * The values of this tuple
     */
    private final float[] values;

    /**
     * Creates a new DefaultFloatTuple with the given values
     *
     * @param values The values
     */
    public DefaultFloatTuple(float[] values) {
        this.values = values;
    }

    /**
     * Creates a new DefaultFloatTuple with the given values
     *
     * @param x The x value
     * @param y The y value
     * @param z The z value
     * @param w The w value
     */
    public DefaultFloatTuple(float x, float y, float z, float w) {
        this(new float[]{x, y, z, w});
    }

    /**
     * Creates a new DefaultFloatTuple with the given values
     *
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public DefaultFloatTuple(float x, float y, float z) {
        this(new float[]{x, y, z});
    }

    /**
     * Creates a new DefaultFloatTuple with the given values
     *
     * @param x The x value
     * @param y The y value
     */
    public DefaultFloatTuple(float x, float y) {
        this(new float[]{x, y});
    }

    /**
     * Creates a new DefaultFloatTuple with the given value
     *
     * @param x The x value
     */
    public DefaultFloatTuple(float x) {
        this(new float[]{x});
    }


    /**
     * Copy constructor.
     *
     * @param other The other FloatTuple
     */
    DefaultFloatTuple(FloatTuple other) {
        this(getValues(other));
    }

    /**
     * Returns the values of the given {@link FloatTuple} as an array
     *
     * @param f The {@link FloatTuple}
     * @return The values
     */
    private static float[] getValues(FloatTuple f) {
        if (f instanceof DefaultFloatTuple) {
            DefaultFloatTuple other = (DefaultFloatTuple) f;
            return other.values.clone();
        }
        float values[] = new float[f.getDimensions()];
        for (int i = 0; i < values.length; i++) {
            values[i] = f.get(i);
        }
        return values;
    }

    @Override
    public float get(int index) {
        return values[index];
    }

    @Override
    public float getX() {
        return values[0];
    }

    /**
     * Set the given component of this tuple
     *
     * @param x The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 1
     *                                   dimensions
     */
    public void setX(float x) {
        values[0] = x;
    }

    @Override
    public float getY() {
        return values[1];
    }

    /**
     * Set the given component of this tuple
     *
     * @param y The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 2
     *                                   dimensions
     */
    public void setY(float y) {
        values[1] = y;
    }

    @Override
    public float getZ() {
        return values[2];
    }

    /**
     * Set the given component of this tuple
     *
     * @param z The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 3
     *                                   dimensions
     */
    public void setZ(float z) {
        values[2] = z;
    }

    @Override
    public float getW() {
        return values[3];
    }

    /**
     * Set the given component of this tuple
     *
     * @param w The component to set
     * @throws IndexOutOfBoundsException If this tuple has less than 4
     *                                   dimensions
     */
    void setW(float w) {
        values[3] = w;
    }

    @Override
    public int getDimensions() {
        return values.length;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < getDimensions(); i++) {
            sb.append(get(i));
            if (i < getDimensions() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (object instanceof DefaultFloatTuple) {
            DefaultFloatTuple other = (DefaultFloatTuple) object;
            return Arrays.equals(values, other.values);
        }
        if (object instanceof FloatTuple) {
            FloatTuple other = (FloatTuple) object;
            if (other.getDimensions() != getDimensions()) {
                return false;
            }
            for (int i = 0; i < getDimensions(); i++) {
                if (get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}

