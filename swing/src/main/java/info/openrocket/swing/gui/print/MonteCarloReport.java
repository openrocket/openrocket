package info.openrocket.swing.gui.print;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics.DispersionEllipse;
import info.openrocket.core.simulation.montecarlo.LandingBody;
import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MetricStatistics;
import info.openrocket.core.simulation.montecarlo.MonteCarloBranchResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloFlightBranch;
import info.openrocket.core.simulation.montecarlo.MonteCarloMetric;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.UncertaintySpec;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.print.MonteCarloReportData.Entry;
import info.openrocket.swing.gui.simulation.MonteCarloLabels;

/** Writes cached Monte Carlo landing and flight-metric results into the print document. */
public final class MonteCarloReport {
	private static final Translator trans = Application.getTranslator();
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.###");
	private static final int CHART_WIDTH = 1400;
	private static final int CHART_HEIGHT = 820;

	private final Document document;
	private final PdfWriter writer;
	private final MonteCarloReportData data;

	public MonteCarloReport(Document document, PdfWriter writer, MonteCarloReportData data) {
		this.document = document;
		this.writer = writer;
		this.data = data;
	}

	public void writeToDocument() throws DocumentException {
		addTitle(trans.get("monteCarloReport.title"));
		addOmittedSimulations();
		if (data.entries().isEmpty()) {
			addParagraph(trans.get("monteCarloReport.noResults"));
			return;
		}

		for (int index = 0; index < data.entries().size(); index++) {
			if (index > 0) {
				startNewPage();
			}
			Entry entry = data.entries().get(index);
			writeSimulationOverview(entry.simulation(), entry.result());
			writeLandingSections(entry.simulation(), entry.result());
			writeMetricSections(entry.result());
		}
	}

	private void addOmittedSimulations() throws DocumentException {
		if (data.omittedSimulations().isEmpty()) {
			return;
		}
		addHeading(trans.get("monteCarloReport.omitted.title"));
		addParagraph(String.format(trans.get("monteCarloReport.omitted.text"),
				String.join(", ", data.omittedSimulations())));
	}

	private void writeSimulationOverview(Simulation simulation, MonteCarloResult result)
			throws DocumentException {
		addHeading(simulation.getName());
		PdfPTable summary = table(4, new float[] { 1.2f, 2.3f, 1.2f, 2.3f });
		addLabelValue(summary, trans.get("monteCarloReport.configuration"),
				simulation.getActiveConfiguration().getName());
		addLabelValue(summary, trans.get("monteCarloReport.seed"),
				Integer.toString(result.getSettings().getSeed()));
		addLabelValue(summary, trans.get("monteCarloReport.runs"),
				Integer.toString(result.getSettings().getRunCount()));
		addLabelValue(summary, trans.get("monteCarloReport.runtime"),
				NUMBER_FORMAT.format(result.getElapsedMillis() / 1000.0) + " s");
		document.add(summary);

		addSubheading(trans.get("monteCarloReport.uncertainties"));
		PdfPTable uncertainties = table(3, new float[] { 2.4f, 1.4f, 1.2f });
		addHeaderRow(uncertainties, trans.get("monteCarloReport.parameter"),
				trans.get("monteCarloReport.distribution"), trans.get("monteCarloReport.spread"));
		for (Map.Entry<MonteCarloParameter, UncertaintySpec> uncertainty
				: result.getSettings().getUncertainties().entrySet()) {
			uncertainties.addCell(bodyCell(MonteCarloLabels.parameter(uncertainty.getKey())));
			uncertainties.addCell(bodyCell(MonteCarloLabels.distribution(uncertainty.getValue().distribution())));
			uncertainties.addCell(bodyCell(formatSpread(uncertainty.getKey(), uncertainty.getValue().spread())));
		}
		if (result.getSettings().getUncertainties().isEmpty()) {
			PdfPCell none = bodyCell(trans.get("monteCarloReport.noUncertainties"));
			none.setColspan(3);
			uncertainties.addCell(none);
		}
		document.add(uncertainties);

		addSubheading(trans.get("monteCarloReport.outcomes"));
		PdfPTable outcomes = table(4, new float[] { 2.4f, 1, 1, 1 });
		addHeaderRow(outcomes, trans.get("monteCarloReport.trajectory"),
				trans.get("monteCarloReport.valid"), trans.get("monteCarloReport.failed"),
				trans.get("monteCarloReport.total"));
		for (LandingBody body : result.getLandingBodies()) {
			int valid = result.getLandingPoints(body.bodyId()).size();
			addOutcomeRow(outcomes, String.format(trans.get("monteCarloReport.outcome.landing"),
					body.branchName()), valid,
					result.getSettings().getRunCount() - valid, result.getSettings().getRunCount());
		}
		for (MonteCarloFlightBranch branch : result.getFlightBranches()) {
			int valid = maximumValidMetrics(result, branch);
			addOutcomeRow(outcomes, String.format(trans.get("monteCarloReport.outcome.metrics"),
					branch.branchName()), valid,
					result.getSettings().getRunCount() - valid, result.getSettings().getRunCount());
		}
		document.add(outcomes);
	}

