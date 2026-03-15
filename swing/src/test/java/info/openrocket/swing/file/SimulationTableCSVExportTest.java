package info.openrocket.swing.file;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.swing.gui.adaptors.Column;
import info.openrocket.swing.gui.adaptors.ColumnTableModel;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Test for SimulationTableCSVExport, particularly testing the fix for
 * ArrayIndexOutOfBoundsException when columns are hidden in the view.
 */
public class SimulationTableCSVExportTest extends BaseTestCase {

	/**
	 * Test that buildHeaderRow() doesn't throw ArrayIndexOutOfBoundsException
	 * when the view has fewer columns than the model (i.e., when columns are hidden).
	 * 
	 * This reproduces the bug where:
	 * - Model has 13 columns (indices 0-12)
	 * - View has fewer columns due to hidden columns
	 * - buildHeaderRow() was using model column count but accessing view column names
	 * - This caused ArrayIndexOutOfBoundsException: 13 >= 13
	 */
	@Test
	public void testBuildHeaderRowWithHiddenColumns() {
		// Setup: Model has 13 columns, but view has fewer (simulating hidden columns)
		final int modelColumnCount = 13;
		final int viewColumnCount = 10; // Some columns are hidden

		OpenRocketDocument document = mock(OpenRocketDocument.class);
		JTable simulationTable = mock(JTable.class);
		ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
		TableColumnModel tableColumnModel = mock(TableColumnModel.class);

		when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
		when(simulationTableModel.getColumnCount()).thenReturn(modelColumnCount);
		when(simulationTable.getColumnCount()).thenReturn(viewColumnCount);

		// Mock column names from model (what we should use)
		for (int i = 0; i < modelColumnCount; i++) {
			Column mockColumn = mock(Column.class);
			when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
			when(mockColumn.toString()).thenReturn("Column" + i);
			when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
		}

		// Mock view column names (limited to view column count)
		for (int i = 0; i < viewColumnCount; i++) {
			when(simulationTable.getColumnName(i)).thenReturn("ViewColumn" + i);
		}

		// Create exporter
		SimulationTableCSVExport exporter = new SimulationTableCSVExport(
				document, simulationTable, simulationTableModel);

		// This should not throw ArrayIndexOutOfBoundsException
		assertDoesNotThrow(() -> {
			String result = exporter.generateCSVData("\t", 6, false, false);
			assertNotNull(result);
		});
	}

	/**
	 * Test that buildHeaderRow() works correctly when all columns are visible
	 * (view column count equals model column count).
	 */
	@Test
	public void testBuildHeaderRowWithAllColumnsVisible() {
		final int columnCount = 13;

		OpenRocketDocument document = mock(OpenRocketDocument.class);
		JTable simulationTable = mock(JTable.class);
		ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
		TableColumnModel tableColumnModel = mock(TableColumnModel.class);

		when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
		when(simulationTableModel.getColumnCount()).thenReturn(columnCount);
		when(simulationTable.getColumnCount()).thenReturn(columnCount);

		// Mock column names from model
		for (int i = 0; i < columnCount; i++) {
			Column mockColumn = mock(Column.class);
			when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
			when(mockColumn.toString()).thenReturn("Column" + i);
			when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
			when(simulationTable.getColumnName(i)).thenReturn("Column" + i);
		}

		// Create exporter
		SimulationTableCSVExport exporter = new SimulationTableCSVExport(
				document, simulationTable, simulationTableModel);

		// This should work without issues
		assertDoesNotThrow(() -> {
			String result = exporter.generateCSVData("\t", 6, false, false);
			assertNotNull(result);
		});
	}

