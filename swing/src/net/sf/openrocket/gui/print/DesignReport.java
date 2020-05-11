/*
 * DesignReport.java
 */
package net.sf.openrocket.gui.print;

import java.awt.Graphics2D;
import java.awt.Window;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.figureelements.RocketInfo;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.gui.simulation.SimulationRunDialog;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.RigidBody;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Utils;

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
	
	/**
	 * Determines whether or not to run out of date simulations.
	 */
	private boolean runOutOfDateSimulations = true;
	
	/**
	 * Determines whether or not to update existing simulations.
	 */
	private boolean updateExistingSimulations = false;
	
	/**
	 * Parent window for showing simulation run dialog as necessary
	 */
	private Window window = null;
	
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
	 * Creates a new DesignReport in the iTextPDF Document based on the
	 * OpenRocketDocument specified. All out of date simulations will be
	 * run as part of generating the iTextPDF report.
	 * 
	 * This is for backwards API compatibility and will copy existing
	 * simulations before running them.
	 * 
	 * @param theRocDoc the OpenRocketDocument which serves as the source
	 *                  of the rocket information
	 * @param theIDoc the iTextPDF Document where the DesignReport is written
	 * @param figureRotation the rotation of the figure used for displaying
	 *        the profile view.
	 */
	public DesignReport(OpenRocketDocument theRocDoc, Document theIDoc, Double figureRotation) {
		this(theRocDoc, theIDoc, figureRotation, true, false, null);
	}
	
	/**
	 * Creates a new DesignReport in the iTextPDF Document based on the
	 * OpenRocketDocument specified. Out of date simulations will be run
	 * when the runOutOfDateSims parameter is set to true.
	 *
	 * @param theRocDoc the OR document
	 * @param theIDoc   the iText document
	 * @param figureRotation the angle the figure is rotated on the screen; printed report will mimic
	 * @param runOutOfDateSims whether or not to run simulations that are not up to date.
	 * @param updateExistingSims whether or not to update existing simulations or to copy the simulations.
	 *                           Previous behavior was to copy existing simulations.
	 * @param window the base AWT window to use 
	 */
	public DesignReport(OpenRocketDocument theRocDoc, Document theIDoc, Double figureRotation,
	                    boolean runOutOfDateSims, boolean updateExistingSims, Window window) {
		document = theIDoc;
		rocketDocument = theRocDoc;
		panel = new RocketPanel(rocketDocument);
		rotation = figureRotation;
		this.runOutOfDateSimulations = runOutOfDateSims;
		this.updateExistingSimulations = updateExistingSims;
		this.window = window;
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
		final FlightConfiguration configuration = rocket.getSelectedConfiguration();
		configuration.setAllStages();
		PdfContentByte canvas = writer.getDirectContent();
		
		final PrintFigure figure = new PrintFigure(rocket);
		figure.setRotation(rotation);
		
		FigureElement cp = panel.getExtraCP();
		FigureElement cg = panel.getExtraCG();
		RocketInfo text = panel.getExtraText();
		
		double scale = paintRocketDiagram(pageImageableWidth, pageImageableHeight, canvas, figure, cp, cg);
		
		canvas.beginText();
		canvas.setFontAndSize(ITextHelper.getBaseFont(), PrintUtilities.NORMAL_FONT_SIZE);
		double figureHeightInPoints = PrintUnit.METERS.toPoints(figure.getFigureHeight());
		int figHeightPts = (int) (figureHeightInPoints * SCALE_FUDGE_FACTOR * (scale / PrintUnit.METERS.toPoints(1)));
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
			/*
			 * Move the internal pointer of the document below the rocket diagram and
			 * the key attributes. The height of the rocket figure is already calculated
			 * as diagramHeigt and the height of the attributes text is finalY - initialY.
			 */
			Paragraph paragraph = new Paragraph();
			float finalY = canvas.getYTLM();
			int heightOfDiagramAndText = (int) (pageSize.getHeight() - (finalY - initialY + diagramHeight));
			
			paragraph.setSpacingAfter(heightOfDiagramAndText);
			document.add(paragraph);
			
			List<Simulation> simulations = getSimulations();
			
			boolean firstMotor = true;
			for (FlightConfigurationId fcid : rocket.getIds()) {
				PdfPTable parent = new PdfPTable(2);
				parent.setWidthPercentage(100);
				parent.setHorizontalAlignment(Element.ALIGN_LEFT);
				parent.setSpacingBefore(0);
				parent.setWidths(new int[] { 1, 3 });
				
				/* The first motor information will get no spacing
				 * before it, while each subsequent table will need
				 * a spacing of 25.
				 */
				int leading = (firstMotor) ? 0 : 25;
				
				FlightData flight = findSimulation(fcid, simulations);
				addFlightData(flight, rocket, fcid, parent, leading);
				addMotorData(rocket, fcid, parent);
				document.add(parent);
					
				firstMotor = false;
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
		/* Conveniently, page dimensions are in points-per-inch, which, in
		 * Java2D, are the same as pixels-per-inch; thus we don't need any
		 * conversion for the figure size.
		 */
		theFigure.setSize(thePageImageableWidth, thePageImageableHeight);
		theFigure.updateFigure();
		
		final DefaultFontMapper mapper = new DefaultFontMapper();
		Graphics2D g2d = theCanvas.createGraphics(thePageImageableWidth, thePageImageableHeight * 2, mapper);
		final double halfFigureHeight = SCALE_FUDGE_FACTOR * theFigure.getFigureHeight() / 2;
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
	private void addMotorData(Rocket rocket, FlightConfigurationId motorId, final PdfPTable parent) {
		
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
		
		if( motorId.hasError() ){
		    throw new IllegalStateException("Attempted to add motor data with an invalid fcid");
		}
		rocket.createFlightConfiguration(motorId);
	    FlightConfiguration config = rocket.getFlightConfiguration(motorId);
		
		int totalMotorCount = 0;
		double totalPropMass = 0;
		double totalImpulse = 0;
		double totalTTW = 0;
		
		double stageMass = 0;
		
		boolean topBorder = false;
		for (RocketComponent c : rocket) {
			
			if (c instanceof AxialStage) {
				config.activateStagesThrough((AxialStage) c); 
				RigidBody launchInfo = MassCalculator.calculateLaunch(config);
				stageMass = launchInfo.getMass();
				// Calculate total thrust-to-weight from only lowest stage motors
				totalTTW = 0;
				topBorder = true;
			}
			
			if (c instanceof MotorMount && ((MotorMount) c).isMotorMount()) {
				MotorMount mount = (MotorMount) c;
				
				MotorConfiguration motorConfig = mount.getMotorConfig(motorId);
				if (null == motorConfig) {
					log.warn("Unable to find motorConfig for motorId {}", motorId);
					continue;
				}
				
				Motor motor = motorConfig.getMotor();
				if (null == motor) {
					log.warn("Motor instance is null for motorId {}", motorId);
					continue;
				}
				
				int motorCount = mount.getMotorCount();
				
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
				
				double propMass = (motor.getLaunchMass() - motor.getBurnoutMass());
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
	private void addFlightData(final FlightData flight, final Rocket theRocket, final FlightConfigurationId motorId, final PdfPTable parent, int leading) {
		
		// Output the flight data
		if (flight != null) {
			try {
				FlightDataBranch branch = new FlightDataBranch();
				if (flight.getBranchCount() > 0) {
					branch = flight.getBranch(0);
				}
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
				labelTable.addCell(ITextHelper.createCell(flightTimeUnit.toStringUnit(branch.getOptimumDelay()), 2, 2));
				
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
	private FlightData findSimulation(final FlightConfigurationId motorId, List<Simulation> simulations) {
		// Perform flight simulation
		FlightData flight = null;
		for (int i = 0; i < simulations.size(); i++) {
			Simulation simulation = simulations.get(i);
			if (Utils.equals(simulation.getId(), motorId)) {
				flight = simulation.getSimulatedData();
				break;
			}
		}
		return flight;
	}
	
	/**
	 * Returns a list of Simulations to use for printing the design report
	 * for the rocket and optionally re-run out of date simulations.
	 * 
	 * If the user has selected to not run any simulations, this method will
	 * simply return the simulations found in the OpenRocketDocument.
	 * 
	 * If the user has selected to run simulations, this method will identify
	 * any simulations which are not up to date and re-run them.
	 * 
	 * @return a list of Simulations to include in the DesignReport.
	 */
	protected List<Simulation> getSimulations() {
		List<Simulation> simulations = rocketDocument.getSimulations();
		if (!runOutOfDateSimulations) {
			log.debug("Using current simulations for rocket.");
			return simulations;
		}
		
		ArrayList<Simulation> simulationsToRun = new ArrayList<Simulation>();
		ArrayList<Simulation> upToDateSimulations = new ArrayList<Simulation>();
		for (Simulation simulation : simulations) {
			boolean simulate = false;
			boolean copy = !this.updateExistingSimulations;

			switch (simulation.getStatus()) {
			case CANT_RUN:
				log.warn("Simulation " + simulation.getId() + " has no motors, skipping");
				// Continue so we don't simulate
				continue;
			case UPTODATE:
				log.trace("Simulation " + simulation.getId() + "is up to date, not running simulation");
				simulate = false;
				break;
			case NOT_SIMULATED:
			case OUTDATED:
			case LOADED:
				log.trace("Running simulation for " + simulation.getId());
				simulate = true;
				break;
			case EXTERNAL:
				log.trace("Simulation " + simulation.getId() + " is external. Using data provided");
				simulate = false;
				break;
			default:
				log.trace("Running simulation for " + simulation.getId());
				simulate = true;
				copy = true;
				break;
			}
			
			if (!simulate) {
				upToDateSimulations.add(simulation);
			} else if (copy) {
				simulationsToRun.add(simulation.copy());
			} else {
				simulationsToRun.add(simulation);
			}
		}
		
		/* Run any simulations that are pending a run. This is done via the
		 * SimulationRunDialog in order to provide user feedback.
		 */
		if (!simulationsToRun.isEmpty()) {
			runSimulations(simulationsToRun);
			upToDateSimulations.addAll(simulationsToRun);
		}
		
		return upToDateSimulations;
	}
	
	/**
	 * Runs the selected set of simulations. If a valid Window was provided when
	 * creating this DesignReport, this method will run simulations using the
	 * SimulationRunDialog in order to present status to the user.
	 * 
	 * @param simulations a list of Simulations to run
	 */
	protected void runSimulations(List<Simulation> simulations) {
		if (window != null) {
			log.debug("Updating " + simulations.size() + "simulations using SimulationRunDialog");
			Simulation[] runMe = simulations.toArray(new Simulation[simulations.size()]);
			new SimulationRunDialog(window, rocketDocument, runMe).setVisible(true);
		} else {
			/* This code is left for compatibility with any developers who are
			 * using the API to generate design reports. This may not be running
			 * graphically and the SimulationRunDialog may not be available for
			 * displaying progress information/updating simulations.
			 */
			log.debug("Updating simulations using thread pool");
			int cores = Runtime.getRuntime().availableProcessors();
			ThreadPoolExecutor executor = new ThreadPoolExecutor(cores, cores, 0L, TimeUnit.MILLISECONDS,
			                                                     new LinkedBlockingQueue<Runnable>(),
			                                                     new SimulationRunnerThreadFactory());
			for (Simulation simulation : simulations) {
				executor.execute(new RunSimulationTask(simulation));
			}
			executor.shutdown();
			try {
				/* Arbitrarily wait for at most 5 minutes for the simulation
				 * to complete. This seems like a long time, but in case there
				 * is a really long running simulation
				 */
				executor.awaitTermination(5, TimeUnit.MINUTES);
			} catch (InterruptedException ie) {
				
			}
		}
	}
	
	private static class SimulationRunnerThreadFactory implements ThreadFactory {
		private ThreadFactory factory = Executors.defaultThreadFactory();
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = factory.newThread(r);
			t.setDaemon(true);
			return t;
		}
	}
	
	/**
	 * The RunSimulationTask is responsible for running simulations within the
	 * DesignReport when run outside of the SimulationRunDialog.
	 */
	private static class RunSimulationTask implements Runnable {

		private final Simulation simulation;
		
		public RunSimulationTask(final Simulation simulation) {
			this.simulation = simulation;
		}
		
		@Override
		public void run() {
			try {
				simulation.simulate();
			} catch (SimulationException ex) {
				log.error("Error simulating " + simulation.getId(), ex);
			}
		}
		
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