	private void writeLandingSections(Simulation simulation, MonteCarloResult result)
			throws DocumentException {
		for (LandingBody body : result.getLandingBodies()) {
			startNewPage();
			addHeading(String.format(trans.get("monteCarloReport.landing.title"), body.branchName()));
			List<LandingPoint> points = result.getLandingPoints(body.bodyId());
			if (points.isEmpty()) {
				addParagraph(trans.get("LandingDispersionResultsDlg.msg.noSuccessfulLandings"));
				continue;
			}
			addLandingStatistics(result, body, points);
			addChart(MonteCarloReportCharts.landingChart(simulation.getName(), result, body), 520);
		}
	}

	private void addLandingStatistics(MonteCarloResult result, LandingBody body, List<LandingPoint> points)
			throws DocumentException {
		Unit unit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		DispersionStatistics statistics = DispersionStatistics.from(points);
		PdfPTable table = table(4, new float[] { 1.4f, 1.4f, 1.4f, 1.4f });
		addLabelValue(table, trans.get("monteCarloReport.validRuns"),
				points.size() + " / " + result.getSettings().getRunCount());
		addLabelValue(table, trans.get("monteCarloReport.meanRange"),
				format(unit.toUnit(statistics.getMeanRangeFromPad()), unit));
		addLabelValue(table, "R50", format(unit.toUnit(statistics.getContainmentRadius(0.50)), unit));
		addLabelValue(table, "R90", format(unit.toUnit(statistics.getContainmentRadius(0.90)), unit));
		addLabelValue(table, "R95", format(unit.toUnit(statistics.getContainmentRadius(0.95)), unit));
		addLabelValue(table, trans.get("monteCarloReport.meanBearing"),
				NUMBER_FORMAT.format(Math.toDegrees(statistics.getMeanBearing())) + "°");
		LandingPoint nominal = result.getNominalResult().getFailureMessage(body.bodyId()) == null
				? result.getNominalResult().getLandingPoint(body.bodyId()) : null;
		addLabelValue(table, trans.get("monteCarloReport.nominalRange"), nominal == null ? "-"
				: format(unit.toUnit(Math.hypot(nominal.east(), nominal.north())), unit));
		addLabelValue(table, trans.get("monteCarloReport.nominalBearing"), nominal == null ? "-"
				: NUMBER_FORMAT.format(bearingDegrees(nominal.east(), nominal.north())) + "°");
		for (int sigma = 1; sigma <= 3; sigma++) {
			DispersionEllipse ellipse = statistics.getEllipse(sigma);
			addLabelValue(table, String.format(trans.get("monteCarloReport.ellipse"), sigma),
					format(unit.toUnit(2 * ellipse.semiMajor()), unit) + " × "
							+ format(unit.toUnit(2 * ellipse.semiMinor()), unit));
		}
		addLabelValue(table, trans.get("monteCarloReport.majorAxisBearing"),
				NUMBER_FORMAT.format(Math.toDegrees(statistics.getMajorAxisBearing())) + "°");
		document.add(table);
	}