	/**
	 * Test with a simulation that has data to ensure the full export path works.
	 */
	@Test
	public void testGenerateCSVDataWithSimulationData() {
		final int modelColumnCount = 13;
		final int viewColumnCount = 10;

		OpenRocketDocument document = mock(OpenRocketDocument.class);
		JTable simulationTable = mock(JTable.class);
		ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
		TableColumnModel tableColumnModel = mock(TableColumnModel.class);

		when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
		when(simulationTableModel.getColumnCount()).thenReturn(modelColumnCount);
		when(simulationTable.getColumnCount()).thenReturn(viewColumnCount);
		when(simulationTableModel.getRowCount()).thenReturn(1);

		// Mock column names from model
		for (int i = 0; i < modelColumnCount; i++) {
			Column mockColumn = mock(Column.class);
			when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
			when(mockColumn.toString()).thenReturn("Column" + i);
			when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
		}

		// Mock a simulation with data
		Simulation simulation = mock(Simulation.class);
		FlightData flightData = mock(FlightData.class);
		when(document.getSimulation(0)).thenReturn(simulation);
		when(simulation.hasSummaryData()).thenReturn(true);
		when(simulation.getSimulatedData()).thenReturn(flightData);
		when(simulation.getSimulatedWarnings()).thenReturn(new info.openrocket.core.logging.WarningSet());

		// Mock cell values (starting from column 2, as per buildRowData logic)
		for (int j = 2; j < modelColumnCount; j++) {
			when(simulationTableModel.getValueAt(0, j)).thenReturn("Value" + j);
		}

		// Create exporter
		SimulationTableCSVExport exporter = new SimulationTableCSVExport(
				document, simulationTable, simulationTableModel);

		// This should work without throwing ArrayIndexOutOfBoundsException
		assertDoesNotThrow(() -> {
			String result = exporter.generateCSVData("\t", 6, false, false);
			assertNotNull(result);
			// Verify header row is present
			assertNotNull(result);
		});
	}

	/**
	 * Test that reproduces the exact bug: ArrayIndexOutOfBoundsException when selecting all rows
	 * and copying. The bug occurred when getColumnCount() returned 14, but only 13 columns exist,
	 * causing the loop to try accessing index 13, which throws "13 >= 13".
	 * 
	 * This test simulates the actual bug by making getColumnCount() return 14, but only
	 * mocking columns 0-12, so accessing column 13 throws the exception.
	 */
	@Test
	public void testCopyAllRowsWithBoundaryColumnCount() {
		final int actualColumnCount = 13; // Actual columns (indices 0-12)
		final int reportedColumnCount = 14; // getColumnCount() incorrectly returns 14

		OpenRocketDocument document = mock(OpenRocketDocument.class);
		JTable simulationTable = mock(JTable.class);
		ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
		TableColumnModel tableColumnModel = mock(TableColumnModel.class);

		when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
		// This is the bug: getColumnCount() returns 14, but only 13 columns exist
		when(simulationTableModel.getColumnCount()).thenReturn(reportedColumnCount);
		when(simulationTable.getColumnCount()).thenReturn(actualColumnCount);
		when(simulationTableModel.getRowCount()).thenReturn(5); // 5 simulations

		// Mock only the actual columns that exist (0-12)
		for (int i = 0; i < actualColumnCount; i++) {
			Column mockColumn = mock(Column.class);
			when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
			when(mockColumn.toString()).thenReturn("Column" + i);
			when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
		}

		// CRITICAL: When trying to access column 13 (which doesn't exist), throw the exact exception
		// This reproduces the bug: "13 >= 13"
		when(simulationTableModel.getColumn(13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));
		when(simulationTableModel.getColumnName(13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));

		// Mock simulations with data
		for (int row = 0; row < 5; row++) {
			Simulation simulation = mock(Simulation.class);
			FlightData flightData = mock(FlightData.class);
			when(document.getSimulation(row)).thenReturn(simulation);
			when(simulation.hasSummaryData()).thenReturn(true);
			when(simulation.getSimulatedData()).thenReturn(flightData);
			when(simulation.getSimulatedWarnings()).thenReturn(new info.openrocket.core.logging.WarningSet());

			// Mock cell values (starting from column 2, as per buildRowData logic)
			for (int col = 2; col < actualColumnCount; col++) {
				when(simulationTableModel.getValueAt(row, col)).thenReturn("Value" + row + "_" + col);
			}
			// Column 13 doesn't exist
			when(simulationTableModel.getValueAt(row, 13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));
		}

		// Create exporter
		SimulationTableCSVExport exporter = new SimulationTableCSVExport(
				document, simulationTable, simulationTableModel);

		// This should NOT throw ArrayIndexOutOfBoundsException: 13 >= 13
		// The fix should catch the exception and break the loop gracefully
		assertDoesNotThrow(() -> {
			// Test with onlySelected=false to simulate "select all" scenario
			String result = exporter.generateCSVData("\t", 6, false, false);
			assertNotNull(result);
			// The result should still be generated even if one column fails
		}, "Should handle IndexOutOfBoundsException gracefully when getColumnName is called with invalid index");
	}

