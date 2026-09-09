package info.openrocket.swing.gui.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.svg.export.SVGExportOptions;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.components.SVGOptionPanel;
import info.openrocket.swing.gui.util.ColorConversion;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared flow for "Export to SVG" for single components. Opens the chooser, handles options,
 * persists preferences, and dispatches to the proper exporter based on component type.
 */
public final class SvgExportHelper {
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private SvgExportHelper() {}

	public static void exportSinglePart(Component parent, OpenRocketDocument document, RocketComponent component) {
		// First: show options dialog (pre-select the component being exported)
		List<RocketComponent> initialSelection = new ArrayList<>();
		initialSelection.add(component);
		SvgOptionsDialog optionsDialog = new SvgOptionsDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(parent), document, initialSelection);
		optionsDialog.setFromPreferences(prefs);
		if (!optionsDialog.showDialog((javax.swing.JComponent) parent)) {
			return;
		}

		// Then: plain file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(FileHelper.SVG_FILTER);
		chooser.setCurrentDirectory(prefs.getDefaultDirectory());
		chooser.setSelectedFile(suggestDefaultFile(component));
		if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(parent)) {
			return;
		}
		File target = FileHelper.forceExtension(chooser.getSelectedFile(), "svg");
		if (!FileHelper.confirmWrite(target, parent)) {
			return;
		}

		// Save preferences
		prefs.setDefaultDirectory(chooser.getCurrentDirectory());
		prefs.setSVGStrokeColor(ColorConversion.fromAwtColor(optionsDialog.getStrokeColor()));
		prefs.setSVGStrokeWidth(optionsDialog.getStrokeWidth());
		prefs.setSVGDrawCrosshair(optionsDialog.isDrawCrosshair());
		prefs.setSVGCrosshairColor(ColorConversion.fromAwtColor(optionsDialog.getCrosshairColor()));
		prefs.setSVGShowLabels(optionsDialog.isShowLabels());
		prefs.setSVGLabelColor(ColorConversion.fromAwtColor(optionsDialog.getLabelColor()));

		SVGExportOptions options = optionsDialog.getExportOptions();

		// Dispatch to the appropriate exporter based on component type
		try {
			if (component instanceof FinSet) {
				ComponentSvgExportService.exportFinSet((FinSet) component, target, options);
			} else if (component instanceof CenteringRing) {
				ComponentSvgExportService.exportCenteringRing((CenteringRing) component, target, options);
			} else if (component instanceof Bulkhead) {
				ComponentSvgExportService.exportBulkhead((Bulkhead) component, target, options);
			} else {
				JOptionPane.showMessageDialog(parent, "SVG export is not supported for this component.",
						"SVG Export", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(parent,
					"An error occurred while exporting to SVG: " + ex.getMessage(),
					"SVG Export Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static File suggestDefaultFile(RocketComponent component) {
		String baseName = component.getName();
		if (baseName == null || baseName.isBlank()) {
			baseName = component.getComponentName();
		}
		if (baseName == null || baseName.isBlank()) {
			baseName = "component";
		}
		String normalized = baseName.trim().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", "-");
		return new File(normalized + ".svg");
	}
}

