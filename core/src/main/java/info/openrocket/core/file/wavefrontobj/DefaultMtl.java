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
import de.javagl.obj.Mtl;
import de.javagl.obj.TextureOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of an Mtl (material)
 */
public final class DefaultMtl implements Mtl {
    /**
     * The name of this material
     */
    private final String name;

    /**
     * The illumination mode
     */
    private Integer illum;

    /**
     * The optical density
     */
    private Float ni;

    /**
     * The transmission filter
     */
    private FloatTuple tf;

    /**
     * The sharpness of reflections
     */
    private Float sharpness;

    /**
     * The ambient part of this material
     */
    private FloatTuple ka;

    /**
     * The ambient map texture options
     */
    private TextureOptions mapKaOptions;

    /**
     * The diffuse part of this material
     */
    private FloatTuple kd;

    /**
     * The diffuse map texture options
     */
    private TextureOptions mapKdOptions;

    /**
     * The specular part of this material
     */
    private FloatTuple ks;

    /**
     * The specular map texture options
     */
    private TextureOptions mapKsOptions;

    /**
     * The shininess of this material
     */
    private Float ns;

    /**
     * The shininess map texture options
     */
    private TextureOptions mapNsOptions;

    /**
     * The opacity of this material
     */
    private Float d;

    /**
     * The halo flag for the opacity
     */
    private Boolean halo;

    /**
     * The opacity map texture options
     */
    private TextureOptions mapDOptions;

    /**
     * The bump map texture options
     */
    private TextureOptions bumpOptions;

    /**
     * The displacement map texture options
     */
    private TextureOptions dispOptions;

    /**
     * The decal map texture options
     */
    private TextureOptions decalOptions;

    /**
     * The reflection map texture options
     */
    private final List<TextureOptions> reflOptions;

    // PBR Parameters:

    /**
     * The roughness of this material
     */
    private Float pr;

    /**
     * The roughness map texture options
     */
    private TextureOptions mapPrOptions;

    /**
     * The metallic part of this material
     */
    private Float pm;

    /**
     * The metallic map texture options
     */
    private TextureOptions mapPmOptions;

    /**
     * The sheen part of this material
     */
    private Float ps;

    /**
     * The sheen map texture options
     */
    private TextureOptions mapPsOptions;

    /**
     * The clearcoat thickness of this material
     */
    private Float pc;

    /**
     * The clearcoat roughness of this material
     */
    private Float pcr;

    /**
     * The emissive part of this material
     */
    private FloatTuple ke;

    /**
     * The emissive map texture options
     */
    private TextureOptions mapKeOptions;

    /**
     * The anisotropy of this material
     */
    private Float aniso;

    /**
     * The anisotropy rotation of this material
     */
    private Float anisor;

    /**
     * The normal map texture options
     */
    private TextureOptions normOptions;

