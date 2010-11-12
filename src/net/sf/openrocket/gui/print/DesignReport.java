/*
 * DesignReport.java
 */
package net.sf.openrocket.gui.print;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.figureelements.RocketInfo;
import net.sf.openrocket.gui.print.visitor.BaseVisitorStrategy;
import net.sf.openrocket.gui.print.visitor.MotorMountVisitorStrategy;
import net.sf.openrocket.gui.print.visitor.StageVisitorStrategy;
import net.sf.openrocket.gui.scalefigure.RocketPanel;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.ComponentVisitor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Prefs;

import java.awt.Graphics2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

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
public class DesignReport extends BaseVisitorStrategy {

    /**
     * The OR Document.
     */
    private OpenRocketDocument rocketDocument;

    /**
     * A panel used for rendering of the design diagram.
     */
    final RocketPanel panel;

    /**
     * A stage visitor.
     */
    private StageVisitorStrategy svs = new StageVisitorStrategy();

    /**
     * Constructor.
     *
     * @param theRocDoc the OR document
     * @param theIDoc   the iText document
     */
    public DesignReport (OpenRocketDocument theRocDoc, Document theIDoc) {
        super(theIDoc, null);
        rocketDocument = theRocDoc;
        panel = new RocketPanel(rocketDocument);
    }

