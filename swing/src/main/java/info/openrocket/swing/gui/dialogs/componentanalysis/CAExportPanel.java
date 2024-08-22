package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.CSVExportPanel;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

import javax.swing.JFileChooser;
import java.awt.Component;
import java.io.File;

public class CAExportPanel extends CSVExportPanel<CADataType> {
	private static final long serialVersionUID = 4423905472892675964L;

	private static final Translator trans = Application.getTranslator();

	private static final int OPTION_FIELD_DESCRIPTIONS = 0;

	private CAExportPanel(CADataType[] types,
						  boolean[] selected, CsvOptionPanel csvOptions, Component... extraComponents) {
		super(types, selected, csvOptions, extraComponents);
	}

	public static CAExportPanel create(CADataType[] types) {
		boolean[] selected = new boolean[types.length];
		for (int i = 0; i < types.length; i++) {
			selected[i] = ((SwingPreferences) Application.getPreferences()).isComponentAnalysisDataTypeExportSelected(types[i]);
		}

		CsvOptionPanel csvOptions = new CsvOptionPanel(CAExportPanel.class,
				trans.get("SimExpPan.checkbox.Includefielddesc"),
				trans.get("SimExpPan.checkbox.ttip.Includefielddesc"));

		return new CAExportPanel(types, selected, csvOptions);
	}

	@Override
	public boolean doExport() {
		JFileChooser chooser = new SaveFileChooser();
		chooser.setFileFilter(FileHelper.CSV_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;

		File file = chooser.getSelectedFile();
		if (file == null)
			return false;

		file = FileHelper.forceExtension(file, "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}


		String commentChar = csvOptions.getCommentCharacter();
		String fieldSep = csvOptions.getFieldSeparator();
		int decimalPlaces = csvOptions.getDecimalPlaces();
		boolean isExponentialNotation = csvOptions.isExponentialNotation();
		boolean fieldComment = csvOptions.getSelectionOption(OPTION_FIELD_DESCRIPTIONS);
		csvOptions.storePreferences();

		// Store preferences and export
		int n = 0;
		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		for (int i = 0; i < selected.length; i++) {
			((SwingPreferences) Application.getPreferences()).setComponentAnalysisExportSelected(types[i], selected[i]);
			if (selected[i])
				n++;
		}


		CADataType[] fieldTypes = new CADataType[n];
		Unit[] fieldUnits = new Unit[n];
		int pos = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				fieldTypes[pos] = types[i];
				fieldUnits[pos] = units[i];
				pos++;
			}
		}

		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}


		/*SaveCSVWorker.export(file, simulation, branch, fieldTypes, fieldUnits, fieldSep, decimalPlaces,
				isExponentialNotation, commentChar, simulationComment, fieldComment, eventComment,
				SwingUtilities.getWindowAncestor(this));*/

		return true;
	}
}
