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

package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.FloatTuples;
import de.javagl.obj.TextureOptions;

import java.util.Objects;

/**
 * Default implementation of {@link TextureOptions}
 */
public final class DefaultTextureOptions implements TextureOptions {
    /**
     * The file name
     */
    private String fileName;

    /**
     * The horizontal blending state
     */
    private Boolean blendu;

    /**
     * The vertical blending state
     */
    private Boolean blendv;

    /**
     * The color correction state
     */
    private Boolean cc;

    /**
     * The mip-map boost value
     */
    private Float boost;

    /**
     * The map modifiers
     */
    private FloatTuple mm;

    /**
     * The origin offset
     */
    private FloatTuple o;

    /**
     * The scale
     */
    private FloatTuple s;

    /**
     * The turbulence
     */
    private FloatTuple t;

    /**
     * The texture resolution
     */
    private Float texres;

    /**
     * The clamping state
     */
    private Boolean clamp;

    /**
     * The bump multiplier
     */
    private Float bm;

    /**
     * The IMF channel
     */
    private String imfchan;

    /**
     * The type
     */
    private String type;

    /**
     * Default constructor
     */
    public DefaultTextureOptions() {
        // Default constructor
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Boolean isBlendu() {
        return blendu;
    }

    @Override
    public void setBlendu(Boolean blendu) {
        this.blendu = blendu;
    }

    @Override
    public Boolean isBlendv() {
        return blendv;
    }

    @Override
    public void setBlendv(Boolean blendv) {
        this.blendv = blendv;
    }

    @Override
    public Float getBoost() {
        return boost;
    }

    @Override
    public Boolean isCc() {
        return cc;
    }

    @Override
    public void setCc(Boolean cc) {
        this.cc = cc;
    }

    @Override
    public void setBoost(Float boost) {
        this.boost = boost;
    }

    @Override
    public FloatTuple getMm() {
        return mm;
    }

    @Override
    public void setMm(Float base, Float gain) {
        if (base == null && gain == null) {
            this.mm = null;
        }
        float baseValue = (base == null ? 0.0f : base);
        float gainValue = (gain == null ? 1.0f : gain);
        this.mm = FloatTuples.create(baseValue, gainValue);
    }

    @Override
    public FloatTuple getO() {
        return o;
    }

    @Override
    public void setO(Float u, Float v, Float w) {
        this.o = ObjUtils.createUvwTuple(u, v, w, 0.0f);
    }

    @Override
    public FloatTuple getS() {
        return s;
    }

    @Override
    public void setS(Float u, Float v, Float w) {
        this.s = ObjUtils.createUvwTuple(u, v, w, 1.0f);
    }

    @Override
    public FloatTuple getT() {
        return t;
    }

    @Override
    public void setT(Float u, Float v, Float w) {
        this.t = ObjUtils.createUvwTuple(u, v, w, 0.0f);
    }

    @Override
    public Float getTexres() {
        return texres;
    }

    @Override
    public void setTexres(Float texres) {
        this.texres = texres;
    }

    @Override
    public Boolean isClamp() {
        return clamp;
    }

    @Override
    public void setClamp(Boolean clamp) {
        this.clamp = clamp;
    }

    @Override
    public Float getBm() {
        return bm;
    }

    @Override
    public void setBm(Float bm) {
        this.bm = bm;
    }

    @Override
    public String getImfchan() {
        return imfchan;
    }

    @Override
    public void setImfchan(String imfchan) {
        this.imfchan = imfchan;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TextureOptions");
        sb.append("[");
        sb.append(DefaultMtlWriter.createString(this));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(blendu, blendv, cc, bm, boost, clamp, fileName,
                imfchan, mm, o, s, t, texres, type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof TextureOptions)) {
            return false;
        }
        TextureOptions other = (TextureOptions) object;

        return
                Objects.equals(isBlendu(), other.isBlendu()) &&
                        Objects.equals(isBlendv(), other.isBlendv()) &&
                        Objects.equals(isCc(), other.isCc()) &&
                        Objects.equals(getBm(), other.getBm()) &&
                        Objects.equals(getBoost(), other.getBoost()) &&
                        Objects.equals(isClamp(), other.isClamp()) &&
                        Objects.equals(getFileName(), other.getFileName()) &&
                        Objects.equals(getImfchan(), other.getImfchan()) &&
                        Objects.equals(getMm(), other.getMm()) &&
                        Objects.equals(getO(), other.getO()) &&
                        Objects.equals(getS(), other.getS()) &&
                        Objects.equals(getT(), other.getT()) &&
                        Objects.equals(getTexres(), other.getTexres()) &&
                        Objects.equals(getType(), other.getType());
    }


}
