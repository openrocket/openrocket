package info.openrocket.swing.gui.print;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.MonteCarloSimulationRunner;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.print.MonteCarloReportData.Entry;
import info.openrocket.swing.util.BaseTestCase;

class MonteCarloReportTest extends BaseTestCase {
	@Test
	void writesConfigurationLandingAndMetricSections() throws Exception {
		installEnglishTranslator();
		try {
			writeAndVerifyReport();
		} finally {
			BaseTestCase.setUp();
		}
	}

	private void writeAndVerifyReport() throws Exception {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		simulation.setName("Report simulation");
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(1234)
				.uncertainty(MonteCarloParameter.WIND_DIRECTION, MonteCarloDistribution.UNIFORM,
						Math.toRadians(5))
				.build();
		simulation.setLandingDispersionSettings(settings);
		MonteCarloResult result = new MonteCarloSimulationRunner().run(simulation, settings);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4);
		PdfWriter writer = PdfWriter.getInstance(document, output);
		document.open();
		new MonteCarloReport(document, writer,
				new MonteCarloReportData(java.util.List.of(new Entry(simulation, result)), java.util.List.of()))
				.writeToDocument();
		document.close();
		String reportOutput = System.getenv("OPENROCKET_MONTE_CARLO_REPORT_TEST_OUTPUT");
		if (reportOutput != null && !reportOutput.isBlank()) {
			Path outputPath = Path.of(reportOutput);
			Files.createDirectories(outputPath.getParent());
			Files.write(outputPath, output.toByteArray());
		}

		PdfReader reader = new PdfReader(output.toByteArray());
		StringBuilder text = new StringBuilder();
		for (int page = 1; page <= reader.getNumberOfPages(); page++) {
			text.append(PdfTextExtractor.getTextFromPage(reader, page));
		}
		reader.close();

		assertTrue(text.toString().contains("Monte Carlo analysis report"), text.toString());
		assertTrue(text.toString().contains("Report simulation"), text.toString());
		assertTrue(text.toString().contains("Input uncertainties"), text.toString());
		assertTrue(text.toString().contains("Landing dispersion"), text.toString());
		assertTrue(text.toString().contains("Flight metrics"), text.toString());
		assertTrue(output.size() > 50_000, "charts should be embedded in the report");
	}

	private static void installEnglishTranslator() {
		Module applicationModule = new ServicesForTesting();
		Module translatorModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new ResourceBundleTranslator("l10n.messages", Locale.ROOT));
			}
		};
		Application.setInjector(Guice.createInjector(
				Modules.override(applicationModule).with(translatorModule), new PluginModule()));
	}
}
