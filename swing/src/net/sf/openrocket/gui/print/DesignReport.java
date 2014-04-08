/*
 * DesignReport.java
 */
package net.sf.openrocket.gui.print;

import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.List;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.figureelements.RocketInfo;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * <pre>
 * #  Title # Section describing the rocket in general without motors
 * # Section describing the rocket in general without motors
 * <p/>
 * design name
 * empty mass & CG
 * CP position
 * CP position at 5 degree AOA (or similar)
 * number of stages
 * parachute/streamer sizes
 * max. diameter (caliber)
 * velocity at exit of rail/rod
 * minimum safe velocity reached in x inches/cm
 * <p/>
 * # Section for each motor configuration
 * <p/>
 * a summary of the motors, e.g. 3xC6-0; B4-6
 * a list of the motors including the manufacturer, designation (maybe also info like burn time, grams of propellant,
 * total impulse)
 * total grams of propellant
 * total impulse
 * takeoff weight
 * CG and CP position, stability margin
 * predicted flight altitude, max. velocity and max. acceleration
 * predicted velocity at chute deployment
 * predicted descent rate
 * Thrust to Weight Ratio of each stage
 * <p/>
 * </pre>
 */
public class DesignReport {
	
	/**
	 * The logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(DesignReport.class);
	public static final double SCALE_FUDGE_FACTOR = 0.4d;
	
	private static final RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	/**
	 * The OR Document.
	 */
	private OpenRocketDocument rocketDocument;
	
	/**
	 * A panel used for rendering of the design diagram.
	 */
	final RocketPanel panel;
	
	/**
	 * The iText document.
	 */
	protected Document document;
	
	/**
	 * The figure rotation.
	 */
	private double rotation = 0d;
	
	/** The displayed strings. */
	private static final String STAGES = "Stages: ";
	private static final String MASS_WITH_MOTORS = "Mass (with motors): ";
	private static final String MASS_WITH_MOTOR = "Mass (with motor): ";
	private static final String MASS_EMPTY = "Mass (Empty): ";
	private static final String STABILITY = "Stability: ";
	private static final String CG = "CG: ";
	private static final String CP = "CP: ";
	private static final String MOTOR = "Motor";
	private static final String AVG_THRUST = "Avg Thrust";
	private static final String BURN_TIME = "Burn Time";
	private static final String MAX_THRUST = "Max Thrust";
	private static final String TOTAL_IMPULSE = "Total Impulse";
	private static final String THRUST_TO_WT = "Thrust to Wt";
	private static final String PROPELLANT_WT = "Propellant Wt";
	private static final String SIZE = "Size";
	private static final String ALTITUDE = "Altitude";
	private static final String FLIGHT_TIME = "Flight Time";
	private static final String OPTIMUM_DELAY = "Optimum Delay";
	private static final String TIME_TO_APOGEE = "Time to Apogee";
	private static final String VELOCITY_OFF_PAD = "Velocity off Pad";
	private static final String MAX_VELOCITY = "Max Velocity";
	private static final String DEPLOYMENT_VELOCITY = "Velocity at Deployment";
	private static final String LANDING_VELOCITY = "Landing Velocity";
	private static final String ROCKET_DESIGN = "Rocket Design";
	private static final double GRAVITY_CONSTANT = 9.80665d;
	
	/**
	 * Constructor.
	 *
	 * @param theRocDoc the OR document
	 * @param theIDoc   the iText document
	 * @param figureRotation the angle the figure is rotated on the screen; printed report will mimic
	 */
	public DesignReport(OpenRocketDocument theRocDoc, Document theIDoc, Double figureRotation) {
		document = theIDoc;
		rocketDocument = theRocDoc;
		panel = new RocketPanel(rocketDocument);
		rotation = figureRotation;
	}
	