	private void writeMetricSections(MonteCarloResult result)
			throws DocumentException {
		for (MonteCarloFlightBranch branch : result.getFlightBranches()) {
			startNewPage();
			addHeading(String.format(trans.get("monteCarloReport.metrics.title"), branch.branchName()));
			List<MonteCarloMetric> availableMetrics = addMetricSummary(result, branch);
			for (MonteCarloMetric metric : availableMetrics) {
				startNewPage();
				addChart(MonteCarloReportCharts.histogramChart(result, branch, metric), 520);
				startNewPage();
				addChart(MonteCarloReportCharts.boxPlotChart(result, branch, metric), 520);
			}
		}
	}

	private List<MonteCarloMetric> addMetricSummary(MonteCarloResult result, MonteCarloFlightBranch branch)
			throws DocumentException {
		PdfPTable metrics = table(8, new float[] { 2.2f, 1, 1, 1, 1, 1, 1, 1 });
		addHeaderRow(metrics, trans.get("LandingDispersionResultsDlg.metrics.col.metric"),
				trans.get("LandingDispersionResultsDlg.metrics.col.nominal"),
				trans.get("LandingDispersionResultsDlg.metrics.col.mean"),
				trans.get("LandingDispersionResultsDlg.metrics.col.median"),
				trans.get("LandingDispersionResultsDlg.metrics.col.standardDeviation"),
				trans.get("LandingDispersionResultsDlg.metrics.col.p5"),
				trans.get("LandingDispersionResultsDlg.metrics.col.p95"),
				trans.get("LandingDispersionResultsDlg.metrics.col.valid"));
		List<MonteCarloMetric> available = new ArrayList<>();
		for (MonteCarloMetric metric : MonteCarloMetric.values()) {
			List<Double> values = result.getMetricValues(branch.branchId(), metric);
			if (values.isEmpty()) {
				continue;
			}
			available.add(metric);
			Unit unit = metric.getUnitGroup().getDefaultUnit();
			MetricStatistics statistics = MetricStatistics.from(values);
			MonteCarloBranchResult nominalBranch = result.getNominalResult().getBranchResult(branch.branchId());
			double nominal = nominalBranch == null ? Double.NaN : nominalBranch.getMetric(metric);
			metrics.addCell(bodyCell(MonteCarloLabels.metric(metric) + " (" + unit.getUnit() + ")"));
			metrics.addCell(bodyCell(formatValue(nominal, unit)));
			metrics.addCell(bodyCell(formatValue(statistics.getMean(), unit)));
			metrics.addCell(bodyCell(formatValue(statistics.getMedian(), unit)));
			metrics.addCell(bodyCell(formatValue(statistics.getStandardDeviation(), unit)));
			metrics.addCell(bodyCell(formatValue(statistics.getQuantile(0.05), unit)));
			metrics.addCell(bodyCell(formatValue(statistics.getQuantile(0.95), unit)));
			metrics.addCell(bodyCell(values.size() + " / " + result.getSettings().getRunCount()));
		}
		document.add(metrics);
		return available;
	}

	private void addChart(JFreeChart chart, float maximumWidth) throws DocumentException {
		Image image = chartImage(chart);
		image.scaleToFit(maximumWidth, 430);
		image.setAlignment(Element.ALIGN_CENTER);
		document.add(image);
	}

