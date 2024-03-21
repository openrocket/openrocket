package info.openrocket.swing.gui.util;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.components.PreferencesOptionPanel;
import info.openrocket.swing.gui.main.MRUDesignFile;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public abstract class PreferencesExporter {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(PreferencesExporter.class);
    private static final info.openrocket.core.startup.Preferences prefs = Application.getPreferences();

    private static final List<String> keysToIgnore = new ArrayList<>();         // Preference keys to ignore when exporting user directories (= keys that export user directories)
    private static final List<String> prefixKeysToIgnore = new ArrayList<>();   // Preference keys to ignore when exporting user directories (= keys that start with these prefixes), e.g.
    private static final List<String> nodesToIgnore = new ArrayList<>();        // Preferences nodes that should not be exported

    public static boolean exportPreferences(Window parent, Preferences preferences) {
        JFileChooser chooser = new SaveFileChooser();
        chooser.setDialogTitle(trans.get("PreferencesExporter.chooser.title"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(FileHelper.XML_FILTER);
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        PreferencesOptionPanel options = new PreferencesOptionPanel();
        chooser.setAccessory(options);

        // TODO: update this dynamically instead of hard-coded values
        // The macOS file chooser has an issue where it does not update its size when the accessory is added.
        if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS && UITheme.isLightTheme(GUIUtil.getUITheme())) {
            Dimension currentSize = chooser.getPreferredSize();
            Dimension newSize = new Dimension((int) (1.35 * currentSize.width), (int) (1.2 * currentSize.height));
            chooser.setPreferredSize(newSize);
        }

        //// Ensures No Problems When Choosing File
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            log.info("Cancelled export of preferences.");
            return false;
        }

        ((SwingPreferences) prefs).setDefaultDirectory(chooser.getCurrentDirectory());

        File file = chooser.getSelectedFile();
        if (file == null) {
            log.info("No file selected to export preferences to.");
            return false;
        }

        final File newFile = FileHelper.forceExtension(file, "xml");
        if (!FileHelper.confirmWrite(newFile, parent)) {
            log.info("Cancelled export of preferences.");
            return false;
        }

        // Decide which keys/nodes to ignore
        boolean ignoreUserDirectories = options.isIgnoreUserDirectories();
        boolean ignoreWindowInformation = options.isIgnoreWindowInformation();
        fillIgnoreKeys(ignoreUserDirectories, ignoreWindowInformation);

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            if (keysToIgnore.isEmpty() && nodesToIgnore.isEmpty() && prefixKeysToIgnore.isEmpty()) {
                // Export all preferences
                preferences.exportSubtree(fos);
            } else {
                // Export all preferences except user directories
                exportFilteredPreferences(preferences, fos);
            }
            log.info("Preferences exported successfully.");
        } catch (IOException | BackingStoreException e) {
            log.warn("Error while importing preferences: " + e.getMessage());
        }

        return true;
    }

    private static void fillIgnoreKeys(boolean ignoreUserDirectories, boolean ignoreWindowInformation) {
        keysToIgnore.clear();
        prefixKeysToIgnore.clear();
        nodesToIgnore.clear();

        if (ignoreUserDirectories) {
            keysToIgnore.add(info.openrocket.core.startup.Preferences.USER_THRUST_CURVES_KEY);
            keysToIgnore.add(info.openrocket.core.startup.Preferences.DEFAULT_DIRECTORY);
            prefixKeysToIgnore.add(MRUDesignFile.MRU_FILE_LIST_PROPERTY);
        }

        if (ignoreWindowInformation) {
            nodesToIgnore.add(SwingPreferences.NODE_WINDOWS);
            nodesToIgnore.add(SwingPreferences.NODE_TABLES);
        }

        keysToIgnore.add(SwingPreferences.UPDATE_PLATFORM);     // Don't export platform-specific settings
    }

    public static void exportPreferencesToFile(Preferences preferences, FileOutputStream fos, boolean filterPreferences)
                throws BackingStoreException, IOException {
        // If no filtering is required, just export the preferences
        if (!filterPreferences) {
            preferences.exportSubtree(fos);
            return;
        }

        // Filter out user directories
        Preferences root = Preferences.userRoot();
        String originalNodeName = ((SwingPreferences) prefs).getNodename();
        String filteredPrefsNodeName = originalNodeName + "-filtered";
        if (root.nodeExists(filteredPrefsNodeName)) {
            root.node(filteredPrefsNodeName).removeNode();
        }
        Preferences filteredPrefs = root.node(filteredPrefsNodeName);

        // Fill in all parameters to the temporary preferences, except for user directories
        copyFilteredPreferences(preferences, filteredPrefs, nodesToIgnore, keysToIgnore, prefixKeysToIgnore);

        // Export the filtered preferences
        try {
            // Export the filtered preferences to a temporary file
            Path tempFile = Files.createTempFile("ORPrefs_" + System.currentTimeMillis(), ".xml");
            try (FileOutputStream tempFos = new FileOutputStream(tempFile.toFile())) {
                filteredPrefs.exportSubtree(tempFos);
            }

            // Read and parse the temporary file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc;
            try (FileInputStream tempFis = new FileInputStream(tempFile.toFile())) {
                doc = factory.newDocumentBuilder().parse(tempFis);
            }

            // Find and rename the filtered prefs node
            NodeList nodeList = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getAttribute("name").equals(filteredPrefsNodeName)) {
                    element.setAttribute("name", ((SwingPreferences) prefs).getNodename());
                    break;
                }
            }

            // Create a transformer to write the XML document to the FileOutputStream
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // Set output properties to include the correct DOCTYPE declaration
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://java.sun.com/dtd/preferences.dtd");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            // Write the XML document to the FileOutputStream
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);

            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        } catch (ParserConfigurationException | TransformerException | SAXException e) {
            e.printStackTrace();
        } finally {
            root.node(filteredPrefsNodeName).removeNode();
        }
    }

    private static void exportFilteredPreferences(Preferences preferences, FileOutputStream fos) throws BackingStoreException, IOException {
        exportPreferencesToFile(preferences, fos, true);
    }

    private static void copyFilteredPreferences(Preferences src, Preferences dest,
                List<String> nodesToIgnore, List<String> keysToIgnore, List<String> prefixKeysToIgnore) throws BackingStoreException {
        for (String key : src.keys()) {
            if (keysToIgnore.contains(key)
                    || prefixKeysToIgnore.stream().anyMatch(key::startsWith)) {
                continue;
            }
            dest.put(key, src.get(key, null));
        }

        for (String childNodeName : src.childrenNames()) {
            if (nodesToIgnore.contains(childNodeName)) {
                continue;
            }
            Preferences srcChild = src.node(childNodeName);
            Preferences destChild = dest.node(childNodeName);
            copyFilteredPreferences(srcChild, destChild, nodesToIgnore, keysToIgnore, prefixKeysToIgnore);
        }
    }
}