	/**
	 * Main entry point.  Prints the rocket drawing and design data.
	 *
	 * @param writer a direct byte writer
	 */
	public void writeToDocument(PdfWriter writer) {
		if (writer == null) {
			return;
		}
		com.itextpdf.text.Rectangle pageSize = document.getPageSize();
		int pageImageableWidth = (int) pageSize.getWidth() - (int) pageSize.getBorderWidth() * 2;
		int pageImageableHeight = (int) pageSize.getHeight() / 2 - (int) pageSize.getBorderWidthTop();
		
		PrintUtilities.addText(document, PrintUtilities.BIG_BOLD, ROCKET_DESIGN);
		
		Rocket rocket = rocketDocument.getRocket();
		final Configuration configuration = rocket.getDefaultConfiguration().clone();
		configuration.setAllStages();
		PdfContentByte canvas = writer.getDirectContent();
		
		final PrintFigure figure = new PrintFigure(configuration);
		figure.setRotation(rotation);
		
		FigureElement cp = panel.getExtraCP();
		FigureElement cg = panel.getExtraCG();
		RocketInfo text = panel.getExtraText();
		
		double scale = paintRocketDiagram(pageImageableWidth, pageImageableHeight, canvas, figure, cp, cg);
		
		canvas.beginText();
		canvas.setFontAndSize(ITextHelper.getBaseFont(), PrintUtilities.NORMAL_FONT_SIZE);
		int figHeightPts = (int) (PrintUnit.METERS.toPoints(figure.getFigureHeight()) * 0.4 * (scale / PrintUnit.METERS
				.toPoints(1)));
		final int diagramHeight = pageImageableHeight * 2 - 70 - (figHeightPts);
		canvas.moveText(document.leftMargin() + pageSize.getBorderWidthLeft(), diagramHeight);
		canvas.moveTextWithLeading(0, -16);
		
		float initialY = canvas.getYTLM();
		
		canvas.showText(rocketDocument.getRocket().getName());
		
		canvas.newlineShowText(STAGES);
		canvas.showText("" + rocket.getStageCount());
		
		
		if (configuration.hasMotors()) {
			if (configuration.getStageCount() > 1) {
				canvas.newlineShowText(MASS_WITH_MOTORS);
			} else {
				canvas.newlineShowText(MASS_WITH_MOTOR);
			}
		} else {
			canvas.newlineShowText(MASS_EMPTY);
		}
		canvas.showText(text.getMass(UnitGroup.UNITS_MASS.getDefaultUnit()));
		
		canvas.newlineShowText(STABILITY);
		canvas.showText(text.getStability());
		
		canvas.newlineShowText(CG);
		canvas.showText(text.getCg());
		
		canvas.newlineShowText(CP);
		canvas.showText(text.getCp());
		canvas.endText();
		
		try {
			//Move the internal pointer of the document below that of what was just written using the direct byte buffer.
			Paragraph paragraph = new Paragraph();
			float finalY = canvas.getYTLM();
			int heightOfDiagramAndText = (int) (pageSize.getHeight() - (finalY - initialY + diagramHeight));
			
			paragraph.setSpacingAfter(heightOfDiagramAndText);
			document.add(paragraph);
			
			String[] motorIds = rocket.getFlightConfigurationIDs();
			List<Simulation> simulations = rocketDocument.getSimulations();
			
			for (int j = 0; j < motorIds.length; j++) {
				String motorId = motorIds[j];
				if (motorId != null) {
					PdfPTable parent = new PdfPTable(2);
					parent.setWidthPercentage(100);
					parent.setHorizontalAlignment(Element.ALIGN_LEFT);
					parent.setSpacingBefore(0);
					parent.setWidths(new int[] { 1, 3 });
					int leading = 0;
					//The first motor config is always null.  Skip it and the top-most motor, then set the leading.
					if (j > 1) {
						leading = 25;
					}
					FlightData flight = findSimulation(motorId, simulations);
					addFlightData(flight, rocket, motorId, parent, leading);
					addMotorData(rocket, motorId, parent);
					document.add(parent);
				}
			}
		} catch (DocumentException e) {
			log.error("Could not modify document.", e);
		}
	}
	
	
	/**
	 * Paint a diagram of the rocket into the PDF document.
	 *
	 * @param thePageImageableWidth  the number of points in the width of the page available for drawing
	 * @param thePageImageableHeight the number of points in the height of the page available for drawing
	 * @param theCanvas              the direct byte writer
	 * @param theFigure              the print figure
	 * @param theCp                  the center of pressure figure element
	 * @param theCg                  the center of gravity figure element
	 *
	 * @return the scale of the diagram
	 */
	private double paintRocketDiagram(final int thePageImageableWidth, final int thePageImageableHeight,
			final PdfContentByte theCanvas, final PrintFigure theFigure,
			final FigureElement theCp, final FigureElement theCg) {
		theFigure.clearAbsoluteExtra();
		theFigure.clearRelativeExtra();
		theFigure.addRelativeExtra(theCp);
		theFigure.addRelativeExtra(theCg);
		theFigure.updateFigure();
		
		double scale =
				(thePageImageableWidth * 2.2) / theFigure.getFigureWidth();
		theFigure.setScale(scale);
		/*
		 * page dimensions are in points-per-inch, which, in Java2D, are the same as pixels-per-inch; thus we don't need any conversion
		 */
		theFigure.setSize(thePageImageableWidth, thePageImageableHeight);
		theFigure.updateFigure();
		
		final DefaultFontMapper mapper = new DefaultFontMapper();
		Graphics2D g2d = theCanvas.createGraphics(thePageImageableWidth, thePageImageableHeight * 2, mapper);
		final double halfFigureHeight = SCALE_FUDGE_FACTOR * theFigure.getFigureHeightPx() / 2;
		int y = PrintUnit.POINTS_PER_INCH;
		//If the y dimension is negative, then it will potentially be drawn off the top of the page.  Move the origin
		//to allow for this.
		if (theFigure.getDimensions().getY() < 0.0d) {
			y += (int) halfFigureHeight;
		}
		g2d.translate(20, y);
		
		g2d.scale(SCALE_FUDGE_FACTOR, SCALE_FUDGE_FACTOR);
		theFigure.paint(g2d);
		g2d.dispose();
		return scale;
	}
	