	private Image chartImage(JFreeChart chart) throws DocumentException {
		return imageFromBufferedImage(chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT));
	}

	private Image imageFromBufferedImage(BufferedImage bufferedImage) throws DocumentException {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ImageIO.write(bufferedImage, "png", output);
			return Image.getInstance(output.toByteArray());
		} catch (java.io.IOException exception) {
			throw new DocumentException(exception);
		}
	}

	private void startNewPage() throws DocumentException {
		writer.setPageEmpty(false);
		document.newPage();
	}

	private static String formatSpread(MonteCarloParameter parameter, double spread) {
		if (parameter.isRelative()) {
			return NUMBER_FORMAT.format(spread * 100) + "%";
		}
		Unit unit = switch (parameter) {
			case WIND_SPEED -> UnitGroup.UNITS_WINDSPEED.getDefaultUnit();
			case WIND_DIRECTION, LAUNCH_GUIDE_ANGLE, LAUNCH_GUIDE_DIRECTION ->
					UnitGroup.UNITS_ANGLE.getDefaultUnit();
			case CG_AXIAL -> UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit();
			case IGNITION_DELAY, DEPLOYMENT_DELAY -> UnitGroup.UNITS_SHORT_TIME.getDefaultUnit();
			default -> null;
		};
		return unit == null ? NUMBER_FORMAT.format(spread) : format(unit.toUnit(spread), unit);
	}

	private static String formatValue(double value, Unit unit) {
		return Double.isFinite(value) ? NUMBER_FORMAT.format(unit.toUnit(value)) : "-";
	}

	private static String format(double value, Unit unit) {
		return NUMBER_FORMAT.format(value) + (unit.getUnit().isBlank() ? "" : " " + unit.getUnit());
	}

	private static double bearingDegrees(double east, double north) {
		double degrees = Math.toDegrees(Math.atan2(east, north));
		return (degrees % 360 + 360) % 360;
	}

	private static int maximumValidMetrics(MonteCarloResult result, MonteCarloFlightBranch branch) {
		int maximum = 0;
		for (MonteCarloMetric metric : MonteCarloMetric.values()) {
			maximum = Math.max(maximum, result.getMetricValues(branch.branchId(), metric).size());
		}
		return maximum;
	}

	private static PdfPTable table(int columns, float[] widths) {
		PdfPTable table = new PdfPTable(columns);
		table.setWidthPercentage(100);
		try {
			table.setWidths(widths);
		} catch (DocumentException exception) {
			throw new IllegalArgumentException(exception);
		}
		table.setSpacingBefore(5);
		table.setSpacingAfter(10);
		return table;
	}

	private static void addHeaderRow(PdfPTable table, String... values) {
		for (String value : values) {
			PdfPCell cell = new PdfPCell(new Phrase(value, PrintUtilities.SMALL));
			cell.setBorder(Rectangle.BOTTOM);
			cell.setPadding(3);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		}
		table.setHeaderRows(1);
	}

	private static void addLabelValue(PdfPTable table, String label, String value) {
		PdfPCell labelCell = new PdfPCell(new Phrase(label, PrintUtilities.BOLD));
		labelCell.setBorder(Rectangle.NO_BORDER);
		labelCell.setPadding(3);
		table.addCell(labelCell);
		table.addCell(bodyCell(value));
	}

	private static void addOutcomeRow(PdfPTable table, String name, int valid, int failed, int total) {
		table.addCell(bodyCell(name));
		table.addCell(bodyCell(Integer.toString(valid)));
		table.addCell(bodyCell(Integer.toString(failed)));
		table.addCell(bodyCell(Integer.toString(total)));
	}

	private static PdfPCell bodyCell(String value) {
		PdfPCell cell = new PdfPCell(new Phrase(value, PrintUtilities.SMALL));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPadding(3);
		return cell;
	}

	private void addTitle(String text) throws DocumentException {
		Paragraph paragraph = new Paragraph(text, PrintUtilities.BIG_BOLD);
		paragraph.setSpacingAfter(10);
		document.add(paragraph);
	}

	private void addHeading(String text) throws DocumentException {
		Paragraph paragraph = new Paragraph(text, PrintUtilities.BIG_BOLD);
		paragraph.setSpacingBefore(6);
		paragraph.setSpacingAfter(6);
		document.add(paragraph);
	}

	private void addSubheading(String text) throws DocumentException {
		Paragraph paragraph = new Paragraph(text, PrintUtilities.BOLD);
		paragraph.setSpacingBefore(8);
		paragraph.setSpacingAfter(3);
		document.add(paragraph);
	}

	private void addParagraph(String text) throws DocumentException {
		Paragraph paragraph = new Paragraph(text, PrintUtilities.NORMAL);
		paragraph.setSpacingAfter(8);
		document.add(paragraph);
	}
}
