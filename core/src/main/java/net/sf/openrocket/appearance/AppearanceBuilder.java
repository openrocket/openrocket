package net.sf.openrocket.appearance;

import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

import java.util.LinkedHashMap;
import java.util.Map;

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
	
	private Color paint;		//current cached color
	private double shine;		//current cached shine
	private double offsetU, offsetV;//current offset to be used
	private double centerU, centerV;//current values for the center of the appearance
	private double scaleU, scaleV; 	//current values for scaling
	private double rotation;	//
	private DecalImage image;
	private Decal.EdgeMode edgeMode;
	
	private boolean batch;

	/**
	 * List of appearance builders that will set their appearance properties to the same as the current appearance
	 */
	private final Map<RocketComponent, AppearanceBuilder> configListeners = new LinkedHashMap<>();
	// If true, appearance change events will not be fired
	private boolean bypassAppearanceChangeEvent = false;
	
	/**
	*	Default constructor
	*	Set the builder to make appearance of null values
	*	
	*/
	public AppearanceBuilder() {
		resetToDefaults();
	}
	
	/**
	*	Constructor that initializes already with a 
	*
	*	@param a the appearance to be copied
	*/
	public AppearanceBuilder(Appearance a) {
		setAppearance(a);
	}
	
	/**
	*	Clears the builder cache and set to build blank appearances
	*/
	private void resetToDefaults() {
		paint = new Color(187, 187, 187);
		shine = 0.3;
		offsetU = offsetV = 0;
		centerU = centerV = 0;
		scaleU = scaleV = 1;
		rotation = 0;
		image = null;
		edgeMode = EdgeMode.REPEAT;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	Sets the builder to create appearance equals to an existing appearance
	*	Fires change only once, hence the call to batch
	*	
	*	@param a the appearance to be used as the new template
	*/
	public void setAppearance(final Appearance a) {
		batch(new Runnable() {
			@Override
			public void run() {
				resetToDefaults();
				if (a != null) {
					setPaint(a.getPaint());
					setShine(a.getShine());
					setDecal(a.getTexture());
				}
			}
		});
	}
	
	/**
	*	makes a full copy of a decal, including information of offsets, center and scale
	*	
	*	@param d The decal
	*/
	public void setDecal(Decal d){
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setDecal(d);
		}
		if (d != null) {
			setOffset(d.getOffset().x, d.getOffset().y);
			setCenter(d.getCenter().x, d.getCenter().y);
			setScaleUV(d.getScale().x, d.getScale().y);
			setRotation(d.getRotation());
			setEdgeMode(d.getEdgeMode());
			setImage(d.getImage());
		}
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	Method creates another object of Appearance
	*	@return	the created appearance
	*/
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
	
	
	/**
	*	get current paint in template
	*	
	*	return the color used in the current paint
	*/
	public Color getPaint() {
		return paint;
	}
	
	/**
	*	sets a new paint color to be used
	*	fires change event
	*
	*	@param paint the new color
	*/
	public void setPaint(Color paint) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setPaint(paint);
		}
		this.paint = paint;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current shine
	*
	*	@return current shine in template
	*/
	public double getShine() {
		return shine;
	}
	
	/**
	*	Sets a new shine for template
	*	fires change event
	*
	*	@param shine	the new shine for template
	*/
	public void setShine(double shine) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setShine(shine);
		}
		this.shine = shine;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}

	/**
	 * Returns the opacity of the paint color, expressed in percentages, where 0 is fully transparent and 1 is fully opaque
	 * @return opacity value of the pain color, expressed in a percentage
	 */
	public double getOpacity() {
		if (this.paint == null) {
			return 100;
		}
		return (double) this.paint.getAlpha() / 255;
	}

	/**
	 * Sets the opacity/alpha of the paint color.
	 *
	 * @param opacity new opacity value expressed in a percentage, where 0 is fully transparent and 1 is fully opaque
	 */
	public void setOpacity(double opacity) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setOpacity(opacity);
		}
		if (this.paint == null) {
			return;
		}

		// Clamp opacity between 0 and 1
		opacity = Math.max(0, Math.min(1, opacity));

		// Instead of simply setting the alpha, we need to create a new color with the new alpha value, otherwise undoing
		// the setOpacity will not work correctly. (don't ask me why)
		Color c = new Color(paint.getRed(), paint.getGreen(), paint.getBlue(), (int) (opacity * 255));
		setPaint(c);
	}
	
	/**
	*	gets the current offset axis U used
	*
	*	@return offset in axis U
	*/
	public double getOffsetU() {
		return offsetU;
	}
	
	
	/**
	*	sets a new offset in axis U for template
	*	fires change event
	*
	*	@param offsetU	the new offset to be used
	*/
	public void setOffsetU(double offsetU) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setOffsetU(offsetU);
		}
		this.offsetU = offsetU;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current offset axis V used
	*
	*	@return offset in axis V
	*/
	public double getOffsetV() {
		return offsetV;
	}
	
	/**
	*	sets a new offset in axis V for template
	*	fires change event
	*
	*	@param offsetV	the new offset to be used
	*/
	public void setOffsetV(double offsetV) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setOffsetV(offsetV);
		}
		this.offsetV = offsetV;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	sets a new offset to be used for template
	*	fires change event
	*
	*	@param u	offset in axis u
	*	@param v	offset in axis v
	*/
	public void setOffset(double u, double v) {
		setOffsetU(u);
		setOffsetV(v);
	}
	
	/**
	*	gets the current center in axis U used in template
	*	
	*	@return the current value of U of cente in template
	*/
	public double getCenterU() {
		return centerU;
	}
	
	/**
	*	set a new value for axis U for center in template
	*	fires change event
	*
	*	@param centerU	value of axis U for center
	*/
	public void setCenterU(double centerU) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setCenterU(centerU);
		}
		this.centerU = centerU;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current center in axis V used in template
	*	
	*	@return the current value of V of cente in template
	*/
	public double getCenterV() {
		return centerV;
	}
	
	/**
	*	set a new value for axis V for center in template
	*	fires change event
	*
	*	@return	value of axis V for center
	*/
	public void setCenterV(double centerV) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setCenterV(centerV);
		}
		this.centerV = centerV;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	sets a new center for template
	*	fires chenge event
	*
	*	@param u	new value for axis u of the center
	*	@param v	new value for axis v of the center
	*/
	public void setCenter(double u, double v) {
		setCenterU(u);
		setCenterV(v);
	}
	
	/**
	*	gets the current scale value of axis u in template
	*
	*	@return current value for axis u of scale
	*/
	public double getScaleU() {
		return scaleU;
	}
	
	/**
	*	sets a new value of axis U for scaling in the template
	*	fires change event
	*
	*	@param scaleU new value of scalling in axis U
	*/
	public void setScaleU(double scaleU) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setScaleU(scaleU);
		}
		this.scaleU = scaleU;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current scale value of axis V in template
	*
	*	@return current value for axis V of scale
	*/
	public double getScaleV() {
		return scaleV;
	}
	
	/**
	*	sets a new value of axis V for scaling in the template
	*	fires change event
	*
	*	@param scaleV new value of scalling in axis V
	*/
	public void setScaleV(double scaleV) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setScaleV(scaleV);
		}
		this.scaleV = scaleV;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	sets a new value of both axis for scaling in the template
	*	fires change event
	*
	*	@param u new value of scalling in axis U
	*	@param v new value of scalling in axis v
	*/
	public void setScaleUV(double u, double v) {
		setScaleU(u);
		setScaleV(v);
	}
	
	/**
	*	gets the current value of X axis for scalling in the template
	*	
	*	@return value of scalling in axis x
	*/
	public double getScaleX() {
		return 1.0 / getScaleU();
	}
	
	/**
	*	sets a new value of axis X for scalling in template
	*	fires change event
	*
	*	@param scaleX the new value for axis X
	*/	
	public void setScaleX(double scaleX) {
		setScaleU(1.0 / scaleX);
	}
	
	/**
	*	gets the current value of Y axis for scalling in the template
	*	
	*	@return value of scalling in axis Y
	*/
	public double getScaleY() {
		return 1.0 / getScaleV();
	}
	
	/**
	*	sets a new value of axis Y for scalling in template
	*	fires change event
	*
	*	@param scaleY the new value for axis Y
	*/	
	public void setScaleY(double scaleY) {
		setScaleV(1.0 / scaleY);
	}
	
	/**
	*	gets the current value of rotation in template
	*
	*	@return the current rotation in template
	*/
	public double getRotation() {
		return rotation;
	}
	
	/**
	*	sets a new value of rotation in template
	*	fires chenge event
	*
	*	@param rotation	the new value for rotation in template
	*/
	public void setRotation(double rotation) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setRotation(rotation);
		}
		this.rotation = rotation;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current image in template
	*
	*	@return the current image in template
	*/
	public DecalImage getImage() {
		return image;
	}
	
	/**
	*	sets a new image in template
	*	fires change event
	*
	*	@param image	the new image to be used as template
	*/	
	public void setImage(DecalImage image) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setImage(image);
		}
		this.image = image;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	gets the current Edge mode in use
	*
	*	@return	the current edge mode in template
	*/
	public Decal.EdgeMode getEdgeMode() {
		return edgeMode;
	}
	
	/**
	*	sets a new edge mode to be used in template
	*	fires change event
	*
	*	@param edgeMode	the new edgeMode to be used
	*/
	public void setEdgeMode(Decal.EdgeMode edgeMode) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setEdgeMode(edgeMode);
		}
		this.edgeMode = edgeMode;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}
	
	/**
	*	only applies change if there is no more changes coming
	*/
	@Override
	protected void fireChangeEvent() {
		if (!batch)
			super.fireChangeEvent();
	}
	
	/**
	*	function that guarantees that changes event only occurs after all changes are made
	*
	*	param r	the functor to be executed
	*/
	public void batch(Runnable r) {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.batch(r);
		}
		batch = true;
		r.run();
		batch = false;
		if (!bypassAppearanceChangeEvent) {
			fireChangeEvent();
		}
	}

	/**
	 * Add a new config listener that will undergo the same configuration changes as this AppearanceBuilder.
	 * @param component the component to add as a config listener
	 * @param ab new AppearanceBuilder config listener
	 * @return true if listener was successfully added, false if not
	 */
	public boolean addConfigListener(RocketComponent component, AppearanceBuilder ab) {
		if (component == null || ab == null || configListeners.values().contains(ab) || ab == this) {
			return false;
		}
		configListeners.put(component, ab);
		ab.setBypassChangeEvent(true);
		return true;
	}

	public void removeConfigListener(RocketComponent listener) {
		configListeners.remove(listener);
		listener.setBypassChangeEvent(false);
	}

	public void clearConfigListeners() {
		for (AppearanceBuilder listener : configListeners.values()) {
			listener.setBypassChangeEvent(false);
		}
		configListeners.clear();
	}

	public Map<RocketComponent, AppearanceBuilder> getConfigListeners() {
		return configListeners;
	}

	public void setBypassChangeEvent(boolean newValue) {
		this.bypassAppearanceChangeEvent = newValue;
	}
	
}
