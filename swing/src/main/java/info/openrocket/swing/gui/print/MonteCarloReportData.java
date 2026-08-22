package info.openrocket.swing.gui.print;

import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;

/** Prepared inputs for a Monte Carlo PDF report. */
public record MonteCarloReportData(List<Entry> entries, List<String> omittedSimulations) {
	public static final MonteCarloReportData EMPTY = new MonteCarloReportData(List.of(), List.of());

	public MonteCarloReportData {
		entries = List.copyOf(entries);
		omittedSimulations = List.copyOf(omittedSimulations);
	}

	public record Entry(Simulation simulation, MonteCarloResult result) {
	}
}