    /**
     * Main entry point.  Prints the rocket drawing and design data.
     *
     * @param writer a direct byte writer
     */
    public void print (PdfWriter writer) {
        if (writer == null) {
            return;
        }
        com.itextpdf.text.Rectangle pageSize = document.getPageSize();
        int pageImageableWidth = (int) pageSize.getWidth() - (int) pageSize.getBorderWidth() * 2;
        int pageImageableHeight = (int) pageSize.getHeight() / 2 - (int) pageSize.getBorderWidthTop();

        PrintUtilities.addText(document, PrintUtilities.BIG_BOLD, "Rocket Design");

        Rocket rocket = rocketDocument.getRocket();
        final Configuration configuration = rocket.getDefaultConfiguration();
        configuration.setAllStages();
        PdfContentByte canvas = writer.getDirectContent();

        final PrintFigure figure = new PrintFigure(configuration);

        FigureElement cp = panel.getExtraCP();
        FigureElement cg = panel.getExtraCG();
        RocketInfo text = panel.getExtraText();

        double scale = paintRocketDiagram(pageImageableWidth, pageImageableHeight, canvas, figure, cp, cg);

        canvas.beginText();
        try {
            canvas.setFontAndSize(BaseFont.createFont(PrintUtilities.NORMAL.getFamilyname(), BaseFont.CP1252,
                                                      BaseFont.EMBEDDED), PrintUtilities.NORMAL_FONT_SIZE);
        }
        catch (DocumentException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        int figHeightPts = (int) (PrintUnit.METERS.toPoints(figure.getFigureHeight()) * 0.4 * (scale / PrintUnit.METERS
                .toPoints(1)));
        final int diagramHeight = pageImageableHeight * 2 - 70 - (int) (figHeightPts);
        canvas.moveText(document.leftMargin() + pageSize.getBorderWidthLeft(), diagramHeight);
        canvas.moveTextWithLeading(0, -16);

        float initialY = canvas.getYTLM();

        canvas.showText(rocketDocument.getRocket().getName());

        canvas.newlineShowText("Stages: ");
        canvas.showText("" + rocket.getStageCount());


        if (configuration.hasMotors()) {
            canvas.newlineShowText("Mass (with motor" + ((configuration.getStageCount() > 1) ? "s): " : "): "));
        }
        else {
            canvas.newlineShowText("Mass (Empty): ");
        }
        canvas.showText(text.getMass(UnitGroup.UNITS_MASS.getDefaultUnit()));

        canvas.newlineShowText("Stability: ");
        canvas.showText(text.getStability());

        canvas.newlineShowText("Cg: ");
        canvas.showText(text.getCg());

        canvas.newlineShowText("Cp: ");
        canvas.showText(text.getCp());
        canvas.endText();

        try {
            //Move the internal pointer of the document below that of what was just written using the direct byte buffer.
            Paragraph paragraph = new Paragraph();
            float finalY = canvas.getYTLM();
            int heightOfDiagramAndText = (int) (pageSize.getHeight() - (finalY - initialY + diagramHeight));

            paragraph.setSpacingAfter(heightOfDiagramAndText);
            document.add(paragraph);

            String[] mids = rocket.getMotorConfigurationIDs();

            List<Double> stages = getStageWeights(rocket);


            for (int j = 0; j < mids.length; j++) {
                String mid = mids[j];
                if (mid != null) {
                    MotorMountVisitorStrategy mmvs = new MotorMountVisitorStrategy(document, mid);
                    rocket.accept(new ComponentVisitor(mmvs));
                    PdfPTable parent = new PdfPTable(2);
                    parent.setWidthPercentage(100);
                    parent.setHorizontalAlignment(Element.ALIGN_LEFT);
                    parent.setSpacingBefore(0);
                    parent.setWidths(new int[]{1, 3});
                    int leading = 0;
                    //The first motor config is always null.  Skip it and the top-most motor, then set the leading.
                    if (j > 1) {
                        leading = 25;
                    }
                    addFlightData(rocket, mid, parent, leading);
                    addMotorData(mmvs.getMotors(), parent, stages);
                    document.add(parent);
                }
            }
        }
        catch (DocumentException e) {
            e.printStackTrace();
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
    private double paintRocketDiagram (final int thePageImageableWidth, final int thePageImageableHeight,
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
        g2d.translate(20, 120);

        g2d.scale(0.4d, 0.4d);
        theFigure.paint(g2d);
        g2d.dispose();
        return scale;
    }

    /**
     * Add the motor data for a motor configuration to the table.
     *
     * @param motors       a motor configuration's list of motors
     * @param parent       the parent to which the motor data will be added
     * @param stageWeights the stageWeights of each stage, in order
     */
    private void addMotorData (List<Motor> motors, final PdfPTable parent, List<Double> stageWeights) {

        PdfPTable motorTable = new PdfPTable(8);
        motorTable.setWidthPercentage(68);
        motorTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        final PdfPCell motorCell = ITextHelper.createCell("Motor", PdfPCell.BOTTOM);
        final int mPad = 10;
        motorCell.setPaddingLeft(mPad);
        motorTable.addCell(motorCell);
        motorTable.addCell(ITextHelper.createCell("Avg Thrust", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Burn Time", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Max Thrust", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Total Impulse", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Thrust to Wt", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Propellant Wt", PdfPCell.BOTTOM));
        motorTable.addCell(ITextHelper.createCell("Size", PdfPCell.BOTTOM));

        DecimalFormat df = new DecimalFormat("#,##0.0#");
        for (int i = 0; i < motors.size(); i++) {
            int border = Rectangle.BOTTOM;
            if (i == motors.size() - 1) {
                border = Rectangle.NO_BORDER;
            }
            Motor motor = motors.get(i);
            double motorWeight = (motor.getLaunchCG().weight - motor.getEmptyCG().weight) * 1000;  //convert to grams

            final PdfPCell motorVCell = ITextHelper.createCell(motor.getDesignation(), border);
            motorVCell.setPaddingLeft(mPad);
            motorTable.addCell(motorVCell);
            motorTable.addCell(ITextHelper.createCell(df.format(motor.getAverageThrustEstimate()) + " " + UnitGroup
                    .UNITS_FORCE
                    .getDefaultUnit().toString(), border));
            motorTable.addCell(ITextHelper.createCell(df.format(motor.getBurnTimeEstimate()) + " " + UnitGroup
                    .UNITS_FLIGHT_TIME
                    .getDefaultUnit().toString(), border));
            motorTable.addCell(ITextHelper.createCell(df.format(motor.getMaxThrustEstimate()) + " " + UnitGroup
                    .UNITS_FORCE.getDefaultUnit()
                    .toString(), border));
            motorTable.addCell(ITextHelper.createCell(df.format(motor.getTotalImpulseEstimate()) + " " + UnitGroup
                    .UNITS_IMPULSE
                    .getDefaultUnit().toString(), border));
            double ttw = motor.getAverageThrustEstimate() / (getStageWeight(stageWeights, i) + (motor
                    .getLaunchCG().weight * 9.80665));
            motorTable.addCell(ITextHelper.createCell(df.format(ttw) + ":1", border));

            motorTable.addCell(ITextHelper.createCell(df.format(motorWeight) + " " + UnitGroup.UNITS_MASS
                    .getDefaultUnit().toString(), border));

            final Unit motorUnit = UnitGroup.UNITS_MOTOR_DIMENSIONS
                    .getDefaultUnit();
            motorTable.addCell(ITextHelper.createCell(motorUnit.toString(motor.getDiameter()) +
                                                      "/" +
                                                      motorUnit.toString(motor.getLength()) + " " +
                                                      motorUnit.toString(), border));
        }
        PdfPCell c = new PdfPCell(motorTable);
        c.setBorder(PdfPCell.LEFT);
        c.setBorderWidthTop(0f);
        parent.addCell(c);
    }


    /**
     * Add the motor data for a motor configuration to the table.
     *
     * @param theRocket the rocket
     * @param mid       a motor configuration id
     * @param parent    the parent to which the motor data will be added
     * @param leading   the number of points for the leading
     */
    private void addFlightData (final Rocket theRocket, final String mid, final PdfPTable parent, int leading) {
        FlightData flight = null;
        if (theRocket.getMotorConfigurationIDs().length > 1) {
            Rocket duplicate= theRocket.copyWithOriginalID();
            Simulation simulation = Prefs.getBackgroundSimulation(duplicate);
            simulation.getConditions().setMotorConfigurationID(mid);

            flight = PrintSimulationWorker.doit(simulation);

            if (flight != null) {
                try {
                    final Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
                    final Unit velocityUnit = UnitGroup.UNITS_VELOCITY.getDefaultUnit();
                    final Unit flightUnit = UnitGroup.UNITS_FLIGHT_TIME.getDefaultUnit();

                    PdfPTable labelTable = new PdfPTable(2);
                    labelTable.setWidths(new int[]{3, 2});
                    final Paragraph chunk = ITextHelper.createParagraph(stripBrackets(
                            theRocket.getMotorConfigurationNameOrDescription(mid)), PrintUtilities.BOLD);
                    chunk.setLeading(leading);
                    chunk.setSpacingAfter(3f);

                    document.add(chunk);

                    DecimalFormat df = new DecimalFormat("#,##0.0#");

                    final PdfPCell cell = ITextHelper.createCell("Altitude", 2, 2);
                    cell.setUseBorderPadding(false);
                    cell.setBorderWidthTop(0f);
                    labelTable.addCell(cell);
                    labelTable.addCell(ITextHelper.createCell(df.format(flight.getMaxAltitude()) + " " + distanceUnit,
                                                              2, 2));

                    labelTable.addCell(ITextHelper.createCell("Flight Time", 2, 2));
                    labelTable.addCell(ITextHelper.createCell(df.format(flight.getFlightTime()) + " " + flightUnit, 2,
                                                              2));

                    labelTable.addCell(ITextHelper.createCell("Time to Apogee", 2, 2));
                    labelTable.addCell(ITextHelper.createCell(df.format(flight.getTimeToApogee()) + " " + flightUnit, 2,
                                                              2));

                    labelTable.addCell(ITextHelper.createCell("Velocity off Pad", 2, 2));
                    labelTable.addCell(ITextHelper.createCell(df.format(
                            flight.getLaunchRodVelocity()) + " " + velocityUnit, 2, 2));

                    labelTable.addCell(ITextHelper.createCell("Max Velocity", 2, 2));
                    labelTable.addCell(ITextHelper.createCell(df.format(flight.getMaxVelocity()) + " " + velocityUnit,
                                                              2, 2));

                    labelTable.addCell(ITextHelper.createCell("Landing Velocity", 2, 2));
                    labelTable.addCell(ITextHelper.createCell(df.format(
                            flight.getGroundHitVelocity()) + " " + velocityUnit, 2, 2));

                    //Add the table to the parent; have to wrap it in a cell
                    PdfPCell c = new PdfPCell(labelTable);
                    c.setBorder(PdfPCell.RIGHT);
                    c.setBorderWidthTop(0);
                    c.setTop(0);
                    parent.addCell(c);
                }
                catch (DocumentException e) {
                    e.printStackTrace();
                }
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
    private String stripBrackets (String target) {
        return stripLeftBracket(stripRightBracket(target));
    }

    /**
     * Strip [ from a string.
     *
     * @param target the original string
     *
     * @return target with [ removed
     */
    private String stripLeftBracket (String target) {
        return target.replace("[", "");
    }

    /**
     * Strip ] from a string.
     *
     * @param target the original string
     *
     * @return target with ] removed
     */
    private String stripRightBracket (String target) {
        return target.replace("]", "");
    }

    /**
     * Use a visitor to get the sorted list of Stage references, then from those get the stage masses and convert to
     * weight.
     *
     * @param rocket the rocket
     *
     * @return a sorted list of Stage weights (mass * gravity), in Newtons
     */
    private List<Double> getStageWeights (Rocket rocket) {
        rocket.accept(new ComponentVisitor(svs));
        svs.close();
        List<Double> stages = svs.getStages();
        for (int i = 0; i < stages.size(); i++) {
            Double stage = stages.get(i);
            stages.set(i, stage * 9.80665);
        }
        return stages;
    }

    /**
     * Compute the total stage weight from a list of stage weights.  This sums up the weight of the given stage plus all
     * stages that sit atop it (depend upon it for thrust).
     *
     * @param weights the list of stage weights, in Newtons
     * @param stage   a stage number, 0 being topmost stage
     *
     * @return the total weight of the stage and all stages sitting atop the given stage, in Newtons
     */
    private double getStageWeight (List<Double> weights, int stage) {

        double result = 0d;
        for (int i = 0; i <= stage; i++) {
            result += weights.get(i);
        }
        return result;
    }
}