	/**
	 * Add the motor data for a motor configuration to the table.
	 *
	 * @param rocket	the rocket
	 * @param motorId	the motor ID to output
	 * @param parent	the parent to which the motor data will be added
	 */
	private void addMotorData(Rocket rocket, String motorId, final PdfPTable parent) {
		
		PdfPTable motorTable = new PdfPTable(8);
		motorTable.setWidthPercentage(68);
		motorTable.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		final PdfPCell motorCell = ITextHelper.createCell(MOTOR, PdfPCell.BOTTOM, PrintUtilities.SMALL);
		final int mPad = 10;
		motorCell.setPaddingLeft(mPad);
		motorTable.addCell(motorCell);
		motorTable.addCell(ITextHelper.createCell(AVG_THRUST, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(BURN_TIME, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(MAX_THRUST, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(TOTAL_IMPULSE, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(THRUST_TO_WT, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(PROPELLANT_WT, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		motorTable.addCell(ITextHelper.createCell(SIZE, PdfPCell.BOTTOM, PrintUtilities.SMALL));
		
		DecimalFormat ttwFormat = new DecimalFormat("0.00");
		
		MassCalculator massCalc = new BasicMassCalculator();
		
		Configuration config = new Configuration(rocket);
		config.setFlightConfigurationID(motorId);
		
		int totalMotorCount = 0;
		double totalPropMass = 0;
		double totalImpulse = 0;
		double totalTTW = 0;
		
		int stage = 0;
		double stageMass = 0;
		
		boolean topBorder = false;
		for (RocketComponent c : rocket) {
			
			if (c instanceof Stage) {
				config.setToStage(stage);
				stage++;
				stageMass = massCalc.getCG(config, MassCalcType.LAUNCH_MASS).weight;
				// Calculate total thrust-to-weight from only lowest stage motors
				totalTTW = 0;
				topBorder = true;
			}
			
			if (c instanceof MotorMount && ((MotorMount) c).isMotorMount()) {
				MotorMount mount = (MotorMount) c;
				
				if (mount.isMotorMount() && mount.getMotor(motorId) != null) {
					Motor motor = mount.getMotor(motorId);
					int motorCount = c.toAbsolute(Coordinate.NUL).length;
					
					
					int border = Rectangle.NO_BORDER;
					if (topBorder) {
						border = Rectangle.TOP;
						topBorder = false;
					}
					
					String name = motor.getDesignation();
					if (motorCount > 1) {
						name += " (" + Chars.TIMES + motorCount + ")";
					}
					
					final PdfPCell motorVCell = ITextHelper.createCell(name, border);
					motorVCell.setPaddingLeft(mPad);
					motorTable.addCell(motorVCell);
					motorTable.addCell(ITextHelper.createCell(
							UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(motor.getAverageThrustEstimate()), border));
					motorTable.addCell(ITextHelper.createCell(
							UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit().toStringUnit(motor.getBurnTimeEstimate()), border));
					motorTable.addCell(ITextHelper.createCell(
							UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(motor.getMaxThrustEstimate()), border));
					motorTable.addCell(ITextHelper.createCell(
							UnitGroup.UNITS_IMPULSE.getDefaultUnit().toStringUnit(motor.getTotalImpulseEstimate()), border));
					
					double ttw = motor.getAverageThrustEstimate() / (stageMass * GRAVITY_CONSTANT);
					motorTable.addCell(ITextHelper.createCell(
							ttwFormat.format(ttw) + ":1", border));
					
					double propMass = (motor.getLaunchCG().weight - motor.getEmptyCG().weight);
					motorTable.addCell(ITextHelper.createCell(
							UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(propMass), border));
					
					final Unit motorUnit = UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit();
					motorTable.addCell(ITextHelper.createCell(motorUnit.toString(motor.getDiameter()) +
							"/" +
							motorUnit.toString(motor.getLength()) + " " +
							motorUnit.toString(), border));
					
					// Sum up total count
					totalMotorCount += motorCount;
					totalPropMass += propMass * motorCount;
					totalImpulse += motor.getTotalImpulseEstimate() * motorCount;
					totalTTW += ttw * motorCount;
				}
			}
		}
		
		if (totalMotorCount > 1) {
			int border = Rectangle.TOP;
			final PdfPCell motorVCell = ITextHelper.createCell("Total:", border);
			motorVCell.setPaddingLeft(mPad);
			motorTable.addCell(motorVCell);
			motorTable.addCell(ITextHelper.createCell("", border));
			motorTable.addCell(ITextHelper.createCell("", border));
			motorTable.addCell(ITextHelper.createCell("", border));
			motorTable.addCell(ITextHelper.createCell(
					UnitGroup.UNITS_IMPULSE.getDefaultUnit().toStringUnit(totalImpulse), border));
			motorTable.addCell(ITextHelper.createCell(
					ttwFormat.format(totalTTW) + ":1", border));
			motorTable.addCell(ITextHelper.createCell(
					UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(totalPropMass), border));
			motorTable.addCell(ITextHelper.createCell("", border));
			
		}
		
		PdfPCell c = new PdfPCell(motorTable);
		c.setBorder(PdfPCell.LEFT);
		c.setBorderWidthTop(0f);
		parent.addCell(c);
		config.release();
	}
	
	
	/**
	 * Add the flight data for a simulation configuration to the table.
	 *
	 * @param flight    the flight data for a single simulation
	 * @param theRocket the rocket
	 * @param motorId   a motor configuration id
	 * @param parent    the parent to which the simulation flight data will be added
	 * @param leading   the number of points for the leading
	 */
	private void addFlightData(final FlightData flight, final Rocket theRocket, final String motorId, final PdfPTable parent, int leading) {
		
		// Output the flight data
		if (flight != null) {
			try {
				final Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
				final Unit velocityUnit = UnitGroup.UNITS_VELOCITY.getDefaultUnit();
				final Unit flightTimeUnit = UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit();
				
				PdfPTable labelTable = new PdfPTable(2);
				labelTable.setWidths(new int[] { 3, 2 });
				final Paragraph chunk = ITextHelper.createParagraph(stripBrackets(
						descriptor.format(theRocket, motorId)), PrintUtilities.BOLD);
				chunk.setLeading(leading);
				chunk.setSpacingAfter(3f);
				
				document.add(chunk);
				
				final PdfPCell cell = ITextHelper.createCell(ALTITUDE, 2, 2);
				cell.setUseBorderPadding(false);
				cell.setBorderWidthTop(0f);
				labelTable.addCell(cell);
				labelTable.addCell(ITextHelper.createCell(distanceUnit.toStringUnit(flight.getMaxAltitude()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(FLIGHT_TIME, 2, 2));
				labelTable.addCell(ITextHelper.createCell(flightTimeUnit.toStringUnit(flight.getFlightTime()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(TIME_TO_APOGEE, 2, 2));
				labelTable.addCell(ITextHelper.createCell(flightTimeUnit.toStringUnit(flight.getTimeToApogee()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(OPTIMUM_DELAY, 2, 2));
				labelTable.addCell(ITextHelper.createCell(flightTimeUnit.toStringUnit(flight.getBranch(0).getOptimumDelay()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(VELOCITY_OFF_PAD, 2, 2));
				labelTable.addCell(ITextHelper.createCell(velocityUnit.toStringUnit(flight.getLaunchRodVelocity()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(MAX_VELOCITY, 2, 2));
				labelTable.addCell(ITextHelper.createCell(velocityUnit.toStringUnit(flight.getMaxVelocity()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(DEPLOYMENT_VELOCITY, 2, 2));
				labelTable.addCell(ITextHelper.createCell(velocityUnit.toStringUnit(flight.getDeploymentVelocity()), 2, 2));
				
				labelTable.addCell(ITextHelper.createCell(LANDING_VELOCITY, 2, 2));
				labelTable.addCell(ITextHelper.createCell(velocityUnit.toStringUnit(flight.getGroundHitVelocity()), 2, 2));
				
				//Add the table to the parent; have to wrap it in a cell
				PdfPCell c = new PdfPCell(labelTable);
				c.setBorder(PdfPCell.RIGHT);
				c.setBorderWidthTop(0);
				c.setTop(0);
				parent.addCell(c);
			} catch (DocumentException e) {
				log.error("Could not add flight data to document.", e);
			}
		}
	}
	
	/**
	 * Locate the simulation based on the motor id.  Copy the simulation and execute it, then return the resulting
	 * flight data.
	 *
	 * @param motorId     the motor id corresponding to the simulation to find
	 * @param simulations the list of simulations currently associated with the rocket
	 *
	 * @return the flight data from the simulation for the specified motor id, or null if not found
	 */
	private FlightData findSimulation(final String motorId, List<Simulation> simulations) {
		// Perform flight simulation
		FlightData flight = null;
		try {
			for (int i = 0; i < simulations.size(); i++) {
				Simulation simulation = simulations.get(i);
				if (Utils.equals(simulation.getOptions().getMotorConfigurationID(), motorId)) {
					simulation = simulation.copy();
					simulation.simulate();
					flight = simulation.getSimulatedData();
					break;
				}
			}
		} catch (SimulationException e1) {
			// Ignore
		}
		return flight;
	}
	
	/**
	 * Strip [] brackets from a string.
	 *
	 * @param target the original string
	 *
	 * @return target with [] removed
	 */
	private String stripBrackets(String target) {
		return stripLeftBracket(stripRightBracket(target));
	}
	
	/**
	 * Strip [ from a string.
	 *
	 * @param target the original string
	 *
	 * @return target with [ removed
	 */
	private String stripLeftBracket(String target) {
		return target.replace("[", "");
	}
	
	/**
	 * Strip ] from a string.
	 *
	 * @param target the original string
	 *
	 * @return target with ] removed
	 */
	private String stripRightBracket(String target) {
		return target.replace("]", "");
	}
	
}
