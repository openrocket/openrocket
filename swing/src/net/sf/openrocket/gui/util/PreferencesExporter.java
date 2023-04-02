package net.sf.openrocket.gui.util;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.gui.components.PreferencesOptionPanel;
import net.sf.openrocket.gui.main.MRUDesignFile;
import net.sf.openrocket.gui.widgets.SaveFileChooser;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public abstract class PreferencesExporter {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(PreferencesExporter.class);
    private static final net.sf.openrocket.startup.Preferences prefs = Application.getPreferences();

    private static final String[] userDirectoriesKeysToIgnore;      // Preference keys to ignore when exporting user directories (= keys that export user directories)
    private static final String[] prefixKeysToIgnore;              // Preference keys to ignore when exporting user directories (= keys that start with these prefixes)

    static {
        userDirectoriesKeysToIgnore = new String[] {
                net.sf.openrocket.startup.Preferences.USER_THRUST_CURVES_KEY,
                net.sf.openrocket.startup.Preferences.DEFAULT_DIRECTORY
        };
        prefixKeysToIgnore = new String[] {
                MRUDesignFile.MRU_FILE_LIST_PROPERTY
        };
    }


    public static boolean exportPreferences(Window parent, Preferences preferences) {
        JFileChooser chooser = new SaveFileChooser();
        chooser.setDialogTitle(trans.get("PreferencesExporter.chooser.title"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(FileHelper.XML_FILTER);
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        chooser.setAccessory(new PreferencesOptionPanel());

        // TODO: update this dynamically instead of hard-coded values
        // The macOS file chooser has an issue where it does not update its size when the accessory is added.
        if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
            Dimension currentSize = chooser.getPreferredSize();
            Dimension newSize = new Dimension((int) (1.25 * currentSize.width), (int) (1.2 * currentSize.height));
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

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            if (prefs.getExportUserDirectories()) {
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

    private static void exportFilteredPreferences(Preferences preferences, FileOutputStream fos) throws BackingStoreException, IOException {
        // Filter out user directories
        Preferences root = Preferences.userRoot();
        String originalNodeName = ((SwingPreferences) prefs).getNodename();
        String nodeName = originalNodeName + "-temp";
        if (root.nodeExists(nodeName)) {
            root.node(nodeName).removeNode();
        }
        Preferences tempPrefs = root.node(nodeName);

        // Fill in all parameters to the temporary preferences, except for user directories
        copyFilteredPreferences(preferences, tempPrefs, userDirectoriesKeysToIgnore, prefixKeysToIgnore);

        // Export the filtered preferences
        try {
            // Export the filtered preferences to a temporary file
            Path tempFile = Files.createTempFile("ORprefs_" + System.currentTimeMillis(), ".xml");
            try (FileOutputStream tempFos = new FileOutputStream(tempFile.toFile())) {
                tempPrefs.exportSubtree(tempFos);
            }

            // Read and parse the temporary file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc;
            try (FileInputStream tempFis = new FileInputStream(tempFile.toFile())) {
                doc = factory.newDocumentBuilder().parse(tempFis);
            }

            // Find and rename the node
            NodeList nodeList = doc.getElementsByTagName("node");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getAttribute("name").equals(nodeName)) {
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
            root.node(nodeName).removeNode();
        }
    }

    private static void copyFilteredPreferences(Preferences src, Preferences dest, String[] keysToIgnore, String[] prefixKeysToIgnore) throws BackingStoreException {
        for (String key : src.keys()) {
            boolean ignoreKey = false;
            for (String keyToIgnore : keysToIgnore) {
                if (key.equals(keyToIgnore)) {
                    ignoreKey = true;
                    break;
                }
            }
            if (!ignoreKey) {
                for (String prefixKeyToIgnore : prefixKeysToIgnore) {
                    if (key.startsWith(prefixKeyToIgnore)) {
                        ignoreKey = true;
                        break;
                    }
                }
            }
            if (!ignoreKey) {
                dest.put(key, src.get(key, null));
            }
        }

        for (String childNodeName : src.childrenNames()) {
            Preferences srcChild = src.node(childNodeName);
            Preferences destChild = dest.node(childNodeName);
            copyFilteredPreferences(srcChild, destChild, keysToIgnore, prefixKeysToIgnore);
        }
    }
}
