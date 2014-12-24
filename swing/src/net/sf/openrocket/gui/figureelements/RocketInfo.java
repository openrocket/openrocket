package net.sf.openrocket.gui.figureelements;

import static net.sf.openrocket.util.Chars.ALPHA;
import static net.sf.openrocket.util.Chars.THETA;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;


/**
 * A <code>FigureElement</code> that draws text at different positions in the figure
 * with general data about the rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketInfo implements FigureElement {
	
	private static final Translator trans = Application.getTranslator();
	// Margin around the figure edges, pixels
	private static final int MARGIN = 8;

	// Font to use
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	private static final Font SMALLFONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

	
	private final Caret cpCaret = new CPCaret(0,0);
	private final Caret cgCaret = new CGCaret(0,0);
	
	private final Configuration configuration;
	private final UnitGroup stabilityUnits;
	
	private double cg = 0, cp = 0;
	private double length = 0, diameter = 0;
	private double mass = 0;
	private double aoa = Double.NaN, theta = Double.NaN, mach = Application.getPreferences().getDefaultMach();
	
	private WarningSet warnings = null;
	
	private boolean calculatingData = false;
	private FlightData flightData = null;
	
	private Graphics2D g2 = null;
	private float line = 0;
	private float x1, x2, y1, y2;
	
	
	
	
	
	public RocketInfo(Configuration configuration) {
		this.configuration = configuration;
		this.stabilityUnits = UnitGroup.stabilityUnits(configuration);
	}
	
	
	@Override
	public void paint(Graphics2D myG2, double scale) {
		throw new UnsupportedOperationException("paint() must be called with coordinates");
	}

	@Override
	public void paint(Graphics2D myG2, double scale, Rectangle visible) {
		this.g2 = myG2;
		this.line = FONT.getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
				myG2.getFontRenderContext()).getHeight() + 
				FONT.getLineMetrics("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
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
	
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public void setWarnings(WarningSet warnings) {
		this.warnings = warnings.clone();
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
		
		String massText;
		if (configuration.hasMotors())
			//// Mass with motors 
			massText = trans.get("RocketInfo.massText1") +" ";
		else
			//// Mass with no motors 
			massText = trans.get("RocketInfo.massText2") +" ";
		
		massText += UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(mass);
		
		GlyphVector massLine = createText(massText);

		
		g2.setColor(Color.BLACK);

		g2.drawGlyphVector(name, x1, y1);
		g2.drawGlyphVector(lengthLine, x1, y1+line);
		g2.drawGlyphVector(massLine, x1, y1+2*line);

	}
	
	
	private void drawStabilityInfo() {
		String at;
		//// at M=
		at = trans.get("RocketInfo.at")+UnitGroup.UNITS_COEFFICIENT.getDefaultUnit().toStringUnit(Application.getPreferences().getDefaultMach());
		if (!Double.isNaN(aoa)) {
			at += " "+ALPHA+"=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(aoa);
		}
		if (!Double.isNaN(theta)) {
			at += " "+THETA+"=" + UnitGroup.UNITS_ANGLE.getDefaultUnit().toStringUnit(theta);
		}
		
		GlyphVector cgValue = createText(
                getCg());
		GlyphVector cpValue = createText(
                getCp());
		GlyphVector stabValue = createText(
                getStability());
		//// CG:		
		GlyphVector cgText = createText(trans.get("RocketInfo.cgText") +"  ");
		//// CP:
		GlyphVector cpText = createText(trans.get("RocketInfo.cpText") +"  ");
		//// Stability:
		GlyphVector stabText = createText(trans.get("RocketInfo.stabText") + "  ");
		GlyphVector atText = createSmallText(at);

		Rectangle2D cgRect = cgValue.getVisualBounds();
		Rectangle2D cpRect = cpValue.getVisualBounds();
		Rectangle2D cgTextRect = cgText.getVisualBounds();
		Rectangle2D cpTextRect = cpText.getVisualBounds();
		Rectangle2D stabRect = stabValue.getVisualBounds();
		Rectangle2D stabTextRect = stabText.getVisualBounds();
		Rectangle2D atTextRect = atText.getVisualBounds();
		
		double unitWidth = MathUtil.max(cpRect.getWidth(), cgRect.getWidth(),
				stabRect.getWidth());
		double textWidth = Math.max(cpTextRect.getWidth(), cgTextRect.getWidth());
		

		g2.setColor(Color.BLACK);

		g2.drawGlyphVector(stabValue, (float)(x2-stabRect.getWidth()), y1);
		g2.drawGlyphVector(cgValue, (float)(x2-cgRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpValue, (float)(x2-cpRect.getWidth()), y1+2*line);

		g2.drawGlyphVector(stabText, (float)(x2-unitWidth-stabTextRect.getWidth()), y1);
		g2.drawGlyphVector(cgText, (float)(x2-unitWidth-cgTextRect.getWidth()), y1+line);
		g2.drawGlyphVector(cpText, (float)(x2-unitWidth-cpTextRect.getWidth()), y1+2*line);
				
		cgCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+line-0.3*line);
		cgCaret.paint(g2, 1.7);

		cpCaret.setPosition(x2 - unitWidth - textWidth - 10, y1+2*line-0.3*line);
		cpCaret.paint(g2, 1.7);
		
		float atPos;
		if (unitWidth + textWidth + 10 > atTextRect.getWidth()) {
			atPos = (float)(x2-(unitWidth+textWidth+10+atTextRect.getWidth())/2);
		} else {
			atPos = (float)(x2 - atTextRect.getWidth());
		}
		
		g2.setColor(Color.GRAY);
		g2.drawGlyphVector(atText, atPos, y1 + 3*line);

	}

    /**
     * Get the mass, in default mass units.
     * 
     * @return the mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * Get the mass in specified mass units.
     * 
     * @param u UnitGroup.MASS
     * 
     * @return the mass
     */
    public String getMass(Unit u) {
        return u.toStringUnit(mass);
    }
    
    /**
     * Get the stability, in calibers.
     * 
     * @return  the current stability margin
     */
    public String getStability () {
        return stabilityUnits.getDefaultUnit().toStringUnit(cp-cg);
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
		
		GlyphVector[] texts = new GlyphVector[warnings.size()+1];
		double max = 0;
		
		//// Warning:
		texts[0] = createText(trans.get("RocketInfo.Warning"));
		int i=1;
		for (Warning w: warnings) {
			texts[i] = createText(w.toString());
			i++;
		}
		
		for (GlyphVector v: texts) {
			Rectangle2D rect = v.getVisualBounds();
			if (rect.getWidth() > max)
				max = rect.getWidth();
		}
		

		float y = y2 - line * warnings.size();
		g2.setColor(new Color(255,0,0,130));

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
			g2.setColor(Color.BLACK);
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
			g2.setColor(new Color(0,0,127));
		else
			g2.setColor(new Color(0,0,127,127));

		
		g2.drawGlyphVector(apogee, (float)x1, (float)(y2-2*line));
		g2.drawGlyphVector(maxVelocity, (float)x1, (float)(y2-line));
		g2.drawGlyphVector(maxAcceleration, (float)x1, (float)(y2));

		g2.drawGlyphVector(apogeeValue, (float)(x1+width), (float)(y2-2*line));
		g2.drawGlyphVector(velocityValue, (float)(x1+width), (float)(y2-line));
		g2.drawGlyphVector(accelerationValue, (float)(x1+width), (float)(y2));
		
		return 3*line;
	}
	
	
	
	private GlyphVector createText(String text) {
		float size=Application.getPreferences().getRocketInfoFontSize();
		return (FONT.deriveFont(size)).createGlyphVector(g2.getFontRenderContext(), text);
	}

	private GlyphVector createSmallText(String text) {
		float size=(float) (Application.getPreferences().getRocketInfoFontSize()-2.0);
		return (SMALLFONT.deriveFont(size)).createGlyphVector(g2.getFontRenderContext(), text);
	}

}