    /**
     * Creates a new material with the given name
     *
     * @param name The name of this material
     */
    public DefaultMtl(String name) {
        this.name = name;
        this.reflOptions = new ArrayList<TextureOptions>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getIllum() {
        return illum;
    }

    @Override
    public void setIllum(Integer illum) {
        this.illum = illum;
    }

    @Override
    public Float getNi() {
        return ni;
    }

    @Override
    public void setNi(Float ni) {
        this.ni = ni;
    }

    @Override
    public FloatTuple getTf() {
        return tf;
    }

    @Override
    public void setTf(Float r, Float g, Float b) {
        this.tf = ObjUtils.createRgbTuple(r, g, b);
    }

    @Override
    public Float getSharpness() {
        return sharpness;
    }

    @Override
    public void setSharpness(Float sharpness) {
        this.sharpness = sharpness;
    }


    @Override
    public FloatTuple getKa() {
        return ka;
    }

    @Override
    public void setKa(Float r, Float g, Float b) {
        this.ka = ObjUtils.createRgbTuple(r, g, b);
    }

    @Override
    public String getMapKa() {
        if (mapKaOptions == null) {
            return null;
        }
        return mapKaOptions.getFileName();
    }

    @Override
    public void setMapKa(String mapKa) {
        if (mapKaOptions == null) {
            mapKaOptions = new DefaultTextureOptions();
        }
        mapKaOptions.setFileName(mapKa);
    }

    @Override
    public TextureOptions getMapKaOptions() {
        return mapKaOptions;
    }

    @Override
    public void setMapKaOptions(TextureOptions options) {
        this.mapKaOptions = options;
    }

    @Override
    public FloatTuple getKd() {
        return kd;
    }

    @Override
    public void setKd(Float r, Float g, Float b) {
        this.kd = ObjUtils.createRgbTuple(r, g, b);
    }

    @Override
    public String getMapKd() {
        if (mapKdOptions == null) {
            return null;
        }
        return mapKdOptions.getFileName();
    }

    @Override
    public void setMapKd(String mapKd) {
        if (mapKdOptions == null) {
            mapKdOptions = new DefaultTextureOptions();
        }
        mapKdOptions.setFileName(mapKd);
    }

    @Override
    public TextureOptions getMapKdOptions() {
        return mapKdOptions;
    }

    @Override
    public void setMapKdOptions(TextureOptions options) {
        this.mapKdOptions = options;
    }

    @Override
    public FloatTuple getKs() {
        return ks;
    }

    @Override
    public void setKs(Float r, Float g, Float b) {
        this.ks = ObjUtils.createRgbTuple(r, g, b);
    }

    @Override
    public String getMapKs() {
        if (mapKsOptions == null) {
            return null;
        }
        return mapKsOptions.getFileName();
    }

    @Override
    public void setMapKs(String mapKs) {
        if (mapKsOptions == null) {
            mapKsOptions = new DefaultTextureOptions();
        }
        mapKsOptions.setFileName(mapKs);
    }

    @Override
    public TextureOptions getMapKsOptions() {
        return mapKsOptions;
    }

    @Override
    public void setMapKsOptions(TextureOptions options) {
        this.mapKsOptions = options;
    }

    @Override
    public Float getNs() {
        return ns;
    }

    @Override
    public void setNs(Float ns) {
        this.ns = ns;
    }

    @Override
    public String getMapNs() {
        if (mapNsOptions == null) {
            return null;
        }
        return mapNsOptions.getFileName();
    }

    @Override
    public void setMapNs(String mapNs) {
        if (mapNsOptions == null) {
            mapNsOptions = new DefaultTextureOptions();
        }
        mapNsOptions.setFileName(mapNs);
    }

    @Override
    public TextureOptions getMapNsOptions() {
        return mapNsOptions;
    }

    @Override
    public void setMapNsOptions(TextureOptions options) {
        this.mapNsOptions = options;
    }

    @Override
    public Float getD() {
        return d;
    }

    @Override
    public void setD(Float d) {
        this.d = d;
    }

    @Override
    public Boolean isHalo() {
        return halo;
    }

    @Override
    public void setHalo(Boolean halo) {
        this.halo = halo;
    }

    @Override
    public String getMapD() {
        if (mapDOptions == null) {
            return null;
        }
        return mapDOptions.getFileName();
    }

    @Override
    public void setMapD(String mapD) {
        if (mapDOptions == null) {
            mapDOptions = new DefaultTextureOptions();
        }
        mapDOptions.setFileName(mapD);
    }

    @Override
    public TextureOptions getMapDOptions() {
        return mapDOptions;
    }

    @Override
    public void setMapDOptions(TextureOptions options) {
        this.mapDOptions = options;
    }

    @Override
    public String getBump() {
        if (bumpOptions == null) {
            return null;
        }
        return bumpOptions.getFileName();
    }

    @Override
    public void setBump(String bump) {
        if (bumpOptions == null) {
            bumpOptions = new DefaultTextureOptions();
        }
        bumpOptions.setFileName(bump);
    }

    @Override
    public TextureOptions getBumpOptions() {
        return bumpOptions;
    }

    @Override
    public void setBumpOptions(TextureOptions options) {
        this.bumpOptions = options;
    }

    @Override
    public String getDisp() {
        if (dispOptions == null) {
            return null;
        }
        return dispOptions.getFileName();
    }

    @Override
    public void setDisp(String disp) {
        if (dispOptions == null) {
            dispOptions = new DefaultTextureOptions();
        }
        dispOptions.setFileName(disp);
    }

    @Override
    public TextureOptions getDispOptions() {
        return dispOptions;
    }

    @Override
    public void setDispOptions(TextureOptions options) {
        this.dispOptions = options;
    }

    @Override
    public String getDecal() {
        if (decalOptions == null) {
            return null;
        }
        return decalOptions.getFileName();
    }

    @Override
    public void setDecal(String decal) {
        if (decalOptions == null) {
            decalOptions = new DefaultTextureOptions();
        }
        decalOptions.setFileName(decal);
    }

    @Override
    public TextureOptions getDecalOptions() {
        return decalOptions;
    }

    @Override
    public void setDecalOptions(TextureOptions options) {
        this.decalOptions = options;
    }

    @Override
    public List<TextureOptions> getReflOptions() {
        return reflOptions;
    }

    // PRB parameters

    @Override
    public Float getPr() {
        return pr;
    }

    @Override
    public void setPr(Float pr) {
        this.pr = pr;
    }

    @Override
    public String getMapPr() {
        if (mapPrOptions == null) {
            return null;
        }
        return mapPrOptions.getFileName();
    }

    @Override
    public void setMapPr(String mapPr) {
        if (mapPrOptions == null) {
            mapPrOptions = new DefaultTextureOptions();
        }
        mapPrOptions.setFileName(mapPr);
    }

    @Override
    public TextureOptions getMapPrOptions() {
        return mapPrOptions;
    }

    @Override
    public void setMapPrOptions(TextureOptions options) {
        this.mapPrOptions = options;
    }

    @Override
    public Float getPm() {
        return pm;
    }

    @Override
    public void setPm(Float pm) {
        this.pm = pm;
    }

    @Override
    public String getMapPm() {
        if (mapPmOptions == null) {
            return null;
        }
        return mapPmOptions.getFileName();
    }

    @Override
    public void setMapPm(String mapPm) {
        if (mapPmOptions == null) {
            mapPmOptions = new DefaultTextureOptions();
        }
        mapPmOptions.setFileName(mapPm);
    }

    @Override
    public TextureOptions getMapPmOptions() {
        return mapPmOptions;
    }

    @Override
    public void setMapPmOptions(TextureOptions options) {
        this.mapPmOptions = options;
    }

    @Override
    public Float getPs() {
        return ps;
    }

    @Override
    public void setPs(Float ps) {
        this.ps = ps;
    }

    @Override
    public String getMapPs() {
        if (mapPsOptions == null) {
            return null;
        }
        return mapPsOptions.getFileName();
    }

    @Override
    public void setMapPs(String mapPs) {
        if (mapPsOptions == null) {
            mapPsOptions = new DefaultTextureOptions();
        }
        mapPsOptions.setFileName(mapPs);
    }

    @Override
    public TextureOptions getMapPsOptions() {
        return mapPsOptions;
    }

    @Override
    public void setMapPsOptions(TextureOptions options) {
        this.mapPsOptions = options;
    }

    @Override
    public Float getPc() {
        return pc;
    }

    @Override
    public void setPc(Float pc) {
        this.pc = pc;
    }

    @Override
    public Float getPcr() {
        return pcr;
    }

    @Override
    public void setPcr(Float pcr) {
        this.pcr = pcr;
    }

    @Override
    public FloatTuple getKe() {
        return ke;
    }

    @Override
    public void setKe(Float r, Float g, Float b) {
        this.ke = ObjUtils.createRgbTuple(r, g, b);
    }

    @Override
    public String getMapKe() {
        if (mapKeOptions == null) {
            return null;
        }
        return mapKeOptions.getFileName();
    }

    @Override
    public void setMapKe(String mapKe) {
        if (mapKeOptions == null) {
            mapKeOptions = new DefaultTextureOptions();
        }
        mapKeOptions.setFileName(mapKe);
    }

    @Override
    public TextureOptions getMapKeOptions() {
        return mapKeOptions;
    }

    @Override
    public void setMapKeOptions(TextureOptions options) {
        this.mapKeOptions = options;
    }

    @Override
    public Float getAniso() {
        return aniso;
    }

    @Override
    public void setAniso(Float aniso) {
        this.aniso = aniso;
    }

    @Override
    public Float getAnisor() {
        return anisor;
    }

    @Override
    public void setAnisor(Float anisor) {
        this.anisor = anisor;
    }

    @Override
    public String getNorm() {
        if (normOptions == null) {
            return null;
        }
        return normOptions.getFileName();
    }

    @Override
    public void setNorm(String norm) {
        if (normOptions == null) {
            normOptions = new DefaultTextureOptions();
        }
        normOptions.setFileName(norm);
    }

    @Override
    public TextureOptions getNormOptions() {
        return normOptions;
    }

    @Override
    public void setNormOptions(TextureOptions options) {
        this.normOptions = options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mtl");
        sb.append("[");
        sb.append("name=").append(getName());
        sb.append("]");
        return sb.toString();
    }

}
