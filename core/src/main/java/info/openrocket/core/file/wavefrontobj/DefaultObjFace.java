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


import de.javagl.obj.ObjFace;

import java.util.Arrays;

/**
 * Default implementation of an ObjFace
 */
public final class DefaultObjFace implements ObjFace {
    /**
     * The vertex indices of this face
     */
    private final int[] vertexIndices;

    /**
     * The texture coordinate indices of this face
     */
    private final int[] texCoordIndices;

    /**
     * The normal indices of this face
     */
    private final int[] normalIndices;

    /**
     * Creates a face from the given parameters. References to the
     * given objects will be stored.
     *
     * @param vertexIndices   The vertex indices
     * @param texCoordIndices The texture coordinate indices
     * @param normalIndices   The normal indices
     */
    public DefaultObjFace(int[] vertexIndices, int[] texCoordIndices, int[] normalIndices) {
        this.vertexIndices = vertexIndices;
        this.texCoordIndices = texCoordIndices;
        this.normalIndices = normalIndices;
    }

    public DefaultObjFace(DefaultObjFace face) {
        this.vertexIndices = Arrays.copyOf(face.vertexIndices, face.vertexIndices.length);
        this.texCoordIndices = face.texCoordIndices != null ? Arrays.copyOf(face.texCoordIndices, face.texCoordIndices.length) : null;
        this.normalIndices = face.normalIndices != null ? Arrays.copyOf(face.normalIndices, face.normalIndices.length) : null;
    }


    @Override
    public boolean containsTexCoordIndices() {
        return texCoordIndices != null;
    }

    @Override
    public boolean containsNormalIndices() {
        return normalIndices != null;
    }

    public int[] getVertexIndices() {
        return vertexIndices;
    }

    public int[] getTexCoordIndices() {
        return texCoordIndices;
    }

    public int[] getNormalIndices() {
        return normalIndices;
    }

    @Override
    public int getVertexIndex(int number) {
        return this.vertexIndices[number];
    }

    @Override
    public int getTexCoordIndex(int number) {
        return this.texCoordIndices[number];
    }

    @Override
    public int getNormalIndex(int number) {
        return this.normalIndices[number];
    }

    /**
     * Set the specified index to the given value
     *
     * @param n     The index to set
     * @param index The value of the index
     */
    public void setVertexIndex(int n, int index) {
        vertexIndices[n] = index;
    }

    /**
     * Set the specified index to the given value
     *
     * @param n     The index to set
     * @param index The value of the index
     */
    public void setNormalIndex(int n, int index) {
        normalIndices[n] = index;
    }

    /**
     * Set the specified index to the given value
     *
     * @param n     The index to set
     * @param index The value of the index
     */
    public void setTexCoordIndex(int n, int index) {
        texCoordIndices[n] = index;
    }

    @Override
    public int getNumVertices() {
        return this.vertexIndices.length;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("ObjFace[");
        for (int i = 0; i < getNumVertices(); i++) {
            result.append(vertexIndices[i]);
            if (texCoordIndices != null || normalIndices != null) {
                result.append("/");
            }
            if (texCoordIndices != null) {
                result.append(texCoordIndices[i]);
            }
            if (normalIndices != null) {
                result.append("/").append(normalIndices[i]);
            }
            if (i < getNumVertices() - 1) {
                result.append(" ");
            }
        }
        result.append("]");
        return result.toString();
    }
}

