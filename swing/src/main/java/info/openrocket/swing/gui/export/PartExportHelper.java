package info.openrocket.swing.gui.export;

import com.formdev.flatlaf.util.SystemFileChooser.FileFilter;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.cad.export.CADDimensionVerifier;
import info.openrocket.core.file.cad.export.CADPartsExporter;
import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.NativeFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports fins and centering rings to a DXF, STEP, STL or 3MF file and shows the dimension check of the written
 * file.
 */
public final class PartExportHelper {
	private static final Logger log = LoggerFactory.getLogger(PartExportHelper.class);
	private static final Translator trans = Application.getTranslator();

	private PartExportHelper() {
	}

	/**
	 * Asks for a file name, exports the parts and shows the dimension check.
	 *
	 * @param components the selected components; only fins and centering rings are exported
	 */
	public static void export(Window parent, OpenRocketDocument document, List<RocketComponent> components,
							  Format format) {
		List<RocketComponent> parts = collectParts(components);
		String title = trans.get("PartExport.title." + format.name());
		if (parts.isEmpty()) {
			JOptionPane.showMessageDialog(parent, trans.get("PartExport.empty"), title,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		SwingPreferences prefs = (SwingPreferences) Application.getPreferences();
		NativeFileChooser chooser = new NativeFileChooser();
		chooser.setFileFilter(fileFilter(format));
		File defaultDir = prefs.getDefaultDirectory();
		if (defaultDir != null) {
			chooser.setCurrentDirectory(defaultDir);
		}
		chooser.setSelectedFile(new File(defaultFileName(parts, document, format)));
		if (chooser.showSaveDialog(parent) != NativeFileChooser.APPROVE_OPTION) {
			return;
		}
		File target = FileHelper.forceExtension(chooser.getSelectedFile(), format.getExtension());
		if (!FileHelper.confirmWrite(target, chooser.getSelectedFile(), parent)) {
			return;
		}
		prefs.setDefaultDirectory(chooser.getCurrentDirectory());

		CADDimensionVerifier.Result result;
		try {
			result = exportToFile(parts, format, target, document.getRocket().getName());
			log.info(Markers.USER_MARKER, "Exported {} parts to {}", format, target.getAbsolutePath());
		} catch (IOException | RuntimeException ex) {
			log.warn("Failed to export " + format, ex);
			JOptionPane.showMessageDialog(parent, String.format(trans.get("PartExport.error"), ex.getMessage()),
					title, JOptionPane.ERROR_MESSAGE);
			return;
		}

		new PartDimensionDialog(parent, target, result).setVisible(true);
	}

	/**
	 * @return the suggested file name: the name of the part, or of the rocket if there are several parts, without
	 * characters that are not allowed in file names on Windows
	 */
	static String defaultFileName(List<RocketComponent> parts, OpenRocketDocument document, Format format) {
		String name = (parts.size() == 1 ? parts.get(0) : document.getRocket()).getName();
		if (name == null || name.isBlank()) {
			name = "parts";
		}
		return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_") + "." + format.getExtension();
	}

	/**
	 * Writes the parts to a file, then reads the file back and checks its dimensions against the design.
	 *
	 * @param parts fins and centering rings, see {@link #collectParts}
	 */
	public static CADDimensionVerifier.Result exportToFile(List<RocketComponent> parts, Format format, File target,
														   String title) throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		for (RocketComponent part : parts) {
			if (part instanceof FinSet finSet) {
				exporter.addFinSet(finSet);
			} else if (part instanceof CenteringRing ring) {
				exporter.addCenteringRing(ring, RingSvgExporter.holesFromMotorMounts(
						ComponentSvgExportService.findSupportingMotorMounts(ring)));
			}
		}

		List<ExportedPart> exported;
		try (OutputStream out = new FileOutputStream(target)) {
			exported = exporter.write(format, title, out);
		}
		try (InputStream in = new FileInputStream(target)) {
			return CADDimensionVerifier.verify(format, in, exported);
		}
	}

	/**
	 * @return the fins and centering rings among the given components. Other components, such as body tubes and motor
	 * tubes, are not exported, and neither are the parts inside them.
	 */
	public static List<RocketComponent> collectParts(List<RocketComponent> components) {
		List<RocketComponent> parts = new ArrayList<>();
		for (RocketComponent component : components) {
			if ((component instanceof FinSet || component instanceof CenteringRing) && !parts.contains(component)) {
				parts.add(component);
			}
		}
		return parts;
	}

	private static FileFilter fileFilter(Format format) {
		return switch (format) {
			case DXF -> FileHelper.DXF_FILTER;
			case STEP -> FileHelper.STEP_FILTER;
			case STL -> FileHelper.STL_FILTER;
			case THREE_MF -> FileHelper.THREE_MF_FILTER;
		};
	}
}