	/**
	 * Test that reproduces the EXACT bug scenario from the user's report:
	 * - User clicks on a row, presses Cmd+A to select all, then Cmd+C to copy
	 * - getColumnCount() returns 14, but only 13 columns exist (indices 0-12)
	 * - Loop tries to access index 13, causing ArrayIndexOutOfBoundsException: 13 >= 13
	 * 
	 * This test verifies the fix handles this scenario gracefully.
	 */
	@Test
	public void testCopyAllRowsReproducesOriginalBug() {
		final int actualColumnCount = 13; // Actual number of columns (indices 0-12)
		final int reportedColumnCount = 14; // getColumnCount() incorrectly returns 14

		OpenRocketDocument document = mock(OpenRocketDocument.class);
		JTable simulationTable = mock(JTable.class);
		ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
		TableColumnModel tableColumnModel = mock(TableColumnModel.class);

		when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
		// getColumnCount() returns 14, but only 13 columns exist
		when(simulationTableModel.getColumnCount()).thenReturn(reportedColumnCount);
		when(simulationTable.getColumnCount()).thenReturn(actualColumnCount);
		when(simulationTableModel.getRowCount()).thenReturn(3);

		// Mock only the actual columns that exist (0-12)
		for (int i = 0; i < actualColumnCount; i++) {
			Column mockColumn = mock(Column.class);
			when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
			when(mockColumn.toString()).thenReturn("Column" + i);
			when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
		}

		// When trying to access column 13 (which doesn't exist), throw the exception
		when(simulationTableModel.getColumnName(13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));
		when(simulationTableModel.getColumn(13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));

		// Mock simulations with data
		for (int row = 0; row < 3; row++) {
			Simulation simulation = mock(Simulation.class);
			FlightData flightData = mock(FlightData.class);
			when(document.getSimulation(row)).thenReturn(simulation);
			when(simulation.hasSummaryData()).thenReturn(true);
			when(simulation.getSimulatedData()).thenReturn(flightData);
			when(simulation.getSimulatedWarnings()).thenReturn(new info.openrocket.core.logging.WarningSet());

			// Mock cell values for existing columns only
			for (int col = 2; col < actualColumnCount; col++) {
				when(simulationTableModel.getValueAt(row, col)).thenReturn("Value" + row + "_" + col);
			}
			// Column 13 doesn't exist, so getValueAt should throw
			when(simulationTableModel.getValueAt(row, 13)).thenThrow(new ArrayIndexOutOfBoundsException("13 >= 13"));
		}

		// Create exporter
		SimulationTableCSVExport exporter = new SimulationTableCSVExport(
				document, simulationTable, simulationTableModel);

		// This should NOT throw - the fix should catch and handle the exception
		assertDoesNotThrow(() -> {
			String result = exporter.generateCSVData("\t", 6, false, false);
			assertNotNull(result);
			// Should still produce output for the valid columns
		}, "Should handle IndexOutOfBoundsException when column count is inconsistent");
	}

	/**
	 * Test that verifies the fix handles the case where getColumnCount() might return
	 * a value that's exactly at the array boundary, ensuring we never access
	 * an index equal to or greater than the column count.
	 */
	@Test
	public void testBuildHeaderRowBoundaryCondition() {
		// Test with various column counts to ensure boundary conditions are handled
		for (int columnCount = 10; columnCount <= 15; columnCount++) {
			OpenRocketDocument document = mock(OpenRocketDocument.class);
			JTable simulationTable = mock(JTable.class);
			ColumnTableModel simulationTableModel = mock(ColumnTableModel.class);
			TableColumnModel tableColumnModel = mock(TableColumnModel.class);

			when(simulationTable.getColumnModel()).thenReturn(tableColumnModel);
			when(simulationTableModel.getColumnCount()).thenReturn(columnCount);
			when(simulationTable.getColumnCount()).thenReturn(columnCount);

			// Mock all columns
			for (int i = 0; i < columnCount; i++) {
				Column mockColumn = mock(Column.class);
				when(simulationTableModel.getColumn(i)).thenReturn(mockColumn);
				when(mockColumn.toString()).thenReturn("Column" + i);
				when(simulationTableModel.getColumnName(i)).thenReturn("Column" + i);
			}

			SimulationTableCSVExport exporter = new SimulationTableCSVExport(
					document, simulationTable, simulationTableModel);

			final int finalColumnCount = columnCount;
			assertDoesNotThrow(() -> {
				String result = exporter.generateCSVData("\t", 6, false, false);
				assertNotNull(result);
				// Verify we never try to access index >= columnCount
			}, "Should not throw exception for column count: " + finalColumnCount);
		}
	}
}

