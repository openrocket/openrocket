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
	
	private Color paint;
	private double shine;
	private double offsetU, offsetV;
	private double centerU, centerV;
	private double scaleU, scaleV;
	private double rotation;
	private DecalImage image;
	private Decal.EdgeMode edgeMode;
	
	private boolean batch;
	
	public AppearanceBuilder() {
		resetToDefaults();
	}
	
	public AppearanceBuilder(Appearance a) {
		setAppearance(a);
	}
	
	private void resetToDefaults() {
		paint = new Color(0, 0, 0);
		shine = 0;
		offsetU = offsetV = 0;
		centerU = centerV = 0;
		scaleU = scaleV = 1;
		rotation = 0;
		image = null;
		edgeMode = EdgeMode.REPEAT;
	}
	
	public void setAppearance(final Appearance a) {
		batch(new Runnable() {
			@Override
			public void run() {
				resetToDefaults();
				if (a != null) {
					setPaint(a.getPaint());
					setShine(a.getShine());
					Decal d = a.getTexture();
					if (d != null) {
						setOffset(d.getOffset().x, d.getOffset().y);
						setCenter(d.getCenter().x, d.getCenter().y);
						setScaleUV(d.getScale().x, d.getScale().y);
						setRotation(d.getRotation());
						setEdgeMode(d.getEdgeMode());
						setImage(d.getImage());
					}
				}
			}
		});
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
		
		return new Appearance(paint, shine, t);
	}
	
	
	
	public Color getPaint() {
		return paint;
	}
	
	public void setPaint(Color paint) {
		this.paint = paint;
		fireChangeEvent();
	}
	
	public double getShine() {
		return shine;
	}
	
	public void setShine(double shine) {
		this.shine = shine;
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
	
	public void setScaleUV(double u, double v) {
		setScaleU(u);
		setScaleV(v);
	}
	
	public double getScaleX() {
		return 1.0 / getScaleU();
	}
	
	public void setScaleX(double scaleU) {
		setScaleU(1.0 / scaleU);
	}
	
	public double getScaleY() {
		return 1.0 / getScaleV();
	}
	
	public void setScaleY(double scaleV) {
		setScaleV(1.0 / scaleV);
	}
	
	public double getRotation() {
		return rotation;
	}
	
	public void setRotation(double rotation) {
		this.rotation = rotation;
		fireChangeEvent();
	}
	
	public DecalImage getImage() {
		return image;
	}
	
	public void setImage(DecalImage image) {
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
		if (!batch)
			super.fireChangeEvent();
	}
	
	public void batch(Runnable r) {
		batch = true;
		r.run();
		batch = false;
		fireChangeEvent();
	}
	
}
