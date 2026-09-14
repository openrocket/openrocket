package info.openrocket.swing.gui.print.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.openrocket.swing.gui.print.OpenRocketPrintable;
import info.openrocket.swing.gui.print.PrintableContext;

class RocketPrintTreeTest {
	@Test
	void leavesThePotentiallyExpensiveMonteCarloReportUnselectedByDefault() {
		assertFalse(RocketPrintTree.isInitiallySelected(OpenRocketPrintable.MONTE_CARLO_REPORT));
		assertTrue(RocketPrintTree.isInitiallySelected(OpenRocketPrintable.DESIGN_REPORT));
	}

	@Test
	void checkboxModelPreservesTheInitialPrintableSelections() {
		RocketPrintTree tree = RocketPrintTree.create("Rocket");
		new CheckTreeManager(tree);
		List<OpenRocketPrintable> selected = new ArrayList<>();
		tree.getToBePrinted().forEachRemaining(context -> selected.add(context.getPrintable()));

		assertTrue(selected.contains(OpenRocketPrintable.DESIGN_REPORT));
		assertFalse(selected.contains(OpenRocketPrintable.MONTE_CARLO_REPORT));
	}
}
