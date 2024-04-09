package info.openrocket.swing.gui.figureelements;

import static info.openrocket.core.util.Chars.ALPHA;
import static info.openrocket.core.util.Chars.THETA;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.MathUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;


/**
 * A <code>FigureElement</code> that draws text at different positions in the figure
 * with general data about the rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketInfo implements FigureElement {
	
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	// Margin around the figure edges, pixels
	private static final int MARGIN = 8;

	// Font to use
	private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	private Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
	
	private final Caret cpCaret = new CPCaret(0,0);
	private final Caret cgCaret = new CGCaret(0,0);
	
	private UnitGroup.StabilityUnitGroup stabilityUnits;
	private UnitGroup.StabilityUnitGroup secondaryStabilityUnits;
	
	private FlightConfiguration configuration;
	private double cg = 0, cp = 0;
	private double length = 0, diameter = 0;
	private double massWithMotors = 0;
	private double aoa = Double.NaN;
	private double theta = Double.NaN;
	private double mach = Application.getPreferences().getDefaultMach();
	private double massWithoutMotors = 0;
	
	private WarningSet warnings = null;
	private boolean showWarnings = true;
	
	private boolean calculatingData = false;
	private FlightData flightData = null;
	
	private Graphics2D g2 = null;
	private float line = 0;
	private float x1, x2, y1, y2;

	private static Color textColor;
	private static Color dimTextColor;
	private static Color darkErrorColor;
	private static Color flightDataTextActiveColor;
	private static Color flightDataTextInactiveColor;

	static {
		initColors();
	}
	
	public RocketInfo(FlightConfiguration configuration) {
		this.configuration = configuration;
		this.stabilityUnits = UnitGroup.stabilityUnits(configuration);
		this.secondaryStabilityUnits = UnitGroup.secondaryStabilityUnits(configuration);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(RocketInfo::updateColors);
	}

	private static void updateColors() {
		textColor = GUIUtil.getUITheme().getTextColor();
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
		darkErrorColor = GUIUtil.getUITheme().getErrorColor();
		flightDataTextActiveColor = GUIUtil.getUITheme().getFlightDataTextActiveColor();
		flightDataTextInactiveColor = GUIUtil.getUITheme().getFlightDataTextInactiveColor();
	}
	
	
	@Override
	public void paint(Graphics2D myG2, double scale) {
		throw new UnsupportedOperationException("paint() must be called with coordinates");
	}

	@Override
	public void paint(Graphics2D myG2, double scale, Rectangle visible) {
		this.g2 = myG2;
		
		this.updateFontSizes();
		
		this.line = font.getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
				myG2.getFontRenderContext()).getHeight() + 
				font.getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
						myG2.getFontRenderContext()).getDescent();
		
		x1 = visible.x + MARGIN;
		x2 = visible.x + visible.width - MARGIN;
		y1 = visible.y + line ;
		y2 = visible.y + visible.height - MARGIN;

		drawMainInfo();
		drawStabilityInfo();
		drawWarnings();
		drawFlightInformation();
	}
	
	
	public void setCG(double cg) {
		this.cg = cg;
	}
	
	public void setCP(double cp) {
		this.cp = cp;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}
	
	public void setMassWithMotors(double mass) {
		this.massWithMotors = mass;
	}
	
	public void setMassWithoutMotors(double mass) {
		this.massWithoutMotors = mass;
	}
	
	public void setWarnings(WarningSet warnings) {
		this.warnings = warnings.clone();
	}

	/**
	 * Set whether warnings should be shown. If false, the warnings are not shown.
	 * @param showWarnings whether to show warnings.
	 */
	public void setShowWarnings(boolean showWarnings) {
		this.showWarnings = showWarnings;
	}

	public void setAOA(double aoa) {
		this.aoa = aoa;
	}
	
	public void setTheta(double theta) {
		this.theta = theta;
	}
	
	public void setMach(double mach) {
		this.mach = mach;
	}
	
	
	public void setFlightData(FlightData data) {
		this.flightData = data;
	}
	
	public void setCalculatingData(boolean calc) {
		this.calculatingData = calc;
	}
	
	private void drawMainInfo() {
		GlyphVector name = createText(configuration.getRocket().getName());
		GlyphVector lengthLine = createText(
				//// Length
				trans.get("RocketInfo.lengthLine.Length") +" " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(length) +
				//// , max. diameter
				trans.get("RocketInfo.lengthLine.maxdiameter") +" " + 
				UnitGroup.UNITS_LENGTH.getDefaultUnit().toStringUnit(diameter));
		
		String massTextWithMotors;
		String massTextWithoutMotors;

		/// Mass with no motors
		massTextWithoutMotors = trans.get("RocketInfo.massWithoutMotors") +" ";
		massTextWithoutMotors += UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(massWithoutMotors);

		GlyphVector massLineWithoutMotors = createText(massTextWithoutMotors);

		g2.setColor(textColor);

		g2.drawGlyphVector(name, x1, y1);
		g2.drawGlyphVector(lengthLine, x1, y1+line);
		g2.drawGlyphVector(massLineWithoutMotors, x1, y1+2*line);

		if( configuration.hasMotors() ) {
			//// Mass with motors
			massTextWithMotors = trans.get("RocketInfo.massWithMotors") + " ";
			massTextWithMotors += UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(massWithMotors);
			GlyphVector massLineWithMotors = createText(massTextWithMotors);
			g2.drawGlyphVector(massLineWithMotors, x1, y1+3*line);
		}
	}
	
	
	private void drawStabilityInfo() {
		GlyphVector cgValue = createText(getCg());
		GlyphVector cpValue = createText(getCp());
		GlyphVector stabValue = createText(getStabilityCombined());
		
		//// CG:		
		GlyphVector cgText = createText(trans.get("RocketInfo.cgText"));
		//// CP:
		GlyphVector cpText = createText(trans.get("RocketInfo.cpText"));
		//// Stability:
		GlyphVector stabText = createText(trans.get("RocketInfo.stabText"));

		//// at M=...
		String at = trans.get("RocketInfo.at")+UnitGroup.UNITS_COEFFICIENT.getDefaultUnit().toStringUnit(this.mach);
		if (!Double.isNaN(aoa)) {
			at += " "+ALPHA+"=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(aoa);
		}
		if (!Double.isNaN(theta)) {
			at += " "+THETA+"=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(theta);
		}
		GlyphVector atText = createSmallText(at);
		
		// GlyphVector visual bounds drops the spaces, so we'll add them
		FontMetrics fontMetrics = g2.getFontMetrics(cgText.getFont());
		int spaceWidth = fontMetrics.stringWidth(" ");

		Rectangle2D cgRect = cgValue.getVisualBounds();
		Rectangle2D cpRect = cpValue.getVisualBounds();
		Rectangle2D cgTextRect = cgText.getVisualBounds();
		Rectangle2D cpTextRect = cpText.getVisualBounds();
		Rectangle2D stabRect = stabValue.getVisualBounds();
		Rectangle2D stabTextRect = stabText.getVisualBounds();
		Rectangle2D atTextRect = atText.getVisualBounds();
		
		double unitWidth = MathUtil.max(cpRect.getWidth(), cgRect.getWidth());
		double stabUnitWidth = stabRect.getWidth();
		double textWidth = Math.max(cpTextRect.getWidth(), cgTextRect.getWidth());
		
		// Add an extra space worth of width so the text doesn't run into the values
		unitWidth = unitWidth + spaceWidth;
		stabUnitWidth = stabUnitWidth + spaceWidth;

		g2.setColor(textColor);

		// Draw the stability, CG & CP values (and units)
		g2.drawGlyphVector(stabValue, (float)(x2-stabRect.getWidth()), y1);
		g2.drawGlyphVector(cgValue, (float)(x2-cgRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpValue, (float)(x2-cpRect.getWidth()), y1+2*line);

		// Draw the stability, CG & CP labels
		g2.drawGlyphVector(stabText, (float)(x2-stabUnitWidth-stabTextRect.getWidth()), y1);
		g2.drawGlyphVector(cgText, (float)(x2-unitWidth-cgTextRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpText, (float)(x2-unitWidth-cpTextRect.getWidth()), y1+2*line);

		// Draw the CG caret
		cgCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+line-0.3*line);
		cgCaret.paint(g2, 1.7);

		// Draw the CP caret
		cpCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+2*line-0.3*line);
		cpCaret.paint(g2, 1.7);
		
		float atPos;
		if (unitWidth + textWidth + 10 > atTextRect.getWidth()) {
			atPos = (float)(x2-(unitWidth+textWidth+10+atTextRect.getWidth())/2);
		} else {
			atPos = (float)(x2 - atTextRect.getWidth());
		}
		
		g2.setColor(dimTextColor);
		g2.drawGlyphVector(atText, atPos, y1 + 3*line);

	}

    /**
     * Get the mass, in default mass units.
     * 
     * @return the mass
     */
    public double getMassWithMotors() {
        return massWithMotors;
    }

    /**
     * Get the mass in specified mass units.
     * 
     * @param u UnitGroup.MASS
     * 
     * @return the mass
     */
    public String getMassWithMotors(Unit u) {
        return u.toStringUnit(massWithMotors);
    }


    /**
     * Get the stability in both the selected stability unit and in percentage, e.g. "2.4 cal (14.1 %)".
	 * If the current unit is already the percentage length unit, only use that.
     * 
     * @return the current stability margin in the currently selected stability unit and in percentage
     */
    public String getStabilityCombined() {
		Unit stabilityUnit = stabilityUnits.getDefaultUnit();
		Unit secondaryStabilityUnit = secondaryStabilityUnits.getDefaultUnit();

		String stabilityStr = getStability();

		// Don't display secondary units if the stability is NaN, or if the secondary unit is the same as the primary unit,
		// or if it is disabled in the preferences
		if (Double.isNaN(getStabilityValue()) || secondaryStabilityUnit.equals(stabilityUnit) ||
				!preferences.isDisplaySecondaryStability()) {
			return stabilityStr;
		}

		String secondaryStabilityStr = getSecondaryStability();

		return stabilityStr + " / " + secondaryStabilityStr;
    }

	/**
	 * Get the stability in the currently selected unit.
	 * @return the current stability margin in the currently selected stability unit
	 */
	private String getStability() {
		return stabilityUnits.getDefaultUnit().toStringUnit(getStabilityValue());
	}

	/**
	 * Get the stability in the secondary stability unit.
	 * @return the current stability margin in the secondary stability unit
	 */
	private String getSecondaryStability() {
		return secondaryStabilityUnits.getDefaultUnit().toStringUnit(getStabilityValue());
	}

	private double getStabilityValue() {
		return cp - cg;
	}

    /**
     * Get the center of pressure in default length units.
     * 
     * @return  the distance from the tip to the center of pressure, in default length units
     */
    public String getCp () {
        return getCp(UnitGroup.UNITS_LENGTH.getDefaultUnit());
    }

    /**
     * Get the center of pressure in default length units.
     * 
     * @param u UnitGroup.LENGTH 
     * 
     * @return  the distance from the tip to the center of pressure, in default length units
     */
    public String getCp (Unit u) {
        return u.toStringUnit(cp);
    }

    /**
     * Get the center of gravity in default length units.
     * 
     * @return  the distance from the tip to the center of gravity, in default length units
     */
    public String getCg () {
        return getCg(UnitGroup.UNITS_LENGTH.getDefaultUnit());
    }

    /**
     * Get the center of gravity in specified length units.
     * 
     * @param u UnitGroup.LENGTH 
     * @return  the distance from the tip to the center of gravity, in specified units
     */
    public String getCg (Unit u) {
        return u.toStringUnit(cg);
    }

    /**
     * Get the flight data for the current motor configuration.
     * 
     * @return flight data, or null
     */
    public FlightData getFlightData () {
        return flightData;
    }
    
    private void drawWarnings() {
		if (warnings == null || warnings.isEmpty())
			return;
		
		final GlyphVector[] texts;
		double max = 0;

		if (showWarnings) {
			texts = new GlyphVector[warnings.size()+1];

			//// Warning:
			texts[0] = createText(trans.get("RocketInfo.Warning"));
			int i = 1;
			for (Warning w : warnings) {
				texts[i] = createText(w.toString());
				i++;
			}

			for (GlyphVector v : texts) {
				Rectangle2D rect = v.getVisualBounds();
				if (rect.getWidth() > max)
					max = rect.getWidth();
			}
		} else {
			texts = new GlyphVector[1];
			texts[0] = createText(String.format(trans.get("RocketInfo.lbl.warnings"), warnings.size()));
			Rectangle2D rect = texts[0].getVisualBounds();
			max = rect.getWidth();
		}
		

		float y = y2 - line * (texts.length-1);
		g2.setColor(darkErrorColor);

		for (GlyphVector v: texts) {
			Rectangle2D rect = v.getVisualBounds();
			g2.drawGlyphVector(v, (float)(x2 - max/2 - rect.getWidth()/2), y);
			y += line;
		}
	}
	
	
	private void drawFlightInformation() {
		double height = drawFlightData();
		
		if (calculatingData) {
			//// Calculating...
			GlyphVector calculating = createText(trans.get("RocketInfo.Calculating"));
			g2.setColor(textColor);
			g2.drawGlyphVector(calculating, x1, (float)(y2-height));
		}
	}
	
	
	private double drawFlightData() {
		if (flightData == null)
			return 0;
		
		double width=0;
		
		//// Apogee: 
		GlyphVector apogee = createText(trans.get("RocketInfo.Apogee")+" ");
		//// Max. velocity:
		GlyphVector maxVelocity = createText(trans.get("RocketInfo.Maxvelocity") +" ");
		//// Max. acceleration: 
		GlyphVector maxAcceleration = createText(trans.get("RocketInfo.Maxacceleration") + " ");

		GlyphVector apogeeValue, velocityValue, accelerationValue;
		if (!Double.isNaN(flightData.getMaxAltitude())) {
			apogeeValue = createText(
					UnitGroup.UNITS_DISTANCE.toStringUnit(flightData.getMaxAltitude()));
		} else {
			//// N/A
			apogeeValue = createText(trans.get("RocketInfo.apogeeValue"));
		}
		if (!Double.isNaN(flightData.getMaxVelocity())) {
			velocityValue = createText(
					UnitGroup.UNITS_VELOCITY.toStringUnit(flightData.getMaxVelocity()) +
					//// (Mach
					"  " +trans.get("RocketInfo.Mach") +" " + 
					UnitGroup.UNITS_COEFFICIENT.toString(flightData.getMaxMachNumber()) + ")");
		} else {
			//// N/A
			velocityValue = createText(trans.get("RocketInfo.velocityValue"));
		}
		if (!Double.isNaN(flightData.getMaxAcceleration())) {
			accelerationValue = createText(
					UnitGroup.UNITS_ACCELERATION.toStringUnit(flightData.getMaxAcceleration()));
		} else {
			//// N/A
			accelerationValue = createText(trans.get("RocketInfo.accelerationValue"));
		}
		
		Rectangle2D rect;
		rect = apogee.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		rect = maxVelocity.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		rect = maxAcceleration.getVisualBounds();
		width = MathUtil.max(width, rect.getWidth());
		
		width += 5;

		if (!calculatingData) 
			g2.setColor(flightDataTextActiveColor);
		else
			g2.setColor(flightDataTextInactiveColor);

		g2.drawGlyphVector(apogee, x1, y2-2*line);
		g2.drawGlyphVector(maxVelocity, x1, y2-line);
		g2.drawGlyphVector(maxAcceleration, x1, y2);

		g2.drawGlyphVector(apogeeValue, (float)(x1+width), y2-2*line);
		g2.drawGlyphVector(velocityValue, (float)(x1+width), y2-line);
		g2.drawGlyphVector(accelerationValue, (float)(x1+width), y2);
		
		return 3*line;
	}
	
	private synchronized void updateFontSizes() {
		float size = ((SwingPreferences) Application.getPreferences()).getRocketInfoFontSize();
		// No change necessary as the font is the same size, just use the existing version
		if (font.getSize2D() == size) {
			return;
		}
		// Update the font sizes to whatever the currently selected font size is
		font = font.deriveFont(size);
		smallFont = smallFont.deriveFont((float)(size - 2.0));
	}
		
	private GlyphVector createText(String text) {
		return font.createGlyphVector(g2.getFontRenderContext(), text);
	}

	private GlyphVector createSmallText(String text) {
		return smallFont.createGlyphVector(g2.getFontRenderContext(), text);
	}
	
	public void setCurrentConfig(FlightConfiguration newConfig) {
		this.configuration = newConfig;
		this.stabilityUnits = UnitGroup.stabilityUnits(newConfig);
		this.secondaryStabilityUnits = UnitGroup.secondaryStabilityUnits(newConfig);
	}
}
