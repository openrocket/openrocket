package net.sf.openrocket.appearance;

import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

/**
 * Use this class to build an immutable Appearance object in a friendly way. Set
 * the various values one at a time with the setter methods and then call
 * getAppearance(). Each call to getAppearance will return a new appearance.
 * 
 * You can use this class repeatedly and resetToDefaults() in between, or create
 * a new one every time.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 * 
 */
public class AppearanceBuilder extends AbstractChangeSource {

	private Color ambient, diffuse, specular;
	private int shininess;
	private double offsetU, offsetV;
	private double centerU, centerV;
	private double scaleU, scaleV;
	private double rotation;
	private String image;
	private Decal.EdgeMode edgeMode;

	public AppearanceBuilder() {
		resetToDefaults();
	}

	public AppearanceBuilder(Appearance a) {
		resetToDefaults();
		if ( a != null ){
			setAmbient(a.getAmbient());
			setDiffuse(a.getDiffuse());
			setSpecular(a.getSpecular());
			setShininess(a.getShininess());
			Decal d = a.getTexture();
			if ( d != null ){
				setOffset(d.getOffset().x, d.getOffset().y);
				setCenter(d.getCenter().x, d.getCenter().y);
				setScale(d.getScale().x, d.getScale().y);
				setRotation(d.getRotation());
				setEdgeMode(d.getEdgeMode());
				setImage(d.getImage());
			}
		// TODO Critical the rest of this!
		}
	}

	public void resetToDefaults() {
		ambient = new Color(0, 0, 0);
		diffuse = new Color(128, 128, 128);
		specular = new Color(255, 255, 255);
		shininess = 100;
		offsetU = offsetV = 0;
		centerU = centerV = 0;
		scaleU = scaleV = 1;
		rotation = 0;
		image = null;
		edgeMode = EdgeMode.REPEAT;
	}

	public Appearance getAppearance() {

		Decal t = null;

		if (image != null) {
			t = new Decal( //
					new Coordinate(offsetU, offsetV), //
					new Coordinate(centerU, centerV), //
					new Coordinate(scaleU, scaleV), //
					rotation, //
					image, //
					edgeMode);
		}

		return new Appearance( ambient, diffuse, specular, shininess, t);
	}



	public Color getAmbient() {
		return ambient;
	}

	public void setAmbient(Color ambient) {
		this.ambient = ambient;
		fireChangeEvent();
	}

	public Color getDiffuse() {
		return diffuse;
	}

	public void setDiffuse(Color diffuse) {
		this.diffuse = diffuse;
		fireChangeEvent();
	}

	public Color getSpecular() {
		return specular;
	}

	public void setSpecular(Color specular) {
		this.specular = specular;
		fireChangeEvent();
	}

	public int getShininess() {
		return shininess;
	}

	public void setShininess(int shininess) {
		this.shininess = shininess;
		fireChangeEvent();
	}

	public double getOffsetU() {
		return offsetU;
	}

	public void setOffsetU(double offsetU) {
		this.offsetU = offsetU;
		fireChangeEvent();
	}

	public double getOffsetV() {
		return offsetV;
	}

	public void setOffsetV(double offsetV) {
		this.offsetV = offsetV;
		fireChangeEvent();
	}

	public void setOffset(double u, double v) {
		setOffsetU(u);
		setOffsetV(v);
	}

	public double getCenterU() {
		return centerU;
	}

	public void setCenterU(double centerU) {
		this.centerU = centerU;
		fireChangeEvent();
	}

	public double getCenterV() {
		return centerV;
	}

	public void setCenterV(double centerV) {
		this.centerV = centerV;
		fireChangeEvent();
	}

	public void setCenter(double u, double v) {
		setCenterU(u);
		setCenterV(v);
	}

	public double getScaleU() {
		return scaleU;
	}

	public void setScaleU(double scaleU) {
		this.scaleU = scaleU;
		fireChangeEvent();
	}

	public double getScaleV() {
		return scaleV;
	}

	public void setScaleV(double scaleV) {
		this.scaleV = scaleV;
		fireChangeEvent();
	}

	public void setScale(double u, double v) {
		setScaleU(u);
		setScaleV(v);
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
		fireChangeEvent();
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
		fireChangeEvent();
	}

	public Decal.EdgeMode getEdgeMode() {
		return edgeMode;
	}

	public void setEdgeMode(Decal.EdgeMode edgeMode) {
		this.edgeMode = edgeMode;
		fireChangeEvent();
	}

	@Override
	protected void fireChangeEvent() {
		super.fireChangeEvent();
	}

}
