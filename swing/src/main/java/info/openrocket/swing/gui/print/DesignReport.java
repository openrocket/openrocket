/*
 * DesignReport.java
 */
package info.openrocket.swing.gui.print;

import java.awt.Window;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.itextpdf.awt.PdfGraphics2D;
import info.openrocket.swing.gui.figureelements.CGCaret;
import info.openrocket.swing.gui.figureelements.CPCaret;
import info.openrocket.swing.gui.scalefigure.AbstractScaleFigure;
import info.openrocket.swing.gui.scalefigure.RocketFigure;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.CaliberUnit;
import info.openrocket.core.unit.PercentageOfLengthUnit;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Chars;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Utils;

import info.openrocket.swing.gui.figureelements.FigureElement;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.simulation.SimulationRunDialog;

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
	private double rotation = 0.0d;
	
	/**
	 * Determines whether or not to run out of date simulations.
	 */
	private boolean runOutOfDateSimulations = true;
	
	/**
	 * Determines whether or not to update existing simulations.
	 */
	private boolean updateExistingSimulations = false;

	/**
	 * Determines whether the per-configuration motor and flight-data tables are printed.
	 */
	private boolean includeMotors = true;

	/**
	 * Parent window for showing simulation run dialog as necessary
	 */
	private Window window = null;

	private final UITheme.Theme originalTheme;
	
	/** The displayed strings. */
	private static final String STAGES = "Stages: ";
	private static final String DESIGNER = "Designer: ";
	private static final String DESIGN_TYPE = "Design type: ";
	private static final String KIT_NAME = "Kit: ";
	private static final String NOTES_HEADING = "Design Notes";
	private static final String REVISION_LABEL = "Revision: ";
	private static final String COMMENT_LABEL = "Comments";
	private static final String MOTORS = "Motors";
	private static final String MOTOR = "Motor";
	private static final String AVG_THRUST = "Avg Thrust";
	private static final String BURN_TIME = "Burn Time";
	private static final String MAX_THRUST = "Max Thrust";
	private static final String TOTAL_IMPULSE = "Total Impulse";
	private static final String THRUST_TO_WT = "Thrust to Wt";
	private static final String MOTOR_WT = "Motor Wt";
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
	//// Rocket static stats block (labels have no trailing colon; the PDF adds it)
	private static final String STAT_LENGTH = "Length";
	private static final String STAT_MAX_DIAMETER = "Max Diameter";
	private static final String STAT_MASS_EMPTY = "Mass (Empty)";
	private static final String STAT_MASS_LOADED = "Mass (Loaded)";
	private static final String STAT_FINENESS = "Fineness (L/D)";
	private static final String STAT_CG_EMPTY = "CG (Empty)";
	private static final String STAT_CG_LOADED = "CG (Loaded)";
	private static final String STAT_CP = "CP";
	private static final String STAT_STABILITY_ON_PAD = "Stability (on pad)";
	private static final String STAT_STABILITY_PERCENT = "Stability (%)";
	private static final String STAT_DRAG_COEFF = "Drag Coeff.";
	private static final String STAT_NORMAL_FORCE_SLOPE = "Normal-Force Slope (CN" + Chars.ALPHA + ")";
	private static final String STAT_PITCH_INERTIA = "Pitch Inertia (Loaded)";
	private static final String STAT_ROLL_INERTIA = "Roll Inertia (Loaded)";
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
		this(theRocDoc, theIDoc, figureRotation, runOutOfDateSims, updateExistingSims, window, true);
	}

	/**
	 * Creates a new DesignReport, additionally controlling whether the per-configuration
	 * motor and flight-data tables are included in the report.
	 *
	 * @param theRocDoc the OR document
	 * @param theIDoc   the iText document
	 * @param figureRotation the angle the figure is rotated on the screen; printed report will mimic
	 * @param runOutOfDateSims whether or not to run simulations that are not up to date.
	 * @param updateExistingSims whether or not to update existing simulations or to copy the simulations.
	 * @param window the base AWT window to use
	 * @param includeMotors whether to include the motor and flight-data tables for each configuration
	 */
	public DesignReport(OpenRocketDocument theRocDoc, Document theIDoc, Double figureRotation,
	                    boolean runOutOfDateSims, boolean updateExistingSims, Window window, boolean includeMotors) {
		this.originalTheme = GUIUtil.getUITheme();
		GUIUtil.setUITheme(UITheme.Themes.LIGHT);
		updateColors();
		this.document = theIDoc;
		this.rocketDocument = theRocDoc;
		this.panel = new RocketPanel(this.rocketDocument);
		this.rotation = figureRotation;
		this.runOutOfDateSimulations = runOutOfDateSims;
		this.updateExistingSimulations = updateExistingSims;
		this.window = window;
		this.includeMotors = includeMotors;
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

		double scale = paintRocketDiagram(pageImageableWidth, pageImageableHeight, canvas, figure, cp, cg);
		
		canvas.beginText();
		canvas.setFontAndSize(ITextHelper.getBaseFont(), PrintUtilities.NORMAL_FONT_SIZE);
		double figureHeightInPoints = PrintUnit.METERS.toPoints(figure.getFigureHeight());
		int figHeightPts = (int) (figureHeightInPoints * SCALE_FUDGE_FACTOR * (scale / PrintUnit.METERS.toPoints(1)));
		// The extra offset leaves a bit more breathing room between the rocket schematic
		// and the name/stages summary drawn beneath it.
		final int diagramHeight = pageImageableHeight * 2 - 70 - (figHeightPts) - 16;
		final float summaryLeftX = document.leftMargin() + pageSize.getBorderWidthLeft();
		final float summaryColumn2X = summaryLeftX + 210;
		final float nameBaseline = diagramHeight - 18;

		// Rocket name: larger and bold. The embedded serif font has no bold face, so
		// synthesise it by both filling and stroking the glyphs.
		canvas.setFontAndSize(ITextHelper.getBaseFont(), PrintUtilities.NORMAL_FONT_SIZE + 4);
		canvas.setTextMatrix(summaryLeftX, nameBaseline);
		float initialY = canvas.getYTLM();
		canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
		canvas.setLineWidth(0.4f);
		canvas.showText(rocketDocument.getRocket().getName());
		canvas.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
		canvas.setLineWidth(0);

		// Design metadata in two columns: stages | design type, then designer | kit.
		canvas.setFontAndSize(ITextHelper.getBaseFont(), PrintUtilities.NORMAL_FONT_SIZE);

		final String stagesText = STAGES + rocket.getStageCount();
		final String designTypeText = rocket.getDesignType() != null
				? DESIGN_TYPE + rocket.getDesignType().getName() : null;
		final String designerText = (rocket.getDesigner() != null && !rocket.getDesigner().trim().isEmpty())
				? DESIGNER + rocket.getDesigner().trim() : null;
		final String kitText = (rocket.getKitName() != null && !rocket.getKitName().trim().isEmpty())
				? KIT_NAME + rocket.getKitName().trim() : null;

		// A little extra spacing after the larger name, then normal line spacing between rows.
		float summaryRowY = nameBaseline - 24;
		drawSummaryRow(canvas, summaryLeftX, summaryColumn2X, summaryRowY, stagesText, designTypeText);
		summaryRowY -= 16;
		drawSummaryRow(canvas, summaryLeftX, summaryColumn2X, summaryRowY, designerText, kitText);

		// Mass, stability, CG and CP are shown in the expanded "static stats" table below;
		// the comment and revision are printed on their own page at the end.
		canvas.endText();
		
		try {
			/*
			 * Move the internal pointer of the document below the rocket diagram and
			 * the key attributes. The height of the rocket figure is already calculated
			 * as diagramHeigt and the height of the attributes text is finalY - initialY.
			 */
			Paragraph paragraph = new Paragraph();
			float finalY = canvas.getYTLM();
			// Pull the static stats table up close underneath the name/stages summary
			// instead of leaving a large band of whitespace below the diagram.
			int heightOfDiagramAndText = (int) (pageSize.getHeight() - (finalY - initialY + diagramHeight)) - 26;

			paragraph.setSpacingAfter(heightOfDiagramAndText);
			document.add(paragraph);

			// Whole-rocket static stats, followed by a table per stage (multi-stage designs only).
			final List<AxialStage> stages = configuration.getActiveStages();
			final boolean perStage = rocket.getStageCount() > 1;
			addStaticStats(configuration, null, 3, perStage ? 8 : 34);
			if (perStage) {
				for (int i = 0; i < stages.size(); i++) {
					final AxialStage stage = stages.get(i);
					final FlightConfiguration stageConfig = configuration.clone();
					stageConfig.setOnlyStage(stage.getStageNumber());
					final boolean last = i == stages.size() - 1;
					addStaticStats(stageConfig, stage.getName(), 14, last ? 34 : 8);
				}
			}

			// Comment and revision, if any, go on their own page after the statistics.
			addDesignInfo(rocket);

			// The per-configuration motor and flight-data tables are optional and start
			// on their own page under a "Motors" header.
			if (includeMotors) {
				document.newPage();
				PrintUtilities.addText(document, PrintUtilities.BIG_BOLD, MOTORS);

				List<Simulation> simulations = getSimulations();

				boolean firstMotor = true;
				for (FlightConfigurationId fcid : rocket.getIds()) {
					PdfPTable parent = new PdfPTable(2);
					parent.setWidthPercentage(100);
					parent.setHorizontalAlignment(Element.ALIGN_LEFT);
					parent.setSpacingBefore(0);
					parent.setWidths(new int[] { 1, 3 });

					/* The first motor block sits just below the "Motors" header;
					 * subsequent blocks get more spacing to separate them.
					 */
					int leading = (firstMotor) ? 18 : 25;

					FlightData flight = findSimulation(fcid, simulations);
					addFlightData(flight, rocket, fcid, parent, leading);
					addMotorData(rocket, fcid, parent);
					document.add(parent);

					firstMotor = false;
				}
			}
		} catch (DocumentException e) {
			log.error("Could not modify document.", e);
		}
	}

	/**
	 * Draw a two-column row of summary text within an open text object. Either column
	 * may be {@code null} to leave it blank.
	 *
	 * @param canvas   the content byte with an active text object
	 * @param leftX    x position of the first column
	 * @param column2X x position of the second column
	 * @param y        baseline of the row
	 * @param left     text for the first column, or {@code null}
	 * @param right    text for the second column, or {@code null}
	 */
	private void drawSummaryRow(final PdfContentByte canvas, final float leftX, final float column2X,
			final float y, final String left, final String right) {
		if (left != null) {
			canvas.setTextMatrix(leftX, y);
			canvas.showText(left);
		}
		if (right != null) {
			canvas.setTextMatrix(column2X, y);
			canvas.showText(right);
		}
	}

	/**
	 * Add a page containing the rocket's revision string and comment/description,
	 * if either is present. Nothing (and no page) is added when both are empty.
	 *
	 * @param rocket the rocket whose design notes are printed
	 */
	private void addDesignInfo(final Rocket rocket) throws DocumentException {
		final String revision = rocket.getRevision() == null ? "" : rocket.getRevision().trim();
		final String comment = rocket.getComment() == null ? "" : rocket.getComment().trim();
		if (revision.isEmpty() && comment.isEmpty()) {
			return;
		}

		document.newPage();
		PrintUtilities.addText(document, PrintUtilities.BIG_BOLD, NOTES_HEADING);

		if (!revision.isEmpty()) {
			Paragraph revisionParagraph = ITextHelper.createParagraph(REVISION_LABEL + revision, PrintUtilities.NORMAL);
			revisionParagraph.setSpacingBefore(8);
			document.add(revisionParagraph);
		}

		if (!comment.isEmpty()) {
			Paragraph commentHeading = ITextHelper.createParagraph(COMMENT_LABEL, PrintUtilities.BOLD);
			commentHeading.setSpacingBefore(12);
			commentHeading.setSpacingAfter(2);
			document.add(commentHeading);
			document.add(ITextHelper.createParagraph(comment, PrintUtilities.NORMAL));
		}
	}
	
	
	/**
	 * Add a table of static design statistics for the given configuration:
	 * length, diameters, masses, CG/CP, stability, drag and inertia. The values
	 * mirror those shown in the design view's info overlay, computed for the active
	 * stages of {@code configuration} at the preference default Mach number.
	 *
	 * @param configuration the flight configuration whose active stages are summarised
	 * @param heading       an optional bold heading printed above the table (e.g. a
	 *                      stage name), or {@code null} for none
	 * @param spacingBefore points of vertical spacing before the block
	 * @param spacingAfter  points of vertical spacing after the table
	 */
	private void addStaticStats(final FlightConfiguration configuration, final String heading,
			final float spacingBefore, final float spacingAfter) {
		final List<Stat> stats = computeStaticStats(configuration, panel.getAerodynamicCalculator());

		PdfPTable table = new PdfPTable(4);
		table.setWidthPercentage(100);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		// When a heading is present it carries the leading space; otherwise the table does.
		table.setSpacingBefore(heading != null ? 0 : spacingBefore);
		table.setSpacingAfter(spacingAfter);
		try {
			table.setWidths(new int[] { 3, 2, 3, 2 });
		} catch (DocumentException e) {
			log.error("Could not set static stats table widths.", e);
		}

		for (Stat stat : stats) {
			final String value = stat.unit().isEmpty() ? stat.value() : stat.value() + " " + stat.unit();
			addStatCell(table, stat.label() + ": ", value);
		}
		table.completeRow();

		try {
			if (heading != null) {
				Paragraph headingParagraph = ITextHelper.createParagraph(heading, PrintUtilities.BOLD);
				headingParagraph.setSpacingBefore(spacingBefore);
				headingParagraph.setSpacingAfter(2);
				document.add(headingParagraph);
			}
			document.add(table);
		} catch (DocumentException e) {
			log.error("Could not add static stats table to document.", e);
		}
	}

	/**
	 * Compute the static design statistics for the active stages of {@code configuration}
	 * at the preference default Mach number, as an ordered map of label to formatted value.
	 * This is the single source of the numbers shown both in the printed report and the
	 * CSV export.
	 *
	 * @param configuration the flight configuration whose active stages are summarised
	 * @param aero          the aerodynamic calculator to use for CP/CNa/CD
	 * @return an ordered list of statistics (label, value, unit)
	 */
	static List<Stat> computeStaticStats(final FlightConfiguration configuration,
			final AerodynamicCalculator aero) {
		final Unit lengthUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		final Unit massUnit = UnitGroup.UNITS_MASS.getDefaultUnit();
		// Inertia is tiny for most designs, so use the smallest unit in the user's chosen
		// unit system to keep precision (the default kg-m^2 would round it to ~0).
		final Unit inertiaUnit = smallestInertiaUnit();
		final DecimalFormat plainFormat = new DecimalFormat("0.00");
		final DecimalFormat dragFormat = new DecimalFormat("0.000");

		// Geometry
		final double length = configuration.getLength();
		double diameter = Double.NaN;
		for (RocketComponent c : configuration.getCoreComponents()) {
			if (c instanceof SymmetricComponent sc) {
				diameter = MathUtil.max(diameter, sc.getForeRadius() * 2, sc.getAftRadius() * 2);
			}
		}
		final double fineness = (!Double.isNaN(diameter) && diameter > 0) ? length / diameter : Double.NaN;

		// Masses / CG / inertia
		final RigidBody launch = MassCalculator.calculateLaunch(configuration);
		final RigidBody structure = MassCalculator.calculateStructure(configuration);
		final double massLoaded = launch.getMass();
		final double massEmpty = structure.getMass();
		final double cgLoaded = launch.getCM().getX();
		final double cgEmpty = structure.getCM().getX();
		final double pitchInertia = launch.getLongitudinalInertia();
		final double rollInertia = launch.getRotationalInertia();

		// Aerodynamics (CP, stability margin, drag, normal-force slope) at the default Mach
		final double mach = Application.getPreferences().getDefaultMach();
		double cpX = Double.NaN;
		double cna = Double.NaN;
		double cd = Double.NaN;
		try {
			final FlightConditions conditions = new FlightConditions(configuration);
			conditions.setMach(mach);
			conditions.setAOA(0);
			final WarningSet warnings = new WarningSet();
			final CoordinateIF cp = aero.getWorstCP(configuration, conditions, warnings);
			if (cp.getWeight() > MathUtil.EPSILON) {
				cpX = cp.getX();
			}
			final AerodynamicForces forces = aero.getAerodynamicForces(configuration, conditions, warnings);
			if (forces.getCP().getWeight() > MathUtil.EPSILON) {
				cna = forces.getCP().getWeight();
			}
			cd = forces.getCD();
		} catch (Exception e) {
			log.warn("Unable to compute aerodynamic static stats for the design report", e);
		}

		final double margin = !Double.isNaN(cpX) ? cpX - cgLoaded : Double.NaN;
		final Unit caliberUnit = new CaliberUnit(configuration);
		final Unit percentUnit = new PercentageOfLengthUnit(configuration);
		final String machStr = new DecimalFormat("0.##").format(mach);

		final List<Stat> stats = new ArrayList<>();
		stats.add(unitStat(STAT_LENGTH, length, lengthUnit));
		stats.add(unitStat(STAT_MAX_DIAMETER, diameter, lengthUnit));
		stats.add(unitStat(STAT_MASS_EMPTY, massEmpty, massUnit));
		stats.add(unitStat(STAT_MASS_LOADED, massLoaded, massUnit));
		stats.add(plainStat(STAT_FINENESS, fineness, plainFormat, ""));
		stats.add(unitStat(STAT_CG_EMPTY, cgEmpty, lengthUnit));
		stats.add(unitStat(STAT_CG_LOADED, cgLoaded, lengthUnit));
		stats.add(unitStat(STAT_CP, cpX, lengthUnit));
		stats.add(unitStat(STAT_STABILITY_ON_PAD, margin, caliberUnit));
		stats.add(unitStat(STAT_STABILITY_PERCENT, margin, percentUnit));
		stats.add(plainStat(STAT_DRAG_COEFF + " (Ma " + machStr + ")", cd, dragFormat, ""));
		stats.add(plainStat(STAT_NORMAL_FORCE_SLOPE, cna, plainFormat, "/rad"));
		stats.add(unitStat(STAT_PITCH_INERTIA, pitchInertia, inertiaUnit));
		stats.add(unitStat(STAT_ROLL_INERTIA, rollInertia, inertiaUnit));
		return stats;
	}

	/**
	 * A single static statistic: its label, formatted numeric value, and unit symbol.
	 * The unit is an empty string for dimensionless values (and for {@code N/A}).
	 */
	record Stat(String label, String value, String unit) {
	}

	/** Build a {@link Stat} for a value expressed in a {@link Unit}; NaN becomes {@code N/A} with no unit. */
	private static Stat unitStat(final String label, final double siValue, final Unit unit) {
		if (Double.isNaN(siValue)) {
			return new Stat(label, "N/A", "");
		}
		return new Stat(label, unit.toString(siValue), unit.getUnit());
	}

	/** Build a {@link Stat} for a dimensionless (or fixed-unit) value; NaN becomes {@code N/A} with no unit. */
	private static Stat plainStat(final String label, final double value, final DecimalFormat format, final String unit) {
		if (Double.isNaN(value)) {
			return new Stat(label, "N/A", "");
		}
		return new Stat(label, format.format(value), unit);
	}

	/**
	 * Find the smallest inertia unit within the user's chosen unit system, so tiny
	 * inertias keep their precision instead of rounding to zero in the default unit.
	 * The system (metric vs imperial) is inferred from the user's default inertia unit;
	 * among the units of that system the one representing the smallest physical quantity
	 * (i.e. the largest numeric value for a fixed inertia) is returned.
	 *
	 * @return the smallest inertia unit in the user's unit system
	 */
	private static Unit smallestInertiaUnit() {
		final UnitGroup group = UnitGroup.UNITS_INERTIA;
		final Unit defaultUnit = group.getDefaultUnit();
		final boolean metric = defaultUnit.getUnit().contains("kg");

		Unit chosen = defaultUnit;
		double best = defaultUnit.toUnit(1.0);
		for (int i = 0; i < group.getUnitCount(); i++) {
			final Unit unit = group.getUnit(i);
			if (unit.getUnit().contains("kg") != metric) {
				continue;
			}
			// A smaller physical unit yields a larger numeric value for the same inertia.
			final double value = unit.toUnit(1.0);
			if (value > best) {
				best = value;
				chosen = unit;
			}
		}
		return chosen;
	}

	/**
	 * Add a label/value pair of cells to the static stats table.
	 *
	 * @param table the table to add the cells to
	 * @param label the (bold) label of the statistic
	 * @param value the formatted value of the statistic
	 */
	private void addStatCell(final PdfPTable table, final String label, final String value) {
		final PdfPCell labelCell = ITextHelper.createCell(label, Rectangle.NO_BORDER, PrintUtilities.BOLD);
		labelCell.setPaddingLeft(5);
		table.addCell(labelCell);
		table.addCell(ITextHelper.createCell(value, Rectangle.NO_BORDER, PrintUtilities.NORMAL));
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
		PdfGraphics2D g2d = new PdfGraphics2D(theCanvas, thePageImageableWidth, thePageImageableHeight * 2, mapper);
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

	public void restoreUITheme() {
		GUIUtil.setUITheme(originalTheme);
		updateColors();
	}

	private void updateColors() {
		AbstractScaleFigure.updateColors();
		RocketFigure.updateColors();
		CGCaret.updateColors();
		CPCaret.updateColors();
		((SwingPreferences) Application.getPreferences()).updateColors();
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
		motorTable.addCell(ITextHelper.createCell(MOTOR_WT, PdfPCell.BOTTOM, PrintUtilities.SMALL));
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
				
				int motorCount = mount.getMotorCountIncludingAssemblyCopies();
				
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
						UnitGroup.UNITS_LONG_TIME.getDefaultUnit().toStringUnit(motor.getBurnTimeEstimate()), border));
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
		c.setBorderWidthTop(0.0f);
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
				final Unit flightTimeUnit = UnitGroup.UNITS_LONG_TIME.getDefaultUnit();
				
				PdfPTable labelTable = new PdfPTable(2);
				labelTable.setWidths(new int[] { 3, 2 });
				final Paragraph chunk = ITextHelper.createParagraph(stripBrackets(
						descriptor.format(theRocket, motorId)), PrintUtilities.BOLD);
				chunk.setLeading(leading);
				chunk.setSpacingAfter(3.0f);
				
				document.add(chunk);
				
				final PdfPCell cell = ITextHelper.createCell(ALTITUDE, 2, 2);
				cell.setUseBorderPadding(false);
				cell.setBorderWidthTop(0.0f);
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
		for (Simulation simulation : simulations) {
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
		
		ArrayList<Simulation> simulationsToRun = new ArrayList<>();
		ArrayList<Simulation> upToDateSimulations = new ArrayList<>();
		for (Simulation simulation : simulations) {
			boolean simulate = false;
			boolean copy = !this.updateExistingSimulations;

			switch (simulation.getStatus()) {
			case CANT_RUN:
				log.warn("Simulation " + simulation.getId() + " has no motors, skipping");
				// Continue so we don't simulate
				continue;
			case LOADED:
			case UPTODATE:
				log.trace("Simulation " + simulation.getId() + "is up to date, not running simulation");
				simulate = false;
				break;
			case NOT_SIMULATED:
			case OUTDATED:
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
			Simulation[] runMe = simulations.toArray(new Simulation[0]);
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
					new LinkedBlockingQueue<>(),
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
