package net.sf.openrocket.appearance;

import net.sf.openrocket.util.Color;

/**
 * A component appearance.
 * This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Appearance {
	public static final Appearance MISSING = new Appearance(new Color(0,0,0), new Color(128,128,128), new Color(255,255,255), 100, null);

	private final Color ambient, diffuse, specular;
	private final int shininess;
	private final Decal texture;
	
	Appearance(final Color ambient, final Color diffuse, final Color specular, final int shininess, final Decal texture){
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.texture = texture;
	}
	
	Appearance(final Color ambient, final Color diffuse, final Color specular, final int shininess){
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.shininess = shininess;
		this.texture = null;
	}
	
	public Color getAmbient() {
		return ambient;
	}

	public Color getDiffuse() {
		return diffuse;
	}

	public Color getSpecular() {
		return specular;
	}

	public int getShininess() {
		return shininess;
	}

	public Decal getTexture() {
		return texture;
	}

	@Override
	public String toString() {
		return "Appearance [ambient=" + ambient + ", diffuse=" + diffuse + ", specular=" + specular + ", shininess="
				+ shininess + ", texture=" + texture + "]";
	}

}
