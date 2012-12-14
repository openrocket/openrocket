package net.sf.openrocket.appearance;

import net.sf.openrocket.util.Color;

public class SimpleAppearanceBuilder extends AppearanceBuilder {

	public SimpleAppearanceBuilder() {
		super();
	}

	public SimpleAppearanceBuilder(Appearance a) {
		super(a);
	}

	public Color getColor() {
		return getDiffuse();
	}

	public void setColor(final Color c) {
		batch(new Runnable() {
			@Override
			public void run() {
				setAmbient(c);
				setDiffuse(c);
			}
		});
	}

	public void setShine(final int s) {
		batch(new Runnable() {
			@Override
			public void run() {
				setShininess(s);
				int c = (int) (s * 2.55);
				setSpecular(new net.sf.openrocket.util.Color(c, c, c));
			}
		});
	}

	public int getShine() {
		return getShininess();
	}

	private Color oldColor = null;

	@Override
	public void setImage(final String image) {
		batch(new Runnable() {
			@Override
			public void run() {
				if (getImage() == null && image != null) {
					oldColor = getColor();
					setColor(new Color(255, 255, 255));
				} else if (getImage() != null && image == null && oldColor != null) {
					setColor(oldColor);
				}
				SimpleAppearanceBuilder.super.setImage(image);
			}
		});
	}

	public double getScaleX() {
		return 1.0 / super.getScaleU();
	}

	public void setScaleX(double scaleU) {
		super.setScaleU(1.0 / scaleU);
	}

	public double getScaleY() {
		return 1.0 / super.getScaleV();
	}

	public void setScaleY(double scaleV) {
		super.setScaleV(1.0 / scaleV);
	}
}
