package info.openrocket.swing.gui.util;

import info.openrocket.core.util.BugException;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class URLUtil {
    public static final String WIKI_URL = "http://wiki.openrocket.info/";

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openWebpage(String url) {
        try {
            return openWebpage(new URI(url));
        } catch (URISyntaxException e) {
            throw new BugException("Illegal URL: " + url, e);
        }
    }
}
